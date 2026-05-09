package com.dealercrest.template;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-page registry of named HTML fragment strings for th:replace.
 *
 * A FragmentRegistry is created once per page (or per page type) and
 * attached to a Model before rendering.  The shared TemplateEngine and
 * DirectiveRegistry remain unchanged — only the Model carries the
 * per-page fragment set.
 *
 * Fragments are stored as raw HTML strings and compiled into an AST on
 * first use.  The compiled AST is cached inside this registry instance,
 * so repeated renders of the same page type only compile each fragment once.
 *
 * Usage pattern:
 *
 *   // Startup — one engine, one directive registry, shared forever
 *   DirectiveRegistry directives = new DirectiveRegistry();
 *   directives.register(new ReplaceDirective());   // no registry arg
 *   TemplateEngine engine = new TemplateEngine(directives);
 *
 *   // Per request — Page A (vehicle list)
 *   FragmentRegistry pageAFrags = new FragmentRegistry();
 *   pageAFrags.register("header", "<header>Vehicles</header>");
 *   pageAFrags.register("nav",    "<nav><a href='/'>Home</a></nav>");
 *
 *   Model modelA = new Model();
 *   modelA.set("vehicles", vehicleList);
 *   modelA.setFragments(pageAFrags);
 *   String htmlA = engine.renderFile("templates/vehicles.html", modelA);
 *
 *   // Per request — Page B (user profile), completely different fragments
 *   FragmentRegistry pageBFrags = new FragmentRegistry();
 *   pageBFrags.register("header", "<header>Profile</header>");
 *   pageBFrags.register("nav",    "<nav><a href='/profile'>Me</a></nav>");
 *
 *   Model modelB = new Model();
 *   modelB.set("user", currentUser);
 *   modelB.setFragments(pageBFrags);
 *   String htmlB = engine.renderFile("templates/profile.html", modelB);
 *
 * Typical lifecycle:
 *   - Create one FragmentRegistry per page type at application startup,
 *     or create a fresh one per request if fragments are dynamic.
 *   - Attach it to the Model immediately before calling renderFile().
 *   - The engine is stateless with respect to fragments — it reads only
 *     from model.getFragments() at render time.
 */
public class FragmentRegistry {

    // Raw HTML strings keyed by fragment name
    private final ConcurrentHashMap<String, String> fragments =
            new ConcurrentHashMap<String, String>();

    // Compiled AST cache — compiled once per (fragment name, this registry)
    private final ConcurrentHashMap<String, Node> compiled =
            new ConcurrentHashMap<String, Node>();

    /**
     * Register a named fragment.
     *
     * @param name  the name used in th:replace="name"
     * @param html  raw HTML string for the fragment body
     */
    public void register(String name, String html) {
        fragments.put(name, html);
        compiled.remove(name); // evict stale compiled AST on re-registration
    }

    public void registerAll(Map<String, String> frags) {
        for (Map.Entry<String, String> e : frags.entrySet()) {
            register(e.getKey(), e.getValue());
        }
    }

    /**
     * Retrieve a compiled fragment Node, compiling it on first access.
     *
     * Called by ReplaceNode at render time.
     *
     * @param name      fragment name
     * @param registry  directive registry used to compile the fragment AST
     * @return compiled root ElementNode, or null if the name is not registered
     */
    public Node get(String name, DirectiveRegistry registry) {
        Node node = compiled.get(name);
        if (node != null) return node;

        String html = fragments.get(name);
        if (html == null) return null;

        HtmlParser parser = new HtmlParser();
        Node parsed      = parser.parse(html);
        Node compiledNode = parsed.compile(registry);

        Node existing = compiled.putIfAbsent(name, compiledNode);
        return existing != null ? existing : compiledNode;
    }

    /**
     * Returns true if a fragment with this name has been registered.
     */
    public boolean contains(String name) {
        return fragments.containsKey(name);
    }

    /**
     * Evict a single compiled fragment, forcing recompilation on next use.
     * Call this after calling register() on an already-registered name
     * if you want immediate effect without creating a new registry instance.
     */
    public void evict(String name) {
        compiled.remove(name);
    }
}