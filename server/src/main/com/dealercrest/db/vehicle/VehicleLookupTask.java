package com.dealercrest.db.vehicle;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Supplier;

import com.dealercrest.db.JdbcTemplate;

public class VehicleLookupTask implements Supplier<Vehicle> {

    private final JdbcTemplate jdbcTemplate;
    private final String vin;

    public VehicleLookupTask(JdbcTemplate jdbcTemplate, String vin) {
        this.jdbcTemplate = jdbcTemplate;
        this.vin = vin;
    }

    @Override
    public Vehicle get() {
        try {
            return jdbcTemplate.queryOne(
                "SELECT d.* FROM dealers d " +
                "JOIN dealer_domains dd ON d.id = dd.dealer_id " +
                "WHERE dd.domain = ?",
                new JdbcTemplate.StatementSetter() {
                        public void set(PreparedStatement ps) throws SQLException {
                            ps.setString(1, vin);
                        }
                },
                new VehicleMapper()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
