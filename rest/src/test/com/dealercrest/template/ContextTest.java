package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for Context — variable set/get and scoped save/restore.
 */
public class ContextTest {

    private DataModel ctx;

    @Before
    public void setUp() {
        ctx = new DataModel();
    }

    // ------------------------------------------------------------------ set / get

    @Test
    public void testSetAndGetString() {
        ctx.set("name", "Alice");
        assertEquals("Alice", ctx.get("name"));
    }

    @Test
    public void testSetAndGetInteger() {
        ctx.set("count", 42);
        assertEquals(42, ctx.get("count"));
    }

    @Test
    public void testGetMissingKeyReturnsNull() {
        assertNull(ctx.get("missing"));
    }

    @Test
    public void testSetOverwritesPreviousValue() {
        ctx.set("key", "first");
        ctx.set("key", "second");
        assertEquals("second", ctx.get("key"));
    }

    @Test
    public void testSetNullValue() {
        ctx.set("key", null);
        assertNull(ctx.get("key"));
    }

    // ------------------------------------------------------------------ saveAndSet / restore

    @Test
    public void testSaveAndSetReturnsNonNull() {
        ctx.set("item", "original");
        DataModel.SavedValue saved = ctx.saveAndSet("item", "override");
        assertNotNull(saved);
    }

    @Test
    public void testSaveAndSetChangesValue() {
        ctx.set("item", "original");
        ctx.saveAndSet("item", "override");
        assertEquals("override", ctx.get("item"));
    }

    @Test
    public void testRestoreReturnsOriginalValue() {
        ctx.set("item", "original");
        DataModel.SavedValue saved = ctx.saveAndSet("item", "override");
        ctx.restore(saved);
        assertEquals("original", ctx.get("item"));
    }

    @Test
    public void testRestoreWhenKeyDidNotExistRemovesIt() {
        // "item" was never set — after restore it should be absent (null)
        DataModel.SavedValue saved = ctx.saveAndSet("item", "temp");
        assertEquals("temp", ctx.get("item"));
        ctx.restore(saved);
        assertNull(ctx.get("item"));
    }

    @Test
    public void testNestedSaveRestoreIsIndependent() {
        ctx.set("x", "outer");

        DataModel.SavedValue s1 = ctx.saveAndSet("x", "inner1");
        assertEquals("inner1", ctx.get("x"));

        DataModel.SavedValue s2 = ctx.saveAndSet("x", "inner2");
        assertEquals("inner2", ctx.get("x"));

        ctx.restore(s2);
        assertEquals("inner1", ctx.get("x"));

        ctx.restore(s1);
        assertEquals("outer", ctx.get("x"));
    }

    @Test
    public void testMultipleIndependentKeys() {
        ctx.set("a", "A");
        ctx.set("b", "B");

        DataModel.SavedValue savedA = ctx.saveAndSet("a", "A2");
        assertEquals("A2", ctx.get("a"));
        assertEquals("B",  ctx.get("b")); // b must be unaffected

        ctx.restore(savedA);
        assertEquals("A", ctx.get("a"));
        assertEquals("B", ctx.get("b"));
    }
}
