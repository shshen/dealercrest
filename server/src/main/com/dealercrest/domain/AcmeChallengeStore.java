package com.dealercrest.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * 
 * 
 CREATE TABLE acme_challenge (
    token       TEXT PRIMARY KEY,
    response    TEXT NOT NULL,
    dealer_id   BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dealer_cert (
    dealer_id       BIGINT PRIMARY KEY REFERENCES dealers(id),
    domain          TEXT NOT NULL,
    cert_pem        TEXT NOT NULL,       -- fullchain
    private_key_pem TEXT NOT NULL,       -- encrypted at rest
    issued_at       TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    renewed_at      TIMESTAMPTZ
);
 */
public class AcmeChallengeStore {

    private final DataSource dataSource;

    public AcmeChallengeStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveChallenge(String token, String response, long dealerId) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO acme_challenge (token, response, dealer_id) " +
            "VALUES (?, ?, ?) ON CONFLICT (token) DO UPDATE SET response = EXCLUDED.response"
        );
        stmt.setString(1, token);
        stmt.setString(2, response);
        stmt.setLong(3, dealerId);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }

    public String lookupAuthorization(String token) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT response FROM acme_challenge WHERE token = ?"
        );
        stmt.setString(1, token);
        ResultSet rs = stmt.executeQuery();
        String response = rs.next() ? rs.getString("response") : null;
        rs.close();
        stmt.close();
        conn.close();
        return response;
    }

    public void deleteChallenge(String token) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "DELETE FROM acme_challenge WHERE token = ?"
        );
        stmt.setString(1, token);
        stmt.executeUpdate();
        stmt.close();
        conn.close();
    }
}
