package com.dealercrest.template;

import java.util.*;

public class FragmentRepository {

    private static final Map<String, Node> fragments = new HashMap<>();

    public static void register(String key, Node node) {
        fragments.put(key, node);
    }

    public static Node get(String key) {
        return fragments.get(key);
    }
}
