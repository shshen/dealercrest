package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for th:each directive through TemplateEngine.
 *
 * Covers: basic iteration, wrapper tag preservation, dot-path inside loop,
 * empty list, nested loops, and context isolation between iterations.
 */
public class TemplateEngineEachTest {

    // ------------------------------------------------------------------ fixture

    public static class Product {
        public String name;
        public double price;

        public Product(String name, double price) {
            this.name  = name;
            this.price = price;
        }
    }

    // ------------------------------------------------------------------ setup

    private TemplateEngine engine;

    @Before
    public void setUp() {
        DirectiveRegistry reg = new DirectiveRegistry();
        reg.register(new EachDirective());
        reg.register(new IfDirective());
        engine = new TemplateEngine(reg);
    }

    private String render(String tmpl, DataModel ctx) {
        return engine.render(tmpl, tmpl, ctx);
    }

    private DataModel ctx() { return new DataModel(); }

    // ------------------------------------------------------------------ basic iteration

    @Test
    public void testEachOverStringList() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("Alpha");
        items.add("Beta");
        ctx.set("items", items);
        assertEquals("<ul><li>Alpha</li><li>Beta</li></ul>",
                render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx));
    }

    @Test
    public void testEachProducesCorrectCount() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("A"); items.add("B"); items.add("C");
        ctx.set("items", items);
        String out = render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx);
        // Count <li> occurrences
        int count = 0;
        int idx = 0;
        while ((idx = out.indexOf("<li>", idx)) != -1) { count++; idx++; }
        assertEquals(3, count);
    }

    @Test
    public void testEachEmptyListProducesNoChildren() {
        DataModel ctx = ctx();
        ctx.set("items", new ArrayList<String>());
        assertEquals("<ul></ul>",
                render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx));
    }

    @Test
    public void testEachWithNullListDoesNotThrow() {
        DataModel ctx = ctx();
        ctx.set("items", null);
        // Should render the wrapper but no iterations
        String out = render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx);
        assertEquals("<ul></ul>", out);
    }

    // ------------------------------------------------------------------ wrapper tag preservation

    @Test
    public void testEachPreservesLiTag() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("X");
        ctx.set("items", items);
        assertTrue(render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx)
                .contains("<li>X</li>"));
    }

    @Test
    public void testEachPreservesTrTag() {
        DataModel ctx = ctx();
        List<String> rows = new ArrayList<String>();
        rows.add("R1"); rows.add("R2");
        ctx.set("rows", rows);
        assertEquals("<table><tr><td>R1</td></tr><tr><td>R2</td></tr></table>",
                render("<table><tr th:each=\"r:rows\"><td>${r}</td></tr></table>", ctx));
    }

    @Test
    public void testEachPreservesAttributesOnWrapperTag() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("item");
        ctx.set("items", items);
        String out = render("<ul><li class=\"row\" th:each=\"i:items\">${i}</li></ul>", ctx);
        assertTrue("class attr should be preserved", out.contains("class=\"row\""));
    }

    // ------------------------------------------------------------------ dot-path inside loop

    @Test
    public void testEachWithObjectDotPath() {
        DataModel ctx = ctx();
        List<Product> products = new ArrayList<Product>();
        products.add(new Product("Apple", 1.5));
        products.add(new Product("Banana", 0.75));
        ctx.set("products", products);
        assertEquals("<ul><li>Apple</li><li>Banana</li></ul>",
                render("<ul><li th:each=\"p:products\">${p.name}</li></ul>", ctx));
    }

    @Test
    public void testEachWithMultipleFieldsInBody() {
        DataModel ctx = ctx();
        List<Product> products = new ArrayList<Product>();
        products.add(new Product("Apple", 1.5));
        ctx.set("products", products);
        String out = render(
                "<ul><li th:each=\"p:products\">${p.name}</li></ul>", ctx);
        assertTrue(out.contains("Apple"));
    }

    // ------------------------------------------------------------------ context isolation

    @Test
    public void testLoopVariableDoesNotLeakAfterLoop() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("X");
        ctx.set("items", items);
        ctx.set("i", "before");

        render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx);

        // After render, "i" should be restored to "before"
        assertEquals("before", ctx.get("i"));
    }

    @Test
    public void testOuterContextAccessibleInsideLoop() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("item");
        ctx.set("items", items);
        ctx.set("prefix", ">>"); // outer variable

        String out = render(
                "<ul><li th:each=\"i:items\">${prefix} ${i}</li></ul>", ctx);
        assertEquals("<ul><li>>> item</li></ul>", out);
    }

    // ------------------------------------------------------------------ order

    @Test
    public void testEachPreservesListOrder() {
        DataModel ctx = ctx();
        List<String> items = new ArrayList<String>();
        items.add("first");
        items.add("second");
        items.add("third");
        ctx.set("items", items);
        String out = render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx);
        int posFirst  = out.indexOf("first");
        int posSecond = out.indexOf("second");
        int posThird  = out.indexOf("third");
        assertTrue(posFirst < posSecond);
        assertTrue(posSecond < posThird);
    }
}
