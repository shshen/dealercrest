package com.dealercrest.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.dealercrest.rest.RequestContext;

public class RouteInfo {

    private final String httpMethod;
    private final String routePath;
    private final Set<String> rolesAllowed;
    private final List<String> pathParams; // param names in route path
    private final Handler httpHandler;

    public RouteInfo(Set<String> rolesAllowed, String httpMethod, String routePath, Handler httpHandler) {
        this.httpMethod = httpMethod;
        this.routePath = routePath;
        this.rolesAllowed = rolesAllowed;
        this.pathParams = parse(routePath);
        this.httpHandler = httpHandler;
    }

    @Override
    public String toString() {
        return "RouteInfo [httpMethod=" + httpMethod + ", routePath=" + routePath + "]";
    }

    public Set<String> getRolesAllowed() {
        return rolesAllowed;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRoutePath() {
        return routePath;
    }

    public List<String> getPathParams() {
        return pathParams;
    }

    public Handler getHttpHandler() {
        return httpHandler;
    }

    public boolean isRoleAllowed(RequestContext context) {
        if (rolesAllowed.isEmpty()) {
            return true;
        }
        JSONArray roles = (JSONArray)context.getAttribute("roles");
        if (roles == null) {
            return false;
        }
        for (int i = 0; i < roles.length(); i++) {
            String r = roles.getString(i);
            if (rolesAllowed.contains(r)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parse(String routePath) {
        String[] segments = routePath.split("/");
        List<String> params = new ArrayList<>();
        for (String seg : segments) {
            if (seg.startsWith("{") && seg.endsWith("**}")) {
                // double wildcard — strip { and **}
                String paramName = seg.substring(1, seg.length() - 3);
                params.add(paramName);
            } else if (seg.startsWith("{") && seg.endsWith("*}")) {
                // single wildcard — strip { and *}
                String paramName = seg.substring(1, seg.length() - 2);
                params.add(paramName);
            } else if (seg.startsWith("{") && seg.endsWith("}")) {
                // plain param — strip { and }
                String paramName = seg.substring(1, seg.length() - 1);
                params.add(paramName);
            }
        }
        return params;
    }


}
