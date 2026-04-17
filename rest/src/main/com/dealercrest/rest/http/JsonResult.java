package com.dealercrest.rest.http;

import org.json.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import java.net.HttpCookie;
import java.util.List;

public class JsonResult extends HttpResult {

    private final String jsonText;
    private final int status;

    public JsonResult(JSONObject jsonObject) {
        this.status = jsonObject.optInt("code", 200);
        this.jsonText = jsonObject.toString();
    }

    public JsonResult(int statusCode, String message) {
        this.status = statusCode;
        this.jsonText = new JSONObject().put("code", statusCode).put("message", message)
                .toString();
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        HttpResponse response = buildHttpResponse(ctx);
        ChannelFuture future = ctx.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse buildHttpResponse(ChannelHandlerContext ctx) {
        if ( status == 204 ) {
            return buildNoContentResponse();
        }
        byte[] jsonBytes = jsonText.getBytes();
        ByteBuf buf = ctx.alloc().ioBuffer(jsonBytes.length);
        buf.writeBytes(jsonBytes);

        HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status),buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, jsonBytes.length);
        List<HttpCookie> cookies = getCookies();
        for (HttpCookie cookie : cookies) {
            Cookie nettyCookie = buildCookie(cookie);
            String cookieStr = ServerCookieEncoder.STRICT.encode(nettyCookie);
            response.headers().add("Set-Cookie", cookieStr);
        }
        return response;
    }

    private Cookie buildCookie(HttpCookie c) {
        Cookie nettyCookie = new DefaultCookie(c.getName(), c.getValue());
        nettyCookie.setDomain(c.getDomain());
        nettyCookie.setHttpOnly(c.isHttpOnly());
        nettyCookie.setPath(c.getPath());
        if (c.getMaxAge() != -1) {
            nettyCookie.setMaxAge(c.getMaxAge());
        }
        return nettyCookie;
    }

    private  HttpResponse buildNoContentResponse() {
        HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NO_CONTENT, Unpooled.EMPTY_BUFFER);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "POST");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return response;
    }

}
