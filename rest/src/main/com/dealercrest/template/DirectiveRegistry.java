package com.dealercrest.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all known directives.
 *
 * CHANGE: Maintains a priority-sorted list of directives so that
 * ElementNode.compile() can check them in a defined order.
 *
 * When an element has multiple th:* attributes (e.g. th:each + th:if),
 * the highest-priority directive wins and the result is re-compiled
 * so that remaining directives are still processed on the inner node.
 */
public class DirectiveRegistry {

    private final Map<String, Directive> map = new HashMap<String, Directive>();

    // Sorted by priority ascending (lower number = higher priority)
    private final List<Directive> sortedList = new ArrayList<Directive>();

    public void register(Directive d) {
        map.put(d.name(), d);
        sortedList.add(d);
        // Keep sorted after each registration
        Collections.sort(sortedList, new Comparator<Directive>() {
            public int compare(Directive a, Directive b) {
                return Integer.compare(a.priority(), b.priority());
            }
        });
    }

    /**
     * Look up a directive by exact attribute name.
     */
    public Directive get(String name) {
        return map.get(name);
    }

    /**
     * All directives in priority order (lowest priority number first).
     * Used by ElementNode.compile() to find the first matching directive
     * on a node's attribute set.
     */
    public List<Directive> inPriorityOrder() {
        return sortedList;
    }

    public Collection<Directive> all() {
        return map.values();
    }
}
