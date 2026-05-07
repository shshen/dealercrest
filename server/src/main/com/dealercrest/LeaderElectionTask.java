package com.dealercrest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Attempts to acquire a PostgreSQL session-level advisory lock on a fixed key.
 * Only one node across the cluster will hold the lock at a time.
 * Runs on a fixed schedule; the lock is re-checked each interval.
 */
public class LeaderElectionTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(LeaderElectionTask.class.getName());

    // Arbitrary stable integer key — same on all nodes
    private static final long LOCK_KEY = 987654321L;

    private final DataSource dataSource;

    // Held across ticks; released when this node loses leadership
    private Connection lockConnection;
    private boolean currentlyLeader;

    public LeaderElectionTask(DataSource dataSource) {
        this.dataSource = dataSource;
        this.lockConnection = null;
        this.currentlyLeader = false;
    }

    @Override
    public void run() {
        if (currentlyLeader) {
            verifyLockStillHeld();
        } else {
            tryAcquireLock();
        }
    }

    private void tryAcquireLock() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true); // Session-level lock: held until connection closes

            PreparedStatement stmt = conn.prepareStatement(
                "SELECT pg_try_advisory_lock(?)"
            );
            stmt.setLong(1, LOCK_KEY);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getBoolean(1)) {
                lockConnection = conn;
                currentlyLeader = true;
                LOG.info("This node acquired leader lock. Becoming leader.");
            } else {
                rs.close();
                stmt.close();
                conn.close();
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to acquire advisory lock", e);
            closeQuietly(conn);
        }
    }

    private void verifyLockStillHeld() {
        // Verify the connection is still alive (e.g., after a DB failover)
        try {
            if (lockConnection == null || lockConnection.isClosed()) {
                LOG.warning("Lock connection lost. Stepping down from leader.");
                stepDown();
                return;
            }
            // Ping the connection
            PreparedStatement ping = lockConnection.prepareStatement("SELECT 1");
            ping.executeQuery();
            ping.close();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Lock connection unhealthy. Stepping down.", e);
            stepDown();
        }
    }

    private void stepDown() {
        closeQuietly(lockConnection);
        lockConnection = null;
        currentlyLeader = false;
    }

    public void shutdown() {
        if (currentlyLeader) {
            stepDown();
        }
    }

    public boolean isLeader() {
        return currentlyLeader;
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close(); // Releases advisory lock automatically
            } catch (SQLException e) {
                LOG.log(Level.FINE, "Error closing lock connection", e);
            }
        }
    }
}
