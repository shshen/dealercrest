package com.dealercrest.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathChain {

    private final List<String> segments = new ArrayList<>();

    public PathChain add(String segment) {
        segments.add(segment);
        return this;
    }

    public List<String> segments() {
        return Collections.unmodifiableList(segments);
    }

    public String join() {
        return String.join(" ", segments);
    }

}
