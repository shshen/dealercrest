package com.dealercrest.template;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached property accessor using MethodHandles.
 *
 * WHY METHODHANDLE INSTEAD OF FIELD/METHOD:
 *
 *   Field.get(obj) and Method.invoke(obj) are reflective invocations.
 *   The JVM treats them as opaque calls — it cannot inline them, and they
 *   carry security-check overhead on each call.
 *
 *   MethodHandle, once obtained and cached, is treated by the JIT as a
 *   near-direct call.  The JVM can inline through it, hoist invariants,
 *   and eliminate the per-call security checks entirely.
 *   In hot loops (th:each over thousands of items) this is measurably faster.
 *
 * WHAT STAYS THE SAME:
 *   The lookup itself (getDeclaredField, getMethod) still uses reflection —
 *   that happens ONCE per (Class, propertyName) pair and the result is cached.
 *   Every subsequent access goes through the cached MethodHandle only.
 *
 * Lookup order:
 *   1. Public field
 *   2. Declared (private/protected) field  [unreflectGetter via setAccessible]
 *   3. getXxx() public getter method
 *   4. isXxx()  public getter method (booleans)
 *
 * JSONObject support:
 *   If the object is a org.json.JSONObject, we call .opt(name) directly
 *   before attempting any reflection, since JSONObject stores data in an
 *   internal map rather than fields.
 */
public class PropertyAccessor {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Sentinel cached when a property cannot be found, so we do not
     * re-attempt the expensive lookup on every render call.
     */
    private static final MethodHandle NOT_FOUND_SENTINEL;

    static {
        MethodHandle sentinel;
        try {
            // A no-op handle we can use as a recognisable "not found" marker.
            // notFoundPlaceholder() always returns null.
            Method m = PropertyAccessor.class
                    .getDeclaredMethod("notFoundPlaceholder", Object.class);
            m.setAccessible(true);
            sentinel = LOOKUP.unreflect(m);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        NOT_FOUND_SENTINEL = sentinel;
    }

    @SuppressWarnings("unused")
    private static Object notFoundPlaceholder(Object ignored) {
        return null;
    }

    // Cache key: "com.example.Vehicle#name"
    private static final ConcurrentHashMap<String, MethodHandle> CACHE =
            new ConcurrentHashMap<String, MethodHandle>();

    /**
     * Access a named property on obj.
     * Returns null if the property cannot be found or is genuinely null.
     */
    public static Object get(Object obj, String name) {
        if (obj == null) return null;

        // Fast path for JSONObject — no reflection needed
        if (obj instanceof org.json.JSONObject) {
            return ((org.json.JSONObject) obj).opt(name);
        }

        String cacheKey = obj.getClass().getName() + "#" + name;
        MethodHandle handle = CACHE.get(cacheKey);

        if (handle == null) {
            handle = resolve(obj.getClass(), name);
            MethodHandle existing = CACHE.putIfAbsent(cacheKey, handle);
            if (existing != null) {
                handle = existing;
            }
        }

        if (handle == NOT_FOUND_SENTINEL) return null;

        try {
            return handle.invoke(obj);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Resolve a MethodHandle for (class, propertyName).
     * Called at most once per pair; result is cached.
     */
    private static MethodHandle resolve(Class<?> cls, String name) {

        // 1. Public field — unreflectGetter gives a direct read handle
        try {
            Field f = cls.getField(name);
            return LOOKUP.unreflectGetter(f);
        } catch (Exception ignored) {}

        // 2. Declared (private/protected) field
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return LOOKUP.unreflectGetter(f);
        } catch (Exception ignored) {}

        // 3. getXxx() / isXxx() public method
        String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String prefix : new String[]{"get", "is"}) {
            try {
                Method m = cls.getMethod(prefix + capitalized);
                return LOOKUP.unreflect(m);
            } catch (Exception ignored) {}
        }

        return NOT_FOUND_SENTINEL;
    }
}
