package com.dealercrest.template;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiles a raw expression string into a CompiledExpression — once.
 *
 * The compiled form is then evaluated many times during rendering
 * with zero re-parsing cost.
 *
 * Rules:
 *   - No ${} present         → LiteralExpression  (constant, free at render)
 *   - Single ${} only        → VariableExpression (one context lookup)
 *   - Mixed text + ${} parts → CompositeExpression (pre-split list)
 *
 * The ExpressionCompiler itself is stateless — all methods are static.
 */
public class ExpressionCompiler {

    private ExpressionCompiler() {}

    /**
     * Compile a raw string (may contain zero or more ${...} markers)
     * into a reusable CompiledExpression.
     */
    public static CompiledExpression compile(String raw) {

        if (raw == null) {
            return new LiteralExpression("");
        }

        // Fast path: no expression markers at all
        if (!raw.contains("${")) {
            return new LiteralExpression(raw);
        }

        List<CompiledExpression> parts = new ArrayList<CompiledExpression>();
        int i = 0;

        while (i < raw.length()) {

            int start = raw.indexOf("${", i);

            if (start == -1) {
                // Remaining tail is plain text
                parts.add(new LiteralExpression(raw.substring(i)));
                break;
            }

            // Text before the ${
            if (start > i) {
                parts.add(new LiteralExpression(raw.substring(i, start)));
            }

            int end = raw.indexOf("}", start);
            if (end == -1) {
                // Unclosed expression — treat remainder as literal
                parts.add(new LiteralExpression(raw.substring(start)));
                break;
            }

            String expr = raw.substring(start + 2, end).trim();
            parts.add(new VariableExpression(expr));

            i = end + 1;
        }

        // Optimise: if we only produced one part, unwrap it
        if (parts.size() == 1) {
            return parts.get(0);
        }

        return new CompositeExpression(parts);
    }

    /**
     * Compile an expression that is purely a variable reference,
     * i.e. the raw string is "${varName}" or "varName" (no surrounding text).
     * Used by IfNode and EachNode where we need the variable path directly.
     */
    public static VariableExpression compileVariable(String raw) {
        String stripped = raw.replace("${", "").replace("}", "").trim();
        return new VariableExpression(stripped);
    }
}
