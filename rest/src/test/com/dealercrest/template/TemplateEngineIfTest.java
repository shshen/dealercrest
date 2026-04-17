package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for th:if directive through TemplateEngine.
 *
 * Covers: true/false Boolean, negation, null/non-null truthiness,
 * nested content, and interaction with other directives.
 */
public class TemplateEngineIfTest {

    private TemplateEngine engine;

    @Before
    public void setUp() {
        DirectiveRegistry reg = new DirectiveRegistry();
        reg.register(new IfDirective());
        reg.register(new EachDirective());
        engine = new TemplateEngine(reg);
    }

    private String render(String tmpl, DataModel ctx) {
        return engine.render(tmpl, tmpl, ctx);
    }

    private DataModel ctx() { return new DataModel(); }

    // ------------------------------------------------------------------ basic true/false

    @Test
    public void testIfTrueRendersContent() {
        DataModel ctx = ctx();
        ctx.set("show", Boolean.TRUE);
        assertEquals("<div><p>Visible</p></div>",
                render("<div th:if=\"${show}\"><p>Visible</p></div>", ctx));
    }

    @Test
    public void testIfFalseRendersNothing() {
        DataModel ctx = ctx();
        ctx.set("show", Boolean.FALSE);
        assertEquals("",
                render("<div th:if=\"${show}\"><p>Hidden</p></div>", ctx));
    }

    @Test
    public void testIfPreservesOuterWrapperTag() {
        DataModel ctx = ctx();
        ctx.set("ok", Boolean.TRUE);
        String out = render("<section th:if=\"${ok}\">body</section>", ctx);
        assertEquals("<section>body</section>", out);
    }

    // ------------------------------------------------------------------ negation

    @Test
    public void testIfNegationTrueHidesElement() {
        DataModel ctx = ctx();
        ctx.set("active", Boolean.TRUE);
        assertEquals("",
                render("<span th:if=\"${!active}\">Inactive</span>", ctx));
    }

    @Test
    public void testIfNegationFalseShowsElement() {
        DataModel ctx = ctx();
        ctx.set("active", Boolean.FALSE);
        assertEquals("<span>Inactive</span>",
                render("<span th:if=\"${!active}\">Inactive</span>", ctx));
    }

    // ------------------------------------------------------------------ null / missing

    @Test
    public void testIfNullVariableRendersNothing() {
        assertEquals("",
                render("<div th:if=\"${missing}\">No</div>", ctx()));
    }

    @Test
    public void testIfNullNegatedRendersElement() {
        // !null is truthy
        assertEquals("<div>Yes</div>",
                render("<div th:if=\"${!missing}\">Yes</div>", ctx()));
    }

    // ------------------------------------------------------------------ non-boolean truthiness

    @Test
    public void testIfNonNullStringIsTruthy() {
        DataModel ctx = ctx();
        ctx.set("label", "hello");
        assertEquals("<p>Yes</p>",
                render("<p th:if=\"${label}\">Yes</p>", ctx));
    }

    @Test
    public void testIfNonNullObjectIsTruthy() {
        DataModel ctx = ctx();
        ctx.set("obj", new Object());
        assertEquals("<p>Yes</p>",
                render("<p th:if=\"${obj}\">Yes</p>", ctx));
    }

    // ------------------------------------------------------------------ nested content

    @Test
    public void testIfWithNestedExpressionInContent() {
        DataModel ctx = ctx();
        ctx.set("show", Boolean.TRUE);
        ctx.set("name", "Alice");
        assertEquals("<div>Hello Alice</div>",
                render("<div th:if=\"${show}\">Hello ${name}</div>", ctx));
    }

    @Test
    public void testIfWithMultipleChildren() {
        DataModel ctx = ctx();
        ctx.set("show", Boolean.TRUE);
        assertEquals("<div><h1>Title</h1><p>Body</p></div>",
                render("<div th:if=\"${show}\"><h1>Title</h1><p>Body</p></div>", ctx));
    }

    // ------------------------------------------------------------------ siblings

    @Test
    public void testOnlyConditionalElementIsAffected() {
        DataModel ctx = ctx();
        ctx.set("show", Boolean.FALSE);
        String out = render(
                "<div>" +
                "<p>Always</p>" +
                "<p th:if=\"${show}\">Conditional</p>" +
                "<p>Also always</p>" +
                "</div>", ctx);
        assertEquals("<div><p>Always</p><p>Also always</p></div>", out);
    }

    @Test
    public void testTwoConditionalsIndependent() {
        DataModel ctx = ctx();
        ctx.set("a", Boolean.TRUE);
        ctx.set("b", Boolean.FALSE);
        String out = render(
                "<div>" +
                "<p th:if=\"${a}\">A</p>" +
                "<p th:if=\"${b}\">B</p>" +
                "</div>", ctx);
        assertEquals("<div><p>A</p></div>", out);
    }
}
