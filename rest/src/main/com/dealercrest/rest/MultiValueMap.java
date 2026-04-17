package com.dealercrest.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiValueMap {

    private final Map<String, List<String>> map;

    public MultiValueMap() {
        this.map = new LinkedHashMap<>();
    }

    public MultiValueMap(String queryString) {
        this.map = new LinkedHashMap<>();
        
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        String cleaned = queryString;
        int hash = cleaned.indexOf('#');
        if (hash != -1) {
            cleaned = cleaned.substring(0, hash);
        }
        parse(cleaned);
    }

    private void parse(String queryString) {
        int len = queryString.length();
        int start = 0;

        while (start < len) {
            int end = queryString.indexOf('&', start);
            if (end == -1) {
                end = len;
            }

            String pair = queryString.substring(start, end);
            int eq = pair.indexOf('=');

            String key;
            String value;

            if (eq == -1) {
                key = decode(pair);
                value = "";
            } else {
                key = decode(pair.substring(0, eq));
                value = decode(pair.substring(eq + 1));
            }

            if (!key.isEmpty()) {
                add(key, value);
            }

            start = end + 1;
        }
    }

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public void add(String key, String value) {
        List<String> values = map.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            map.put(key, values);
        }
        values.add(value);
    }

    public void add(String key, List<String> value) {
        List<String> values = map.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            map.put(key, values);
        }
        values.addAll(values);
    }

    public String getFirst(String key) {
        List<String> values = map.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    public String getFirst(String key, String defaultValue) {
        String value = getFirst(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public List<String> get(String key) {
        List<String> values = map.get(key);
        if (values == null) {
            return Collections.emptyList();
        }
        return values;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Map<String, List<String>> toMap() {
        return Collections.unmodifiableMap(map);
    }

    public Map<String, String> toFlatMap() {
        Map<String, String> flat = new LinkedHashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                flat.put(entry.getKey(), values.get(0));
            }
        }
        return flat;
    }
}
