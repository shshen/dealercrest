package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.db.JdbcTemplate;

import io.netty.buffer.ByteBuf;

/**
 * 1, traverse folder/jar file to get ThemeBlocks and WebSite Pages
 * 2, featch the dealer json page definitions which recently changed
 * 3, render the theme blocks with json definitions, and build dealer website pages.
 * 4, scheduled jon to run strategy 2 and 3 every 5 minutes, to keep the website up to date.
 */
public class RenderedBlockCacheTask implements Runnable {

    private final WebResources resources;
    private final Map<String, ByteBuf> blockCache;
    private static final Logger logger = Logger.getLogger(RenderedBlockCacheTask.class.getName());

    public RenderedBlockCacheTask(WebResources resources, JdbcTemplate jdbcTemplate) {
        this.resources = resources;
        this.blockCache = new HashMap<>();
    }

    // call this in main thread to load theme blocks and app pages when application start.
    public void rebuildDealerPages() {
        // 1, featch the dealer json page definitions which recently changed
        // 2, render the theme blocks with json definitions, and build dealer website pages.
        logger.log(Level.INFO, "resource {0}, blockCache {2}", 
            new String[]{resources.getAppPages().getDealerId(), blockCache.size()+""});
    }

    @Override
    public void run() {
        rebuildDealerPages();
    }
    
}
