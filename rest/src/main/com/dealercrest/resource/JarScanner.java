package com.dealercrest.resource;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.dealercrest.page.HtmlPageSource;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeStore;
import com.dealercrest.template.TemplateEngine;

public class JarScanner {

    public WebResources scan(JarFile jarFile, String domain, TemplateEngine templateEngine) {
        SitePages websitePages = new SitePages(domain);
        ErrorPages errorPages = new ErrorPages();
        ThemeStore themeStore = new ThemeStore();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith("website/")) {
                addWebSite(websitePages, jarFile, entry, templateEngine);
            } else if (entryName.startsWith("themes/")) {
                addTheme(themeStore, jarFile, entry, templateEngine);
            } else if (entryName.startsWith("errors/")) {
                addErrorPage(errorPages, jarFile, entry);
            }
        }
        return new WebResources(websitePages, errorPages, themeStore);
    }

    private void addWebSite(SitePages website, JarFile jarFile, JarEntry entry, TemplateEngine templateEngine) {
        if (entry.isDirectory()) {
            return;
        }
        String entryName = entry.getName();
        String pagePath = entryName.substring("website/".length());
        // read content from jar entry and add to website
        String content = ""; // read content from jar entry
        long lastModified = entry.getTime();
        HtmlPageSource pageSource = HtmlPageSource.parse(content, lastModified);
        
    }

    private void addErrorPage(ErrorPages errorPages, JarFile jarFile, JarEntry entry) {
        // read content from jar entry and add to errorPages
    }

    private void addTheme(ThemeStore themeStore, JarFile jarFile, JarEntry entry, TemplateEngine templateEngine) {
        // read content from jar entry and add to themeStore
    }
    
}
