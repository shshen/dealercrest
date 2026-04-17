package com.dealercrest.block;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.db.JdbcTemplate;
import com.dealercrest.http.QueryRequest;
import com.dealercrest.page.BlockTemplate;
import com.dealercrest.template.TemplateEngine;

import io.netty.buffer.ByteBuf;

public class DynamicPreparedBlock implements PreparedBlock {

    private final BlockTemplate template;
    private final BlockJsonSource jsonSource;
    private final JdbcTemplate jdbcTemplate;
    private final TemplateEngine templateEngine;
    private static final Logger logger = Logger.getLogger(DynamicPreparedBlock.class.getName());

    public DynamicPreparedBlock(BlockTemplate template, BlockJsonSource jsonSource,
            JdbcTemplate jdbcTemplate, TemplateEngine templateEngine) {
        this.template = template;
        this.jsonSource = jsonSource;
        this.jdbcTemplate = jdbcTemplate;
        this.templateEngine = templateEngine;
    }

    @Override
    public ByteBuf render(QueryRequest queryRequest) {
        logger.log(Level.INFO, "{0},{1}",
                new String[] { template.getName(), jsonSource.getClass().getName(), jdbcTemplate.getClass().getName(),
                        templateEngine.getClass().getName() });
        return null;
    }

}
