package com.dealercrest.db;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.db.model.Dealer;


public class DealerCacheTask implements Runnable {

    private final JdbcTemplate jdbcTemplate;
    private volatile Instant lastRefreshedAt = Instant.EPOCH;
    private volatile Map<String, Dealer> cache = new HashMap<>();
    private static final Logger logger = Logger.getLogger(DealerCacheTask.class.getName());

    public DealerCacheTask(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getDealerId(String domain) {
        Dealer dealer = cache.get(domain);
        if ( dealer!=null ) {
            return dealer.getId();
        }
        return null;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "start", new Object[]{jdbcTemplate, lastRefreshedAt});
    }

}
