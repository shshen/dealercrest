package com.dealercrest.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import javax.net.ssl.SSLException;

import com.dealercrest.rest.NettyRouters;
import com.dealercrest.rest.http.HttpResult;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyHttpsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // private final NettyRouters nettyRouters;
    // private final String domainHost = "dev.dataleading.com";
    public static final int HTTP_CACHE_SECONDS = 60;
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final Logger logger = Logger.getLogger(NettyHttpsHandler.class.getName());

    public NettyHttpsHandler(NettyRouters nettyRouters) {
        // this.nettyRouters = nettyRouters;
        // this.domainHost = Environment.getInstance().getDomainHost();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
            throws Exception {
        String path = sanitizeUri(request.uri());
        if (path.endsWith(".mp4")) {
            // Mp4VideoService streamingService = new Mp4VideoService();
            // streamingService.handle(ctx, request, path);
            return;
        }
        HttpResult httpResult = buildHttpResult(request, path);
        httpResult.write(ctx);
    }

    private HttpResult buildHttpResult(FullHttpRequest request, String path) {
        return null;
    }

    private String sanitizeUri(String path) {
        int idx = path.indexOf("?");
        if (idx > -1) {
            path = path.substring(0, idx);
        }

        if ("/".equals(path)) {
            path = "/product.html";
        }
        return path;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String m = cause.getMessage();
        String clientIp = "unknown";
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = remoteAddress.getAddress().getHostAddress();
        } catch (Exception e) {
            // do nothing...
        }

        if ( m != null && m.contains("Connection reset") ) {
            logger.log(Level.WARNING, "host={0}, connection reset by peer", clientIp );
        } else if ( isSSLException(cause) ) {
            logger.log(Level.WARNING, "host={0}, not a valid ssl record", clientIp );
        } else {
            logger.log(Level.WARNING, "host={0}, message={1}", new Object[] { clientIp, m });
        }

        if (ctx.channel().isActive()) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
    }

    private boolean isSSLException(Throwable cause) {
        int max_depth = 5;
        while (cause != null && max_depth>0) {
            if (cause instanceof SSLException) {
                return true;
            }
            max_depth = max_depth - 1;
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}
