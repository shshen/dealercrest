package com.dealercrest.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.dealercrest.page.HtmlPageSource;
import com.dealercrest.page.Layout;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.StaticPage;
import com.dealercrest.page.ThemeStore;
import com.dealercrest.page.DealerTheme;
import com.dealercrest.template.TemplateEngine;

public class LocalScanner {

    public WebResources scan(Path webappRoot, String domain, TemplateEngine templateEngine) throws IOException {
        ThemeStore themeStore = scanThemes(webappRoot);
        SitePages appPages = scanWebsite(webappRoot, domain, templateEngine);
        ErrorPages errorPages = scanErrorPages(webappRoot);
        return new WebResources(appPages, errorPages, themeStore);
    }

    private SitePages scanWebsite(Path webappRoot, String domain, TemplateEngine templateEngine) throws IOException {
        Path websiteRoot = webappRoot.resolve("website");
        if ( !Files.isDirectory(websiteRoot) ) {
            throw new IllegalArgumentException("website is not found");
        }
        SitePages appPages = new SitePages(domain);
        try (Stream<Path> all = Files.walk(websiteRoot)) {
            Iterator<Path> it = all.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                if (Files.isRegularFile(p)) {
                    FileTime lastModified = Files.getLastModifiedTime(p);
                    String content = Files.readString(p, StandardCharsets.UTF_8);
                    HtmlPageSource pageSource = HtmlPageSource.parse(content, lastModified.toMillis());
                    if (pageSource == null) {
                        String pp = websiteRoot.relativize(p).toString();
                        if ( !pp.startsWith("/") ) {
                            pp = "/" + pp;
                        }
                        StaticPage page = new StaticPage(pp, lastModified.toMillis(), content);
                        appPages.addPage(page);
                    } else {
                        String pp = websiteRoot.relativize(p).toString();
                        if ( !pp.startsWith("/") ) {
                            pp = "/" + pp;
                        }
                        String layoutPath = pageSource.getLayoutPath();
                        if (layoutPath.startsWith("/")) {
                            layoutPath = layoutPath.substring(1);
                        }
                        Layout layout = appPages.getLayout(layoutPath);
                        if ( layout == null ) {
                            Path layoutFile = websiteRoot.resolve(layoutPath);
                            if (Files.isRegularFile(layoutFile)) {
                                String layoutContent = Files.readString(layoutFile, StandardCharsets.UTF_8);
                                FileTime layoutLastModified = Files.getLastModifiedTime(layoutFile);
                                appPages.addLayout(layoutPath, layoutLastModified.toMillis(), layoutContent);
                                layout = appPages.getLayout(layoutPath);
                            } else {
                                throw new IllegalArgumentException("layout " + layoutPath + " is not found for page " + pp);
                            }
                        }
                        StaticPage page = new StaticPage(pp, pageSource, layout, templateEngine);
                        appPages.addPage(page);
                    }
                }
            }
        }
        return appPages;
    }

    private ErrorPages scanErrorPages(Path webappRoot) throws IOException {
        Path errorRoot = webappRoot.resolve("errors");
        if ( !Files.isDirectory(errorRoot) ) {
            throw new IllegalArgumentException("errors are not found");
        }
        ErrorPages errorPages = new ErrorPages();
        try (Stream<Path> all = Files.walk(errorRoot)) {
            Iterator<Path> it = all.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                if (Files.isRegularFile(p)) {
                    FileTime lastModified = Files.getLastModifiedTime(p);
                    String content = Files.readString(p, StandardCharsets.UTF_8);
                    String pp = errorRoot.relativize(p).toString();
                    if ( !pp.startsWith("/") ) {
                        pp = "/" + pp;
                    }
                    StaticPage page = new StaticPage(pp, lastModified.toMillis(), content);
                    String fileName = p.getFileName().toString();
                    if (fileName.endsWith(".html")) {
                        String statusCodeStr = fileName.substring(0, fileName.length() - 5);
                        try {
                            int statusCode = Integer.parseInt(statusCodeStr);
                            errorPages.addErrorPage(statusCode, page);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("invalid error page file name: " + fileName);
                        }
                    } else {
                        throw new IllegalArgumentException("invalid error page file name: " + fileName);
                    }
                }
            }
        }
        return errorPages;
    }

    private ThemeStore scanThemes(Path webappRoot) throws IOException {
        Path themesRoot = webappRoot.resolve("themes");
        if ( !Files.isDirectory(themesRoot) ) {
            throw new IllegalArgumentException("themes are not found");
        }
        List<Path> themeDirs = new ArrayList<Path>();
        try (Stream<Path> listing = Files.list(themesRoot)) {
            Iterator<Path> it = listing.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                if (Files.isDirectory(p)) {
                    themeDirs.add(p);
                }
            }
        }
        ThemeStore themeStore = new ThemeStore();
        // Map<String, DealerTheme> themesMap = new LinkedHashMap<>();
        for (Path themeDir : themeDirs) {
            String themeName = themeDir.getFileName().toString();
            DealerTheme themeBlocks = new DealerTheme(themeName);
            try (Stream<Path> all = Files.walk(themeDir)) {
                Iterator<Path> it = all.iterator();
                while (it.hasNext()) {
                    Path p = it.next();
                    if (Files.isRegularFile(p)) {
                        // if it is block, add as block
                        // if it is layout, add as layout
                        // themeFiles.add(toForwardSlashString(
                        //         webappRoot.getParent().relativize(p)));
                    }
                }
            }
            themeStore.addTheme(themeBlocks);
            // themesMap.put(themeName, themeBlocks);
        }
        return themeStore;
    }
}
