package com.dealercrest.template;

/**
 * Wraps an ElementNode to append a dynamic CSS class at render time.
 *
 * Rendering strategy:
 *   1. Evaluate the appendExpr to get the class string.
 *   2. If the result is non-null and non-empty, temporarily merge it
 *      into the element's "class" attribute before rendering.
 *   3. Restore the original "class" value after rendering so the
 *      compiled AST stays clean for the next render call.
 *
 * Why not mutate the attribute map permanently?
 *   The compiled AST is cached and shared across all renders.
 *   The appended class may depend on context (e.g. ${item.active}),
 *   so it must be computed fresh on every render.  We must not bake
 *   a context-dependent value into the shared tree.
 */
public class ClassAppendNode extends Node {

    private Node inner;
    private final CompiledExpression appendExpr;

    public ClassAppendNode(ElementNode inner, CompiledExpression appendExpr) {
        this.inner      = inner;
        this.appendExpr = appendExpr;
    }

    @Override
    public Node compile(DirectiveRegistry registry) {
        inner = inner.compile(registry);
        return this;
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {

        Object appended = appendExpr.evaluate(ctx);
        String appendStr = (appended == null) ? null : appended.toString().trim();

        if (appendStr == null || appendStr.isEmpty()) {
            // Nothing to append — render inner element as-is
            inner.render(ctx, out);
            return;
        }

        // inner is always an ElementNode here (ClassAppendDirective guarantees it)
        if (!(inner instanceof ElementNode)) {
            inner.render(ctx, out);
            return;
        }

        ElementNode el = (ElementNode) inner;

        // Save the current class value (may be null / absent)
        String originalClass = el.attributes.get("class");

        // Temporarily set merged class value
        String merged;
        if (originalClass == null || originalClass.trim().isEmpty()) {
            merged = appendStr;
        } else {
            merged = originalClass + " " + appendStr;
        }
        el.attributes.put("class", merged);

        // Render with the merged class
        el.render(ctx, out);

        // Restore original state so the cached AST is not polluted
        if (originalClass == null) {
            el.attributes.remove("class");
        } else {
            el.attributes.put("class", originalClass);
        }
    }
}
