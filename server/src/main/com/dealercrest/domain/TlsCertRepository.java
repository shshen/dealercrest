package com.dealercrest.domain;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TlsCertRepository {

    private final DataSource dataSource;

    public TlsCertRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<DealerDomain> findByStatus(String status) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT dealer_id, domain, status, order_url, challenge_url, " +
            "       attempt_count, last_checked, error_message " +
            "FROM dealer_domain WHERE status = ?"
        );
        stmt.setString(1, status);
        ResultSet rs = stmt.executeQuery();

        List<DealerDomain> results = new ArrayList<DealerDomain>();
        while (rs.next()) {
            results.add(mapRow(rs));
        }
        rs.close();
        stmt.close();
        conn.close();
        return results;
    }

    public void updateStatus(long dealerId, String status) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE dealer_domain SET status = ?, last_checked = ? WHERE dealer_id = ?"
        );
        stmt.setString(1, status);
        stmt.setTimestamp(2, Timestamp.from(Instant.now()));
        stmt.setLong(3, dealerId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    public void updateStatusWithError(long dealerId, String status, String error) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE dealer_domain " +
            "SET status = ?, error_message = ?, last_checked = ?, attempt_count = attempt_count + 1 " +
            "WHERE dealer_id = ?"
        );
        stmt.setString(1, status);
        stmt.setString(2, error);
        stmt.setTimestamp(3, Timestamp.from(Instant.now()));
        stmt.setLong(4, dealerId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    public void updateAcmeUrls(long dealerId, String orderUrl, String challengeUrl) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE dealer_domain " +
            "SET order_url = ?, challenge_url = ?, status = ?, last_checked = ? " +
            "WHERE dealer_id = ?"
        );
        stmt.setString(1, orderUrl);
        stmt.setString(2, challengeUrl);
        stmt.setString(3, DomainStatus.VALIDATING);
        stmt.setTimestamp(4, Timestamp.from(Instant.now()));
        stmt.setLong(5, dealerId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    public void markActive(long dealerId) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE dealer_domain " +
            "SET status = ?, activated_at = ?, last_checked = ? " +
            "WHERE dealer_id = ?"
        );
        stmt.setString(1, DomainStatus.ACTIVE);
        stmt.setTimestamp(2, Timestamp.from(Instant.now()));
        stmt.setTimestamp(3, Timestamp.from(Instant.now()));
        stmt.setLong(4, dealerId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    public List<DealerDomain> findActivatedAfter(Instant since) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT * FROM dealer_domain WHERE status = ? AND activated_at > ?"
        );
        stmt.setString(1, DomainStatus.ACTIVE);
        stmt.setTimestamp(2, Timestamp.from(since));
        ResultSet rs = stmt.executeQuery();
    
        List<DealerDomain> results = new ArrayList<DealerDomain>();
        while (rs.next()) {
            results.add(mapRow(rs));
        }
        rs.close();
        stmt.close();
        conn.close();
        return results;
    }

    private DealerDomain mapRow(ResultSet rs) throws SQLException {
        DealerDomain d = new DealerDomain();
        d.setDealerId(rs.getLong("dealer_id"));
        d.setDomain(rs.getString("domain"));
        d.setStatus(rs.getString("status"));
        d.setOrderUrl(rs.getString("order_url"));
        d.setChallengeUrl(rs.getString("challenge_url"));
        d.setAttemptCount(rs.getInt("attempt_count"));
        Timestamp ts = rs.getTimestamp("last_checked");
        d.setLastChecked(ts != null ? ts.toInstant() : null);
        d.setErrorMessage(rs.getString("error_message"));
        return d;
    }
}
