package com.dealercrest.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.PERMANENT_REDIRECT;

public class RedirectHttpsHandler extends SimpleChannelInboundHandler<HttpRequest> {

    public static final String NO_HOST_ERROR = "The client software did not provide a hostname using Server Name Indication (SNI), "
            + "which is required to access this server.";
    private static final Logger logger = Logger.getLogger(RedirectHttpsHandler.class.getName());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {
        String host = request.headers().get("host");

        HttpResponse response;
        if (host != null && !host.isEmpty()) {
            String address = request.uri();
            response = new DefaultHttpResponse(HTTP_1_1, PERMANENT_REDIRECT);
            response.headers().set(LOCATION, "https://" + host + address);
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, NOT_IMPLEMENTED,
                    Unpooled.copiedBuffer(NO_HOST_ERROR.getBytes()));
        }

        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String msg = cause.getMessage();
        String host = "unknown";
        try {
            host = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            // do nothing...
        }

        if (msg != null
                && !msg.equals("Connection reset by peer")) {
            logger.log(Level.WARNING, "host={0}, message={1}", new Object[] { host, msg });
        } else {
            logger.log(Level.WARNING, "host={0}, ignored exception message={1}", new String[] { host, msg });
        }
        ctx.close();
    }

}
