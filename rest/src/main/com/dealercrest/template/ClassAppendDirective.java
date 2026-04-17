package com.dealercrest.template;

/**
 * Handles th:classappend="${expression}".
 *
 * Appends a dynamically resolved CSS class to the element's existing
 * "class" attribute value.  Unlike th:if or th:each, this directive does
 * NOT replace the element — it modifies it in-place and returns the
 * same ElementNode (after removing the th:classappend attribute).
 *
 * Priority 30: runs after th:each (10) and th:if (20), on the inner element.
 *
 * Examples:
 *
 *   <div class="card" th:classappend="${active ? 'active' : ''}">
 *   <!-- resolves active=true  -->  <div class="card active">
 *   <!-- resolves active=false -->  <div class="card ">       (trailing space harmless)
 *
 *   <tr th:classappend="${row.highlight}">
 *   <!-- highlight="danger" -->  <tr class="danger">
 *   <!-- highlight=null     -->  <tr>
 *
 * DESIGN NOTE:
 *   Because th:classappend modifies the element rather than wrapping it,
 *   it stores a CompiledExpression on a special ClassAppendNode wrapper
 *   so the append logic runs at render time (when the context is available).
 *   The original ElementNode is kept as the inner body.
 */
public class ClassAppendDirective implements Directive {

    public String name() {
        return "th:classappend";
    }

    public int priority() {
        return 30;
    }

    public Node apply(ElementNode node) {
        String raw = node.attributes.remove("th:classappend");
        CompiledExpression appendExpr = ExpressionCompiler.compile(raw);
        return new ClassAppendNode(node, appendExpr);
    }
}
