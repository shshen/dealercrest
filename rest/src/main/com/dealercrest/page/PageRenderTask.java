package com.dealercrest.page;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.dealercrest.http.QueryRequest;
import com.dealercrest.template.TemplateEngine;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponse;

public class PageRenderTask implements Runnable {

    public PageRenderTask(List<ByteBuf> blockBytes, ThemeFiles themeFiles, DealerSiteJson siteDefinition,
            TemplateEngine templateEngine, QueryRequest queryResource, CompletableFuture<HttpResponse> future) {
        // Auto-generated constructor stub
    }

    @Override
    public void run() {

    }

}
