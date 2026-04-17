package com.dealercrest.rest.http;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * A DeferredResult represents a long-polling HTTP response that will be completed
 * when certain conditions are met, such as when new data is available.
 * It registers a watcher to be notified when the condition is satisfied.
 */
public class DeferredResult extends HttpResult {

    private final CompletableFuture<HttpResponse> future;

    public DeferredResult(CompletableFuture<HttpResponse> future) {
        this.future = future;
    }

    @Override
    public void write(ChannelHandlerContext ctx) throws IOException {
        final BiConsumer<HttpResponse, Throwable> consumer = createCompletionListener(ctx);
        future.whenComplete(consumer);
    }

    private BiConsumer<HttpResponse, Throwable> createCompletionListener(ChannelHandlerContext ctx) {
        return new BiConsumer<HttpResponse, Throwable>() {
            @Override
            public void accept(HttpResponse json, Throwable ex) {
                HttpResponse response = json;
                if (ex != null) {
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                } 
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        };
    }
    
}
