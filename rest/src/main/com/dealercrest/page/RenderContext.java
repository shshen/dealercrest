package com.dealercrest.page;

import java.util.concurrent.ExecutorService;

import com.dealercrest.db.JdbcTemplate;

public class RenderContext {
   
    private final JdbcTemplate jdbcTemplate;
    private final TemplateEngine templateEngine;
    private final ExecutorService executor;

    public RenderContext(
            TemplateEngine templateEngine, 
            JdbcTemplate jdbcTemplate,
            ExecutorService executor) {
        this.jdbcTemplate = jdbcTemplate;
        this.templateEngine = templateEngine;
        this.executor = executor;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
    public ExecutorService getExecutorService() {
        return executor;
    }

}
