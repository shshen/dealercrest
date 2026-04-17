package com.dealercrest.rest.http;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SharedResult extends HttpResult{

    private final int length;
    private final long lastModified;
    private final ByteBuf byteBuf;
    private final Map<String,Object> headers;

    public SharedResult(byte[] content, long lastModified) {
        this.byteBuf = Unpooled.wrappedBuffer(content).asReadOnly();
        this.length = content.length;
        this.lastModified = lastModified;
        this.headers = buildDateAndCacheHeaders(lastModified);
    }

    /**
     * This could be reused for multiple requests. 
     * 
    */
    @Override
    public void write(ChannelHandlerContext ctx) {
        FullHttpResponse response = buildHttpResponse();
        try {
            ChannelFuture future = ctx.writeAndFlush(response);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) {
                    if (!f.isSuccess()) {
                        // Write failed -> Netty will NOT release, so we must release manually
                        response.release();
                    }
                    f.channel().close(); // Close after write
                }
            });
        } catch (Throwable t) {
            // writeAndFlush threw before returning future -> release manually
            response.release();
            throw t;
        }
    }

    private FullHttpResponse buildHttpResponse() {
        ByteBuf content = byteBuf.retainedDuplicate();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.OK, content);
        response.headers().set("Content-Length", length);
        Set<Entry<String,Object>> entries = headers.entrySet();
        for(Entry<String,Object> entry: entries) {
            response.headers().set(entry.getKey(), entry.getValue());
        }

        List<HttpCookie> cookies = getCookies();
        for(HttpCookie cookie: cookies) {
            response.headers().add("Set-Cookie", cookie.toString());
        }
        return response;
    }

    public long getLastModified() {
        return lastModified;
    }
}
