package com.dealercrest.template;

/**
 * Handles th:replace="fragmentName".
 *
 * Replaces the entire element (tag + children) with the named fragment.
 * Priority 5 — processed before th:each (10) and th:if (20).
 */
public class ReplaceDirective implements Directive {

    private final FragmentRegistry fragmentRegistry;

    public ReplaceDirective(FragmentRegistry fragmentRegistry) {
        this.fragmentRegistry = fragmentRegistry;
    }

    public String name() {
        return "th:replace";
    }

    public int priority() {
        return 5;
    }

    public Node apply(ElementNode node) {
        String fragmentName = node.attributes.remove("th:replace").trim();
        return new ReplaceNode(fragmentName, fragmentRegistry);
    }
}
