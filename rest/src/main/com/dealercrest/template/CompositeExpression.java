package com.dealercrest.template;

import java.util.List;

/**
 * A compiled expression made of multiple segments — literals and variables
 * interleaved.
 *
 * Example: "Hello ${user.name}, you have ${count} messages"
 * compiles to:
 *   [ LiteralExpression("Hello "),
 *     VariableExpression("user.name"),
 *     LiteralExpression(", you have "),
 *     VariableExpression("count"),
 *     LiteralExpression(" messages") ]
 *
 * At render time we just iterate the pre-built list and append to a
 * StringBuilder — no scanning, no splitting.
 */
public class CompositeExpression implements CompiledExpression {

    private final List<CompiledExpression> parts;

    public CompositeExpression(List<CompiledExpression> parts) {
        this.parts = parts;
    }

    @Override
    public Object evaluate(DataModel ctx) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.size(); i++) {
            Object val = parts.get(i).evaluate(ctx);
            if (val != null) {
                sb.append(val.toString());
            }
        }

        return sb.toString();
    }
}
