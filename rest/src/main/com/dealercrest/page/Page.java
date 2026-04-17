package com.dealercrest.page;

import org.json.JSONObject;

import com.dealercrest.rest.http.HttpResult;

public abstract class Page {

    private String path;
    private long lastModified;

    public Page(String path, long lastModified) {
        this.path = path;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return path;
    }
    
    public long getLastModified() {
        return lastModified;
    }

    static long getRecentModified(Layout layout, JSONObject common, JSONObject pageData) {
        return 0;
    }

    public abstract HttpResult render(RenderContext ctx);

}
