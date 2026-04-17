package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for expression rendering through TemplateEngine.
 *
 * Covers: plain text, single variable, multiple variables, dot-path,
 * missing variable, attribute expressions, and private-field POJO access.
 */
public class TemplateEngineExpressionTest {

    // ------------------------------------------------------------------ fixtures

    public static class Car {
        public String brand;
        public String model;
        public int    year;

        public Car(String brand, String model, int year) {
            this.brand = brand;
            this.model = model;
            this.year  = year;
        }
    }

    public static class Driver {
        private String firstName;
        private String lastName;

        public Driver(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName  = lastName;
        }

        public String getFirstName() { return firstName; }
        public String getLastName()  { return lastName;  }
    }

    // ------------------------------------------------------------------ setup

    private TemplateEngine engine;

    @Before
    public void setUp() {
        DirectiveRegistry reg = new DirectiveRegistry();
        engine = new TemplateEngine(reg);
    }

    private String render(String tmpl, DataModel ctx) {
        return engine.render(tmpl, tmpl, ctx);
    }

    private DataModel ctx() { return new DataModel(); }

    // ------------------------------------------------------------------ plain text

    @Test
    public void testPlainTextNoExpression() {
        assertEquals("<p>Static</p>", render("<p>Static</p>", ctx()));
    }

    @Test
    public void testEmptyTemplate() {
        assertEquals("", render("", ctx()));
    }

    // ------------------------------------------------------------------ single variable

    @Test
    public void testSingleVariableSubstitution() {
        DataModel ctx = ctx();
        ctx.set("title", "Hello");
        assertEquals("<h1>Hello</h1>", render("<h1>${title}</h1>", ctx));
    }

    @Test
    public void testMissingVariableRendersEmpty() {
        assertEquals("<p></p>", render("<p>${missing}</p>", ctx()));
    }

    @Test
    public void testNullVariableRendersEmpty() {
        DataModel ctx = ctx();
        ctx.set("val", null);
        assertEquals("<p></p>", render("<p>${val}</p>", ctx));
    }

    @Test
    public void testIntegerVariable() {
        DataModel ctx = ctx();
        ctx.set("count", 42);
        assertEquals("<span>42</span>", render("<span>${count}</span>", ctx));
    }

    // ------------------------------------------------------------------ multiple variables

    @Test
    public void testTwoVariablesOnOneLine() {
        DataModel ctx = ctx();
        ctx.set("a", "foo");
        ctx.set("b", "bar");
        assertEquals("<p>foo bar</p>", render("<p>${a} ${b}</p>", ctx));
    }

    @Test
    public void testThreeVariablesInText() {
        DataModel ctx = ctx();
        ctx.set("x", "1");
        ctx.set("y", "2");
        ctx.set("z", "3");
        assertEquals("<p>1-2-3</p>", render("<p>${x}-${y}-${z}</p>", ctx));
    }

    // ------------------------------------------------------------------ dot-path (POJO)

    @Test
    public void testPublicFieldDotPath() {
        DataModel ctx = ctx();
        ctx.set("car", new Car("Toyota", "Corolla", 2022));
        assertEquals("<p>Toyota</p>", render("<p>${car.brand}</p>", ctx));
    }

    @Test
    public void testMultipleFieldsOnSamePojo() {
        DataModel ctx = ctx();
        ctx.set("car", new Car("Honda", "Civic", 2020));
        assertEquals("<span>Honda Civic</span>",
                render("<span>${car.brand} ${car.model}</span>", ctx));
    }

    @Test
    public void testPrivateFieldViaGetter() {
        DataModel ctx = ctx();
        ctx.set("d", new Driver("Ada", "Lovelace"));
        assertEquals("<p>Ada Lovelace</p>",
                render("<p>${d.firstName} ${d.lastName}</p>", ctx));
    }

    @Test
    public void testDotPathNullRootRendersEmpty() {
        // "car" is not set — should not throw, should render empty
        assertEquals("<p></p>", render("<p>${car.brand}</p>", ctx()));
    }

    // ------------------------------------------------------------------ attribute expression

    @Test
    public void testAttributeValueExpression() {
        DataModel ctx = ctx();
        ctx.set("cls", "highlight");
        assertEquals("<div class=\"highlight\">x</div>",
                render("<div class=\"${cls}\">x</div>", ctx));
    }

    @Test
    public void testAttributeExpressionMissingVarRendersEmpty() {
        assertEquals("<a href=\"\">link</a>",
                render("<a href=\"${url}\">link</a>", ctx()));
    }

    // ------------------------------------------------------------------ template caching

    @Test
    public void testSameTemplateRenderedTwiceGivesSameOutput() {
        DataModel c1 = ctx(); c1.set("v", "first");
        DataModel c2 = ctx(); c2.set("v", "second");
        String tmpl = "<p>${v}</p>";
        assertEquals("<p>first</p>",  render(tmpl, c1));
        assertEquals("<p>second</p>", render(tmpl, c2));
    }

    @Test
    public void testClearCacheDoesNotBreakSubsequentRenders() {
        DataModel ctx = ctx();
        ctx.set("x", "val");
        String tmpl = "<p>${x}</p>";
        render(tmpl, ctx);        // populate cache
        engine.clearCache();
        assertEquals("<p>val</p>", render(tmpl, ctx)); // should re-compile cleanly
    }

    // ------------------------------------------------------------------ ThreadLocal buffer

    @Test
    public void testSequentialRendersDoNotBleedOver() {
        DataModel c1 = ctx(); c1.set("v", "AAA");
        DataModel c2 = ctx(); c2.set("v", "BBB");
        String tmpl = "<span>${v}</span>";
        assertEquals("<span>AAA</span>", render(tmpl, c1));
        assertEquals("<span>BBB</span>", render(tmpl, c2));
    }
}
