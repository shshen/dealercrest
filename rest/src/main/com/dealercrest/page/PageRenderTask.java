package com.dealercrest.page;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponse;

public class PageRenderTask implements Runnable {

    public PageRenderTask(RenderContext ctx, PageDefinition pageDefinition,
            List<ByteBuf> cachedBlockBytes, CompletableFuture<HttpResponse> future) {

    }

    @Override
    public void run() {

    }

}
