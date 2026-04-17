package com.dealercrest.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null");
        }
        this.dataSource = dataSource;
    }

    // -------------------------------------------------------------------------
    // Public interfaces
    // -------------------------------------------------------------------------

    /**
     * Sets parameters on a PreparedStatement before execution.
     */
    public interface StatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    /**
     * Maps a single row from a ResultSet to an object of type T.
     * Called once per row — do not call rs.next() inside implementations.
     */
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Processes a ResultSet directly for cases where row-by-row mapping
     * is insufficient (e.g. grouped results, early termination).
     */
    public interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }

    /**
     * Unit of work to execute inside a transaction.
     * Receives the active Connection so callers can issue multiple statements
     * within the same transaction boundary.
     */
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws SQLException;
    }

    // -------------------------------------------------------------------------
    // Query — single row
    // -------------------------------------------------------------------------

    /**
     * Executes a query and maps the first row to T.
     * Returns null if no rows match.
     */
    public <T> T queryOne(String sql, StatementSetter setter, RowMapper<T> mapper)
            throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        return mapper.map(rs);
                    }
                    return null;
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Executes a query with no parameters and maps the first row to T.
     * Returns null if no rows match.
     */
    public <T> T queryOne(String sql, RowMapper<T> mapper) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        return mapper.map(rs);
                    }
                    return null;
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    // -------------------------------------------------------------------------
    // Query — list
    // -------------------------------------------------------------------------

    /**
     * Executes a query and maps every row to a List of T.
     * Returns an empty list if no rows match.
     */
    public <T> List<T> queryList(String sql, StatementSetter setter, RowMapper<T> mapper)
            throws SQLException {
        List<T> results = new ArrayList<T>();
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
        return results;
    }

    /**
     * Executes a query with no parameters and maps every row to a List of T.
     * Returns an empty list if no rows match.
     */
    public <T> List<T> queryList(String sql, RowMapper<T> mapper) throws SQLException {
        List<T> results = new ArrayList<T>();
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                ResultSet rs = ps.executeQuery();
                try {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // Query — raw ResultSet handler
    // -------------------------------------------------------------------------

    /**
     * Executes a query and passes the full ResultSet to a handler.
     * Use when row-by-row mapping does not fit (e.g. grouped aggregation).
     */
    public <T> T query(String sql, StatementSetter setter, ResultSetHandler<T> handler)
            throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    return handler.handle(rs);
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    // -------------------------------------------------------------------------
    // Scalar — single value
    // -------------------------------------------------------------------------

    /**
     * Executes a query and returns the first column of the first row as a Long.
     * Useful for COUNT, SUM, etc.
     * Returns null if no rows match or the value is SQL NULL.
     */
    public Long queryForLong(String sql, StatementSetter setter) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        long value = rs.getLong(1);
                        if (rs.wasNull()) {
                            return null;
                        }
                        return value;
                    }
                    return null;
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Executes a query and returns the first column of the first row as a String.
     * Returns null if no rows match or the value is SQL NULL.
     */
    public String queryForString(String sql, StatementSetter setter) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                    return null;
                } finally {
                    rs.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    // -------------------------------------------------------------------------
    // Update / Insert / Delete
    // -------------------------------------------------------------------------

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     * Returns the number of rows affected.
     */
    public int update(String sql, StatementSetter setter) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            try {
                setter.set(ps);
                return ps.executeUpdate();
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    /**
     * Executes an INSERT and returns the first generated key as a String.
     * Use for tables where the database generates the primary key (sequences, serials).
     * Returns null if no key was generated.
     *
     * Note: for application-generated keys (UUIDs, nanoid), use update() instead.
     */
    public String insert(String sql, StatementSetter setter) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                sql, PreparedStatement.RETURN_GENERATED_KEYS);
            try {
                setter.set(ps);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                try {
                    if (keys.next()) {
                        return keys.getString(1);
                    }
                    return null;
                } finally {
                    keys.close();
                }
            } finally {
                ps.close();
            }
        } finally {
            conn.close();
        }
    }

    // -------------------------------------------------------------------------
    // Transaction support
    // -------------------------------------------------------------------------

    /**
     * Executes a callback inside a single transaction.
     * Commits on success, rolls back on any exception (SQL or runtime).
     * The connection is always closed when the method returns.
     *
     * Note: do not call setAutoCommit() inside the callback — the template
     * owns the connection lifecycle here.
     */
    public <T> T transaction(TransactionCallback<T> callback) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);
            try {
                T result = callback.execute(conn);
                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } finally {
            // HikariCP resets autoCommit to the configured default on connection return.
            // Calling setAutoCommit(true) here is unnecessary and risks a second exception
            // masking the real one if the connection is already broken.
            conn.close();
        }
    }
}
