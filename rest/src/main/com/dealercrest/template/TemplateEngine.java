package com.dealercrest.template;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TemplateEngine is the central entry point of the template system.
 *
 * Architecture:
 *   Template String -> AST -> Directive Processing -> Render Output
 *
 * Features:
 *   - Compiled AST cache (parse+compile once per unique template string)
 *   - ThreadLocal StringBuilder (no buffer allocation per render)
 *   - Optional FragmentRegistry for th:replace support
 *
 * Minimal setup (no fragments):
 *
 *   DirectiveRegistry directives = new DirectiveRegistry();
 *   directives.register(new EachDirective());
 *   directives.register(new IfDirective());
 *   directives.register(new ClassAppendDirective());
 *   TemplateEngine engine = new TemplateEngine(directives);
 *
 * Full setup with fragments:
 *
 *   FragmentRegistry fragments = new FragmentRegistry();
 *   fragments.register("nav", "<nav><a href='/'>Home</a></nav>");
 *
 *   DirectiveRegistry directives = new DirectiveRegistry();
 *   directives.register(new EachDirective());
 *   directives.register(new IfDirective());
 *   directives.register(new ClassAppendDirective());
 *   directives.register(new ReplaceDirective(fragments));
 *
 *   TemplateEngine engine = new TemplateEngine(directives);
 */
public class TemplateEngine {

    private final HtmlParser parser;
    private final DirectiveRegistry registry;

    /** Compiled AST cache — parse+compile runs at most once per template string. */
    private final ConcurrentHashMap<String, Node> cache =
            new ConcurrentHashMap<String, Node>();

    /**
     * Reusable StringBuilder per thread.
     * Cleared (not replaced) between renders — the internal char[] grows
     * to fit the largest template on each thread and stays there.
     */
    private static final ThreadLocal<StringBuilder> BUFFER =
            new ThreadLocal<StringBuilder>() {
                protected StringBuilder initialValue() {
                    return new StringBuilder(4096);
                }
            };

    /** Constructor — no fragment support. */
    public TemplateEngine(DirectiveRegistry registry) {
        this.registry = registry;
        this.parser   = new HtmlParser();
    }

    /**
     * Render a template string with the given context.
     *
     * @param template raw HTML template string containing th:* attributes
     * @param ctx      runtime data context (variables, objects, lists)
     * @return final rendered HTML output
     */
    public String render(String templateKey, String templateContent, DataModel ctx) {

        Node compiledAst = cache.get(templateKey);
        if (compiledAst == null) {
            compiledAst = parseAndCompile(templateContent);
            Node existing = cache.putIfAbsent(templateKey, compiledAst);
            if (existing != null) {
                compiledAst = existing;
            }
        }

        StringBuilder out = BUFFER.get();
        out.setLength(0);

        // HtmlParser wraps everything in a synthetic <root> node — render children only
        ElementNode root = (ElementNode) compiledAst;
        for (int i = 0; i < root.children.size(); i++) {
            root.children.get(i).render(ctx, out);
        }

        return out.toString();
    }

    private Node parseAndCompile(String template) {
        Node ast = parser.parse(template);
        return ast.compile(registry);
    }

    /** Evict a single template from the cache (e.g. after hot-reload). */
    public void evict(String template) {
        cache.remove(template);
    }

    /** Clear the entire template cache. */
    public void clearCache() {
        cache.clear();
    }
}
