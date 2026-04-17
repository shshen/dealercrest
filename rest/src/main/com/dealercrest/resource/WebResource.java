package com.dealercrest.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.dealercrest.page.Page;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeFiles;

public class WebResource {

    private final Map<Integer, Page> errorPages;
    private final Map<String, ThemeFiles> themes;
    private final Map<String, SitePages> dealerSites;
 
    public WebResource(SitePages appPages, Map<String, ThemeFiles> themesMap) {
        this.themes = new HashMap<>(themesMap);
        this.dealerSites = new HashMap<>();
        this.errorPages = new HashMap<>();
        addDealerPages(appPages);
    }

    public ThemeFiles getTheme(String themeName) {
        return themes.get(themeName);
    }

    public void addTheme(ThemeFiles theme) {
        themes.put(theme.getThemeName(), theme);
    }

    public SitePages getDealerPages(String hostId) {
        return dealerSites.get(hostId);
    }

    public void addDealerPages(SitePages pages) {
        dealerSites.put(pages.getHostId(), pages);
    }

    public void addErrorPage(int statusCode, Page page) {
        errorPages.put(statusCode, page);
    }
    public Page getErrorPage(int statusCode) {
        return errorPages.get(statusCode);
    }

    public static WebResource load(Path localBase, String domain) throws IOException {
        Path localWebApp = localBase.resolve("webapp");
        if (Files.isDirectory(localWebApp)) {
            LocalScanner localResource = new LocalScanner();
            return localResource.scan(localWebApp, domain);
        }
        JarScanner jarResource = new JarScanner();
        return jarResource.scan(localWebApp, domain);
    }
 
}
