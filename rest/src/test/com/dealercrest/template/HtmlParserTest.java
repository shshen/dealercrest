package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for HtmlParser — verifies AST structure for various HTML inputs.
 */
public class HtmlParserTest {

    private HtmlParser parser;

    @Before
    public void setUp() {
        parser = new HtmlParser();
    }

    /** Parse and return the synthetic root node's children. */
    private ElementNode root(String html) {
        return (ElementNode) parser.parse(html);
    }

    private ElementNode firstChild(String html) {
        return (ElementNode) root(html).children.get(0);
    }

    // ------------------------------------------------------------------ basic structure

    @Test
    public void testParseSingleElement() {
        ElementNode root = root("<div></div>");
        assertEquals(1, root.children.size());
        ElementNode div = (ElementNode) root.children.get(0);
        assertEquals("div", div.tag);
    }

    @Test
    public void testParseNestedElements() {
        ElementNode root = root("<ul><li>text</li></ul>");
        ElementNode ul = (ElementNode) root.children.get(0);
        assertEquals("ul", ul.tag);
        assertEquals(1, ul.children.size());
        ElementNode li = (ElementNode) ul.children.get(0);
        assertEquals("li", li.tag);
    }

    @Test
    public void testParseTextNode() {
        ElementNode root = root("<p>Hello</p>");
        ElementNode p = (ElementNode) root.children.get(0);
        assertEquals(1, p.children.size());
        assertTrue(p.children.get(0) instanceof TextNode);
    }

    @Test
    public void testParseAttribute() {
        ElementNode el = firstChild("<div class=\"box\"></div>");
        assertEquals("box", el.attributes.get("class"));
    }

    @Test
    public void testParseMultipleAttributes() {
        ElementNode el = firstChild("<input type=\"text\" name=\"q\"/>");
        assertEquals("text", el.attributes.get("type"));
        assertEquals("q",    el.attributes.get("name"));
    }

    @Test
    public void testParseSelfClosingElement() {
        ElementNode root = root("<p>Line<br/>Two</p>");
        ElementNode p = (ElementNode) root.children.get(0);
        // br must be a child of p, not cause it to close
        boolean foundBr = false;
        for (Node child : p.children) {
            if (child instanceof ElementNode) {
                ElementNode el = (ElementNode) child;
                if ("br".equals(el.tag)) foundBr = true;
            }
        }
        assertTrue("br should be a child of p", foundBr);
    }

    @Test
    public void testRootNodeTagIsRoot() {
        ElementNode root = root("<div></div>");
        assertEquals("root", root.tag);
    }

    @Test
    public void testMultipleSiblings() {
        ElementNode root = root("<h1>A</h1><h2>B</h2>");
        assertEquals(2, root.children.size());
    }

    // ------------------------------------------------------------------ void elements

    @Test
    public void testVoidElementBrHasNoChildren() {
        ElementNode root = root("<br/>");
        ElementNode br = (ElementNode) root.children.get(0);
        assertEquals("br", br.tag);
        assertEquals(0, br.children.size());
    }

    @Test
    public void testVoidElementDoesNotConsumeSiblings() {
        ElementNode root = root("<p><br/><span>text</span></p>");
        ElementNode p = (ElementNode) root.children.get(0);
        // p should have br and span as siblings, not span inside br
        assertEquals(2, p.children.size());
    }

    // ------------------------------------------------------------------ th:* attributes

    @Test
    public void testThymeleafAttributeParsed() {
        ElementNode el = firstChild("<li th:each=\"i:items\"></li>");
        assertNotNull(el.attributes.get("th:each"));
        assertEquals("i:items", el.attributes.get("th:each"));
    }

    @Test
    public void testThymeleafIfAttributeParsed() {
        ElementNode el = firstChild("<div th:if=\"${show}\"></div>");
        assertEquals("${show}", el.attributes.get("th:if"));
    }

    // ------------------------------------------------------------------ comment / doctype

    @Test
    public void testHtmlCommentDoesNotProduceNode() {
        ElementNode root = root("<!-- comment --><p>Hi</p>");
        assertEquals(1, root.children.size());
        ElementNode p = (ElementNode) root.children.get(0);
        assertEquals("p", p.tag);
    }

    @Test
    public void testDoctypeDoesNotProduceNode() {
        ElementNode root = root("<!DOCTYPE html><html></html>");
        assertEquals(1, root.children.size());
        ElementNode html = (ElementNode) root.children.get(0);
        assertEquals("html", html.tag);
    }
}
