package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for th:replace with per-page FragmentRegistry.
 *
 * Key difference from the old design:
 *   - ReplaceDirective takes NO constructor argument
 *   - Each test attaches a fresh FragmentRegistry to the Model via
 *     model.setFragments(registry)
 *   - The same engine serves all pages; only the Model changes per request
 */
public class TemplateEngineReplaceTest {

    private TemplateEngine engine;

    @Before
    public void setUp() {
        DirectiveRegistry directives = new DirectiveRegistry();
        directives.register(new ReplaceDirective()); // no FragmentRegistry arg
        directives.register(new IfDirective());
        directives.register(new EachDirective());

        engine = new TemplateEngine(directives);
    }

    /** Build a model with a fresh per-page FragmentRegistry. */
    private Model modelWithFragments(FragmentRegistry fr) {
        Model model = new Model();
        model.setFragments(fr);
        return model;
    }

    private String render(String tmpl, Model model) {
        return engine.render(tmpl, tmpl, model);
    }

    // ------------------------------------------------------------------ basic replacement

    @Test
    public void testReplaceWithSimpleFragment() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("greeting", "<p>Hello</p>");

        assertEquals("<p>Hello</p>",
                render("<div th:replace=\"greeting\"></div>",
                        modelWithFragments(fr)));
    }

    @Test
    public void testOriginalElementTagIsDiscarded() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("badge", "<span>Badge</span>");

        String out = render("<section th:replace=\"badge\"></section>",
                modelWithFragments(fr));

        assertFalse("Original <section> must not appear", out.contains("<section>"));
        assertTrue("Fragment output must appear", out.contains("<span>Badge</span>"));
    }

    @Test
    public void testOriginalElementChildrenAreDiscarded() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("box", "<div>Fragment</div>");

        String out = render(
                "<div th:replace=\"box\"><p>original child</p></div>",
                modelWithFragments(fr));

        assertFalse("Original children must not appear", out.contains("original child"));
        assertTrue("Fragment must appear", out.contains("Fragment"));
    }

    // ------------------------------------------------------------------ no registry / unknown fragment

    @Test
    public void testNoFragmentRegistryRendersEmpty() {
        // Model has no fragments attached — should not throw
        Model model = new Model(); // no setFragments() call
        assertEquals("",
                render("<div th:replace=\"nav\"></div>", model));
    }

    @Test
    public void testUnknownFragmentNameRendersEmpty() {
        FragmentRegistry fr = new FragmentRegistry();
        // "nonexistent" is never registered

        assertEquals("",
                render("<div th:replace=\"nonexistent\"></div>",
                        modelWithFragments(fr)));
    }

    // ------------------------------------------------------------------ fragment with expression

    @Test
    public void testFragmentResolvesModelVariables() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("greeting", "<span>Hello ${name}</span>");

        Model model = new Model();
        model.set("name", "World");
        model.setFragments(fr);

        assertEquals("<span>Hello World</span>",
                render("<div th:replace=\"greeting\"></div>", model));
    }

    @Test
    public void testFragmentUsesRenderTimeModelNotCompileTimeModel() {
        // Same template, same fragment, different model — must give different output
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("label", "<b>${text}</b>");

        Model m1 = new Model(); m1.set("text", "First");  m1.setFragments(fr);
        Model m2 = new Model(); m2.set("text", "Second"); m2.setFragments(fr);

        String tmpl = "<div th:replace=\"label\"></div>";
        assertEquals("<b>First</b>",  engine.render(tmpl, tmpl, m1));
        assertEquals("<b>Second</b>", engine.render(tmpl, tmpl, m2));
    }

    // ------------------------------------------------------------------ per-page isolation

    @Test
    public void testDifferentPagesUseDifferentFragments() {
        // Page A — vehicle nav
        FragmentRegistry frA = new FragmentRegistry();
        frA.register("nav", "<nav>Vehicles</nav>");

        // Page B — profile nav
        FragmentRegistry frB = new FragmentRegistry();
        frB.register("nav", "<nav>Profile</nav>");

        String tmpl = "<div th:replace=\"nav\"></div>";

        Model modelA = new Model(); modelA.setFragments(frA);
        Model modelB = new Model(); modelB.setFragments(frB);

        assertEquals("<nav>Vehicles</nav>", engine.render(tmpl, tmpl, modelA));
        assertEquals("<nav>Profile</nav>",  engine.render(tmpl, tmpl, modelB));
    }

    @Test
    public void testPageAFragmentDoesNotLeakToPageB() {
        // Page A registers "footer"; Page B does not
        FragmentRegistry frA = new FragmentRegistry();
        frA.register("footer", "<footer>Page A Footer</footer>");

        FragmentRegistry frB = new FragmentRegistry();
        // "footer" intentionally absent from frB

        String tmpl = "<div th:replace=\"footer\"></div>";

        Model modelA = new Model(); modelA.setFragments(frA);
        Model modelB = new Model(); modelB.setFragments(frB);

        assertEquals("<footer>Page A Footer</footer>",
                engine.render(tmpl, tmpl, modelA));
        assertEquals("", // frB has no "footer" — must render empty
                engine.render(tmpl, tmpl, modelB));
    }

    @Test
    public void testSameRegistryReusedAcrossMultipleRequests() {
        // One FragmentRegistry created at startup, shared across many requests
        FragmentRegistry sharedFr = new FragmentRegistry();
        sharedFr.register("chip", "<span>Chip</span>");

        String tmpl = "<div th:replace=\"chip\"></div>";

        // Simulate multiple requests reusing the same registry
        for (int i = 0; i < 5; i++) {
            Model model = new Model();
            model.setFragments(sharedFr);
            assertEquals("<span>Chip</span>",
                    engine.render(tmpl, tmpl, model));
        }
    }

    // ------------------------------------------------------------------ nested fragment

    @Test
    public void testFragmentWithNestedElements() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("nav", "<nav><ul><li>Home</li><li>About</li></ul></nav>");

        assertEquals("<nav><ul><li>Home</li><li>About</li></ul></nav>",
                render("<header th:replace=\"nav\"></header>",
                        modelWithFragments(fr)));
    }

    // ------------------------------------------------------------------ surrounding content

    @Test
    public void testReplaceDoesNotAffectSurroundingContent() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("chip", "<span>Chip</span>");

        String out = render(
                "<div><p>Before</p><div th:replace=\"chip\"></div><p>After</p></div>",
                modelWithFragments(fr));

        assertEquals("<div><p>Before</p><span>Chip</span><p>After</p></div>", out);
    }

    // ------------------------------------------------------------------ re-registration

    @Test
    public void testReregisteredFragmentUsesNewContent() {
        FragmentRegistry fr = new FragmentRegistry();
        fr.register("msg", "<p>Old</p>");

        String tmpl = "<div th:replace=\"msg\"></div>";
        Model model = new Model();
        model.setFragments(fr);

        assertEquals("<p>Old</p>", engine.render(tmpl, tmpl, model));

        fr.register("msg", "<p>New</p>"); // re-register evicts compiled cache

        assertEquals("<p>New</p>", engine.render(tmpl, tmpl, model));
    }
}