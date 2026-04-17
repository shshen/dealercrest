package com.dealercrest.template;

/**
 * Directive defines a transformation rule for th:* attributes.
 *
 * CHANGE: Added priority() so DirectiveRegistry can dispatch directives
 * in a defined order when multiple th:* attributes exist on the same element.
 *
 * Convention (lower number = higher priority):
 *   th:each  → 10   (loop wraps everything, processed first)
 *   th:if    → 20   (condition is inner to loop)
 *   th:text  → 30   (text replacement is innermost)
 *
 * Example:
 *   th:each → converts ElementNode into EachNode
 */
public interface Directive {

    /**
     * Attribute name this directive handles.
     * Example: "th:each", "th:if"
     */
    String name();

    /**
     * Priority for ordered dispatch.
     * Lower number = processed first.
     * Default is 100 (low priority) so existing directives without
     * an explicit priority still work correctly.
     */
    int priority();

    /**
     * Transforms an AST node into a new executable node.
     */
    Node apply(ElementNode node);
}
