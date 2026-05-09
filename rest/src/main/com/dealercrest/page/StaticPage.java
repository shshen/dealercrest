package com.dealercrest.page;

import com.dealercrest.http.HttpResult;
import com.dealercrest.http.NettyResult;
import com.dealercrest.http.QueryRequest;
import com.dealercrest.template.FragmentRegistry;
import com.dealercrest.template.Model;
import com.dealercrest.template.TemplateEngine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

import java.nio.charset.StandardCharsets;

public class StaticPage extends Page {

    private final ByteBuf byteBuf;

    public StaticPage(String path, HtmlPageSource pageSource, Layout layout,
            TemplateEngine templateEngine) {
        super(path, Math.max(pageSource.getLastModified(), layout.getLastModified()));
        Model dataModel = new Model();
        dataModel.setAll(pageSource.getMetadata());
        FragmentRegistry fr = new FragmentRegistry();
        fr.registerAll(pageSource.getSlots());
        dataModel.setFragments(fr);

        String output = templateEngine.render(layout.getPath(), layout.getContent(), dataModel);
        this.byteBuf = Unpooled.copiedBuffer(output, StandardCharsets.UTF_8).asReadOnly();
    }

    public StaticPage(String path, long lastModified, String content) {
        super(path, lastModified);
        this.byteBuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8).asReadOnly();
    }

    public StaticPage(String path, ThemeFiles themeTemplate, DealerSiteJson siteDefinitin,
            TemplateEngine engine) {
        super(path, 0l);
        this.byteBuf = null;
    }

    @Override
    public HttpResult render(QueryRequest queryRequest) {
        ByteBuf viewByteBuf = byteBuf.retainedDuplicate();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, viewByteBuf);
        response.headers().set("Content-Length", viewByteBuf.readableBytes());
        response.headers().set("Content-Type", getContentType());
        return new NettyResult(response);
    }

}
