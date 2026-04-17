package com.dealercrest.page;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.dealercrest.block.PreparedBlock;
import com.dealercrest.http.QueryRequest;
import com.dealercrest.template.TemplateEngine;

import io.netty.handler.codec.http.HttpResponse;

public class PageRenderTask implements Runnable {

    public PageRenderTask(List<PreparedBlock> preparedBlocks, TemplateEngine templateEngine, QueryRequest queryRequest,
            CompletableFuture<HttpResponse> future) {
        
    }

    @Override
    public void run() {

    }

}
