package com.dealercrest.template;

/**
 * A pre-compiled expression that can be evaluated against a Context.
 *
 * Expressions are compiled ONCE at parse/compile time and evaluated
 * many times at render time — eliminating repeated string scanning.
 *
 * Three concrete implementations:
 *   LiteralExpression  — constant text, no lookup needed
 *   VariableExpression — resolves a dot-path from Context
 *   CompositeExpression — concatenates a mix of literals and variables
 */
public interface CompiledExpression {

    /**
     * Evaluate this expression against the given rendering context.
     * Returns a String (for text output) or any Object (for logic checks).
     */
    Object evaluate(DataModel ctx);
}
