package com.dealercrest.template;

/**
 * A compiled expression that always returns the same constant string.
 *
 * Used for plain text segments like "Hello, " in "Hello, ${name}!".
 * evaluate() is a simple field read — zero overhead.
 */
public class LiteralExpression implements CompiledExpression {

    private final String value;

    public LiteralExpression(String value) {
        this.value = value;
    }

    @Override
    public Object evaluate(DataModel ctx) {
        return value;
    }
}
