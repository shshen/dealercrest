package com.dealercrest.rest;

import org.junit.Test;

import com.dealercrest.rest.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MultiValueMapTest {

    // -------------------------------------------------------------------------
    // Constructor - empty / null
    // -------------------------------------------------------------------------

    @Test
    public void testDefaultConstructorIsEmpty() {
        MultiValueMap map = new MultiValueMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testNullQueryStringIsEmpty() {
        MultiValueMap map = new MultiValueMap(null);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testEmptyQueryStringIsEmpty() {
        MultiValueMap map = new MultiValueMap("");
        assertTrue(map.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Basic parsing
    // -------------------------------------------------------------------------

    @Test
    public void testSingleParam() {
        MultiValueMap map = new MultiValueMap("page=1");
        assertEquals("1", map.getFirst("page"));
    }

    @Test
    public void testMultipleParams() {
        MultiValueMap map = new MultiValueMap("page=1&limit=20&order=asc");
        assertEquals("1", map.getFirst("page"));
        assertEquals("20", map.getFirst("limit"));
        assertEquals("asc", map.getFirst("order"));
        assertEquals(3, map.size());
    }

    @Test
    public void testMultiValueKey() {
        MultiValueMap map = new MultiValueMap("color=red&color=blue&color=green");
        List<String> colors = map.get("color");
        assertEquals(3, colors.size());
        assertEquals("red", colors.get(0));
        assertEquals("blue", colors.get(1));
        assertEquals("green", colors.get(2));
    }

    @Test
    public void testGetFirstReturnsFirstValue() {
        MultiValueMap map = new MultiValueMap("color=red&color=blue");
        assertEquals("red", map.getFirst("color"));
    }

    @Test
    public void testMixedSingleAndMultiValue() {
        MultiValueMap map = new MultiValueMap("page=2&color=red&color=blue");
        assertEquals("2", map.getFirst("page"));
        assertEquals(2, map.get("color").size());
    }

    // -------------------------------------------------------------------------
    // Missing keys and defaults
    // -------------------------------------------------------------------------

    @Test
    public void testGetFirstMissingKeyReturnsNull() {
        MultiValueMap map = new MultiValueMap("page=1");
        assertNull(map.getFirst("missing"));
    }

    @Test
    public void testGetFirstWithDefaultReturnsFallback() {
        MultiValueMap map = new MultiValueMap("page=1");
        assertEquals("10", map.getFirst("limit", "10"));
    }

    @Test
    public void testGetFirstWithDefaultReturnsValueWhenPresent() {
        MultiValueMap map = new MultiValueMap("limit=50");
        assertEquals("50", map.getFirst("limit", "10"));
    }

    @Test
    public void testGetMissingKeyReturnsEmptyList() {
        MultiValueMap map = new MultiValueMap("page=1");
        List<String> result = map.get("missing");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testContainsKeyTrue() {
        MultiValueMap map = new MultiValueMap("page=1");
        assertTrue(map.containsKey("page"));
    }

    @Test
    public void testContainsKeyFalse() {
        MultiValueMap map = new MultiValueMap("page=1");
        assertFalse(map.containsKey("limit"));
    }

    // -------------------------------------------------------------------------
    // URL decoding
    // -------------------------------------------------------------------------

    @Test
    public void testDecodesPercentEncoding() {
        MultiValueMap map = new MultiValueMap("q=hello%20world");
        assertEquals("hello world", map.getFirst("q"));
    }

    @Test
    public void testDecodesPlusAsSpace() {
        MultiValueMap map = new MultiValueMap("q=hello+world");
        assertEquals("hello world", map.getFirst("q"));
    }

    @Test
    public void testDecodesSpecialCharacters() {
        MultiValueMap map = new MultiValueMap("redirect=https%3A%2F%2Fexample.com%2Fpath");
        assertEquals("https://example.com/path", map.getFirst("redirect"));
    }

    @Test
    public void testDecodesKeyAndValue() {
        MultiValueMap map = new MultiValueMap("my%20key=my%20value");
        assertEquals("my value", map.getFirst("my key"));
    }

    // -------------------------------------------------------------------------
    // Edge cases in query string format
    // -------------------------------------------------------------------------

    @Test
    public void testKeyWithNoValueGetsEmptyString() {
        MultiValueMap map = new MultiValueMap("flag");
        assertEquals("", map.getFirst("flag"));
    }

    @Test
    public void testKeyWithEqualsButNoValue() {
        MultiValueMap map = new MultiValueMap("flag=");
        assertEquals("", map.getFirst("flag"));
    }

    @Test
    public void testSkipsEmptyKeyFromLeadingAmpersand() {
        MultiValueMap map = new MultiValueMap("&page=1");
        assertFalse(map.containsKey(""));
        assertEquals("1", map.getFirst("page"));
    }

    @Test
    public void testSkipsEmptyKeyFromDoubleAmpersand() {
        MultiValueMap map = new MultiValueMap("page=1&&limit=10");
        assertFalse(map.containsKey(""));
        assertEquals("1", map.getFirst("page"));
        assertEquals("10", map.getFirst("limit"));
    }

    @Test
    public void testFragmentIsStripped() {
        MultiValueMap map = new MultiValueMap("page=1&limit=10#section2");
        assertEquals("1", map.getFirst("page"));
        assertEquals("10", map.getFirst("limit"));
        assertFalse(map.containsKey("section2"));
        assertEquals(2, map.size());
    }

    // -------------------------------------------------------------------------
    // Manual add
    // -------------------------------------------------------------------------

    @Test
    public void testManualAdd() {
        MultiValueMap map = new MultiValueMap();
        map.add("page", "1");
        map.add("color", "red");
        map.add("color", "blue");
        assertEquals("1", map.getFirst("page"));
        assertEquals(2, map.get("color").size());
    }

    // -------------------------------------------------------------------------
    // toFlatMap
    // -------------------------------------------------------------------------

    @Test
    public void testToFlatMapUsesFirstValue() {
        MultiValueMap map = new MultiValueMap("page=1&color=red&color=blue");
        Map<String, String> flat = map.toFlatMap();
        assertEquals("1", flat.get("page"));
        assertEquals("red", flat.get("color"));
        assertEquals(2, flat.size());
    }

    @Test
    public void testToFlatMapOnEmptyIsEmpty() {
        MultiValueMap map = new MultiValueMap();
        assertTrue(map.toFlatMap().isEmpty());
    }
}
