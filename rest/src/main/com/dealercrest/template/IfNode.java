package com.dealercrest.template;

/**
 * Conditionally renders a node based on a boolean expression.
 *
 * CHANGES:
 *  1. Uses a pre-compiled VariableExpression instead of re-parsing the
 *     ${...} string on every render call.
 *
 *  2. Handles negation: th:if="${!active}" works correctly.
 *
 *  3. Truthiness rules (in order):
 *       - Boolean true/false         → direct
 *       - Negated (!key)             → inverted
 *       - Non-null, non-Boolean value → treated as true
 *       - null                        → false
 */
public class IfNode extends Node {

    private final VariableExpression expr;
    private final boolean negate;
    private Node body;

    public IfNode(String raw, Node body) {
        // Strip ${ }
        String stripped = raw.replace("${", "").replace("}", "").trim();

        if (stripped.startsWith("!")) {
            this.negate = true;
            this.expr   = new VariableExpression(stripped.substring(1).trim());
        } else {
            this.negate = false;
            this.expr   = new VariableExpression(stripped);
        }

        this.body = body;
    }

    @Override
    public Node compile(DirectiveRegistry registry) {
        body = body.compile(registry);
        return this;
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {
        Object val = expr.evaluate(ctx);

        boolean result;

        if (val == null) {
            result = false;
        } else if (val instanceof Boolean) {
            result = (Boolean) val;
        } else {
            // Any non-null, non-Boolean value is truthy
            result = true;
        }

        if (negate) {
            result = !result;
        }

        if (result) {
            body.render(ctx, out);
        }
    }
}
