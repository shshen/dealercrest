package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class WebResources {

    private final SitePages appPages;
    private final Map<String, ThemeTemplates> themes;
    private final Map<String, SitePages> dealerSites;
 
    public WebResources(SitePages appPages, Map<String, ThemeTemplates> themes) {
        this.themes = themes;
        this.appPages = appPages;
        this.dealerSites = new HashMap<>();
    }
 
    public ThemeTemplates getThemes(String themeName) {
        return themes.get(themeName);
    }
 
    public SitePages getAppPages() {
        return appPages;
    }

    public SitePages getDealerPages(String dealerId) {
        return dealerSites.get(dealerId);
    }

    public void addDealerPages(SitePages pages) {
        dealerSites.put(pages.getDealerId(), pages);
    }
 
}
