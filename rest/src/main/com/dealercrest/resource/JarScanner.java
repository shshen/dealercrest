package com.dealercrest.resource;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.dealercrest.template.TemplateEngine;

public class JarScanner {

    public WebResources scan(JarFile jar, String domain, TemplateEngine templateEngine) {
        
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith("website/")) {
                // process webapp entry
            } else if (entryName.startsWith("themes/")) {
                // process themes entry
            } else if (entryName.startsWith("errors/")) {
                // process error-pages entry
            }
        }

        throw new UnsupportedOperationException("Unimplemented method 'scan'");
    }
    
}
