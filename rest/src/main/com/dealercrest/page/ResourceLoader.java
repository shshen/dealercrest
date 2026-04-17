package com.dealercrest.page;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Scans webapp resources from two sources, in priority order:
 *
 * <ol>
 * <li><b>Local folder</b> – a {@code webapp/} directory relative to
 * {@code localBase}
 * (defaults to the current working directory).</li>
 * <li><b>ClassLoader</b> – the provided (or default) {@link ClassLoader} is
 * asked for a
 * resource named {@code "webapp-entries.txt"}, which must be a UTF-8 text file
 * listing
 * one webapp-relative path per line (e.g.
 * {@code webapp/themes/classic/style.css}).
 * This design removes any dependency on JAR files or the file system inside the
 * scanner,
 * making it fully testable with an in-memory {@code ClassLoader}.</li>
 * </ol>
 *
 * <p>
 * The scanner populates a {@link WebResources} object with:
 * <ul>
 * <li>{@code themes} – {@code Map<String, List<String>>} keyed by theme name,
 * each value
 * being the sorted list of resource paths under
 * {@code webapp/themes/<name>/}.</li>
 * <li>{@code websiteFiles} – sorted {@code List<String>} of every path under
 * {@code webapp/website/}.</li>
 * </ul>
 *
 * <h3>ClassLoader contract</h3>
 * The ClassLoader must be able to return an {@link InputStream} for the
 * resource name
 * {@code "webapp-entries.txt"}. Each line of that file is a forward-slash path
 * string
 * starting with {@code webapp/} (directory lines ending with {@code /} are
 * ignored).
 * In production, package this file inside your JAR alongside the webapp
 * resources.
 * In tests, serve it from an in-memory {@link ClassLoader} — no real JAR
 * required.
 */
public class ResourceLoader {

    // private static final String WEBAPP_PREFIX = "webapp/";
    // private static final String THEMES_PREFIX = "webapp/themes/";
    // private static final String WEBSITE_PREFIX = "webapp/website/";

    private final Path localBase;
    // private final ClassLoader classLoader;

    /**
     * Uses the current working directory as local-base and the thread context
     * class-loader (falling back to the system class-loader) for JAR resources.
     */
    public ResourceLoader() {
        this(Paths.get("").toAbsolutePath(), defaultClassLoader());
    }

    /**
     * Uses an explicit local-base directory; class-loader defaults to the thread
     * context class-loader (falling back to the system class-loader).
     *
     * @param localBase directory that <em>contains</em> the {@code webapp/} folder
     */
    public ResourceLoader(Path localBase) {
        this(localBase, defaultClassLoader());
    }

    /**
     * Full constructor: explicit local-base <em>and</em> explicit class-loader.
     *
     * @param localBase   directory that contains the {@code webapp/} folder
     * @param classLoader class-loader used to locate {@value #ENTRIES_RESOURCE}
     */
    public ResourceLoader(Path localBase, ClassLoader classLoader) {
        this.localBase = Objects.requireNonNull(localBase, "localBase must not be null");
        // this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
    }

    /**
     * Scans and returns webapp resources.
     *
     * @throws IOException if an I/O error occurs
     */
    public WebResources scan() throws IOException {
        Path localWebApp = localBase.resolve("webapp");
        if (Files.isDirectory(localWebApp)) {
            return scanLocalDirectory(localWebApp);
        }
        return scanClassLoader();
    }

    private WebResources scanLocalDirectory(Path webappRoot) throws IOException {
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
        Map<String, ThemeTemplates> themesMap = new LinkedHashMap<>();
        for (Path themeDir : themeDirs) {
            String themeName = themeDir.getFileName().toString();
            ThemeTemplates themeBlocks = new ThemeTemplates(themeName);
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

        // --- website ---
        Path websiteRoot = webappRoot.resolve("website");
        if (Files.isDirectory(websiteRoot)) {
            throw new IllegalArgumentException("website is not found");
        }
        SitePages appPages = new SitePages("dealersystem.com");
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

        return new WebResources(appPages, themesMap);
    }

    /**
     * Asks the class-loader for {@value #ENTRIES_RESOURCE} and parses each line
     * as a webapp resource path. If the resource is absent, empty collections
     * are returned.
     */
    private WebResources scanClassLoader() throws IOException {
        throw new UnsupportedOperationException("ClassLoader scanning not implemented yet");
    }

    /**
     * Returns the thread context class-loader, falling back to the system
     * class-loader.
     */
    private static ClassLoader defaultClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }
}
