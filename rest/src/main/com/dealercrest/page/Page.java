package com.dealercrest.page;

import org.json.JSONObject;

import com.dealercrest.http.QueryRequest;
import com.dealercrest.rest.http.HttpResult;

public abstract class Page {

    private final String dealerId;
    private final String path;
    private final long lastModified;

    public Page(String dealerId, String path, long lastModified) {
        this.dealerId = dealerId;
        this.path = path;
        this.lastModified = lastModified;
    }

    public String getDealerId() {
        return dealerId;
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

    public abstract HttpResult render(QueryRequest requestContext);

}
