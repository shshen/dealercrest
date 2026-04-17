package com.dealercrest.template;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of named HTML fragment strings.
 *
 * Used by th:replace to swap an element's entire output with a
 * pre-registered fragment template.
 *
 * Fragments are registered as raw HTML strings and compiled on first use.
 * The compiled AST is cached just like top-level templates in TemplateEngine.
 *
 * Registration (at startup):
 *
 *   FragmentRegistry frags = new FragmentRegistry();
 *   frags.register("nav", "<nav><a href='/'>Home</a></nav>");
 *   frags.register("footer", "<footer>Copyright 2025</footer>");
 *
 * Usage in template:
 *
 *   <div th:replace="nav"></div>
 *   <!-- the entire <div> is replaced by the "nav" fragment -->
 */
public class FragmentRegistry {

    // Raw HTML strings keyed by fragment name
    private final ConcurrentHashMap<String, String> fragments =
            new ConcurrentHashMap<String, String>();

    // Compiled AST cache — compiled once on first use
    private final ConcurrentHashMap<String, Node> compiled =
            new ConcurrentHashMap<String, Node>();

    /**
     * Register a named fragment.
     *
     * @param name     the name used in th:replace="name"
     * @param html     the raw HTML string for the fragment
     */
    public void register(String name, String html) {
        fragments.put(name, html);
        compiled.remove(name); // evict stale compiled version if re-registering
    }

    /**
     * Retrieve a compiled fragment Node, compiling it on first access.
     *
     * @param name     fragment name
     * @param registry directive registry used for compilation
     * @return compiled root ElementNode, or null if the fragment is unknown
     */
    public Node get(String name, DirectiveRegistry registry) {
        Node node = compiled.get(name);
        if (node != null) return node;

        String html = fragments.get(name);
        if (html == null) return null;

        // Parse + compile, then cache
        HtmlParser parser = new HtmlParser();
        Node parsed = parser.parse(html);       // returns synthetic <root>
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
}
