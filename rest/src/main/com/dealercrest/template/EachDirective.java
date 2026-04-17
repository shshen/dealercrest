package com.dealercrest.template;

public class EachDirective implements Directive {

    public String name() {
        return "th:each";
    }

    /**
     * Priority 10: processed first so the loop wraps any inner conditionals.
     */
    public int priority() {
        return 10;
    }

    @Override
    public Node apply(ElementNode node) {
        String expr = node.attributes.remove("th:each");
        String[] parts = expr.split(":");

        String var      = parts[0].trim();
        String listName = parts[1].trim();

        return new EachNode(var, listName, node);
    }
}
