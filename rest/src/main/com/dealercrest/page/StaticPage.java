package com.dealercrest.page;

import com.dealercrest.http.QueryRequest;
import com.dealercrest.rest.http.HttpResult;
import com.dealercrest.rest.http.NettyResult;
import com.dealercrest.template.DataModel;
import com.dealercrest.template.TemplateEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class StaticPage extends Page {

    private final ByteBuf byteBuf;
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("css",  "text/css");
        MIME_TYPES.put("png",  "image/png");
        MIME_TYPES.put("ico",  "image/x-icon");
        MIME_TYPES.put("svg",  "image/svg+xml");
        MIME_TYPES.put("js",   "application/javascript");
        MIME_TYPES.put("xml",  "text/xml");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("html", "text/html; charset=utf-8");
        MIME_TYPES.put("woff2","font/woff2");
    }

    public StaticPage(String dealerId, String path, HtmlPageSource pageSource, Layout layout,
            TemplateEngine templateEngine) {
        super(dealerId, path, Math.max(pageSource.getLastModified(), layout.getLastModified()));

        DataModel dataModel = new DataModel();
        dataModel.setAll(pageSource.getMetadata());
        dataModel.setAll(pageSource.getSlots());
        String output = templateEngine.render(layout.getPath(), layout.getContent(), dataModel);
        this.byteBuf = Unpooled.copiedBuffer(output, StandardCharsets.UTF_8);
    }

    public StaticPage(String dealerId, String path, long lastModified, String content) {
        super(dealerId, path, lastModified);
        this.byteBuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
    }

    public StaticPage(String dealerId, String path, ThemeFiles themeTemplate, DealerSiteJson siteDefinitin,
            TemplateEngine engine) {
        super(dealerId, path, 0l);
        this.byteBuf = null;
    }

    @Override
    public HttpResult render(QueryRequest queryResource) {
        ByteBuf viewByteBuf = byteBuf.retainedDuplicate();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, viewByteBuf);
        response.headers().set("Content-Length", viewByteBuf.readableBytes());
        response.headers().set("Content-Type", getContentType(getPath()));
        return new NettyResult(response);
    }

    private String getContentType(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1) {
            return "text/plain";
        }
        String ext = path.substring(dot + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    }

}
