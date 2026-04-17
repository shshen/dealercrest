package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Integration tests for th:replace directive through TemplateEngine.
 *
 * Covers: basic replacement, unknown fragment, fragment with expression,
 * fragment with nested elements, and re-registration.
 */
public class TemplateEngineReplaceTest {

    private TemplateEngine   engine;
    private FragmentRegistry fragments;

    @Before
    public void setUp() {
        fragments = new FragmentRegistry();

        DirectiveRegistry reg = new DirectiveRegistry();
        reg.register(new ReplaceDirective(fragments));
        reg.register(new IfDirective());
        reg.register(new EachDirective());

        engine = new TemplateEngine(reg);
    }

    private String render(String tmpl, DataModel ctx) {
        return engine.render(tmpl, tmpl, ctx);
    }

    private DataModel ctx() { return new DataModel(); }

    // ------------------------------------------------------------------ basic replacement

    @Test
    public void testReplaceWithSimpleFragment() {
        fragments.register("greeting", "<p>Hello</p>");
        engine.clearCache();
        assertEquals("<p>Hello</p>",
                render("<div th:replace=\"greeting\"></div>", ctx()));
    }

    @Test
    public void testOriginalElementTagIsDiscarded() {
        fragments.register("badge", "<span>Badge</span>");
        engine.clearCache();
        String out = render("<section th:replace=\"badge\"></section>", ctx());
        assertFalse("Original <section> tag must not appear", out.contains("<section>"));
        assertTrue("Fragment output must appear", out.contains("<span>Badge</span>"));
    }

    @Test
    public void testOriginalElementChildrenAreDiscarded() {
        fragments.register("box", "<div>Fragment</div>");
        engine.clearCache();
        String out = render("<div th:replace=\"box\"><p>original child</p></div>", ctx());
        assertFalse("Original children must not appear", out.contains("original child"));
        assertTrue("Fragment must appear", out.contains("Fragment"));
    }

    // ------------------------------------------------------------------ unknown fragment

    @Test
    public void testUnknownFragmentRendersEmpty() {
        assertEquals("",
                render("<div th:replace=\"nonexistent\"></div>", ctx()));
    }

    // ------------------------------------------------------------------ fragment with expression

    @Test
    public void testFragmentWithExpressionResolvesContext() {
        fragments.register("greeting", "<span>Hello ${name}</span>");
        engine.clearCache();

        DataModel ctx = ctx();
        ctx.set("name", "World");

        assertEquals("<span>Hello World</span>",
                render("<div th:replace=\"greeting\"></div>", ctx));
    }

    @Test
    public void testFragmentExpressionUsesRenderTimeContext() {
        fragments.register("label", "<b>${text}</b>");
        engine.clearCache();

        DataModel c1 = ctx(); c1.set("text", "First");
        DataModel c2 = ctx(); c2.set("text", "Second");

        assertEquals("<b>First</b>",  render("<div th:replace=\"label\"></div>", c1));
        assertEquals("<b>Second</b>", render("<div th:replace=\"label\"></div>", c2));
    }

    // ------------------------------------------------------------------ nested fragment

    @Test
    public void testFragmentWithNestedElements() {
        fragments.register("nav",
                "<nav><ul><li>Home</li><li>About</li></ul></nav>");
        engine.clearCache();
        String out = render("<header th:replace=\"nav\"></header>", ctx());
        assertEquals("<nav><ul><li>Home</li><li>About</li></ul></nav>", out);
    }

    // ------------------------------------------------------------------ surrounding content

    @Test
    public void testReplaceDoesNotAffectSurroundingContent() {
        fragments.register("chip", "<span>Chip</span>");
        engine.clearCache();
        String out = render(
                "<div><p>Before</p><div th:replace=\"chip\"></div><p>After</p></div>",
                ctx());
        assertEquals("<div><p>Before</p><span>Chip</span><p>After</p></div>", out);
    }

    // ------------------------------------------------------------------ re-registration

    @Test
    public void testReregisteredFragmentUsesNewContent() {
        fragments.register("msg", "<p>Old</p>");
        engine.clearCache();
        assertEquals("<p>Old</p>",
                render("<template th:replace=\"msg\"></template>", ctx()));

        fragments.register("msg", "<p>New</p>");
        engine.clearCache();
        assertEquals("<p>New</p>",
                render("<div th:replace=\"msg\"></div>", ctx()));
    }
}
