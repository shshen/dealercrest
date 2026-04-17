package com.dealercrest.page;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;


public class HtmlPageSourceTest {

    // -------------------------------------------------------------------------
    // Basic parsing
    // -------------------------------------------------------------------------

    @Test
    public void testLayoutPath() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">\n" +
            "    <h2>Hello</h2>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals("/layout/classic.html", source.getLayoutPath());
    }

    @Test
    public void testTitle() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\"><h2>Hello</h2></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals("Welcome", source.getTitle());
    }

    @Test
    public void testSingleSlot() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">\n" +
            "    <h2>Body content</h2>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertTrue(source.getSlots().containsKey("body"));
        assertTrue(source.getSlots().get("body").contains("<h2>Body content</h2>"));
    }

    @Test
    public void testMultipleSlots() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body1\">\n" +
            "    <h2>Body content 1</h2>\n" +
            "  </div>\n" +
            "  <div data-slot=\"body2\">\n" +
            "    <h2>Body content 2</h2>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals(2, source.getSlots().size());
        assertTrue(source.getSlots().get("body1").contains("<h2>Body content 1</h2>"));
        assertTrue(source.getSlots().get("body2").contains("<h2>Body content 2</h2>"));
    }

    @Test
    public void testSlotOrderPreserved() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"first\"><p>First</p></div>\n" +
            "  <div data-slot=\"second\"><p>Second</p></div>\n" +
            "  <div data-slot=\"third\"><p>Third</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        String[] keys = source.getSlots().keySet().toArray(new String[0]);
        assertEquals("first",  keys[0]);
        assertEquals("second", keys[1]);
        assertEquals("third",  keys[2]);
    }

    // -------------------------------------------------------------------------
    // Elements outside slots are ignored
    // -------------------------------------------------------------------------

    @Test
    public void testContentOutsideSlotIsIgnored() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">\n" +
            "    <h2>Slot content</h2>\n" +
            "  </div>\n" +
            "  <h2>should be skipped</h2>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals(1, source.getSlots().size());
        assertTrue(source.getSlots().containsKey("body"));
    }

    @Test
    public void testOnlyNonSlotContent() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <h2>No slots here</h2>\n" +
            "  <p>Just bare content</p>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertTrue(source.getSlots().isEmpty());
        assertEquals("/layout/classic.html", source.getLayoutPath());
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    public void testMultipleMetadataAttributes() {
        String html =
            "<html data-layout=\"/layout/classic.html\" " +
                  "data-title=\"My Page\" " +
                  "data-description=\"A test page\" " +
                  "data-author=\"Shane\">\n" +
            "  <div data-slot=\"body\"><p>Content</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        Map<String, String> meta = source.getMetadata();
        assertEquals("My Page",    meta.get("title"));
        assertEquals("A test page", meta.get("description"));
        assertEquals("Shane",       meta.get("author"));
    }

    @Test
    public void testMetadataDoesNotContainLayout() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\"><p>Content</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        // data-layout is promoted to layoutPath, not stored in metadata
        assertFalse(source.getMetadata().containsKey("layout"));
    }

    @Test
    public void testTitleNullWhenAbsent() {
        String html =
            "<html data-layout=\"/layout/classic.html\">\n" +
            "  <div data-slot=\"body\"><p>Content</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertNull(source.getTitle());
    }

    @Test
    public void testLayoutPathNullWhenAbsent() {
        String html =
            "<html data-title=\"No Layout\">\n" +
            "  <div data-slot=\"body\"><p>Content</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertNull(source.getLayoutPath());
    }

    // -------------------------------------------------------------------------
    // Nested divs inside slots
    // -------------------------------------------------------------------------

    @Test
    public void testNestedDivInsideSlot() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">\n" +
            "    <div class=\"wrapper\">\n" +
            "      <div class=\"inner\"><p>Deep content</p></div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        String slotContent = source.getSlots().get("body");
        assertNotNull(slotContent);
        assertTrue(slotContent.contains("class=\"wrapper\""));
        assertTrue(slotContent.contains("class=\"inner\""));
        assertTrue(slotContent.contains("<p>Deep content</p>"));
    }

    @Test
    public void testMultipleLevelsOfNestedDivs() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"content\">\n" +
            "    <div>\n" +
            "      <div>\n" +
            "        <div><span>Three levels deep</span></div>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        String slotContent = source.getSlots().get("content");
        assertNotNull(slotContent);
        assertTrue(slotContent.contains("<span>Three levels deep</span>"));
    }

    @Test
    public void testNestedDivDoesNotBreakSubsequentSlot() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"first\">\n" +
            "    <div class=\"nested\"><p>Nested</p></div>\n" +
            "  </div>\n" +
            "  <div data-slot=\"second\">\n" +
            "    <p>Second slot</p>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals(2, source.getSlots().size());
        assertTrue(source.getSlots().get("first").contains("class=\"nested\""));
        assertTrue(source.getSlots().get("second").contains("<p>Second slot</p>"));
    }

    // -------------------------------------------------------------------------
    // Slot inner HTML content
    // -------------------------------------------------------------------------

    @Test
    public void testSlotInnerHtmlIsTrimmed() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">   <p>Trimmed</p>   </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        String content = source.getSlots().get("body");
        assertFalse(content.startsWith(" "));
        assertFalse(content.endsWith(" "));
    }

    @Test
    public void testSlotWithMultipleChildElements() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"body\">\n" +
            "    <h2>Title</h2>\n" +
            "    <p>Paragraph one</p>\n" +
            "    <p>Paragraph two</p>\n" +
            "  </div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        String content = source.getSlots().get("body");
        assertTrue(content.contains("<h2>Title</h2>"));
        assertTrue(content.contains("<p>Paragraph one</p>"));
        assertTrue(content.contains("<p>Paragraph two</p>"));
    }

    @Test
    public void testEmptySlot() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"empty\"></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertTrue(source.getSlots().containsKey("empty"));
        assertEquals("", source.getSlots().get("empty"));
    }

    // -------------------------------------------------------------------------
    // Single-quoted attributes
    // -------------------------------------------------------------------------

    @Test
    public void testSingleQuotedAttributes() {
        String html =
            "<html data-layout='/layout/classic.html' data-title='Welcome'>\n" +
            "  <div data-slot='body'><p>Content</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertEquals("/layout/classic.html", source.getLayoutPath());
        assertEquals("Welcome", source.getTitle());
        assertTrue(source.getSlots().containsKey("body"));
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    public void testNoSlots() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        assertNotNull(source);
        assertTrue(source.getSlots().isEmpty());
        assertEquals("/layout/classic.html", source.getLayoutPath());
    }

    @Test
    public void testEmptyHtml() {
        HtmlPageSource source = HtmlPageSource.parse("", 0L);
        assertNotNull(source);
        assertNull(source.getLayoutPath());
        assertTrue(source.getMetadata().isEmpty());
        assertTrue(source.getSlots().isEmpty());
    }

    @Test
    public void testGetSlotsReturnsAllSlotNames() {
        String html =
            "<html data-layout=\"/layout/classic.html\" data-title=\"Welcome\">\n" +
            "  <div data-slot=\"header\"><p>Header</p></div>\n" +
            "  <div data-slot=\"body\"><p>Body</p></div>\n" +
            "  <div data-slot=\"footer\"><p>Footer</p></div>\n" +
            "</html>";

        HtmlPageSource source = HtmlPageSource.parse(html, 0L);
        Map<String, String> slots = source.getSlots();
        assertTrue(slots.containsKey("header"));
        assertTrue(slots.containsKey("body"));
        assertTrue(slots.containsKey("footer"));
        assertEquals(3, slots.size());
    }
}
