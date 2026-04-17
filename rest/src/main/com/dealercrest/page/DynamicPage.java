package com.dealercrest.page;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dealercrest.rest.http.DeferredResult;
import com.dealercrest.rest.http.HttpResult;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponse;

public class DynamicPage extends Page {

    private final Layout layout;
    private final TemplateEngine engine;
    private final JSONObject pageData;
    private final ThemeTemplates themeTemplates;
    private final List<ByteBuf> cachedBlockBytes;

    public DynamicPage(ThemeTemplates themeTemplates,JSONObject common, 
            JSONObject pageData, TemplateEngine engine) throws Exception {
        super(pageData.getString("path"), 
            getRecentModified(
                themeTemplates.getLayout(common.getString("layout")),
                common,pageData));
        this.themeTemplates = themeTemplates;
        this.layout = themeTemplates.getLayout(common.getString("layout"));
        this.engine = engine;
        this.pageData = pageData;
        this.cachedBlockBytes = build();
    }

    private List<ByteBuf> build() throws Exception {
        JSONArray blockArray = pageData.getJSONArray("blocks");
        int size = blockArray.length();
        List<ByteBuf> cachedBlockBytes = new ArrayList<>(size);
        for(int i=0; i<size; i++) {
            JSONObject blockConfig = blockArray.getJSONObject(i);
            String type = blockConfig.getString("type");
            BlockTemplate template = themeTemplates.getBlock(type);
            if ( "static".equals(template.getRender()) ) {
                String renderedContent = engine.process(template.getHtmlContent(), blockConfig);
                ByteBuf buf = Unpooled.copiedBuffer(renderedContent, StandardCharsets.UTF_8);
                cachedBlockBytes.add(buf);
            } else {
                cachedBlockBytes.add(null);
            }
        }
        return cachedBlockBytes;
    }

    @Override
    public HttpResult render(RenderContext renderContext) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        ExecutorService executor = renderContext.getExecutorService();
        PageDefinition pageDefinition = new PageDefinition(getPath(), themeTemplates, pageData, layout);
        PageRenderTask task = new PageRenderTask(renderContext, pageDefinition, cachedBlockBytes, future);
        executor.submit(task);
        return new DeferredResult(future);
    }

}
