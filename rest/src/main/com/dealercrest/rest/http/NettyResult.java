package com.dealercrest.rest.http;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCounted;

public class NettyResult extends HttpResult {
    
    private final HttpResponse response;

    public NettyResult(HttpResponse response) {
        this.response = response;
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        try {
            ChannelFuture future = ctx.writeAndFlush(response);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) {
                    if (!f.isSuccess()) {
                        safeRelease(response);
                    }
                    f.channel().close();
                }
            });
        } catch (Throwable t) {
            safeRelease(response);
            ctx.close();
        }
    }
    
    static void safeRelease(Object obj) {
        if (obj instanceof ReferenceCounted rc) {
            rc.release();
        }
    }
}
