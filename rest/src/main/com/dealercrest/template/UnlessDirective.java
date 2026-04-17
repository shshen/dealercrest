package com.dealercrest.template;

public class UnlessDirective implements Directive {

    public String name() {
        return "th:unless";
    }

    public int priority() {
        return 100;
    }

    public Node apply(ElementNode node) {
        throw new UnsupportedOperationException("th:unless is not implemented yet");
        // String expr = node.attributes.remove("th:unless");
        // return new IfNode("!" + expr, node.children);
    }
}
