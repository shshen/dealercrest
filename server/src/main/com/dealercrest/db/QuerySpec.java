package com.dealercrest.db;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dealercrest.rest.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerySpec {

    private int page;
    private int limit;
    private String sort;
    private MultiValueMap filters;

    public QuerySpec() {
        this.filters = new MultiValueMap();
        this.sort = null;
        this.limit = 0;
        this.page = 1;
    }

    public static QuerySpec fromJson(JSONObject json) {
        QuerySpec spec = new QuerySpec();

        if (json.has("sort")) {
            spec.sort = json.getString("sort");
        }
        if (json.has("limit")) {
            spec.limit = json.getInt("limit");
        }

        MultiValueMap filters = new MultiValueMap();
        for (String key : json.keySet()) {
            if (key.equals("sort") || key.equals("limit")) {
                continue;
            }
            Object value = json.get(key);
            List<String> values = new ArrayList<String>();
            if (value instanceof JSONArray) {
                JSONArray arr = (JSONArray) value;
                for (int i = 0; i < arr.length(); i++) {
                    values.add(arr.getString(i));
                }
            } else {
                values.add(value.toString());
            }
            filters.add(key, values);
        }
        spec.filters = filters;
        return spec;
    }

    public static QuerySpec fromRequestParams(Map<String, List<String>> params) {
        QuerySpec spec = new QuerySpec();

        if (params.containsKey("sort")) {
            spec.sort = params.get("sort").get(0);
        }
        if (params.containsKey("limit")) {
            spec.limit = Integer.parseInt(params.get("limit").get(0));
        }
        if (params.containsKey("page")) {
            spec.page = Integer.parseInt(params.get("page").get(0));
        }

        MultiValueMap filters = new MultiValueMap();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.equals("sort") || key.equals("limit") || key.equals("page")) {
                continue;
            }
            filters.add(key, new ArrayList<>(entry.getValue()));
        }
        spec.filters = filters;
        return spec;
    }

    public static QuerySpec merge(QuerySpec blockSpec, QuerySpec requestSpec) {
        QuerySpec resolved = new QuerySpec();
        return resolved;
    }

    public MultiValueMap getFilters() {
        return filters;
    }

    public String getSort() {
        return sort;
    }

    public int getLimit() {
        return limit;
    }

    public int getPage() {
        return page;
    }
}
