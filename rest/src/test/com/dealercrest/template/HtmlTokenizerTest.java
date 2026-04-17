package com.dealercrest.template;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for HtmlTokenizer — verifies token stream for various HTML inputs.
 */
public class HtmlTokenizerTest {

    private List<Token> tokenize(String html) {
        return new HtmlTokenizer(html).tokenize();
    }

    private Token find(List<Token> tokens, TokenType type) {
        for (Token t : tokens) {
            if (t.type == type) return t;
        }
        return null;
    }

    private int countType(List<Token> tokens, TokenType type) {
        int count = 0;
        for (Token t : tokens) {
            if (t.type == type) count++;
        }
        return count;
    }

    // ------------------------------------------------------------------ basic structure

    @Test
    public void testSimpleOpenTag() {
        List<Token> tokens = tokenize("<div>");
        Token open = find(tokens, TokenType.TAG_OPEN);
        assertEquals("div", open.value);
    }

    @Test
    public void testSimpleCloseTag() {
        List<Token> tokens = tokenize("</div>");
        Token close = find(tokens, TokenType.TAG_CLOSE);
        assertEquals("div", close.value);
    }

    @Test
    public void testSelfClosingTag() {
        List<Token> tokens = tokenize("<br/>");
        assertHasType(tokens, TokenType.SELF_CLOSE);
    }

    @Test
    public void testTextContent() {
        List<Token> tokens = tokenize("<p>Hello</p>");
        Token text = find(tokens, TokenType.TEXT);
        assertEquals("Hello", text.value);
    }

    @Test
    public void testAlwaysEndsWithEof() {
        List<Token> tokens = tokenize("<p>x</p>");
        Token last = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.EOF, last.type);
    }

    // ------------------------------------------------------------------ attributes

    @Test
    public void testAttributeName() {
        List<Token> tokens = tokenize("<div class=\"box\">");
        Token attrName = find(tokens, TokenType.ATTRIBUTE_NAME);
        assertEquals("class", attrName.value);
    }

    @Test
    public void testAttributeValue() {
        List<Token> tokens = tokenize("<div class=\"box\">");
        Token attrVal = find(tokens, TokenType.ATTRIBUTE_VALUE);
        assertEquals("box", attrVal.value);
    }

    @Test
    public void testMultipleAttributes() {
        List<Token> tokens = tokenize("<input type=\"text\" name=\"q\">");
        assertEquals(2, countType(tokens, TokenType.ATTRIBUTE_NAME));
        assertEquals(2, countType(tokens, TokenType.ATTRIBUTE_VALUE));
    }

    @Test
    public void testThymeleafAttribute() {
        List<Token> tokens = tokenize("<li th:each=\"i:items\">");
        Token attrName = find(tokens, TokenType.ATTRIBUTE_NAME);
        assertEquals("th:each", attrName.value);
        Token attrVal = find(tokens, TokenType.ATTRIBUTE_VALUE);
        assertEquals("i:items", attrVal.value);
    }

    @Test
    public void testAttributeWithSingleQuotes() {
        List<Token> tokens = tokenize("<div class='box'>");
        Token attrVal = find(tokens, TokenType.ATTRIBUTE_VALUE);
        assertEquals("box", attrVal.value);
    }

    @Test
    public void testAttributeWithExpressionValue() {
        // Attribute value contains '>' inside quotes — must not break tokenizer
        List<Token> tokens = tokenize("<div th:if=\"${x}\">text</div>");
        Token attrVal = find(tokens, TokenType.ATTRIBUTE_VALUE);
        assertEquals("${x}", attrVal.value);
    }

    // ------------------------------------------------------------------ special content

    @Test
    public void testHtmlCommentIsSkipped() {
        List<Token> tokens = tokenize("<!-- comment --><p>Hi</p>");
        // Comment must produce no TEXT token containing "--"
        for (Token t : tokens) {
            if (t.type == TokenType.TEXT) {
                assertTrue("Comment should not appear in text",
                        !t.value.contains("--"));
            }
        }
    }

    @Test
    public void testDoctypeIsSkipped() {
        List<Token> tokens = tokenize("<!DOCTYPE html><html>");
        Token open = find(tokens, TokenType.TAG_OPEN);
        assertEquals("html", open.value);
    }

    @Test
    public void testHyphenatedTagName() {
        List<Token> tokens = tokenize("<my-component>");
        Token open = find(tokens, TokenType.TAG_OPEN);
        assertEquals("my-component", open.value);
    }

    @Test
    public void testEmptyInput() {
        List<Token> tokens = tokenize("");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type);
    }

    // ------------------------------------------------------------------ helpers

    private void assertHasType(List<Token> tokens, TokenType type) {
        for (Token t : tokens) {
            if (t.type == type) return;
        }
        throw new AssertionError("No token of type " + type + " found in stream");
    }
}
