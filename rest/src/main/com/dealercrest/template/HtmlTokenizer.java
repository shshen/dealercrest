package com.dealercrest.template;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizes an HTML string into a flat list of Tokens.
 *
 * CHANGES:
 *  1. Skips HTML comments (<!-- ... -->) instead of emitting them as TEXT,
 *     which previously caused TextNode fragments with "-->" in the output.
 *
 *  2. readTagName() now accepts underscores and hyphens (for custom elements
 *     like <my-component> and XML-namespaced tags).
 *
 *  3. readAttrValue() now also handles attribute values with '>' characters
 *     inside quotes, which previously terminated tag reading prematurely.
 *
 *  4. Skips <!DOCTYPE ...> declarations gracefully.
 */
public class HtmlTokenizer {

    private final String input;
    private int i = 0;

    public HtmlTokenizer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {

        List<Token> tokens = new ArrayList<Token>();

        while (i < input.length()) {

            char c = input.charAt(i);

            if (c == '<') {

                // HTML comment: <!-- ... -->
                if (peek("<!--")) {
                    skipComment();
                    continue;
                }

                // DOCTYPE declaration
                if (peek("<!")) {
                    skipUntil('>');
                    continue;
                }

                // Closing tag: </tag>
                if (peek("</")) {
                    i += 2;
                    String tagName = readTagName();
                    tokens.add(new Token(TokenType.TAG_CLOSE, tagName));
                    consume('>');
                    continue;
                }

                // Opening tag: <tag ...>
                i++;
                String tagName = readTagName();
                tokens.add(new Token(TokenType.TAG_OPEN, tagName));

                readAttributes(tokens);

                if (peek("/>")) {
                    i += 2;
                    tokens.add(new Token(TokenType.SELF_CLOSE, ""));
                } else {
                    consume('>');
                }

            } else {
                String text = readText();
                if (text != null && !text.isEmpty()) {
                    tokens.add(new Token(TokenType.TEXT, text));
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private void readAttributes(List<Token> tokens) {

        while (i < input.length()) {

            skipWhitespace();

            if (i >= input.length()) return;

            if (input.charAt(i) == '>' || peek("/>")) return;

            String name = readAttrName();
            if (name.isEmpty()) {
                // Safety: skip one character to avoid infinite loop
                i++;
                continue;
            }

            tokens.add(new Token(TokenType.ATTRIBUTE_NAME, name));

            skipWhitespace();

            if (i < input.length() && input.charAt(i) == '=') {
                i++;
                skipWhitespace();
                String value = readAttrValue();
                tokens.add(new Token(TokenType.ATTRIBUTE_VALUE, value));
            }
        }
    }

    private String readTagName() {
        skipWhitespace();
        StringBuilder sb = new StringBuilder();

        while (i < input.length()) {
            char c = input.charAt(i);
            // Allow letters, digits, hyphens, underscores (custom elements, namespaced)
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                sb.append(c);
                i++;
            } else {
                break;
            }
        }

        return sb.toString();
    }

    private String readAttrName() {
        StringBuilder sb = new StringBuilder();

        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ':' || c == '-' || c == '_') {
                sb.append(c);
                i++;
            } else {
                break;
            }
        }

        return sb.toString();
    }

    private String readAttrValue() {

        if (i >= input.length()) return "";

        char quote = input.charAt(i);

        if (quote == '"' || quote == '\'') {
            i++;
            StringBuilder sb = new StringBuilder();

            // Correctly handles '>' inside quoted values (original would stop early)
            while (i < input.length() && input.charAt(i) != quote) {
                sb.append(input.charAt(i));
                i++;
            }

            if (i < input.length()) i++; // consume closing quote

            return sb.toString();
        }

        // Unquoted attribute value — read until whitespace or >
        StringBuilder sb = new StringBuilder();
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '>' || c == '/' || Character.isWhitespace(c)) break;
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private String readText() {
        StringBuilder sb = new StringBuilder();

        while (i < input.length() && input.charAt(i) != '<') {
            sb.append(input.charAt(i));
            i++;
        }

        // Trim leading/trailing whitespace but preserve internal spaces
        String text = sb.toString();
        String trimmed = text.trim();
        return trimmed;
    }

    private void skipWhitespace() {
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
    }

    private void skipComment() {
        // Skip past <!-- ... -->
        i += 4; // skip <!--
        while (i < input.length()) {
            if (peek("-->")) {
                i += 3;
                return;
            }
            i++;
        }
    }

    private void skipUntil(char target) {
        while (i < input.length() && input.charAt(i) != target) {
            i++;
        }
        if (i < input.length()) i++; // consume the target char
    }

    private boolean peek(String s) {
        return input.startsWith(s, i);
    }

    private void consume(char c) {
        if (i < input.length() && input.charAt(i) == c) {
            i++;
        }
    }
}
