package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class SitePages {

    private final String hostId;
    private final Map<String, Layout> layouts;
    private final Map<String, Page> pages;

    public SitePages(String hostId) {
        this.hostId = hostId;
        this.layouts = new HashMap<>();
        this.pages = new HashMap<>();
    }

    public Page getPage(String resourcePath) {
        return pages.get(resourcePath);
    }

    public String getHostId() {
        return hostId;
    }

    public void addPage(Page page) {
        String resourcePath = page.getResourcePath();
        if ( resourcePath.endsWith(".html") ) {
            int endIdx = resourcePath.length() - 5;
            resourcePath = resourcePath.substring(0, endIdx);
        }
        if ( "/index".equals(resourcePath)) {
            pages.put("/", page);
        }
        pages.put(resourcePath, page);
    }

    public void addLayout(String path, long lastModified, String content) {
        layouts.put(path, new Layout(path, content, lastModified));
    }

    public Layout getLayout(String path) {
        return layouts.get(path);
    }

}
