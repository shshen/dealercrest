package com.dealercrest.db;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilterRegistry {
    private static final FilterRegistry INSTANCE = new FilterRegistry();

    private final Map<String, FilterDefinition> definitions = new LinkedHashMap<>();

    private FilterRegistry() {
        register("condition", "i.condition", "=", String.class);
        register("make", "i.make", "=", String.class);
        register("model", "i.model", "=", String.class);
        register("priceMax", "i.price", "<=", BigDecimal.class);
        register("priceMin", "i.price", ">=", BigDecimal.class);
        register("yearMin", "i.year", ">=", Integer.class);
        register("yearMax", "i.year", "<=", Integer.class);
        register("mileageMax", "i.mileage", "<=", Integer.class);
    }

    public static FilterRegistry getInstance() {
        return INSTANCE;
    }

    private void register(String key, String column, String op, Class<?> type) {
        definitions.put(key, new FilterDefinition(key, column, op, type));
    }

    public FilterDefinition get(String key) {
        return definitions.get(key);
    }

    public boolean contains(String key) {
        return definitions.containsKey(key);
    }
}
