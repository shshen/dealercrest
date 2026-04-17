package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

import com.dealercrest.http.HttpResult;

public class ThemeFiles {

    private final String themeName;
    private final Map<String, BlockTemplate> blocks;
    private final Map<String, Layout> layouts;
    private final Map<String, StaticPage> staticResources;

    public ThemeFiles(String themeName) {
        this.themeName = themeName;
        this.blocks = new HashMap<>();
        this.layouts = new HashMap<>();
        this.staticResources = new HashMap<>();
    }

    public String getThemeName() {
        return themeName;
    }
    public void addBlock(BlockTemplate block) {
        blocks.put(block.getName(), block);
    }
    public BlockTemplate getBlock(String name) {
        return blocks.get(name);
    }
    public Layout getLayout(String name) {
        return layouts.get(name);
    }
    public void addLayout(String path, String layout, long lastModified) {
        layouts.put(path, new Layout(path, layout, lastModified));
        
    }
    // css, js, images etc.
    public HttpResult buildHttpResult(String path, String ifModifiedSince) {
        StaticPage page = staticResources.get(path);
        // if page not found, return 404 result.
        return page.render(null);
    }

}
