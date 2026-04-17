package com.dealercrest.db.vehicle;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dealercrest.db.JdbcTemplate;

class VehicleMapper implements JdbcTemplate.RowMapper<Vehicle> {
 
    public Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setId(rs.getString("id"));
        v.setDealerId(rs.getString("dealer_id"));
        v.setVin(rs.getString("vin"));
        v.setYear(rs.getInt("year"));
        v.setMake(rs.getString("make"));
        v.setModel(rs.getString("model"));
        v.setTrim(rs.getString("trim"));
        v.setExteriorColor(rs.getString("exterior_color"));
        v.setMileage(rs.getInt("mileage"));
        v.setPrice(rs.getBigDecimal("price"));
        v.setStatus(rs.getString("status"));
        v.setCondition(rs.getString("condition"));
        return v;
    }
}
