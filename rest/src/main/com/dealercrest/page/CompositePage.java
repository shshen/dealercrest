package com.dealercrest.page;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.http.QueryRequest;
import com.dealercrest.rest.http.DeferredResult;
import com.dealercrest.rest.http.HttpResult;
import com.dealercrest.template.TemplateEngine;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponse;

// static blocks resolved, dynamic blocks pending
public class CompositePage extends Page {

    private final List<ByteBuf> blockBytes;
    private final ThemeFiles themeFiles;
    private final DealerSiteJson siteDefinition;
    private final RenderContext renderContext;
    private static final Logger logger = Logger.getLogger(CompositePage.class.getName());

    public CompositePage(String dealerId, String path, ThemeFiles themeFiles, DealerSiteJson siteDefinitin,
            RenderContext renderContext)
            throws Exception {
        super(dealerId, path, 0l);
        this.themeFiles = themeFiles;
        this.siteDefinition = siteDefinitin;
        this.renderContext = renderContext;
        this.blockBytes = build();
    }

    private List<ByteBuf> build() throws Exception {
        logger.log(Level.INFO, "Building dynamic page for path: " + getPath());
        return null;
    }

    @Override
    public HttpResult render(QueryRequest queryRequest) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ExecutorService executor = renderContext.getExecutorService();
        TemplateEngine templateEngine = renderContext.getTemplateEngine();
        PageRenderTask task = new PageRenderTask(blockBytes, themeFiles, siteDefinition, templateEngine,
            queryRequest, future);
        executor.submit(task);
        return new DeferredResult(future);
    }

}
