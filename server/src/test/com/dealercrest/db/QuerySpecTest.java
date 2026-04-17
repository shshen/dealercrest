package com.dealercrest.db;

import org.json.JSONObject;
import org.junit.Test;

import com.dealercrest.db.QuerySpec;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class QuerySpecTest {

    // --- fromJson ---

    @Test
    public void testFromJson_singleValueFilter() {
        JSONObject json = new JSONObject();
        json.put("sort", "price_asc");
        json.put("limit", 10);
        json.put("make", "Toyota");

        QuerySpec spec = QuerySpec.fromJson(json);

        assertEquals("price_asc", spec.getSort());
        assertEquals(10, spec.getLimit());
        assertEquals(Arrays.asList("Toyota"), spec.getFilters().get("make"));
    }

    @Test
    public void testFromJson_multiValueFilter() {
        JSONObject json = new JSONObject();
        json.put("bodyStyle", new org.json.JSONArray(Arrays.asList("SUV", "Truck")));

        QuerySpec spec = QuerySpec.fromJson(json);

        assertEquals(Arrays.asList("SUV", "Truck"), spec.getFilters().get("bodyStyle"));
    }

    @Test
    public void testFromJson_missingOptionalFields() {
        JSONObject json = new JSONObject();

        QuerySpec spec = QuerySpec.fromJson(json);

        assertNull(spec.getSort());
        assertEquals(0, spec.getLimit());
        assertTrue(spec.getFilters().isEmpty());
    }

    // --- fromRequestParams ---

    @Test
    public void testFromRequestParams_singleValueFilter() {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("sort", Arrays.asList("price_desc"));
        params.put("limit", Arrays.asList("20"));
        params.put("page", Arrays.asList("2"));
        params.put("make", Arrays.asList("Honda"));

        QuerySpec spec = QuerySpec.fromRequestParams(params);

        assertEquals("price_desc", spec.getSort());
        assertEquals(20, spec.getLimit());
        assertEquals(2, spec.getPage());
        assertEquals(Arrays.asList("Honda"), spec.getFilters().get("make"));
    }

    @Test
    public void testFromRequestParams_multiValueFilter() {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("bodyStyle", Arrays.asList("SUV", "Truck"));

        QuerySpec spec = QuerySpec.fromRequestParams(params);

        assertEquals(Arrays.asList("SUV", "Truck"), spec.getFilters().get("bodyStyle"));
    }

    @Test
    public void testFromRequestParams_reservedKeysNotInFilters() {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("sort", Arrays.asList("price_asc"));
        params.put("limit", Arrays.asList("10"));
        params.put("page", Arrays.asList("1"));

        QuerySpec spec = QuerySpec.fromRequestParams(params);

        assertFalse(spec.getFilters().containsKey("sort"));
        assertFalse(spec.getFilters().containsKey("limit"));
        assertFalse(spec.getFilters().containsKey("page"));
    }

    @Test
    public void testFromRequestParams_emptyParams() {
        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();

        QuerySpec spec = QuerySpec.fromRequestParams(params);

        assertNull(spec.getSort());
        assertEquals(0, spec.getLimit());
        assertEquals(1, spec.getPage());
        assertTrue(spec.getFilters().isEmpty());
    }

    // --- merge ---

    @Test
    public void testMerge_requestOverridesSort() {
        JSONObject blockJson = new JSONObject();
        blockJson.put("sort", "price_asc");
        QuerySpec blockSpec = QuerySpec.fromJson(blockJson);

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("sort", Arrays.asList("price_desc"));
        QuerySpec requestSpec = QuerySpec.fromRequestParams(params);

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals("price_desc", resolved.getSort());
    }

    @Test
    public void testMerge_blockSortUsedWhenRequestHasNone() {
        JSONObject blockJson = new JSONObject();
        blockJson.put("sort", "price_asc");
        QuerySpec blockSpec = QuerySpec.fromJson(blockJson);

        QuerySpec requestSpec = QuerySpec.fromRequestParams(new LinkedHashMap<String, List<String>>());

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals("price_asc", resolved.getSort());
    }

    @Test
    public void testMerge_blockLimitUsedWhenRequestHasNone() {
        JSONObject blockJson = new JSONObject();
        blockJson.put("limit", 10);
        QuerySpec blockSpec = QuerySpec.fromJson(blockJson);

        QuerySpec requestSpec = QuerySpec.fromRequestParams(new LinkedHashMap<String, List<String>>());

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals(10, resolved.getLimit());
    }

    @Test
    public void testMerge_requestFilterOverridesBlockFilter() {
        JSONObject blockJson = new JSONObject();
        blockJson.put("make", "Toyota");
        QuerySpec blockSpec = QuerySpec.fromJson(blockJson);

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("make", Arrays.asList("BMW"));
        QuerySpec requestSpec = QuerySpec.fromRequestParams(params);

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals(Arrays.asList("BMW"), resolved.getFilters().get("make"));
    }

    @Test
    public void testMerge_blockFilterPreservedWhenRequestHasNoOverride() {
        JSONObject blockJson = new JSONObject();
        blockJson.put("make", "Toyota");
        QuerySpec blockSpec = QuerySpec.fromJson(blockJson);

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("bodyStyle", Arrays.asList("SUV", "Truck"));
        QuerySpec requestSpec = QuerySpec.fromRequestParams(params);

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals(Arrays.asList("Toyota"), resolved.getFilters().get("make"));
        assertEquals(Arrays.asList("SUV", "Truck"), resolved.getFilters().get("bodyStyle"));
    }

    @Test
    public void testMerge_requestMultiValueFilter() {
        QuerySpec blockSpec = QuerySpec.fromJson(new JSONObject());

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("bodyStyle", Arrays.asList("SUV", "Truck"));
        QuerySpec requestSpec = QuerySpec.fromRequestParams(params);

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals(Arrays.asList("SUV", "Truck"), resolved.getFilters().get("bodyStyle"));
    }

    @Test
    public void testMerge_pageComesFromRequest() {
        QuerySpec blockSpec = QuerySpec.fromJson(new JSONObject());

        Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
        params.put("page", Arrays.asList("3"));
        QuerySpec requestSpec = QuerySpec.fromRequestParams(params);

        QuerySpec resolved = QuerySpec.merge(blockSpec, requestSpec);

        assertEquals(3, resolved.getPage());
    }
}
