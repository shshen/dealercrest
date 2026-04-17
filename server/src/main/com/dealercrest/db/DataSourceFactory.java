package com.dealercrest.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/**
 * Builds a HikariCP connection pool for the dealer platform.
 *
 * Dependency (Maven):
 *   <dependency>
 *       <groupId>com.zaxxer</groupId>
 *       <artifactId>HikariCP</artifactId>
 *       <version>5.1.0</version>
 *   </dependency>
 *
 * Usage:
 *   DataSource ds = DataSourceFactory.build(
 *       "jdbc:postgresql://localhost:5432/dealer", "app_user", "secret");
 *   JdbcTemplate db = new JdbcTemplate(ds);
 *
 * Shutdown (at application stop):
 *   ((HikariDataSource) ds).close();
 */
public class DataSourceFactory {

    private DataSourceFactory() {
        // static factory — do not instantiate
    }

    /**
     * Creates a HikariCP DataSource configured for PostgreSQL.
     *
     * Fail-fast: initializationFailTimeout=1 means Hikari will attempt a real
     * connection during pool construction. If the database is unreachable, the
     * constructor throws immediately rather than silently returning a broken pool.
     * Set to 0 to disable this check (lazy connection), or -1 to disable entirely.
     *
     * @param jdbcUrl  e.g. "jdbc:postgresql://localhost:5432/dealer"
     * @param username database user
     * @param password database password
     * @return a live, validated DataSource
     * @throws RuntimeException (wrapped HikariPool.PoolInitializationException)
     *                          if the database cannot be reached at startup
     */
    public static DataSource build(String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // Pool sizing — tune per deployment
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);

        // How long a thread waits for a connection before throwing (ms)
        config.setConnectionTimeout(3000);

        // How long an idle connection is kept before being retired (ms)
        config.setIdleTimeout(600_000);

        // Max lifetime of any connection in the pool (ms) — should be less
        // than the database's server-side connection timeout
        config.setMaxLifetime(1_800_000);

        // SQL used to validate connections before handing them out.
        // For PostgreSQL, the JDBC driver validates via ping by default,
        // but an explicit query is more portable.
        config.setConnectionTestQuery("SELECT 1");

        // Fail-fast: attempt a real connection at pool construction time.
        // A value > 0 is the number of milliseconds Hikari will wait for
        // a successful connection before throwing.
        config.setInitializationFailTimeout(1);

        // Pool name — useful in logs and JMX
        config.setPoolName("DealerPool");

        return new HikariDataSource(config);
    }
}
