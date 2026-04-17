package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for DirectiveRegistry — registration, lookup, and priority ordering.
 */
public class DirectiveRegistryTest {

    private DirectiveRegistry registry;

    // ------------------------------------------------------------------ stub directives

    private static class LowPriorityDirective implements Directive {
        public String name()     { return "th:low"; }
        public int priority()    { return 100; }
        public Node apply(ElementNode node) { return node; }
    }

    private static class HighPriorityDirective implements Directive {
        public String name()     { return "th:high"; }
        public int priority()    { return 5; }
        public Node apply(ElementNode node) { return node; }
    }

    private static class MidPriorityDirective implements Directive {
        public String name()     { return "th:mid"; }
        public int priority()    { return 50; }
        public Node apply(ElementNode node) { return node; }
    }

    // ------------------------------------------------------------------ setup

    @Before
    public void setUp() {
        registry = new DirectiveRegistry();
    }

    // ------------------------------------------------------------------ basic registration

    @Test
    public void testRegisterAndGetByName() {
        registry.register(new EachDirective());
        assertNotNull(registry.get("th:each"));
    }

    @Test
    public void testGetUnknownNameReturnsNull() {
        assertNull(registry.get("th:unknown"));
    }

    @Test
    public void testAllReturnsRegisteredDirectives() {
        registry.register(new EachDirective());
        registry.register(new IfDirective());
        assertEquals(2, registry.all().size());
    }

    @Test
    public void testRegisterRealDirectives() {
        registry.register(new EachDirective());
        registry.register(new IfDirective());
        assertNotNull(registry.get("th:each"));
        assertNotNull(registry.get("th:if"));
    }

    // ------------------------------------------------------------------ priority ordering

    @Test
    public void testPriorityOrderIsAscending() {
        registry.register(new LowPriorityDirective());
        registry.register(new HighPriorityDirective());
        registry.register(new MidPriorityDirective());

        List<Directive> ordered = registry.inPriorityOrder();
        assertEquals(3, ordered.size());

        // Must be sorted low-number first (high priority first)
        assertTrue("First should be priority 5",
                ordered.get(0).priority() == 5);
        assertTrue("Second should be priority 50",
                ordered.get(1).priority() == 50);
        assertTrue("Third should be priority 100",
                ordered.get(2).priority() == 100);
    }

    @Test
    public void testEachBeforeIfByPriority() {
        registry.register(new IfDirective());   // priority 20
        registry.register(new EachDirective()); // priority 10

        List<Directive> ordered = registry.inPriorityOrder();
        assertEquals("th:each", ordered.get(0).name());
        assertEquals("th:if",   ordered.get(1).name());
    }

    @Test
    public void testInPriorityOrderIsStable() {
        // Registering in a different order must still produce correct output
        registry.register(new LowPriorityDirective());   // 100
        registry.register(new HighPriorityDirective());  //   5

        List<Directive> ordered = registry.inPriorityOrder();
        assertEquals("th:high", ordered.get(0).name());
        assertEquals("th:low",  ordered.get(1).name());
    }

    @Test
    public void testSingleDirectiveInPriorityOrder() {
        registry.register(new EachDirective());
        List<Directive> ordered = registry.inPriorityOrder();
        assertEquals(1, ordered.size());
        assertEquals("th:each", ordered.get(0).name());
    }
}
