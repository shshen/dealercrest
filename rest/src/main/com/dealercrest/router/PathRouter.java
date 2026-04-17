package com.dealercrest.router;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dealercrest.rest.BodyStream;
import com.dealercrest.rest.ContextParam;
import com.dealercrest.rest.FormParam;
import com.dealercrest.rest.HeaderParam;
import com.dealercrest.rest.HttpMethod;
import com.dealercrest.rest.MapParam;
import com.dealercrest.rest.ParamEntry;
import com.dealercrest.rest.Path;
import com.dealercrest.rest.PathParam;
import com.dealercrest.rest.PostParam;
import com.dealercrest.rest.QueryParam;
import com.dealercrest.rest.RolesAllowed;
import com.dealercrest.rest.Route;

/**
 * Annotation-driven HTTP request router that maps incoming paths to {@link RouteInfo} handlers.
 *
 * <p>Routes are registered either via {@link #addHandler(Object)} (which scans a controller
 * instance for annotated methods) or directly via {@link #addRoute(RouteInfo)}. Once all routes
 * are registered, {@link #match(String, String)} resolves an incoming HTTP method and path to the
 * best matching {@link MatchResult}.
 *
 * <p>Match priority (highest to lowest):
 * <ol>
 *   <li>Static routes — exact path, no variables (e.g. {@code /inventory/new})</li>
 *   <li>Dynamic trie routes — path params {@code {id}} or single wildcard {@code {file*}}</li>
 *   <li>Fallback route — double wildcard {@code {path**}}, matches anything not matched above</li>
 * </ol>
 *
 * <p>Path segment syntax:
 * <ul>
 *   <li>{@code /inventory}          — static segment, exact match only</li>
 *   <li>{@code /inventory/{id}}     — named path parameter, captures one segment</li>
 *   <li>{@code /assets/{file*}}     — single wildcard, captures remaining path as one value</li>
 *   <li>{@code /{path**}}           — double wildcard fallback, must be the only segment</li>
 * </ul>
 *
 * <p>Coexistence rules at the same trie level:
 * <ul>
 *   <li>Static + param: allowed — static always wins in {@link #match}</li>
 *   <li>Static + wildcard: allowed — static always wins in {@link #match}</li>
 *   <li>Param + wildcard: not allowed — ambiguous, throws at registration time</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 *   PathRouter router = new PathRouter();
 *   router.addHandler(new InventoryController());
 *   router.addHandler(new CmsController());
 *
 *   MatchResult result = router.match("GET", "/inventory/abc123");
 *   if (result != null) {
 *       result.getRouteInfo().getHttpHandler().invoke(result.getParamValues());
 *   }
 * </pre>
 */
public class PathRouter {

    private final TrieNode dynamicRootNode = new TrieNode();
    private final Map<String, RouteInfo> staticRoutes = new HashMap<>();

    /** At most one fallback route is permitted per router instance. */
    private RouteInfo fallbackRoute = null;

    /**
     * Scans all public methods on the given controller instance and registers any
     * method annotated with {@link Route}, {@link Path}, or {@link HttpMethod} as a route.
     * Methods with no recognized path annotation are silently skipped.
     *
     * @param controller the controller instance to scan
     * @throws IllegalArgumentException if any discovered route conflicts with an existing one
     */
    public void addHandler(Object controller) {
        Class<?> clazz = controller.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            RouteInfo routeInfo = buildRouteInfo(controller, method);
            if (routeInfo != null) {
                addRoute(routeInfo);
            }
        }
    }

    /**
     * Builds a {@link RouteInfo} from a single annotated method on a controller.
     * Returns {@code null} if the method has no path annotation.
     *
     * @param controller the controller instance that owns the method
     * @param method     the method to inspect
     * @return a {@link RouteInfo}, or {@code null} if the method is not a route handler
     */
    private RouteInfo buildRouteInfo(Object controller, Method method) {
        Annotation[] annotations = method.getAnnotations();
        String httpMethod = "GET";
        String path = "";
        Set<String> roles = new HashSet<>();
        for (Annotation a : annotations) {
            if (a instanceof HttpMethod) {
                HttpMethod ha = (HttpMethod) a;
                httpMethod = ha.value();
            } else if (a instanceof Path) {
                Path p = (Path) a;
                path = p.value();
            } else if (a instanceof Route) {
                Route r = (Route) a;
                path = r.path();
                httpMethod = r.method();
            } else if (a instanceof RolesAllowed) {
                RolesAllowed ac = (RolesAllowed) a;
                String[] xx = ac.value();
                for (String x : xx) {
                    roles.add(x);
                }
            }
        }
        if (path == null || path.isEmpty()) {
            return null;
        }

        Parameter[] params = method.getParameters();
        List<ParamEntry> paramList = new ArrayList<>();
        for (Parameter param : params) {
            Annotation[] paramAnnotations = param.getAnnotations();
            Annotation paramAnnotation = getParamKey(paramAnnotations);
            Class<?> clazzType = param.getType();
            paramList.add(new ParamEntry(paramAnnotation, clazzType));
        }
        Handler handlerInfo = new Handler(method, controller, paramList);
        return new RouteInfo(roles, httpMethod, path, handlerInfo);
    }

    /**
     * Returns the first recognized parameter-binding annotation from the given array.
     * Every handler method parameter must carry exactly one such annotation.
     *
     * @param annotations the annotations present on the parameter
     * @return the recognized parameter annotation
     * @throws IllegalArgumentException if no recognized annotation is found
     */
    private Annotation getParamKey(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a instanceof QueryParam || a instanceof PostParam || a instanceof PathParam
                    || a instanceof MapParam || a instanceof HeaderParam || a instanceof FormParam
                    || a instanceof ContextParam || a instanceof BodyStream) {
                return a;
            }
        }
        throw new IllegalArgumentException("annotation is not defined");
    }

    /**
     * Looks up a named path parameter value and converts it to the requested type.
     *
     * @param pathParams the path parameter map from a {@link MatchResult}
     * @param key        the parameter name
     * @param clazz      the target type
     * @return the converted value, or {@code null} if the key is not present
     */
    protected Object getPathParam(Map<String, String> pathParams, String key, Class<?> clazz) {
        String value = pathParams.get(key);
        if (value != null) {
            return convert(value, clazz);
        }
        return null;
    }

    /**
     * Converts a list of string values to the requested type.
     * If the target type is {@link java.util.List}, the list is returned as-is.
     * Otherwise the first element is converted via {@link #convert(String, Class)}.
     *
     * @param values the raw string values
     * @param clazz  the target type
     * @return the converted value, or {@code null} if the list is null or empty
     */
    protected Object convert(List<String> values, Class<?> clazz) {
        if (clazz.equals(List.class)) {
            return values;
        }
        if (values == null || values.size() < 1) {
            return null;
        }
        return convert(values.get(0), clazz);
    }

    /**
     * Converts a single string value to the requested type.
     * Supported types: {@code int}, {@code Integer}, {@code long}, {@code Long},
     * {@code String}, {@code boolean}, {@code Boolean}.
     *
     * @param value the raw string value
     * @param clazz the target type
     * @return the converted value
     * @throws IllegalArgumentException if the type is not supported
     */
    protected Object convert(String value, Class<?> clazz) {
        Object o;
        if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            if (value != null) {
                o = Integer.parseInt(value);
            } else {
                o = 0;
            }
        } else if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            if (value != null) {
                o = Long.parseLong(value);
            } else {
                o = 0L;
            }
        } else if (clazz.equals(String.class)) {
            o = value;
        } else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            if (value != null) {
                o = Boolean.parseBoolean(value);
            } else {
                o = false;
            }
        } else {
            throw new IllegalArgumentException(clazz + " is not supported!");
        }
        return o;
    }

    /**
     * Registers a single route. Routes with no path variables are stored in a flat map
     * for O(1) lookup. Routes with path variables are inserted into the trie. A double
     * wildcard segment ({@code {name**}}) registers the route as the fallback handler.
     *
     * @param routeInfo the route to register
     * @throws IllegalArgumentException if the route conflicts with or duplicates an existing one
     */
    public void addRoute(RouteInfo routeInfo) {
        String httpMethod = routeInfo.getHttpMethod();
        String routePath = normalizePath(routeInfo.getRoutePath());

        if (!routePath.contains("{")) {
            addStaticRoute(httpMethod, routePath, routeInfo);
            return;
        }

        String[] segments = routePath.split("/");
        TrieNode currentNode = dynamicRootNode;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) {
                continue;
            }
            if (isDoubleWildcard(segment)) {
                boolean hasMore = false;
                for (int j = i + 1; j < segments.length; j++) {
                    if (!segments[j].isEmpty()) {
                        hasMore = true;
                        break;
                    }
                }
                if (hasMore) {
                    throw new IllegalArgumentException(
                        "Double wildcard must be the last segment: " + routePath);
                }
                addFallbackRoute(routeInfo, routePath);
                return;
            } else if (isParam(segment)) {
                currentNode = addParamNode(currentNode, routePath);
            } else if (isWildcard(segment)) {
                boolean hasMore = false;
                for (int j = i + 1; j < segments.length; j++) {
                    if (!segments[j].isEmpty()) {
                        hasMore = true;
                        break;
                    }
                }
                if (hasMore) {
                    throw new IllegalArgumentException(
                        "Wildcard must be the last segment: " + routePath);
                }
                currentNode = addWildcardNode(currentNode, routePath);
            } else {
                currentNode = addStaticNode(currentNode, segment, routePath);
            }
        }

        if (currentNode.hasRouteHandler(httpMethod)) {
            throw new IllegalArgumentException(
                "Duplicate route handler for path: " + routePath + " method: " + httpMethod);
        }
        currentNode.addRouteInfo(httpMethod, routeInfo);
    }

    /**
     * Strips a trailing slash from any path longer than one character.
     * {@code /inventory/} becomes {@code /inventory}; {@code /} is unchanged.
     *
     * @param path the raw route path
     * @return the normalized path
     */
    private String normalizePath(String path) {
        if (path.endsWith("/") && path.length() > 1) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Adds a no-variable route to the static map keyed by {@code "METHOD_/path"}.
     *
     * @param method  the HTTP method
     * @param path    the exact route path
     * @param handler the route to register
     * @throws IllegalArgumentException if an identical method + path combination already exists
     */
    private void addStaticRoute(String method, String path, RouteInfo handler) {
        String key = method + "_" + path;
        if (staticRoutes.containsKey(key)) {
            throw new IllegalArgumentException(
                "Duplicate static routing path: " + path + " method: " + method);
        }
        staticRoutes.put(key, handler);
    }

    /**
     * Returns {@code true} if the segment is a plain named parameter: {@code {name}}.
     * Wildcard variants ({@code {name*}} and {@code {name**}}) are excluded.
     */
    private boolean isParam(String segment) {
        return segment.startsWith("{") && segment.endsWith("}")
            && !segment.endsWith("*}") && !segment.endsWith("**}");
    }

    /**
     * Returns {@code true} if the segment is a single wildcard: {@code {name*}}.
     * Captures the current segment and all remaining path segments as one value.
     * Double wildcard ({@code {name**}}) is excluded and handled separately.
     */
    private boolean isWildcard(String segment) {
        return segment.startsWith("{") && segment.endsWith("*}")
            && !segment.endsWith("**}");
    }

    /**
     * Returns {@code true} if the segment is a double wildcard: {@code {name**}}.
     * Used exclusively to register the fallback catch-all route.
     */
    private boolean isDoubleWildcard(String segment) {
        return segment.startsWith("{") && segment.endsWith("**}");
    }

    /**
     * Advances the trie by creating or reusing the param child of {@code current}.
     *
     * <p>A param child and a wildcard child cannot coexist at the same trie level because
     * it would be ambiguous which one should match at request time. A param child and
     * static children can coexist — static always takes priority in {@link #match}.
     *
     * @param current   the current trie node
     * @param routePath the full route path, used in the error message on conflict
     * @return the param child node
     * @throws IllegalArgumentException if a wildcard child already exists at this level
     */
    private TrieNode addParamNode(TrieNode current, String routePath) {
        if (current.getWildcardChild() != null) {
            throw new IllegalArgumentException(
                "Conflict: param vs wildcard at " + routePath);
        }
        if (current.getParamChild() == null) {
            current.setParamChild(new TrieNode());
        }
        return current.getParamChild();
    }

    /**
     * Advances the trie by creating or reusing the wildcard child of {@code current}.
     *
     * <p>A wildcard child and a param child cannot coexist at the same trie level.
     * A wildcard child and static children can coexist — static always takes priority.
     *
     * @param current   the current trie node
     * @param routePath the full route path, used in the error message on conflict
     * @return the wildcard child node
     * @throws IllegalArgumentException if a param child already exists at this level
     */
    private TrieNode addWildcardNode(TrieNode current, String routePath) {
        if (current.getParamChild() != null) {
            throw new IllegalArgumentException(
                "Conflict: wildcard vs param at " + routePath);
        }
        if (current.getWildcardChild() == null) {
            current.setWildcardChild(new TrieNode());
        }
        return current.getWildcardChild();
    }

    /**
     * Advances the trie by creating or reusing the static child for {@code segment}.
     *
     * <p>Static nodes may freely coexist with param or wildcard children at the same
     * level — static segments always win during {@link #match}.
     *
     * @param current   the current trie node
     * @param segment   the literal path segment (e.g. {@code "inventory"})
     * @param routePath the full route path, reserved for future conflict messages
     * @return the static child node for the given segment
     */
    private TrieNode addStaticNode(TrieNode current, String segment, String routePath) {
        TrieNode next = current.getStaticChild(segment);
        if (next == null) {
            next = new TrieNode();
            current.addStaticChild(segment, next);
        }
        return next;
    }

    /**
     * Resolves an HTTP method and request path to the best matching {@link MatchResult}.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Static route map — O(1) exact lookup</li>
     *   <li>Dynamic trie — walks segments, preferring static children over param over wildcard</li>
     *   <li>Fallback route — used when no trie path matches</li>
     * </ol>
     *
     * @param httpMethod  the HTTP method (e.g. {@code "GET"}, {@code "POST"})
     * @param requestPath the request path without query string (e.g. {@code "/inventory/abc"})
     * @return a {@link MatchResult} with the matched route and extracted path params,
     *         or {@code null} if no route matches and no fallback is registered
     */
    public MatchResult match(String httpMethod, String requestPath) {
        RouteInfo staticRoute = staticRoutes.get(httpMethod + "_" + requestPath);
        if (staticRoute != null) {
            return new MatchResult(staticRoute, Collections.emptyMap());
        }

        String[] segments = requestPath.split("/");
        TrieNode currentNode = dynamicRootNode;
        List<String> paramValues = new ArrayList<>();

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) {
                continue;
            }

            TrieNode staticChild   = currentNode.getStaticChild(segment);
            TrieNode paramChild    = currentNode.getParamChild();
            TrieNode wildcardChild = currentNode.getWildcardChild();

            if (staticChild != null) {
                currentNode = staticChild;
            } else if (paramChild != null) {
                paramValues.add(segment);
                currentNode = paramChild;
            } else if (wildcardChild != null) {
                paramValues.add(joinRemainingSegments(segments, i));
                currentNode = wildcardChild;
                break;
            } else {
                return matchFallback(requestPath);
            }
        }

        MatchResult result = buildMatchResult(currentNode, httpMethod, paramValues);
        if (result != null) {
            return result;
        }

        return matchFallback(requestPath);
    }

    /**
     * Concatenates path segments from {@code startIndex} onward into a single
     * slash-separated string, skipping empty segments from leading or trailing slashes.
     *
     * @param segments   the full split segment array
     * @param startIndex the index of the first segment to include
     * @return the joined remaining path (e.g. {@code "css/main.css"})
     */
    private String joinRemainingSegments(String[] segments, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < segments.length; i++) {
            if (!segments[i].isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('/');
                }
                sb.append(segments[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Attempts to construct a {@link MatchResult} from the trie node reached after walking
     * all request segments. Returns {@code null} if the node has no handler for the given
     * HTTP method, or if the number of captured param values does not match the route definition.
     *
     * @param trieNode    the trie node reached after segment traversal
     * @param httpMethod  the HTTP method of the request
     * @param paramValues the path parameter values captured during traversal, in order
     * @return a populated {@link MatchResult}, or {@code null} if no handler matches
     */
    private MatchResult buildMatchResult(TrieNode trieNode, String httpMethod,
                                          List<String> paramValues) {
        RouteInfo routeInfo = trieNode.getRouteInfo(httpMethod);
        if (routeInfo == null) {
            return null;
        }
        List<String> names = routeInfo.getPathParams();
        if (names.size() != paramValues.size()) {
            return null;
        }
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            params.put(names.get(i), paramValues.get(i));
        }
        return new MatchResult(routeInfo, params);
    }

    /**
     * Registers the given route as the fallback handler. Only one fallback is permitted
     * per router instance. The fallback is selected only after all static and trie routes
     * have been tried.
     *
     * @param routeInfo the fallback route
     * @param routePath the route path, used in the error message on duplicate detection
     * @throws IllegalArgumentException if a fallback route is already registered
     */
    private void addFallbackRoute(RouteInfo routeInfo, String routePath) {
        if (fallbackRoute != null) {
            throw new IllegalArgumentException(
                "Duplicate fallback route detected: " + routePath);
        }
        fallbackRoute = routeInfo;
    }

    /**
     * Matches the request against the fallback route, passing the full request path
     * (minus its leading slash) as the sole path parameter value.
     * Returns {@code null} if no fallback route has been registered.
     *
     * @param requestPath the original request path (e.g. {@code "/our-team"})
     * @return a {@link MatchResult} for the fallback route, or {@code null}
     */
    private MatchResult matchFallback(String requestPath) {
        if (fallbackRoute == null) {
            return null;
        }
        String path = requestPath;
        if (path.length() > 0 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        Map<String, String> params = new HashMap<>();
        List<String> names = fallbackRoute.getPathParams();
        if (names.size() > 0) {
            params.put(names.get(0), path);
        }
        return new MatchResult(fallbackRoute, params);
    }
}
