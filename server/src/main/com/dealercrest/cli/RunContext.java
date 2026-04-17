package com.dealercrest.cli;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RunContext {

    private final String path;
    private final List<Option> options;
    private final Map<String, String> longNameValues;
    private final EnvResolver envResolver = new EnvResolver();

    public RunContext(String path, List<Option> options, Map<String, String> values) {
        this.path = path;
        this.options = options;
        this.longNameValues = buildLongNameValues(values);
    }

    private Map<String,String> buildLongNameValues(Map<String, String> values) {
        Map<String,String> map = new LinkedHashMap<>();
        for(Option o: options) {
            String shortName = o.shortName();
            String longName = o.longName();
            String v = values.getOrDefault(shortName, "");
            if ( v.isEmpty()) {
                v = values.getOrDefault(longName, "");
            }
            if (v.isEmpty() && !o.defaultValue().isEmpty()) {
                String dValue = o.defaultValue();
                dValue = envResolver.resolve(dValue);
                v = dValue;
            }
            if ( o.type() == boolean.class &&
                (  values.containsKey(shortName ) || values.containsKey(longName)) ) {
                v = "true";
            }
            map.put(longName, v);
        }
        return map;
    }

    public String getPath() {
        return path;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String get(String longName) {
        return longNameValues.getOrDefault(longName, "");
    }

    public boolean has(String longName) {
        return longNameValues.containsKey(longName);
    }

    public int getInt(String name) {
        String v = longNameValues.get(name);
        return Integer.parseInt(v);
    }

    public boolean getBool(String name) {
        return "true".equalsIgnoreCase(longNameValues.get(name));
    }

    public boolean isEmpty() {
        Collection<String> xxx = longNameValues.values();
        for(String x: xxx) {
            if ( x!=null && !x.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Map<String,String> getConfig() {
        return Collections.unmodifiableMap(longNameValues);
    }
}
