package com.dealercrest.page;

import java.util.List;

import org.json.JSONObject;

import com.dealercrest.rest.http.HttpResult;

import io.netty.buffer.ByteBuf;

public class StaticPage extends Page {

    private final ByteBuf byteBuf;

    public StaticPage(String path, long lastModified, FragmentFile content, 
            Layout layout) {
        super(path, lastModified);
        this.byteBuf = null; // convert content to ByteBuf
        // 1, parse content, if layout is null. write content to byteBuf.
        // 2, if layout not found throw exception.
        // 3, if layout found,apply layout and write to byteBuf. Also, set lastModified, length etc.
    }

    public StaticPage(Layout layout, List<BlockTemplate> blocks, JSONObject common, 
            JSONObject pageData, TemplateEngine engine) {        
        super(pageData.getString("path"), getRecentModified(layout,common,pageData));
        this.byteBuf = null;
        // Placeholders placeholders = renderContext.renderBlock(blocks, layout);
        // this.byteBuf = layout.apply(placeholders);
    }

    public StaticPage(String path, long lastModified, String content) {
        super(path, lastModified);
        this.byteBuf = null; // convert content to ByteBuf
    }

    @Override
    public HttpResult render(RenderContext ctx) {
        // return byteBuf.retainedDuplicate();
        return null;
    }

} 
