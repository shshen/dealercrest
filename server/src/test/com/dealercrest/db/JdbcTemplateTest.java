package com.dealercrest.db;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.dealercrest.db.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JdbcTemplate using Mockito.
 *
 * No database, no in-memory DB, no stub classes.
 * Mockito mocks DataSource, Connection, PreparedStatement, and ResultSet.
 *
 * Dependencies (Maven):
 * <dependency>
 * <groupId>org.mockito</groupId>
 * <artifactId>mockito-core</artifactId>
 * <version>4.11.0</version>
 * <scope>test</scope>
 * </dependency>
 * <dependency>
 * <groupId>junit</groupId>
 * <artifactId>junit</artifactId>
 * <version>4.13.2</version>
 * <scope>test</scope>
 * </dependency>
 */
public class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;
    private JdbcTemplate db;

    @Before
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(Mockito.anyString())).thenReturn(ps);
        when(conn.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(ps.getGeneratedKeys()).thenReturn(rs);

        db = new JdbcTemplate(dataSource);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final JdbcTemplate.StatementSetter NO_PARAMS = new JdbcTemplate.StatementSetter() {
        public void set(PreparedStatement ps) throws SQLException {
            // no parameters
        }
    };

    private static final JdbcTemplate.RowMapper<String> NAME_MAPPER = new JdbcTemplate.RowMapper<String>() {
        public String map(ResultSet rs) throws SQLException {
            return rs.getString("name");
        }
    };

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void constructor_rejectsNullDataSource() {
        new JdbcTemplate(null);
    }

    // -------------------------------------------------------------------------
    // queryOne()
    // -------------------------------------------------------------------------

    @Test
    public void queryOne_withSetter_returnsMappedValue() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getString("name")).thenReturn("Acme Motors");

        String result = db.queryOne(
                "SELECT name FROM dealer WHERE id = ?",
                new JdbcTemplate.StatementSetter() {
                    public void set(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "abc12345");
                    }
                },
                NAME_MAPPER);

        assertEquals("Acme Motors", result);
        verify(ps).setString(1, "abc12345");
    }

    @Test
    public void queryOne_returnsNullOnEmptyResultSet() throws SQLException {
        when(rs.next()).thenReturn(false);

        String result = db.queryOne(
                "SELECT name FROM dealer WHERE id = ?",
                NO_PARAMS,
                NAME_MAPPER);

        assertNull(result);
    }

    @Test
    public void queryOne_closesAllResources() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getString("name")).thenReturn("Acme Motors");

        db.queryOne("SELECT name FROM dealer", NO_PARAMS, NAME_MAPPER);

        verify(rs).close();
        verify(ps).close();
        verify(conn).close();
    }

    @Test
    public void queryOne_noSetter_returnsMappedValue() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getString("name")).thenReturn("Acme Motors");

        String result = db.queryOne("SELECT name FROM dealer", NAME_MAPPER);

        assertEquals("Acme Motors", result);
    }

    // -------------------------------------------------------------------------
    // queryList()
    // -------------------------------------------------------------------------

    @Test
    public void queryList_returnsAllMappedRows() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("name")).thenReturn("Dealer A", "Dealer B");

        List<String> names = db.queryList(
                "SELECT name FROM dealer",
                NO_PARAMS,
                NAME_MAPPER);

        assertEquals(2, names.size());
        assertEquals("Dealer A", names.get(0));
        assertEquals("Dealer B", names.get(1));
    }

    @Test
    public void queryList_returnsEmptyListOnEmptyResultSet() throws SQLException {
        when(rs.next()).thenReturn(false);

        List<String> names = db.queryList("SELECT name FROM dealer", NO_PARAMS, NAME_MAPPER);

        assertNotNull(names);
        assertEquals(0, names.size());
    }

    @Test
    public void queryList_closesAllResources() throws SQLException {
        when(rs.next()).thenReturn(false);

        db.queryList("SELECT name FROM dealer", NO_PARAMS, NAME_MAPPER);

        verify(rs).close();
        verify(ps).close();
        verify(conn).close();
    }

    // -------------------------------------------------------------------------
    // queryForLong()
    // -------------------------------------------------------------------------

    @Test
    public void queryForLong_returnsValue() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(42L);
        when(rs.wasNull()).thenReturn(false);

        Long result = db.queryForLong("SELECT COUNT(*) FROM dealer", NO_PARAMS);

        assertEquals(Long.valueOf(42), result);
    }

    @Test
    public void queryForLong_returnsNullOnEmptyResultSet() throws SQLException {
        when(rs.next()).thenReturn(false);

        Long result = db.queryForLong("SELECT COUNT(*) FROM dealer", NO_PARAMS);

        assertNull(result);
    }

    @Test
    public void queryForLong_returnsNullForSqlNull() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        Long result = db.queryForLong("SELECT null_col FROM dealer", NO_PARAMS);

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // queryForString()
    // -------------------------------------------------------------------------

    @Test
    public void queryForString_returnsValue() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("Acme Motors");

        String result = db.queryForString("SELECT name FROM dealer WHERE id = ?", NO_PARAMS);

        assertEquals("Acme Motors", result);
    }

    @Test
    public void queryForString_returnsNullOnEmptyResultSet() throws SQLException {
        when(rs.next()).thenReturn(false);

        String result = db.queryForString("SELECT name FROM dealer WHERE id = ?", NO_PARAMS);

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    public void update_returnsAffectedRowCount() throws SQLException {
        when(ps.executeUpdate()).thenReturn(3);

        int rows = db.update("DELETE FROM dealer WHERE city = ?", NO_PARAMS);

        assertEquals(3, rows);
    }

    @Test
    public void update_closesAllResources() throws SQLException {
        when(ps.executeUpdate()).thenReturn(1);

        db.update("DELETE FROM dealer WHERE id = ?", NO_PARAMS);

        verify(ps).close();
        verify(conn).close();
    }

    @Test
    public void update_setsParametersOnPreparedStatement() throws SQLException {
        when(ps.executeUpdate()).thenReturn(1);

        db.update(
                "UPDATE dealer SET city = ? WHERE id = ?",
                new JdbcTemplate.StatementSetter() {
                    public void set(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "Provo");
                        ps.setString(2, "abc12345");
                    }
                });

        verify(ps).setString(1, "Provo");
        verify(ps).setString(2, "abc12345");
    }

    // -------------------------------------------------------------------------
    // insert()
    // -------------------------------------------------------------------------

    @Test
    public void insert_returnsGeneratedKey() throws SQLException {
        when(ps.executeUpdate()).thenReturn(1);
        when(rs.next()).thenReturn(true);
        when(rs.getString(1)).thenReturn("generated-id-001");

        String key = db.insert(
                "INSERT INTO dealer (name) VALUES (?)",
                new JdbcTemplate.StatementSetter() {
                    public void set(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "Acme Motors");
                    }
                });

        assertEquals("generated-id-001", key);
    }

    @Test
    public void insert_returnsNullWhenNoKeyGenerated() throws SQLException {
        when(ps.executeUpdate()).thenReturn(1);
        when(rs.next()).thenReturn(false);

        String key = db.insert("INSERT INTO dealer (name) VALUES (?)", NO_PARAMS);

        assertNull(key);
    }

    // -------------------------------------------------------------------------
    // transaction()
    // -------------------------------------------------------------------------

    @Test
    public void transaction_setsAutoCommitFalseAndCommitsOnSuccess() throws SQLException {
        db.transaction(new JdbcTemplate.TransactionCallback<Void>() {
            public Void execute(Connection c) throws SQLException {
                return null;
            }
        });

        verify(conn).setAutoCommit(false);
        verify(conn).commit();
        verify(conn).close();
    }

    @Test
    public void transaction_rollsBackAndClosesOnSqlException() throws SQLException {
        try {
            db.transaction(new JdbcTemplate.TransactionCallback<Void>() {
                public Void execute(Connection c) throws SQLException {
                    throw new SQLException("forced failure");
                }
            });
            fail("Expected SQLException");
        } catch (SQLException e) {
            // expected
        }

        verify(conn).rollback();
        verify(conn).close();
    }

    @Test
    public void transaction_rollsBackAndClosesOnRuntimeException() throws SQLException {
        try {
            db.transaction(new JdbcTemplate.TransactionCallback<Void>() {
                public Void execute(Connection c) throws SQLException {
                    throw new RuntimeException("unexpected");
                }
            });
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }

        verify(conn).rollback();
        verify(conn).close();
    }

    @Test
    public void transaction_returnsCallbackResult() throws SQLException {
        String result = db.transaction(new JdbcTemplate.TransactionCallback<String>() {
            public String execute(Connection c) throws SQLException {
                return "Acme Motors";
            }
        });

        assertEquals("Acme Motors", result);
    }

    @Test
    public void transaction_closesConnectionEvenIfRollbackFails() throws SQLException {
        doThrow(new SQLException("rollback failed")).when(conn).rollback();

        try {
            db.transaction(new JdbcTemplate.TransactionCallback<Void>() {
                public Void execute(Connection c) throws SQLException {
                    throw new SQLException("original failure");
                }
            });
            fail("Expected SQLException");
        } catch (SQLException e) {
            // expected
        }

        verify(conn).close();
    }
}