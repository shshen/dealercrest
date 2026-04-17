package com.dealercrest.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeFiles;

public class LocalScanner {

    public WebResource scan(Path webappRoot, String domain) throws IOException {
        Map<String, ThemeFiles> themesMap = scanThemes(webappRoot);
        SitePages appPages = scanWebsite(webappRoot, domain);
        return new WebResource(appPages, themesMap);
    }

    private SitePages scanWebsite(Path webappRoot, String domain) throws IOException {
        Path websiteRoot = webappRoot.resolve("website");
        if (Files.isDirectory(websiteRoot)) {
            throw new IllegalArgumentException("website is not found");
        }
        SitePages appPages = new SitePages(domain);
        try (Stream<Path> all = Files.walk(websiteRoot)) {
            Iterator<Path> it = all.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                if (Files.isRegularFile(p)) {
                    // website.add(toForwardSlashString(
                    //         webappRoot.getParent().relativize(p)));
                }
            }
        }
        return appPages;
    }

    private Map<String, ThemeFiles> scanThemes(Path webappRoot) throws IOException {
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
        Map<String, ThemeFiles> themesMap = new LinkedHashMap<>();
        for (Path themeDir : themeDirs) {
            String themeName = themeDir.getFileName().toString();
            ThemeFiles themeBlocks = new ThemeFiles(themeName);
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
            themesMap.put(themeName, themeBlocks);
        }
        return themesMap;
    }
}
