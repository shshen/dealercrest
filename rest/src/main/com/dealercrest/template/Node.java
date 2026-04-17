package com.dealercrest.template;

/**
 * Base AST node.
 *
 * Every template element (text, tag, loop, condition)
 * is represented as a Node.
 *
 * Two-phase lifecycle:
 *
 *   compile(registry) — called ONCE at startup/first-use.
 *                        Transforms raw parse nodes into executable nodes.
 *                        Directives are applied here.
 *                        Expressions are pre-compiled here.
 *                        The compiled tree is cached and reused.
 *
 *   render(ctx, out)  — called on EVERY render request.
 *                        Should do the minimum possible work:
 *                        context lookups, string appends, no parsing.
 */
public abstract class Node {

    /**
     * Render phase: produces final HTML output into the provided StringBuilder.
     * Must not modify the node itself (compiled AST is shared across renders).
     */
    public abstract void render(DataModel ctx, StringBuilder out);

    /**
     * Compile phase: transforms raw AST into executable AST.
     * Default implementation is a no-op (returns self).
     * Subclasses override to apply transformations.
     */
    public Node compile(DirectiveRegistry registry) {
        return this;
    }
}
