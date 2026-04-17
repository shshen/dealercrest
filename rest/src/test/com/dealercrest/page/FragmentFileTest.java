package com.dealercrest.page;

import org.junit.Test;

import com.dealercrest.page.FragmentFile;

import java.util.Map;

import static org.junit.Assert.*;

public class FragmentFileTest {

    @Test
    public void testReturnsNullForNormalHtml() {
        String html = "<html>\n<body>Hello</body>\n</html>";
        assertNull(FragmentFile.parse(html));
    }

    @Test
    public void testReturnsPageWhenLayoutAttributePresent() {
        String html = "<html layout=\"/layout/classic.html\">\n</html>";
        assertNotNull(FragmentFile.parse(html));
    }

    // -------------------------------------------------------------------------
    // layout field
    // -------------------------------------------------------------------------

    @Test
    public void testLayoutValueIsParsedCorrectly() {
        String html = "<html layout=\"/layout/classic.html\" title=\"Home\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertEquals("/layout/classic.html", page.getLayout());
    }

    @Test
    public void testLayoutIsNotIncludedInAttributes() {
        String html = "<html layout=\"/layout/classic.html\" title=\"Home\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertFalse(page.getAttributes().containsKey("layout"));
    }

    // -------------------------------------------------------------------------
    // attributes field
    // -------------------------------------------------------------------------

    @Test
    public void testSingleAttributeParsed() {
        String html = "<html layout=\"/layout/classic.html\" title=\"Home\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertEquals("Home", page.getAttributes().get("title"));
    }

    @Test
    public void testMultipleAttributesParsed() {
        String html = "<html layout=\"/layout/classic.html\" title=\"Home\" description=\"Dealer system home page\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        Map<String, String> attrs = page.getAttributes();
        assertEquals("Home", attrs.get("title"));
        assertEquals("Dealer system home page", attrs.get("description"));
    }

    @Test
    public void testNoExtraAttributesWhenOnlyLayoutPresent() {
        String html = "<html layout=\"/layout/classic.html\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertTrue(page.getAttributes().isEmpty());
    }

    @Test
    public void testAttributeWithSingleQuotes() {
        String html = "<html layout='/layout/classic.html' title='Home'>\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertEquals("/layout/classic.html", page.getLayout());
        assertEquals("Home", page.getAttributes().get("title"));
    }

    @Test
    public void testAttributeValueContainingGreaterThan() {
        // '>' inside a quoted value must not terminate the tag early
        String html = "<html layout=\"/layout/classic.html\" title=\"a > b\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertNotNull(page);
        assertEquals("a > b", page.getAttributes().get("title"));
    }

    // -------------------------------------------------------------------------
    // placeholderValues field
    // -------------------------------------------------------------------------

    @Test
    public void testSinglePlaceholderParsed() {
        String html = "<html layout=\"/layout/classic.html\">\n"
                + "<!-- $begin:body -->\n"
                + "<div class=\"hero\">Hello</div>\n"
                + "<!-- $end:body -->\n"
                + "</html>";
                FragmentFile page = FragmentFile.parse(html);
        assertEquals("<div class=\"hero\">Hello</div>", page.getPlaceholderValues().get("body"));
    }

    @Test
    public void testMultiplePlaceholdersParsed() {
        String html = "<html layout=\"/layout/classic.html\">\n"
                + "<!-- $begin:body -->\n"
                + "<div>Body content</div>\n"
                + "<!-- $end:body -->\n"
                + "<!-- $begin:sidebar -->\n"
                + "<div>Sidebar content</div>\n"
                + "<!-- $end:sidebar -->\n"
                + "</html>";
                FragmentFile page = FragmentFile.parse(html);
        assertEquals("<div>Body content</div>", page.getPlaceholderValues().get("body"));
        assertEquals("<div>Sidebar content</div>", page.getPlaceholderValues().get("sidebar"));
    }

    @Test
    public void testEmptyPlaceholderValues() {
        String html = "<html layout=\"/layout/classic.html\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertTrue(page.getPlaceholderValues().isEmpty());
    }

    @Test
    public void testPlaceholderWithMultilineContent() {
        String html = "<html layout=\"/layout/classic.html\">\n"
                + "<!-- $begin:body -->\n"
                + "<div>\n"
                + "  <p>Line one</p>\n"
                + "  <p>Line two</p>\n"
                + "</div>\n"
                + "<!-- $end:body -->\n"
                + "</html>";
        FragmentFile page = FragmentFile.parse(html);
        String body = page.getPlaceholderValues().get("body");
        assertTrue(body.contains("<p>Line one</p>"));
        assertTrue(body.contains("<p>Line two</p>"));
    }

    @Test
    public void testUnclosedPlaceholderIsIgnored() {
        // $begin:body has no matching $end:body — should not throw, just be absent
        String html = "<html layout=\"/layout/classic.html\">\n"
                + "<!-- $begin:body -->\n"
                + "<div>Content</div>\n"
                + "</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertFalse(page.getPlaceholderValues().containsKey("body"));
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testNoHtmlTagReturnsNull() {
        String html = "<div>Not an html document</div>";
        assertNull(FragmentFile.parse(html));
    }

    @Test
    public void testHtmlTagWithNoAttributesReturnsNull() {
        String html = "<html></html>";
        assertNull(FragmentFile.parse(html));
    }

    @Test
    public void testLayoutAttributeValuePreservesPath() {
        String html = "<html layout=\"/layouts/nested/deep/template.html\">\n</html>";
        FragmentFile page = FragmentFile.parse(html);
        assertEquals("/layouts/nested/deep/template.html", page.getLayout());
    }
}
