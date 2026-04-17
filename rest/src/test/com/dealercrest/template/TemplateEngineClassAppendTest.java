package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for th:classappend directive through TemplateEngine.
 *
 * Covers: append to existing class, append when no class exists,
 * null/missing variable, cache isolation across renders.
 */
public class TemplateEngineClassAppendTest {

    private TemplateEngine engine;

    @Before
    public void setUp() {
        DirectiveRegistry reg = new DirectiveRegistry();
        reg.register(new ClassAppendDirective());
        reg.register(new IfDirective());
        reg.register(new EachDirective());
        engine = new TemplateEngine(reg);
    }

    private String render(String tmpl, DataModel ctx) {
        return engine.render(tmpl, tmpl, ctx);
    }

    private DataModel ctx() { return new DataModel(); }

    // ------------------------------------------------------------------ basic append

    @Test
    public void testAppendToExistingClass() {
        DataModel ctx = ctx();
        ctx.set("extra", "active");
        assertEquals("<div class=\"card active\">x</div>",
                render("<div class=\"card\" th:classappend=\"${extra}\">x</div>", ctx));
    }

    @Test
    public void testAppendWhenNoExistingClass() {
        DataModel ctx = ctx();
        ctx.set("extra", "highlight");
        assertEquals("<div class=\"highlight\">x</div>",
                render("<div th:classappend=\"${extra}\">x</div>", ctx));
    }

    @Test
    public void testAppendMultipleClasses() {
        DataModel ctx = ctx();
        ctx.set("extra", "active selected");
        assertEquals("<div class=\"card active selected\">x</div>",
                render("<div class=\"card\" th:classappend=\"${extra}\">x</div>", ctx));
    }

    // ------------------------------------------------------------------ null / missing

    @Test
    public void testNullAppendDoesNotAddClass() {
        // "extra" is not set — should not add a class attribute
        assertEquals("<div class=\"card\">x</div>",
                render("<div class=\"card\" th:classappend=\"${extra}\">x</div>", ctx()));
    }

    @Test
    public void testNullAppendOnNoExistingClassProducesNoClassAttr() {
        String out = render("<div th:classappend=\"${extra}\">x</div>", ctx());
        assertFalse("class attribute should not appear", out.contains("class="));
    }

    @Test
    public void testEmptyStringAppendIsSkipped() {
        DataModel ctx = ctx();
        ctx.set("extra", "");
        String out = render("<div class=\"card\" th:classappend=\"${extra}\">x</div>", ctx);
        // class should remain just "card" with no trailing space
        assertTrue(out.contains("class=\"card\""));
    }

    // ------------------------------------------------------------------ cache isolation

    @Test
    public void testCacheDoesNotRetainPreviousAppendedClass() {
        String tmpl = "<div class=\"box\" th:classappend=\"${extra}\">x</div>";

        DataModel c1 = ctx(); c1.set("extra", "red");
        DataModel c2 = ctx(); c2.set("extra", "blue");

        assertEquals("<div class=\"box red\">x</div>",  render(tmpl, c1));
        assertEquals("<div class=\"box blue\">x</div>", render(tmpl, c2));
    }

    @Test
    public void testCacheIsolationNullAfterNonNull() {
        String tmpl = "<div class=\"box\" th:classappend=\"${extra}\">x</div>";

        DataModel c1 = ctx(); c1.set("extra", "active");
        DataModel c2 = ctx(); // extra not set

        assertEquals("<div class=\"box active\">x</div>", render(tmpl, c1));
        assertEquals("<div class=\"box\">x</div>",        render(tmpl, c2));
    }

    // ------------------------------------------------------------------ children preserved

    @Test
    public void testInnerChildrenStillRendered() {
        DataModel ctx = ctx();
        ctx.set("extra", "active");
        String out = render(
                "<div class=\"card\" th:classappend=\"${extra}\"><p>content</p></div>", ctx);
        assertTrue(out.contains("<p>content</p>"));
    }

    @Test
    public void testInnerExpressionStillEvaluated() {
        DataModel ctx = ctx();
        ctx.set("extra", "active");
        ctx.set("name",  "Alice");
        String out = render(
                "<div class=\"card\" th:classappend=\"${extra}\">Hello ${name}</div>", ctx);
        assertEquals("<div class=\"card active\">Hello Alice</div>", out);
    }

    // ------------------------------------------------------------------ with th:if sibling

    @Test
    public void testClassAppendAndIfOnSiblings() {
        DataModel ctx = ctx();
        ctx.set("show",  Boolean.TRUE);
        ctx.set("extra", "highlight");
        String out = render(
                "<div>" +
                "<p th:if=\"${show}\">Shown</p>" +
                "<p class=\"note\" th:classappend=\"${extra}\">Note</p>" +
                "</div>", ctx);
        assertTrue(out.contains("<p>Shown</p>"));
        assertTrue(out.contains("class=\"note highlight\""));
    }
}
