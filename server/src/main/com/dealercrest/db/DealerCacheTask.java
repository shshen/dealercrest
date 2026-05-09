package com.dealercrest.db;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.dealercrest.db.model.Dealer;

public class DealerCacheTask implements Runnable {

    private final DataSource dataSource;
    private final String saasDomain;
    private volatile Instant lastRefreshedAt = Instant.EPOCH;
    private volatile Map<String, Dealer> cache = new HashMap<>();
    private static final Logger logger = Logger.getLogger(DealerCacheTask.class.getName());

    public DealerCacheTask(DataSource dataSource, String saasDomain) {
        this.dataSource = dataSource;
        this.saasDomain = saasDomain;
    }

    public String getDealerId(String domain) {
        if (saasDomain.equals(domain)) {
            return saasDomain;
        }
        
        Dealer dealer = cache.get(domain);
        if ( dealer!=null ) {
            return dealer.getId();
        }
        return null;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "start dealer cache fetching...", new Object[]{dataSource, lastRefreshedAt});
    }

}
