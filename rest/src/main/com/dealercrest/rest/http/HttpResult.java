package com.dealercrest.rest.http;

import java.io.IOException;
import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;

public abstract class HttpResult {

    public static final int HTTP_CACHE_SECONDS = 60;
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private final List<HttpCookie> cookies;
    public abstract void write(ChannelHandlerContext ctx) throws IOException;

    public HttpResult() {
        this.cookies = new ArrayList<>();
    }

    public List<HttpCookie> getCookies() {
        return Collections.unmodifiableList(cookies);
    }

    public HttpResult addCookie(String name, String value, boolean httpOnly) {
        HttpCookie httpCookie = new HttpCookie(name, value);
        httpCookie.setPath("/");
        httpCookie.setHttpOnly(httpOnly);
        cookies.add(httpCookie);
        return this;
    }

    public HttpResult addCookie(String name, String value, boolean httpOnly, long maxAge) {
        HttpCookie httpCookie = new HttpCookie(name, value);
        httpCookie.setPath("/");
        httpCookie.setHttpOnly(httpOnly);
        httpCookie.setMaxAge(maxAge);
        cookies.add(httpCookie);
        return this;
    }

    protected Map<String,Object> buildDateAndCacheHeaders(long lastModified) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        Map<String,Object> internalHeaders = new HashMap<>();
        internalHeaders.put(HttpHeaderNames.DATE.toString(), dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        internalHeaders.put(HttpHeaderNames.EXPIRES.toString(), dateFormatter.format(time.getTime()));
        internalHeaders.put(HttpHeaderNames.CACHE_CONTROL.toString(),
                "private, max-age=" + HTTP_CACHE_SECONDS);
        internalHeaders.put(HttpHeaderNames.LAST_MODIFIED.toString(),
                dateFormatter.format(new Date(lastModified)));
        return internalHeaders;
    }

}
