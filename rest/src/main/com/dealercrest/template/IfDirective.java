package com.dealercrest.template;

public class IfDirective implements Directive {

    public String name() {
        return "th:if";
    }

    /**
     * Priority 20: processed after th:each (10) so that a loop can wrap
     * a conditional element correctly.
     */
    public int priority() {
        return 20;
    }

    @Override
    public Node apply(ElementNode node) {
        String raw = node.attributes.remove("th:if");
        return new IfNode(raw, node);
    }
}
