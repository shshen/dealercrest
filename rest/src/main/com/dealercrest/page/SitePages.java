package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class SitePages {

    private final String dealerId;
    private final Map<String, Page> pages;

    public SitePages(String dealerId) {
        this(dealerId, new HashMap<>());
    }
    public SitePages(String dealerId, Map<String, Page> pages) {
        this.pages = pages;
        this.dealerId = dealerId;
    }

    public Page getPage(String pagePath) {
        return pages.get(pagePath);
    }

    public String getDealerId() {
        return dealerId;
    }

    public void addPage(Page page) {
        pages.put(page.getPath(), page);
    }

}
