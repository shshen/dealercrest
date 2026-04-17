package com.dealercrest.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an HTML element node in the AST.
 *
 * Example:
 *   <div class="box">Hello</div>
 *
 * Becomes:
 *   tag        = "div"
 *   attributes = {class="box"}
 *   children   = [TextNode("Hello")]
 *
 * CHANGES:
 *  1. compile() now iterates directives in PRIORITY ORDER via registry.inPriorityOrder(),
 *     so th:each is always processed before th:if when both appear on one element.
 *
 *  2. After a directive's apply() returns a new node, we call compile() on THAT
 *     node too, so any remaining directives on the inner element are not skipped.
 *
 *  3. Attribute values that contain ${} are pre-compiled via ExpressionCompiler
 *     and stored in compiledAttributes for zero-cost evaluation at render time.
 *
 *  4. Replaced "var e" (Java 10 local variable type inference) with explicit
 *     Map.Entry<String,String> for broader Java compatibility.
 *
 *  5. render() skips the closing tag for HTML void elements (br, hr, img, etc.)
 */
public class ElementNode extends Node {

    /**
     * HTML void elements that must not emit a closing tag.
     */
    private static final Set<String> VOID_ELEMENTS = new HashSet<String>(Arrays.asList(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    ));

    public String tag;

    public Map<String, String> attributes = new LinkedHashMap<String, String>();

    public List<Node> children = new ArrayList<Node>();

    /**
     * Pre-compiled attribute values.
     * Populated during compile() for any attribute whose value contains ${}.
     * At render time we evaluate from here instead of re-parsing the string.
     */
    private Map<String, CompiledExpression> compiledAttributes =
            new LinkedHashMap<String, CompiledExpression>();

    @Override
    public Node compile(DirectiveRegistry registry) {

        // Check directives in PRIORITY ORDER.
        // Use registry.inPriorityOrder() so th:each (priority 10) is matched
        // before th:if (priority 20) when both exist on the same element.
        for (Directive d : registry.inPriorityOrder()) {
            if (attributes.containsKey(d.name())) {
                // Re-compile the result so any remaining directives on the
                // returned node are also processed.
                return d.apply(this).compile(registry);
            }
        }

        // Pre-compile attribute values that contain expressions
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (value != null && value.contains("${")) {
                compiledAttributes.put(key, ExpressionCompiler.compile(value));
            }
        }

        // Recursively compile children
        for (int i = 0; i < children.size(); i++) {
            children.set(i, children.get(i).compile(registry));
        }

        return this;
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {
        out.append("<").append(tag);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("th:")) {
                continue;
            }

            out.append(" ").append(key).append("=\"");

            // Use pre-compiled expression if available, otherwise raw value
            CompiledExpression compiled = compiledAttributes.get(key);
            if (compiled != null) {
                Object val = compiled.evaluate(ctx);
                out.append(val == null ? "" : val.toString());
            } else {
                out.append(entry.getValue());
            }

            out.append("\"");
        }

        out.append(">");

        // Void elements have no children and no closing tag
        if (VOID_ELEMENTS.contains(tag.toLowerCase())) {
            return;
        }

        for (int i = 0; i < children.size(); i++) {
            children.get(i).render(ctx, out);
        }

        out.append("</").append(tag).append(">");
    }
}
