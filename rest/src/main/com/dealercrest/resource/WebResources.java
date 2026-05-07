package com.dealercrest.resource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.dealercrest.page.Page;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeFiles;
import com.dealercrest.template.TemplateEngine;

public class WebResources {

    private final ErrorPages errorPages;
    private final Map<String, ThemeFiles> themes;
    private final Map<String, SitePages> hostSites;
 
    public WebResources(SitePages appPages, ErrorPages errorPages, Map<String, ThemeFiles> themesMap) {
        this.themes = new HashMap<>(themesMap);
        this.hostSites = new HashMap<>();
        this.errorPages = errorPages;
        addDealerPages(appPages);
    }

    public ThemeFiles getTheme(String themeName) {
        return themes.get(themeName);
    }

    public void addTheme(ThemeFiles theme) {
        themes.put(theme.getThemeName(), theme);
    }

    public SitePages getDealerPages(String hostId) {
        return hostSites.get(hostId);
    }

    public void addDealerPages(SitePages pages) {
        hostSites.put(pages.getHostId(), pages);
    }

    public Page getErrorPage(int statusCode) {
        return errorPages.get(statusCode);
    }

    public static WebResources load(String domain, TemplateEngine templateEngine) 
            throws IOException, URISyntaxException {
        URL url = WebResources.class.getClassLoader().getResource("webapp");
        if ( "file".equals(url.getProtocol() )) {
            Path localWebApp = Paths.get(url.toURI());
            LocalScanner localResource = new LocalScanner();
            return localResource.scan(localWebApp, domain, templateEngine);
        } else {
            throw new IllegalStateException("Unsupported protocol: " + url.getProtocol());
        }
        // Path localWebApp = localBase.resolve("webapp");
        // if (Files.isDirectory(localWebApp)) {
        //     LocalScanner localResource = new LocalScanner();
        //     return localResource.scan(localWebApp, domain, templateEngine);
        // }
        // JarScanner jarResource = new JarScanner();
        // return jarResource.scan(localWebApp, domain);
    }
 
}
