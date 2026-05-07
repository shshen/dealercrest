package com.dealercrest.page;

import org.json.JSONObject;

import com.dealercrest.http.HttpResult;
import com.dealercrest.http.QueryRequest;

public abstract class Page {

    private final String dealerId;
    private final String resourcePath;
    private final long lastModified;

    public Page(String dealerId, String resourcePath, long lastModified) {
        this.dealerId = dealerId;
        this.resourcePath = resourcePath;
        this.lastModified = lastModified;
    }

    public String getDealerId() {
        return dealerId;
    }

    public String getResourcePath() {
        return resourcePath;
    }
    
    public long getLastModified() {
        return lastModified;
    }

    static long getRecentModified(Layout layout, JSONObject common, JSONObject pageData) {
        return 0;
    }

    public abstract HttpResult render(QueryRequest requestContext);

    @Override
    public String toString() {
        return "Page [dealerId=" + dealerId + ", resourcePath=" + resourcePath + ", lastModified=" + lastModified + "]";
    }
}
