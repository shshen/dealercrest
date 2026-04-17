package com.dealercrest.router;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {

    private TrieNode paramChild;
    private TrieNode wildcardChild;
    private Map<String, RouteInfo> routes = new HashMap<>();
    private Map<String, TrieNode> staticChildren = new HashMap<>();

    public TrieNode getStaticChild(String segment) {
        return staticChildren.get(segment);
    }

    public void addStaticChild(String segment, TrieNode node) {
        staticChildren.put(segment, node);
    }

    public boolean hasStaticChild() {
        return staticChildren.isEmpty();
    }

    public TrieNode getParamChild() {
        return paramChild;
    }

    public void setParamChild(TrieNode paramChild) {
        this.paramChild = paramChild;
    }

    public TrieNode getWildcardChild() {
        return wildcardChild;
    }

    public boolean hasRouteHandler(String httpMethod) {
        return routes.containsKey(httpMethod);
    }

    public void setWildcardChild(TrieNode wildcardChild) {
        this.wildcardChild = wildcardChild;
    }

    public RouteInfo getRouteInfo(String httpMethod) {
        return routes.get(httpMethod);
    }

    public void addRouteInfo(String httpMethod, RouteInfo routeInfo) {
        routes.put(httpMethod, routeInfo);
    }

}



