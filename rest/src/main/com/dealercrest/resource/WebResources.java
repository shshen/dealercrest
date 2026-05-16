package com.dealercrest.resource;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import com.dealercrest.page.Page;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeStore;
import com.dealercrest.page.DealerTheme;
import com.dealercrest.template.TemplateEngine;

public class WebResources {

    private final ErrorPages errorPages;
    private final ThemeStore themeStore;
    // private final Map<String, DealerTheme> themes;
    private final Map<String, SitePages> hostSites;
 
    public WebResources(SitePages appPages, ErrorPages errorPages, ThemeStore themeStore) {
        // this.themes = new HashMap<>(themesMap);
        this.themeStore = themeStore;
        this.hostSites = new HashMap<>();
        this.errorPages = errorPages;
        addDealerPages(appPages);
    }

    public DealerTheme getTheme(String themeName) {
        return themeStore.getTheme(themeName);
    }

    public void addTheme(DealerTheme theme) {
        themeStore.addTheme(theme);
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
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarFile jar = conn.getJarFile();
            JarScanner jarResource = new JarScanner();
            return jarResource.scan(jar, domain, templateEngine);
        }
    }
 
}
