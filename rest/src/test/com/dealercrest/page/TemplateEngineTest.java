package com.dealercrest.page;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dealercrest.page.TemplateEngine;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TemplateEngineTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private TemplateEngine engine;

    @Before
    public void setUp() {
        engine = new TemplateEngine(tmp.getRoot().toPath());
    }

    // ── Helper: write a template file ─────────────────────────────────────────

    private void write(String name, String content) throws Exception {
        File f = new File(tmp.getRoot(), name);
        FileWriter fw = new FileWriter(f);
        fw.write(content);
        fw.close();
    }

    private Map<String, Object> ctx() {
        return new HashMap<String, Object>();
    }

    // =========================================================================
    // 1. LITERAL TEXT
    // =========================================================================

    @Test
    public void testPlainHtmlPassthrough() throws Exception {
        write("plain.html", "<h1>Hello World</h1>");
        String result = engine.process("plain.html", ctx());
        assertEquals("<h1>Hello World</h1>", result);
    }

    @Test
    public void testHtmlCommentPreserved() throws Exception {
        write("comment.html", "<!-- nav -->   <p>hi</p>");
        String result = engine.process("comment.html", ctx());
        assertTrue(result.contains("<!-- nav -->"));
        assertTrue(result.contains("<p>hi</p>"));
    }

    // =========================================================================
    // 2. th:text — Map context
    // =========================================================================

    @Test
    public void testThTextFromMap() throws Exception {
        write("t.html", "<p th:text=\"${name}\"></p>");
        Map<String, Object> data = ctx();
        data.put("name", "Premier Auto");
        String result = engine.process("t.html", data);
        assertEquals("<p>Premier Auto</p>", result);
    }

    @Test
    public void testThTextEscapesHtml() throws Exception {
        write("t.html", "<p th:text=\"${val}\"></p>");
        Map<String, Object> data = ctx();
        data.put("val", "<script>alert('xss')</script>");
        String result = engine.process("t.html", data);
        assertTrue(result.contains("&lt;script&gt;"));
        assertFalse(result.contains("<script>"));
    }

    @Test
    public void testThTextNullRendersEmpty() throws Exception {
        write("t.html", "<p th:text=\"${missing}\"></p>");
        String result = engine.process("t.html", ctx());
        assertEquals("<p></p>", result);
    }

    // =========================================================================
    // 3. th:text — JSONObject context (flat and nested)
    // =========================================================================

    @Test
    public void testThTextFromJsonObject() throws Exception {
        write("t.html", "<h1 th:text=\"${dealerName}\"></h1>");
        JSONObject data = new JSONObject();
        data.put("dealerName", "Premier Auto");
        String result = engine.process("t.html", data);
        assertEquals("<h1>Premier Auto</h1>", result);
    }

    @Test
    public void testThTextNestedJsonDotPath() throws Exception {
        write("t.html", "<span th:text=\"${user.userName}\"></span>");
        JSONObject user = new JSONObject();
        user.put("userName", "Shane");
        JSONObject data = new JSONObject();
        data.put("user", user);
        String result = engine.process("t.html", data);
        assertEquals("<span>Shane</span>", result);
    }

    @Test
    public void testThTextDeepNestedJson() throws Exception {
        write("t.html", "<span th:text=\"${dealer.address.city}\"></span>");
        JSONObject address = new JSONObject();
        address.put("city", "Salt Lake City");
        JSONObject dealer = new JSONObject();
        dealer.put("address", address);
        JSONObject data = new JSONObject();
        data.put("dealer", dealer);
        String result = engine.process("t.html", data);
        assertEquals("<span>Salt Lake City</span>", result);
    }

    @Test
    public void testJsonNullRendersEmpty() throws Exception {
        write("t.html", "<p th:text=\"${user.phone}\"></p>");
        JSONObject user = new JSONObject();
        user.put("phone", JSONObject.NULL);
        JSONObject data = new JSONObject();
        data.put("user", user);
        String result = engine.process("t.html", data);
        assertEquals("<p></p>", result);
    }

    // =========================================================================
    // 4. th:href, th:src, th:alt
    // =========================================================================

    @Test
    public void testThHref() throws Exception {
        write("t.html", "<a th:href=\"${link}\">click</a>");
        Map<String, Object> data = ctx();
        data.put("link", "/inventory");
        String result = engine.process("t.html", data);
        assertEquals("<a href=\"/inventory\">click</a>", result);
    }

    @Test
    public void testThSrcAndAlt() throws Exception {
        write("t.html", "<img th:src=\"${img}\" th:alt=\"${label}\"/>");
        Map<String, Object> data = ctx();
        data.put("img", "/logo.png");
        data.put("label", "Logo");
        String result = engine.process("t.html", data);
        assertTrue(result.contains("src=\"/logo.png\""));
        assertTrue(result.contains("alt=\"Logo\""));
    }

    // =========================================================================
    // 5. th:if / th:unless
    // =========================================================================

    @Test
    public void testThIfTrue() throws Exception {
        write("t.html", "<p th:if=\"${show}\" th:text=\"${msg}\"></p>");
        Map<String, Object> data = ctx();
        data.put("show", true);
        data.put("msg", "Visible");
        String result = engine.process("t.html", data);
        assertEquals("<p>Visible</p>", result);
    }

    @Test
    public void testThIfFalseHidesElement() throws Exception {
        write("t.html", "<p th:if=\"${show}\">Hidden</p>");
        Map<String, Object> data = ctx();
        data.put("show", false);
        String result = engine.process("t.html", data);
        assertEquals("", result.trim());
    }

    @Test
    public void testThUnlessHidesWhenTrue() throws Exception {
        write("t.html", "<p th:unless=\"${loggedIn}\">Please log in</p>");
        Map<String, Object> data = ctx();
        data.put("loggedIn", true);
        String result = engine.process("t.html", data);
        assertEquals("", result.trim());
    }

    @Test
    public void testThUnlessShowsWhenFalse() throws Exception {
        write("t.html", "<p th:unless=\"${loggedIn}\">Please log in</p>");
        Map<String, Object> data = ctx();
        data.put("loggedIn", false);
        String result = engine.process("t.html", data);
        assertTrue(result.contains("Please log in"));
    }

    @Test
    public void testThIfWithJsonBoolean() throws Exception {
        write("t.html", "<p th:if=\"${dealer.active}\" th:text=\"${dealer.name}\"></p>");
        JSONObject dealer = new JSONObject();
        dealer.put("active", true);
        dealer.put("name", "Premier Auto");
        JSONObject data = new JSONObject();
        data.put("dealer", dealer);
        String result = engine.process("t.html", data);
        assertEquals("<p>Premier Auto</p>", result);
    }

    // =========================================================================
    // 6. th:each — List<Map>
    // =========================================================================

    @Test
    public void testThEachOverListOfMaps() throws Exception {
        write("t.html",
            "<ul>" +
            "<li th:each=\"v : ${vehicles}\" th:text=\"${v.make}\"></li>" +
            "</ul>");

        List<Object> vehicles = new ArrayList<Object>();
        Map<String, Object> v1 = new HashMap<String, Object>();
        v1.put("make", "Toyota");
        Map<String, Object> v2 = new HashMap<String, Object>();
        v2.put("make", "Honda");
        vehicles.add(v1);
        vehicles.add(v2);

        Map<String, Object> data = ctx();
        data.put("vehicles", vehicles);

        String result = engine.process("t.html", data);
        assertTrue(result.contains("<li>Toyota</li>"));
        assertTrue(result.contains("<li>Honda</li>"));
    }

    // =========================================================================
    // 7. th:each — JSONArray of JSONObjects
    // =========================================================================

    @Test
    public void testThEachOverJsonArray() throws Exception {
        write("t.html",
            "<ul>" +
            "<li th:each=\"v : ${vehicles}\" th:text=\"${v.make}\"></li>" +
            "</ul>");

        JSONArray vehicles = new JSONArray();
        JSONObject v1 = new JSONObject();
        v1.put("make", "Ford");
        JSONObject v2 = new JSONObject();
        v2.put("make", "Chevy");
        vehicles.put(v1);
        vehicles.put(v2);

        JSONObject data = new JSONObject();
        data.put("vehicles", vehicles);

        String result = engine.process("t.html", data);
        assertTrue(result.contains("<li>Ford</li>"));
        assertTrue(result.contains("<li>Chevy</li>"));
    }

    @Test
    public void testThEachCountIsCorrect() throws Exception {
        write("t.html",
            "<div th:each=\"item : ${items}\" th:text=\"${item.label}\"></div>");

        JSONArray items = new JSONArray();
        for (int i = 1; i <= 5; i++) {
            JSONObject obj = new JSONObject();
            obj.put("label", "Item " + i);
            items.put(obj);
        }

        JSONObject data = new JSONObject();
        data.put("items", items);

        String result = engine.process("t.html", data);
        int count = 0;
        int idx = 0;
        while ((idx = result.indexOf("<div", idx)) != -1) { count++; idx++; }
        assertEquals(5, count);
    }

    @Test
    public void testThEachEmptyListRendersNothing() throws Exception {
        write("t.html",
            "<ul><li th:each=\"v : ${vehicles}\" th:text=\"${v.make}\"></li></ul>");
        Map<String, Object> data = ctx();
        data.put("vehicles", new ArrayList<Object>());
        String result = engine.process("t.html", data);
        assertFalse(result.contains("<li>"));
    }

    // =========================================================================
    // 8. th:each with th:if inside loop
    // =========================================================================

    @Test
    public void testThEachWithInnerThIf() throws Exception {
        write("t.html",
            "<div th:each=\"v : ${vehicles}\">" +
            "<span th:if=\"${v.featured}\" th:text=\"${v.make}\"></span>" +
            "</div>");

        JSONArray vehicles = new JSONArray();
        JSONObject v1 = new JSONObject();
        v1.put("make", "BMW");
        v1.put("featured", true);
        JSONObject v2 = new JSONObject();
        v2.put("make", "Kia");
        v2.put("featured", false);
        vehicles.put(v1);
        vehicles.put(v2);

        JSONObject data = new JSONObject();
        data.put("vehicles", vehicles);

        String result = engine.process("t.html", data);
        assertTrue(result.contains("<span>BMW</span>"));
        assertFalse(result.contains("<span>Kia</span>"));
    }

    // =========================================================================
    // 9. Expressions — ternary, comparison, arithmetic
    // =========================================================================

    @Test
    public void testTernaryTrue() throws Exception {
        write("t.html", "<p th:text=\"${score > 90 ? 'A' : 'B'}\"></p>");
        Map<String, Object> data = ctx();
        data.put("score", 95.0);
        String result = engine.process("t.html", data);
        assertEquals("<p>A</p>", result);
    }

    @Test
    public void testTernaryFalse() throws Exception {
        write("t.html", "<p th:text=\"${score > 90 ? 'A' : 'B'}\"></p>");
        Map<String, Object> data = ctx();
        data.put("score", 75.0);
        String result = engine.process("t.html", data);
        assertEquals("<p>B</p>", result);
    }

    @Test
    public void testStringEquality() throws Exception {
        write("t.html", "<p th:text=\"${status == 'active' ? 'Online' : 'Offline'}\"></p>");
        Map<String, Object> data = ctx();
        data.put("status", "active");
        String result = engine.process("t.html", data);
        assertEquals("<p>Online</p>", result);
    }

    @Test
    public void testArithmeticAddition() throws Exception {
        write("t.html", "<p th:text=\"${price + tax}\"></p>");
        Map<String, Object> data = ctx();
        data.put("price", 20000.0);
        data.put("tax", 1500.0);
        String result = engine.process("t.html", data);
        assertEquals("<p>21500</p>", result);
    }

    @Test
    public void testArithmeticMultiplication() throws Exception {
        write("t.html", "<p th:text=\"${qty * price}\"></p>");
        Map<String, Object> data = ctx();
        data.put("qty", 3.0);
        data.put("price", 100.0);
        String result = engine.process("t.html", data);
        assertEquals("<p>300</p>", result);
    }

    @Test
    public void testStringConcatenation() throws Exception {
        write("t.html", "<p th:text=\"${make + ' ' + model}\"></p>");
        Map<String, Object> data = ctx();
        data.put("make", "Toyota");
        data.put("model", "Camry");
        String result = engine.process("t.html", data);
        assertEquals("<p>Toyota Camry</p>", result);
    }

    // =========================================================================
    // 10. Utility functions
    // =========================================================================

    @Test
    public void testStringsToLowerCase() throws Exception {
        write("t.html", "<p th:text=\"${#strings.toLowerCase(name)}\"></p>");
        Map<String, Object> data = ctx();
        data.put("name", "PREMIER AUTO");
        String result = engine.process("t.html", data);
        assertEquals("<p>premier auto</p>", result);
    }

    @Test
    public void testStringsReplace() throws Exception {
        write("t.html", "<p th:text=\"${#strings.replace(slug, '-', ' ')}\"></p>");
        Map<String, Object> data = ctx();
        data.put("slug", "toyota-camry-2024");
        String result = engine.process("t.html", data);
        assertEquals("<p>toyota camry 2024</p>", result);
    }

    @Test
    public void testNumbersFormatInteger() throws Exception {
        write("t.html", "<p th:text=\"${#numbers.formatInteger(price, 1, 'COMMA')}\"></p>");
        Map<String, Object> data = ctx();
        data.put("price", 35000.0);
        String result = engine.process("t.html", data);
        assertEquals("<p>35,000</p>", result);
    }

    @Test
    public void testListsIsEmpty() throws Exception {
        write("t.html", "<p th:text=\"${#lists.isEmpty(items)}\"></p>");
        Map<String, Object> data = ctx();
        data.put("items", new ArrayList<Object>());
        String result = engine.process("t.html", data);
        assertEquals("<p>true</p>", result);
    }

    @Test
    public void testListsSize() throws Exception {
        write("t.html", "<p th:text=\"${#lists.size(items)}\"></p>");
        List<Object> items = new ArrayList<Object>();
        items.add("a");
        items.add("b");
        items.add("c");
        Map<String, Object> data = ctx();
        data.put("items", items);
        String result = engine.process("t.html", data);
        assertEquals("<p>3</p>", result);
    }

    // =========================================================================
    // 11. Void / self-closing tags
    // =========================================================================

    @Test
    public void testImgIsSelfClosing() throws Exception {
        write("t.html", "<img th:src=\"${url}\" th:alt=\"${label}\"/>");
        Map<String, Object> data = ctx();
        data.put("url", "/car.jpg");
        data.put("label", "Car photo");
        String result = engine.process("t.html", data);
        assertTrue(result.contains("src=\"/car.jpg\""));
        assertTrue(result.contains("alt=\"Car photo\""));
        assertFalse(result.contains("</img>"));
    }

    @Test
    public void testInputIsSelfClosing() throws Exception {
        write("t.html", "<input type=\"text\" th:style=\"${style}\"/>");
        Map<String, Object> data = ctx();
        data.put("style", "color:red");
        String result = engine.process("t.html", data);
        assertFalse(result.contains("</input>"));
        assertTrue(result.contains("style=\"color:red\""));
    }

    // =========================================================================
    // 12. Template caching
    // =========================================================================

    @Test
    public void testTemplateIsCached() throws Exception {
        write("cached.html", "<p th:text=\"${msg}\"></p>");
        Map<String, Object> data = ctx();
        data.put("msg", "First");
        String r1 = engine.process("cached.html", data);

        // Change the file on disk — cached engine should still use old parse
        write("cached.html", "<div>CHANGED</div>");
        data.put("msg", "Second");
        String r2 = engine.process("cached.html", data);

        assertEquals("<p>First</p>", r1);
        // Cached — still uses parsed segments from first load
        assertEquals("<p>Second</p>", r2);
    }

    // =========================================================================
    // 13. exists()
    // =========================================================================

    @Test
    public void testExistsReturnsTrueForKnownTemplate() throws Exception {
        write("home.html", "<p>Home</p>");
        assertTrue(engine.exists("home.html"));
    }

    @Test
    public void testExistsReturnsFalseForMissing() {
        assertFalse(engine.exists("does-not-exist.html"));
    }

    @Test
    public void testExistsResolvesWithoutExtension() throws Exception {
        write("vehicle.html", "<p>Vehicle</p>");
        assertTrue(engine.exists("vehicle")); // resolves to vehicle.html
    }

    // =========================================================================
    // 14. JSONObject convenience overload
    // =========================================================================

    @Test
    public void testProcessWithJsonObjectOverload() throws Exception {
        write("t.html", "<h1 th:text=\"${dealer.name}\"></h1>");
        JSONObject dealer = new JSONObject();
        dealer.put("name", "Premier Auto");
        JSONObject data = new JSONObject();
        data.put("dealer", dealer);
        String result = engine.process("t.html", data);
        assertEquals("<h1>Premier Auto</h1>", result);
    }

    // =========================================================================
    // 15. jsonObjectToMap / jsonArrayToList helpers
    // =========================================================================

    @Test
    public void testJsonObjectToMapFlattensTopLevel() {
        JSONObject json = new JSONObject();
        json.put("name", "Shane");
        json.put("age", 30);
        Map<String, Object> map = TemplateEngine.jsonObjectToMap(json);
        assertEquals("Shane", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    public void testJsonObjectToMapHandlesNull() {
        JSONObject json = new JSONObject();
        json.put("phone", JSONObject.NULL);
        Map<String, Object> map = TemplateEngine.jsonObjectToMap(json);
        assertNull(map.get("phone"));
    }

    @Test
    public void testJsonArrayToListPreservesOrder() {
        JSONArray arr = new JSONArray();
        arr.put("Ford");
        arr.put("Toyota");
        arr.put("Honda");
        List<Object> list = TemplateEngine.jsonArrayToList(arr);
        assertEquals(3, list.size());
        assertEquals("Ford",   list.get(0));
        assertEquals("Toyota", list.get(1));
        assertEquals("Honda",  list.get(2));
    }

    // =========================================================================
    // 16. Mixed context — JSON static + List<Map> dynamic
    // =========================================================================

    @Test
    public void testMixedJsonAndListContext() throws Exception {
        write("t.html",
            "<h1 th:text=\"${dealerName}\"></h1>" +
            "<ul><li th:each=\"v : ${vehicles}\" th:text=\"${v.make}\"></li></ul>");

        // Static data from JSON
        JSONObject page = new JSONObject();
        page.put("dealerName", "Premier Auto");
        Map<String, Object> data = TemplateEngine.jsonObjectToMap(page);

        // Dynamic data from DB as List<Map>
        List<Object> vehicles = new ArrayList<Object>();
        Map<String, Object> v1 = new HashMap<String, Object>();
        v1.put("make", "Tesla");
        Map<String, Object> v2 = new HashMap<String, Object>();
        v2.put("make", "Rivian");
        vehicles.add(v1);
        vehicles.add(v2);
        data.put("vehicles", vehicles);

        String result = engine.process("t.html", data);
        assertTrue(result.contains("<h1>Premier Auto</h1>"));
        assertTrue(result.contains("<li>Tesla</li>"));
        assertTrue(result.contains("<li>Rivian</li>"));
    }

    // =========================================================================
    // 17. Edge cases
    // =========================================================================

    @Test
    public void testMissingTemplateThrows() {
        try {
            engine.process("no-such-template.html", ctx());
            fail("Expected exception for missing template");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Template not found"));
        }
    }

    @Test
    public void testStaticAttributesPreserved() throws Exception {
        write("t.html", "<div class=\"card\" id=\"main\"><p>Hello</p></div>");
        String result = engine.process("t.html", ctx());
        assertTrue(result.contains("class=\"card\""));
        assertTrue(result.contains("id=\"main\""));
    }

    @Test
    public void testNestedElementsRenderCorrectly() throws Exception {
        write("t.html",
            "<div>" +
            "<h1 th:text=\"${title}\"></h1>" +
            "<p th:text=\"${body}\"></p>" +
            "</div>");
        Map<String, Object> data = ctx();
        data.put("title", "Inventory");
        data.put("body", "Browse our vehicles");
        String result = engine.process("t.html", data);
        assertTrue(result.contains("<h1>Inventory</h1>"));
        assertTrue(result.contains("<p>Browse our vehicles</p>"));
    }

    @Test
    public void testBooleanLiteralInExpression() throws Exception {
        write("t.html", "<p th:if=\"true\" th:text=\"${msg}\"></p>");
        Map<String, Object> data = ctx();
        data.put("msg", "Always shown");
        String result = engine.process("t.html", data);
        assertEquals("<p>Always shown</p>", result);
    }

    @Test
    public void testStringLiteralInThText() throws Exception {
        write("t.html", "<p th:text=\"'Hello Dealer'\"></p>");
        String result = engine.process("t.html", ctx());
        assertEquals("<p>Hello Dealer</p>", result);
    }
}
