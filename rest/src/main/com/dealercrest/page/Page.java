package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.dealercrest.http.HttpResult;
import com.dealercrest.http.QueryRequest;

public abstract class Page {

    private final int status;
    private final String resourcePath;
    private final long lastModified;
    private final String contentType;

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

    public Page(String resourcePath, long lastModified) {
        this(200, resourcePath, lastModified);
    }

    public Page(int status, String resourcePath, long lastModified) {
        this.status = status;
        this.resourcePath = resourcePath;
        this.lastModified = lastModified;
        this.contentType = getContentType(resourcePath);
    }

    public String getResourcePath() {
        return resourcePath;
    }
    
    public long getLastModified() {
        return lastModified;
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatus() {
        return status;
    }

    static long getRecentModified(Layout layout, JSONObject common, JSONObject pageData) {
        return 0;
    }

    private String getContentType(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1) {
            return "text/plain";
        }
        String ext = path.substring(dot + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    }

    public abstract HttpResult render(QueryRequest requestContext);

    @Override
    public String toString() {
        return "Page [resourcePath=" + resourcePath + ", lastModified=" + lastModified + "]";
    }
}
