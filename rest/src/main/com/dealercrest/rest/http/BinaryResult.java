package com.dealercrest.rest.http;

import javax.imageio.ImageIO;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BinaryResult extends HttpResult {

    private final byte[] content;
    private final int length;
    private final long lastModified;
    private final Map<String,Object> headers;

    public BinaryResult(BufferedImage image, String security) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        addCookie("dl_captcha", security, false, 30);
        this.content = baos.toByteArray();
        this.length = content.length;
        this.headers = Map.of("Content-Type", "image/jpeg");
        this.lastModified = 0;
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        FullHttpResponse response = buildHttpResponse(ctx);
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

    public long getLastModified() {
        return lastModified;
    }

    private FullHttpResponse buildHttpResponse(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().ioBuffer(content.length);
        buf.writeBytes(content);

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                HttpResponseStatus.OK, buf);
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

}
