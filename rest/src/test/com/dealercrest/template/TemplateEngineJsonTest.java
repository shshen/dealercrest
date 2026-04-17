package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for JSONObject / JSONArray support through TemplateEngine.
 *
 * Covers: top-level JSONObject property access, nested JSONObject, JSONObject
 * in a List, JSONArray iteration, and JSONObject boolean in th:if.
 */
public class TemplateEngineJsonTest {

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

    // ------------------------------------------------------------------ JSONObject property access

    @Test
    public void testJsonObjectStringProperty() {
        JSONObject obj = new JSONObject();
        obj.put("city", "Tokyo");
        DataModel ctx = ctx();
        ctx.set("place", obj);
        assertEquals("<p>Tokyo</p>", render("<p>${place.city}</p>", ctx));
    }

    @Test
    public void testJsonObjectIntegerProperty() {
        JSONObject obj = new JSONObject();
        obj.put("count", 5);
        DataModel ctx = ctx();
        ctx.set("data", obj);
        assertEquals("<p>5</p>", render("<p>${data.count}</p>", ctx));
    }

    @Test
    public void testJsonObjectMissingPropertyRendersEmpty() {
        JSONObject obj = new JSONObject();
        DataModel ctx = ctx();
        ctx.set("data", obj);
        assertEquals("<p></p>", render("<p>${data.missing}</p>", ctx));
    }

    // ------------------------------------------------------------------ nested JSONObject

    @Test
    public void testNestedJsonObjectTwoLevels() {
        JSONObject address = new JSONObject();
        address.put("street", "Main St");

        JSONObject person = new JSONObject();
        person.put("name",    "Alice");
        person.put("address", address);

        DataModel ctx = ctx();
        ctx.set("person", person);

        assertEquals("<p>Alice</p>",
                render("<p>${person.name}</p>", ctx));
        assertEquals("<p>Main St</p>",
                render("<p>${person.address.street}</p>", ctx));
    }

    // ------------------------------------------------------------------ JSONObject in List (th:each)

    @Test
    public void testJsonObjectsInListIteration() {
        JSONObject v1 = new JSONObject(); v1.put("name", "Toyota");
        JSONObject v2 = new JSONObject(); v2.put("name", "Honda");

        List<JSONObject> list = new ArrayList<JSONObject>();
        list.add(v1);
        list.add(v2);

        DataModel ctx = ctx();
        ctx.set("vehicles", list);

        assertEquals("<ul><li>Toyota</li><li>Honda</li></ul>",
                render("<ul><li th:each=\"v:vehicles\">${v.name}</li></ul>", ctx));
    }

    @Test
    public void testJsonObjectMultiplePropertiesInLoop() {
        JSONObject row = new JSONObject();
        row.put("id",   "001");
        row.put("label", "Item A");

        List<JSONObject> rows = new ArrayList<JSONObject>();
        rows.add(row);

        DataModel ctx = ctx();
        ctx.set("rows", rows);

        String out = render(
                "<ul><li th:each=\"r:rows\">${r.id}: ${r.label}</li></ul>", ctx);
        assertEquals("<ul><li>001: Item A</li></ul>", out);
    }

    // ------------------------------------------------------------------ JSONArray (th:each)

    @Test
    public void testJsonArrayIteration() {
        JSONObject v1 = new JSONObject(); v1.put("name", "Ford");
        JSONObject v2 = new JSONObject(); v2.put("name", "Chevy");

        JSONArray arr = new JSONArray();
        arr.put(v1);
        arr.put(v2);

        DataModel ctx = ctx();
        ctx.set("vehicles", arr);

        assertEquals("<ul><li>Ford</li><li>Chevy</li></ul>",
                render("<ul><li th:each=\"v:vehicles\">${v.name}</li></ul>", ctx));
    }

    @Test
    public void testEmptyJsonArrayProducesNoChildren() {
        DataModel ctx = ctx();
        ctx.set("items", new JSONArray());
        assertEquals("<ul></ul>",
                render("<ul><li th:each=\"i:items\">${i}</li></ul>", ctx));
    }

    @Test
    public void testJsonArrayPreservesOrder() {
        JSONArray arr = new JSONArray();
        arr.put("first");
        arr.put("second");
        arr.put("third");

        DataModel ctx = ctx();
        ctx.set("words", arr);

        String out = render(
                "<ul><li th:each=\"w:words\">${w}</li></ul>", ctx);
        int p1 = out.indexOf("first");
        int p2 = out.indexOf("second");
        int p3 = out.indexOf("third");
        assertTrue(p1 < p2);
        assertTrue(p2 < p3);
    }

    // ------------------------------------------------------------------ JSONObject in th:if

    @Test
    public void testJsonObjectBooleanTrueIsConditionallyShown() {
        JSONObject obj = new JSONObject();
        obj.put("active", Boolean.TRUE);

        DataModel ctx = ctx();
        ctx.set("item", obj);

        assertEquals("<div>Active</div>",
                render("<div th:if=\"${item.active}\">Active</div>", ctx));
    }

    @Test
    public void testJsonObjectBooleanFalseIsConditionallyHidden() {
        JSONObject obj = new JSONObject();
        obj.put("active", Boolean.FALSE);

        DataModel ctx = ctx();
        ctx.set("item", obj);

        assertEquals("",
                render("<div th:if=\"${item.active}\">Active</div>", ctx));
    }

    @Test
    public void testJsonObjectNullPropertyIsFalsy() {
        JSONObject obj = new JSONObject();
        // "status" not set → opt() returns null

        DataModel ctx = ctx();
        ctx.set("item", obj);

        assertEquals("",
                render("<div th:if=\"${item.status}\">Status</div>", ctx));
    }
}
