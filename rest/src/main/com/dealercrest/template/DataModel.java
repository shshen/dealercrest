package com.dealercrest.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Context stores runtime variables used during rendering.
 *
 * Supports plain Java objects and org.json.JSONObject values.
 *
 * When a variable value is a JSONObject, property traversal in
 * VariableExpression delegates to PropertyAccessor.get(), which calls
 * JSONObject.opt(name) directly — no reflection involved.
 *
 * Usage with JSONObject:
 *
 *   JSONObject vehicle = new JSONObject();
 *   vehicle.put("name", "Toyota");
 *   vehicle.put("type", "Sedan");
 *
 *   Context ctx = new Context();
 *   ctx.set("vehicle", vehicle);
 *
 *   // Template: ${vehicle.name}  →  "Toyota"
 *
 * Usage with a JSONArray list for th:each:
 *
 *   JSONArray list = new JSONArray();
 *   list.put(vehicle1);
 *   list.put(vehicle2);
 *   ctx.set("vehicles", list);
 *
 *   // Template: th:each="v:vehicles"  iterates the JSONArray
 *   //           ${v.name} resolves via JSONObject.opt("name")
 *
 * PERFORMANCE:
 *   Single flat HashMap — no stack allocation per th:each iteration.
 *   saveAndSet()/restore() write into the existing map and undo the change
 *   after each iteration, with zero extra object allocation.
 */
public class DataModel {

    private final Map<String, Object> map;

    public DataModel() {
        this.map = new HashMap<>();
    }

    /**
     * Set a top-level variable.  Value may be any Object including
     * JSONObject, JSONArray, List, or a plain POJO.
     */
    public DataModel set(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public DataModel setAll(Map<String, String> values) {
        map.putAll(values);
        return this;
    }

    /**
     * Retrieve a variable by name.
     */
    public Object get(String key) {
        return map.get(key);
    }

    /**
     * Set a scoped variable for the duration of one loop iteration,
     * saving whatever was bound to that name before.
     *
     * The returned SavedValue MUST be passed to restore() after the iteration.
     */
    public SavedValue saveAndSet(String key, Object value) {
        Object  previous = map.get(key);
        boolean existed  = map.containsKey(key);
        map.put(key, value);
        return new SavedValue(key, previous, existed);
    }

    /**
     * Restore a variable to the state it was in before saveAndSet().
     */
    public void restore(SavedValue saved) {
        if (saved.existed) {
            map.put(saved.key, saved.previous);
        } else {
            map.remove(saved.key);
        }
    }

    /**
     * Captures the pre-save state of a single variable slot.
     */
    public static class SavedValue {
        final String  key;
        final Object  previous;
        final boolean existed;

        SavedValue(String key, Object previous, boolean existed) {
            this.key      = key;
            this.previous = previous;
            this.existed  = existed;
        }
    }
}
