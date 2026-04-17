package com.dealercrest.template;

/**
 * Represents raw text or expression text inside a template.
 *
 * CHANGE: Expression is compiled ONCE at construction time via ExpressionCompiler.
 *
 * Original called Expression.eval() on every render(), re-scanning the string
 * for ${ } markers each time. Now render() just calls compiled.evaluate(ctx),
 * which is a direct field read (LiteralExpression) or a context map lookup
 * (VariableExpression) — no scanning.
 */
public class TextNode extends Node {

    private final CompiledExpression compiled;

    public TextNode(String text) {
        this.compiled = ExpressionCompiler.compile(text);
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {
        Object val = compiled.evaluate(ctx);
        if (val != null) {
            out.append(val.toString());
        }
    }
}
