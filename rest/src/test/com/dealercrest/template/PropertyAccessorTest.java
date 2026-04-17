package com.dealercrest.template;

import org.junit.Test;
import org.json.JSONObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for PropertyAccessor — public fields, private fields via getter,
 * getter methods, JSONObject, and caching consistency.
 */
public class PropertyAccessorTest {

    // ------------------------------------------------------------------ fixtures

    public static class PublicFields {
        public String  name  = "Alice";
        public int     score = 99;
        public boolean active = true;
    }

    public static class PrivateWithGetters {
        private String  city;
        private boolean enabled;

        public PrivateWithGetters(String city, boolean enabled) {
            this.city    = city;
            this.enabled = enabled;
        }

        public String  getCity()    { return city;    }
        public boolean isEnabled()  { return enabled; }
    }

    public static class NoAccessor {
        // no public field named "secret", no getter
        @SuppressWarnings("unused")
        private String secret = "hidden";
    }

    public static class Parent {
        public String parentField = "parent";
    }

    public static class Child extends Parent {
        public String childField = "child";
    }

    // ------------------------------------------------------------------ public fields

    @Test
    public void testPublicStringField() {
        PublicFields obj = new PublicFields();
        assertEquals("Alice", PropertyAccessor.get(obj, "name"));
    }

    @Test
    public void testPublicIntField() {
        PublicFields obj = new PublicFields();
        assertEquals(99, PropertyAccessor.get(obj, "score"));
    }

    @Test
    public void testPublicBooleanField() {
        PublicFields obj = new PublicFields();
        assertEquals(Boolean.TRUE, PropertyAccessor.get(obj, "active"));
    }

    // ------------------------------------------------------------------ private fields via getter

    @Test
    public void testPrivateFieldViaGetGetter() {
        PrivateWithGetters obj = new PrivateWithGetters("London", true);
        assertEquals("London", PropertyAccessor.get(obj, "city"));
    }

    @Test
    public void testPrivateFieldViaIsGetter() {
        PrivateWithGetters obj = new PrivateWithGetters("London", true);
        assertEquals(Boolean.TRUE, PropertyAccessor.get(obj, "enabled"));
    }

    // ------------------------------------------------------------------ not found

    @Test
    public void testMissingPropertyReturnsNull() {
        PublicFields obj = new PublicFields();
        assertNull(PropertyAccessor.get(obj, "doesNotExist"));
    }

    @Test
    public void testNullObjectReturnsNull() {
        assertNull(PropertyAccessor.get(null, "name"));
    }

    // ------------------------------------------------------------------ inheritance

    @Test
    public void testInheritedPublicField() {
        Child obj = new Child();
        assertEquals("parent", PropertyAccessor.get(obj, "parentField"));
        assertEquals("child",  PropertyAccessor.get(obj, "childField"));
    }

    // ------------------------------------------------------------------ JSONObject fast-path

    @Test
    public void testJsonObjectStringProperty() {
        JSONObject obj = new JSONObject();
        obj.put("brand", "Toyota");
        assertEquals("Toyota", PropertyAccessor.get(obj, "brand"));
    }

    @Test
    public void testJsonObjectMissingPropertyReturnsNull() {
        JSONObject obj = new JSONObject();
        assertNull(PropertyAccessor.get(obj, "missing"));
    }

    @Test
    public void testJsonObjectNullObjectReturnsNull() {
        assertNull(PropertyAccessor.get(null, "anything"));
    }

    // ------------------------------------------------------------------ caching

    @Test
    public void testCachedAccessorWorksForMultipleInstances() {
        // First access builds the cache entry; second must use it correctly
        PublicFields a = new PublicFields();
        a.name = "First";

        PublicFields b = new PublicFields();
        b.name = "Second";

        assertEquals("First",  PropertyAccessor.get(a, "name"));
        assertEquals("Second", PropertyAccessor.get(b, "name"));
    }

    @Test
    public void testCachedAccessorWorksAcrossTypes() {
        PublicFields  pf = new PublicFields();
        PrivateWithGetters pg = new PrivateWithGetters("Paris", false);

        // Both classes happen to have a "name"/"city" property — caching must
        // not mix them up (cache key includes class name)
        assertEquals("Alice",  PropertyAccessor.get(pf, "name"));
        assertEquals("Paris",  PropertyAccessor.get(pg, "city"));
    }
}
