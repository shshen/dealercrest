package com.dealercrest.template;

/**
 * Handles th:replace="fragmentName".
 *
 * Replaces the entire element (tag + children) with the named fragment.
 * Priority 5 — processed before th:each (10) and th:if (20).
 *
 * CHANGE: No longer takes a FragmentRegistry in its constructor.
 * The registry is now per-page — it lives in the Model and is resolved
 * by ReplaceNode at render time.  This allows different pages to use
 * completely different fragment sets through the same TemplateEngine.
 *
 * Setup — one directive instance shared by all pages:
 *
 *   DirectiveRegistry directives = new DirectiveRegistry();
 *   directives.register(new ReplaceDirective());   // no registry argument
 *   TemplateEngine engine = new TemplateEngine(directives);
 *
 * Per-page usage:
 *
 *   FragmentRegistry pageFragments = new FragmentRegistry();
 *   pageFragments.register("nav", "<nav>...</nav>");
 *
 *   Model model = new Model();
 *   model.set("vehicles", list);
 *   model.setFragments(pageFragments);          // attach per-page fragments
 *
 *   engine.renderFile("templates/vehicles.html", model);
 */
public class ReplaceDirective implements Directive {

    public String name() {
        return "th:replace";
    }

    public int priority() {
        return 5;
    }

    public Node apply(ElementNode node) {
        String fragmentName = node.attributes.remove("th:replace").trim();
        return new ReplaceNode(fragmentName);
    }
}