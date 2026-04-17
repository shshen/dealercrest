package com.dealercrest.template;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Parses a flat token list into an AST of Nodes.
 *
 * CHANGES:
 *  1. Knows about HTML void elements (br, hr, img, input, etc.) and never
 *     pushes them onto the stack, avoiding spurious pop mismatches.
 *
 *  2. On TAG_CLOSE, verifies the tag name matches the top of the stack
 *     before popping (original popped blindly regardless of tag name).
 *
 *  3. If a TAG_CLOSE has no matching open tag on the stack, it is silently
 *     ignored rather than corrupting the tree.
 */
public class HtmlParser {

    /**
     * HTML void elements that cannot have children and have no closing tag.
     */
    private static final Set<String> VOID_ELEMENTS = new HashSet<String>(Arrays.asList(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    ));

    private List<Token> tokens;
    private int pos;

    public Node parse(String html) {

        HtmlTokenizer tokenizer = new HtmlTokenizer(html);
        tokens = tokenizer.tokenize();
        pos    = 0;

        ElementNode root = new ElementNode();
        root.tag = "root";

        Stack<ElementNode> stack = new Stack<ElementNode>();
        stack.push(root);

        while (!isEOF()) {

            Token t = peek();

            if (t.type == TokenType.TAG_OPEN) {

                consume();

                ElementNode node = new ElementNode();
                node.tag = t.value;

                // Read attributes
                while (peek().type == TokenType.ATTRIBUTE_NAME) {
                    String name  = consume().value;
                    String value = "";

                    if (peek().type == TokenType.ATTRIBUTE_VALUE) {
                        value = consume().value;
                    }

                    node.attributes.put(name, value);
                }

                stack.peek().children.add(node);

                boolean isSelfClose = peek().type == TokenType.SELF_CLOSE;
                if (isSelfClose) consume();

                // Do not push void elements or self-closing tags
                if (!isSelfClose && !VOID_ELEMENTS.contains(node.tag.toLowerCase())) {
                    stack.push(node);
                }

            } else if (t.type == TokenType.TAG_CLOSE) {

                Token closeToken = consume();

                // Find the nearest matching open tag on the stack and pop up to it.
                // This handles slightly malformed HTML gracefully.
                if (!stack.isEmpty() && stack.peek().tag.equals(closeToken.value)) {
                    // Fast path: top of stack matches
                    stack.pop();
                } else {
                    // Search deeper — pop intermediate mismatched elements
                    for (int depth = stack.size() - 1; depth >= 1; depth--) {
                        if (stack.get(depth).tag.equals(closeToken.value)) {
                            // Pop everything down to and including the match
                            while (stack.size() > depth) {
                                stack.pop();
                            }
                            break;
                        }
                    }
                    // If not found at all, ignore the orphan close tag
                }

            } else if (t.type == TokenType.TEXT) {

                String text = consume().value;

                if (text != null && !text.trim().isEmpty()) {
                    stack.peek().children.add(new TextNode(text));
                }

            } else {
                consume();
            }
        }

        return root;
    }

    private boolean isEOF() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token consume() {
        return tokens.get(pos++);
    }
}
