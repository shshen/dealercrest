package com.dealercrest.router;

import java.util.Map;

public class MatchResult {

    private final RouteInfo routeInfo;
    private final Map<String, String> paramValues;

    public MatchResult(RouteInfo routeInfo, Map<String, String> paramValues) {
        this.routeInfo = routeInfo;
        this.paramValues = paramValues;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    public Map<String, String> getParamValues() {
        return paramValues;
    }
}

