package com.dealercrest.db;

public class FilterDefinition {

    private final String key;
    private final String column;
    private final String op;
    private final Class<?> type;

    public FilterDefinition(String key, String column, String op, Class<?> type) {
        this.key = key;
        this.column = column;
        this.op = op;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public String getColumn() {
        return column;
    }

    public String getOp() {
        return op;
    }

    public Class<?> getType() {
        return type;
    }
}
