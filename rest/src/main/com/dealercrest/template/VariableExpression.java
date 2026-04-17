package com.dealercrest.template;

/**
 * A compiled expression that resolves a dot-path from the Context.
 *
 * Example: "vehicle.name" is split into ["vehicle", "name"] at compile time.
 * At render time we just walk the array — no string splitting, no indexOf.
 *
 * Property access at each step is handled by PropertyAccessor,
 * which caches the reflection lookup per (Class, propertyName).
 */
public class VariableExpression implements CompiledExpression {

    private final String[] path;

    /**
     * @param expr a raw variable path like "vehicle.name" or just "title"
     *             (without the ${ } wrapper — that is stripped by ExpressionCompiler)
     */
    public VariableExpression(String expr) {
        this.path = expr.split("\\.");
    }

    /**
     * Returns the path array — useful for IfNode / EachNode
     * that need the root variable name.
     */
    public String[] getPath() {
        return path;
    }

    /**
     * Returns the root variable name (first segment of the path).
     */
    public String getRootName() {
        return path[0];
    }

    @Override
    public Object evaluate(DataModel ctx) {
        Object obj = ctx.get(path[0]);

        for (int i = 1; i < path.length; i++) {
            obj = PropertyAccessor.get(obj, path[i]);
            if (obj == null) return null;
        }

        return obj;
    }
}
