package com.dealercrest.template;

import java.util.HashMap;
import java.util.Map;

/**
 * Model stores the per-request data passed to TemplateEngine.render*().
 *
 * It carries two kinds of per-request state:
 *
 *   1. DATA VARIABLES — any named values (POJOs, JSONObject, List, etc.)
 *      set via set() and read by expression evaluation during rendering.
 *
 *   2. FRAGMENT REGISTRY — a per-page FragmentRegistry set via
 *      setFragments().  Different pages can register completely different
 *      fragment sets without interfering with each other or with the
 *      shared TemplateEngine.
 *
 * Typical per-page usage:
 *
 *   // Page A — vehicle list page
 *   FragmentRegistry pageAFragments = new FragmentRegistry();
 *   pageAFragments.register("header", "<header>Vehicles</header>");
 *   pageAFragments.register("nav",    "<nav>...</nav>");
 *
 *   Model modelA = new Model();
 *   modelA.set("vehicles", vehicleList);
 *   modelA.setFragments(pageAFragments);
 *
 *   engine.renderFile("templates/vehicles.html", modelA);
 *
 *   // Page B — user profile page, completely different fragments
 *   FragmentRegistry pageBFragments = new FragmentRegistry();
 *   pageBFragments.register("header", "<header>Profile</header>");
 *   pageBFragments.register("nav",    "<nav>...</nav>");
 *
 *   Model modelB = new Model();
 *   modelB.set("user", currentUser);
 *   modelB.setFragments(pageBFragments);
 *
 *   engine.renderFile("templates/profile.html", modelB);
 *
 * If no FragmentRegistry is set and th:replace is encountered,
 * ReplaceNode renders nothing (silent skip) — same as an unknown fragment.
 *
 * PERFORMANCE:
 *   Single flat HashMap — no stack allocation per th:each iteration.
 *   saveAndSet()/restore() write into the existing map and restore it
 *   after each iteration with zero extra object allocation.
 */
public class Model {

    private final Map<String, Object> map = new HashMap<String, Object>();

    /**
     * Per-page fragment registry — null means no fragments registered.
     * Set by the caller before passing the Model to renderFile/renderString.
     */
    private FragmentRegistry fragments = null;

    // ------------------------------------------------------------------ data variables

    /**
     * Set a named variable.
     * Value may be any Object: POJO, JSONObject, JSONArray, List, String, etc.
     */
    public void set(String key, Object value) {
        map.put(key, value);
    }

    public void setAll(Map<String, String> variables) {
        map.putAll(variables);
    }

    /**
     * Retrieve a named variable. Returns null if not set.
     */
    public Object get(String key) {
        return map.get(key);
    }

    /**
     * Set a scoped variable for one loop iteration, saving the previous value.
     * The returned SavedValue MUST be passed to restore() after the iteration.
     *
     * Called by EachNode — not part of the public caller API.
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

    // ------------------------------------------------------------------ fragment registry

    /**
     * Attach a per-page FragmentRegistry to this model.
     *
     * th:replace directives will look up fragment names from this registry
     * at render time.  Each page can supply a completely different set of
     * fragments without any shared state or locking between pages.
     *
     * @param fragments the registry for this page, or null to disable th:replace
     */
    public void setFragments(FragmentRegistry fragments) {
        this.fragments = fragments;
    }

    /**
     * Return the fragment registry attached to this model.
     * May be null if the caller did not set one.
     *
     * Called by ReplaceNode at render time.
     */
    public FragmentRegistry getFragments() {
        return fragments;
    }

    // ------------------------------------------------------------------ inner class

    /**
     * Captures the pre-save state of a single variable slot.
     * Returned by saveAndSet(), consumed by restore().
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
