package com.dealercrest.page;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dealercrest.block.PreparedBlock;
import com.dealercrest.http.DeferredResult;
import com.dealercrest.http.HttpResult;
import com.dealercrest.http.QueryRequest;
import com.dealercrest.template.TemplateEngine;

import io.netty.handler.codec.http.HttpResponse;

// static blocks resolved, dynamic blocks pending
public class CompositePage extends Page {

    private final List<PreparedBlock> preparedBlocks;
    private final ThemeFiles themeFiles;
    private final DealerSiteJson siteDefinition;
    private final RenderContext renderContext;
    private static final Logger logger = Logger.getLogger(CompositePage.class.getName());

    public CompositePage(String resourcePath, ThemeFiles themeFiles, DealerSiteJson siteDefinitin,
            RenderContext renderContext) throws Exception {
        super(resourcePath, 0l);
        this.themeFiles = themeFiles;
        this.siteDefinition = siteDefinitin;
        this.renderContext = renderContext;
        this.preparedBlocks = build();
    }

    private List<PreparedBlock> build() throws Exception {
        logger.log(Level.INFO, "Building dynamic page for path: ", new String[]{
            getResourcePath(),
            themeFiles.getClass().getName(),
            siteDefinition.getClass().getName(),
            renderContext.getTemplateEngine().getClass().getName()
        });
        return null;
    }

    @Override
    public HttpResult render(QueryRequest queryRequest) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ExecutorService executor = renderContext.getExecutorService();
        TemplateEngine templateEngine = renderContext.getTemplateEngine();
        PageRenderTask task = new PageRenderTask(preparedBlocks, templateEngine, queryRequest, future);
        executor.submit(task);
        return new DeferredResult(future);
    }

}
