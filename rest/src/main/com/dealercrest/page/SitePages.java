package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class SitePages {

    private final String hostId;
    private final Map<String, Layout> layouts;
    private final Map<String, Page> pages;

    public SitePages(String hostId) {
        this(hostId, new HashMap<>());
    }

    public SitePages(String hostId, Map<String, Page> pages) {
        this.pages = pages;
        this.hostId = hostId;
        this.layouts = new HashMap<>();
    }

    public Page getPage(String pagePath) {
        return pages.get(pagePath);
    }

    public String getHostId() {
        return hostId;
    }

    public void addPage(Page page) {
        pages.put(page.getPath(), page);
    }

    public void addLayout(String path, long lastModified, String content) {
        layouts.put(path, new Layout(path, lastModified, content));
    }

    public Layout getLayout(String path) {
        return layouts.get(path);
    }

}
