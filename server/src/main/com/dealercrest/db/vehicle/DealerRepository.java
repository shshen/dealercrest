package com.dealercrest.db.vehicle;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.dealercrest.db.JdbcTemplate;

public class DealerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService jdbcExecutor;

    public DealerRepository(JdbcTemplate jdbcTemplate, ExecutorService jdbcExecutor) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcExecutor = jdbcExecutor;
    }

    public CompletableFuture<Vehicle> findByVin(String vin) {
        VehicleLookupTask task = new VehicleLookupTask(jdbcTemplate, vin);
        return CompletableFuture.supplyAsync(task, jdbcExecutor);
    }

    public CompletableFuture<String> findDealerId(String domain) {
        return null;
    }

}
