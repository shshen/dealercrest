package com.dealercrest.page;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 tests for {@link BlockConfig#from(String)}.
 *
 * Dependency:
 *   Maven:  &lt;dependency&gt;
 *               &lt;groupId&gt;junit&lt;/groupId&gt;
 *               &lt;artifactId&gt;junit&lt;/artifactId&gt;
 *               &lt;version&gt;4.13.2&lt;/version&gt;
 *               &lt;scope&gt;test&lt;/scope&gt;
 *           &lt;/dependency&gt;
 *   Gradle: testImplementation 'junit:junit:4.13.2'
 */
public class BlockConfigTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final String VALID_HTML =
        "<footer\n" +
        "  data-schema-name=\"Footer — Standard\"\n" +
        "  data-schema-description=\"Site footer with dealer branding\"\n" +
        "  data-schema-fields=\"logo,copyright\"\n" +
        "  data-schema-layout=\"layout.html\"\n" +
        "  data-schema-preview=\"themes/modern/footer/preview.jpg\"\n" +
        "  data-schema-render=\"static\"\n" +
        "  data-schema-order=\"10\"\n" +
        ">\n" +
        "  <img data-field=\"logo\" src=\"{{logo}}\" />\n" +
        "  <p data-field=\"copyright\">{{copyright}}</p>\n" +
        "</footer>";

    // -------------------------------------------------------------------------
    // Happy path — field values
    // -------------------------------------------------------------------------

    @Test
    public void testParsesName() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals("Footer — Standard", block.getName());
    }

    @Test
    public void testParsesDescription() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals("Site footer with dealer branding", block.getDescription());
    }

    @Test
    public void testParsesLayout() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals("layout.html", block.getLayout());
    }

    @Test
    public void testParsesPreview() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals("themes/modern/footer/preview.jpg", block.getPreview());
    }

    @Test
    public void testParsesRender() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals("static", block.getRender());
    }

    @Test
    public void testParsesOrder() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        assertEquals(Integer.valueOf(10), block.getOrder());
    }

    @Test
    public void testParsesFields() {
        BlockConfig block = BlockConfig.from(VALID_HTML);
        List<String> fields = block.getFields();
        assertEquals(2, fields.size());
        assertEquals("logo",      fields.get(0));
        assertEquals("copyright", fields.get(1));
    }

    @Test
    public void testParsesMultipleFields() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo, copyright, nav_links, social_links\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";

        BlockConfig block = BlockConfig.from(html);
        List<String> fields = block.getFields();
        assertEquals(4, fields.size());
        assertEquals("nav_links",    fields.get(2));
        assertEquals("social_links", fields.get(3));
    }

    // -------------------------------------------------------------------------
    // htmlContent
    // -------------------------------------------------------------------------

    @Test
    public void testHtmlContentStripsAllSchemaAttrs() {
        BlockConfig  block = BlockConfig.from(VALID_HTML);
        String html  = block.getHtmlContent();
        assertFalse(html.contains("data-schema-name"));
        assertFalse(html.contains("data-schema-fields"));
        assertFalse(html.contains("data-schema-layout"));
        assertFalse(html.contains("data-schema-preview"));
        assertFalse(html.contains("data-schema-render"));
        assertFalse(html.contains("data-schema-order"));
        assertFalse(html.contains("data-schema-description"));
    }

    @Test
    public void testHtmlContentKeepsDataFieldAttrs() {
        BlockConfig  block = BlockConfig.from(VALID_HTML);
        String html  = block.getHtmlContent();
        assertTrue(html.contains("data-field=\"logo\""));
        assertTrue(html.contains("data-field=\"copyright\""));
    }

    @Test
    public void testHtmlContentKeepsTemplateExpressions() {
        BlockConfig  block = BlockConfig.from(VALID_HTML);
        String html  = block.getHtmlContent();
        assertTrue(html.contains("{{logo}}"));
        assertTrue(html.contains("{{copyright}}"));
    }

    @Test
    public void testHtmlContentKeepsRootTag() {
        BlockConfig  block = BlockConfig.from(VALID_HTML);
        String html  = block.getHtmlContent().trim();
        assertTrue(html.startsWith("<footer"));
        assertTrue(html.endsWith("</footer>"));
    }

    // -------------------------------------------------------------------------
    // Optional fields — defaults and nulls
    // -------------------------------------------------------------------------

    @Test
    public void testRenderDefaultsToStatic() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig block = BlockConfig.from(html);
        assertEquals("static", block.getRender());
    }

    @Test
    public void testDescriptionNullWhenAbsent() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig block = BlockConfig.from(html);
        assertNull(block.getDescription());
    }

    @Test
    public void testOrderNullWhenAbsent() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig block = BlockConfig.from(html);
        assertNull(block.getOrder());
    }

    @Test
    public void testSingleQuotedAttributeValues() {
        String html =
            "<footer\n" +
            "  data-schema-name='Footer'\n" +
            "  data-schema-fields='logo,copyright'\n" +
            "  data-schema-layout='layout.html'\n" +
            "  data-schema-preview='preview.jpg'\n" +
            "></footer>";
        BlockConfig block = BlockConfig.from(html);
        assertEquals("Footer", block.getName());
        assertEquals(2, block.getFields().size());
    }

    @Test
    public void testLeadingAndTrailingWhitespace() {
        BlockConfig block = BlockConfig.from("   \n" + VALID_HTML + "\n   ");
        assertEquals("Footer — Standard", block.getName());
    }

    // -------------------------------------------------------------------------
    // Validation — required attributes
    // -------------------------------------------------------------------------

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenNameMissing() {
        String html =
            "<footer\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenFieldsMissing() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenLayoutMissing() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenPreviewMissing() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenFieldsIsEmpty() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    // -------------------------------------------------------------------------
    // Validation — structural
    // -------------------------------------------------------------------------

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenHtmlIsNull() {
        BlockConfig.from(null);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenHtmlIsBlank() {
        BlockConfig.from("   ");
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenNoRootElement() {
        BlockConfig.from("just some text with no tags");
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenMultipleRootElements() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>\n" +
            "<div>extra root</div>";
        BlockConfig.from(html);
    }

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenMultipleRootElementsSameTag() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>\n" +
            "<footer></footer>";
        BlockConfig.from(html);
    }

    // -------------------------------------------------------------------------
    // Validation — bad data types
    // -------------------------------------------------------------------------

    @Test(expected = BlockConfig.ParseException.class)
    public void testThrowsWhenOrderIsNotInteger() {
        String html =
            "<footer\n" +
            "  data-schema-name=\"Footer\"\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "  data-schema-order=\"abc\"\n" +
            "></footer>";
        BlockConfig.from(html);
    }

    // -------------------------------------------------------------------------
    // Exception message quality
    // -------------------------------------------------------------------------

    @Test
    public void testExceptionMessageMentionsMissingAttr() {
        String html =
            "<footer\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></footer>";
        try {
            BlockConfig.from(html);
            fail("Expected ParseException");
        } catch (BlockConfig.ParseException e) {
            assertTrue(e.getMessage().contains("data-schema-name"));
        }
    }

    @Test
    public void testExceptionMessageMentionsTagName() {
        String html =
            "<section\n" +
            "  data-schema-fields=\"logo\"\n" +
            "  data-schema-layout=\"layout.html\"\n" +
            "  data-schema-preview=\"preview.jpg\"\n" +
            "></section>";
        try {
            BlockConfig.from(html);
            fail("Expected ParseException");
        } catch (BlockConfig.ParseException e) {
            assertTrue(e.getMessage().contains("section"));
        }
    }
}
