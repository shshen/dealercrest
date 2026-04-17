package com.dealercrest.router;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Collections;

import static org.junit.Assert.*;

public class PathRouterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PathRouter router;

    // -----------------------------------------------------------------------
    // Helper — builds a RouteInfo with no roles and no Handler (null)
    // Real constructor: RouteInfo(Set<String> rolesAllowed, String httpMethod,
    //                             String routePath, Handler httpHandler)
    // -----------------------------------------------------------------------
    private static RouteInfo makeRoute(String method, String path) {
        return new RouteInfo(Collections.<String>emptySet(), method, path, null);
    }

    @Before
    public void setUp() {
        router = new PathRouter();
    }

    // -----------------------------------------------------------------------
    // Static routes
    // -----------------------------------------------------------------------

    @Test
    public void staticRoute_exactMatch() {
        RouteInfo route = makeRoute("GET", "/inventory");
        router.addRoute(route);

        MatchResult result = router.match("GET", "/inventory");

        assertNotNull(result);
        assertEquals(route, result.getRouteInfo());
        assertTrue(result.getParamValues().isEmpty());
    }

    @Test
    public void staticRoute_noMatch_returnsNull() {
        router.addRoute(makeRoute("GET", "/inventory"));

        assertNull(router.match("GET", "/about"));
    }

    @Test
    public void staticRoute_wrongMethod_returnsNull() {
        router.addRoute(makeRoute("GET", "/inventory"));

        assertNull(router.match("POST", "/inventory"));
    }

    @Test
    public void staticRoute_duplicate_throws() {
        router.addRoute(makeRoute("GET", "/inventory"));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Duplicate static routing path");

        router.addRoute(makeRoute("GET", "/inventory"));
    }

    @Test
    public void staticRoute_trailingSlashNormalized() {
        router.addRoute(makeRoute("GET", "/inventory/"));

        assertNotNull(router.match("GET", "/inventory"));
    }

    @Test
    public void staticRoute_samePathDifferentMethod_allowed() {
        RouteInfo getRoute  = makeRoute("GET",  "/inventory");
        RouteInfo postRoute = makeRoute("POST", "/inventory");
        router.addRoute(getRoute);
        router.addRoute(postRoute);

        assertEquals(getRoute,  router.match("GET",  "/inventory").getRouteInfo());
        assertEquals(postRoute, router.match("POST", "/inventory").getRouteInfo());
    }

    // -----------------------------------------------------------------------
    // Dynamic routes — path params
    // -----------------------------------------------------------------------

    @Test
    public void dynamicRoute_singleParam_matched() {
        RouteInfo route = makeRoute("GET", "/inventory/{id}");
        router.addRoute(route);

        MatchResult result = router.match("GET", "/inventory/abc123");

        assertNotNull(result);
        assertEquals("abc123", result.getParamValues().get("id"));
    }

    @Test
    public void dynamicRoute_multipleParams_matched() {
        RouteInfo route = makeRoute("GET", "/dealers/{dealerId}/pages/{slug}");
        router.addRoute(route);

        MatchResult result = router.match("GET", "/dealers/d1/pages/about");

        assertNotNull(result);
        assertEquals("d1",    result.getParamValues().get("dealerId"));
        assertEquals("about", result.getParamValues().get("slug"));
    }

    @Test
    public void dynamicRoute_staticBeforeParam_staticWins() {
        RouteInfo staticRoute = makeRoute("GET", "/inventory/new");
        RouteInfo paramRoute  = makeRoute("GET", "/inventory/{id}");
        router.addRoute(staticRoute);
        router.addRoute(paramRoute);

        assertEquals(staticRoute, router.match("GET", "/inventory/new").getRouteInfo());
    }

    @Test
    public void dynamicRoute_paramCaptures_nonStaticSegment() {
        RouteInfo staticRoute = makeRoute("GET", "/inventory/new");
        RouteInfo paramRoute  = makeRoute("GET", "/inventory/{id}");
        router.addRoute(staticRoute);
        router.addRoute(paramRoute);

        MatchResult result = router.match("GET", "/inventory/abc999");

        assertNotNull(result);
        assertEquals(paramRoute, result.getRouteInfo());
        assertEquals("abc999", result.getParamValues().get("id"));
    }

    // -----------------------------------------------------------------------
    // Wildcard routes (single *)
    // -----------------------------------------------------------------------

    @Test
    public void wildcardRoute_matchesRemainingSegments() {
        RouteInfo route = makeRoute("GET", "/assets/{file*}");
        router.addRoute(route);

        MatchResult result = router.match("GET", "/assets/css/main.css");

        assertNotNull(result);
        assertEquals("css/main.css", result.getParamValues().get("file"));
    }

    @Test
    public void wildcardRoute_singleSegment_matched() {
        RouteInfo route = makeRoute("GET", "/assets/{file*}");
        router.addRoute(route);

        MatchResult result = router.match("GET", "/assets/logo.png");

        assertNotNull(result);
        assertEquals("logo.png", result.getParamValues().get("file"));
    }

    @Test
    public void wildcardRoute_notLastSegment_throws() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Wildcard must be the last segment");

        router.addRoute(makeRoute("GET", "/assets/{file*}/extra"));
    }

    // -----------------------------------------------------------------------
    // RouteInfo.parse() — param name extraction
    // -----------------------------------------------------------------------

    @Test
    public void routeInfo_parse_plainParam_extractsName() {
        RouteInfo route = makeRoute("GET", "/inventory/{id}");

        assertEquals(1, route.getPathParams().size());
        assertEquals("id", route.getPathParams().get(0));
    }

    @Test
    public void routeInfo_parse_singleWildcard_extractsName() {
        RouteInfo route = makeRoute("GET", "/assets/{file*}");

        assertEquals(1, route.getPathParams().size());
        assertEquals("file", route.getPathParams().get(0));
    }

    @Test
    public void routeInfo_parse_doubleWildcard_extractsName_notPathStar() {
        // BUG FIX in RouteInfo.parse(): {path**} endsWith("*}") is true, so
        // without checking **} first, paramName would be "path*" instead of "path"
        RouteInfo route = makeRoute("GET", "/{path**}");

        assertEquals(1, route.getPathParams().size());
        assertEquals("path", route.getPathParams().get(0));  // must NOT be "path*"
    }

    @Test
    public void routeInfo_parse_multipleParams_allExtracted() {
        RouteInfo route = makeRoute("GET", "/dealers/{dealerId}/pages/{slug}");

        assertEquals(2, route.getPathParams().size());
        assertEquals("dealerId", route.getPathParams().get(0));
        assertEquals("slug",     route.getPathParams().get(1));
    }

    // -----------------------------------------------------------------------
    // Fallback route (**)
    // -----------------------------------------------------------------------

    @Test
    public void fallbackRoute_matchesUnknownPath() {
        RouteInfo fallback = makeRoute("GET", "/{path**}");
        router.addRoute(fallback);

        MatchResult result = router.match("GET", "/our-team");

        assertNotNull(result);
        assertEquals("our-team", result.getParamValues().get("path"));
    }

    @Test
    public void fallbackRoute_matchesDeepPath() {
        RouteInfo fallback = makeRoute("GET", "/{path**}");
        router.addRoute(fallback);

        MatchResult result = router.match("GET", "/blog/2026/march");

        assertNotNull(result);
        assertEquals("blog/2026/march", result.getParamValues().get("path"));
    }

    @Test
    public void fallbackRoute_stripsLeadingSlash_fromPathParam() {
        RouteInfo fallback = makeRoute("GET", "/{path**}");
        router.addRoute(fallback);

        MatchResult result = router.match("GET", "/our-team");

        // Must be "our-team", not "/our-team"
        assertEquals("our-team", result.getParamValues().get("path"));
    }

    @Test
    public void fallbackRoute_notMatchedWhenStaticRouteExists() {
        RouteInfo staticRoute = makeRoute("GET", "/about");
        RouteInfo fallback    = makeRoute("GET", "/{path**}");
        router.addRoute(staticRoute);
        router.addRoute(fallback);

        assertEquals(staticRoute, router.match("GET", "/about").getRouteInfo());
    }

    @Test
    public void fallbackRoute_notMatchedWhenParamRouteExists() {
        RouteInfo paramRoute = makeRoute("GET", "/inventory/{id}");
        RouteInfo fallback   = makeRoute("GET", "/{path**}");
        router.addRoute(paramRoute);
        router.addRoute(fallback);

        MatchResult result = router.match("GET", "/inventory/xyz");

        assertNotNull(result);
        assertEquals(paramRoute, result.getRouteInfo());
        assertEquals("xyz", result.getParamValues().get("id"));
    }

    @Test
    public void fallbackRoute_duplicate_throws() {
        router.addRoute(makeRoute("GET", "/{path**}"));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Duplicate fallback route");

        router.addRoute(makeRoute("GET", "/{other**}"));
    }

    @Test
    public void fallbackRoute_notLastSegment_throws() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Double wildcard must be the last segment");

        router.addRoute(makeRoute("GET", "/{path**}/extra"));
    }

    @Test
    public void fallbackRoute_noFallbackRegistered_returnsNull() {
        router.addRoute(makeRoute("GET", "/inventory"));

        assertNull(router.match("GET", "/unknown-page"));
    }

    // -----------------------------------------------------------------------
    // Priority ordering
    // -----------------------------------------------------------------------

    @Test
    public void priority_static_over_param_over_fallback() {
        RouteInfo staticRoute = makeRoute("GET", "/inventory/new");
        RouteInfo paramRoute  = makeRoute("GET", "/inventory/{id}");
        RouteInfo fallback    = makeRoute("GET", "/{path**}");
        router.addRoute(staticRoute);
        router.addRoute(paramRoute);
        router.addRoute(fallback);

        assertEquals(staticRoute, router.match("GET", "/inventory/new").getRouteInfo());
        assertEquals(paramRoute,  router.match("GET", "/inventory/abc").getRouteInfo());
        assertEquals(fallback,    router.match("GET", "/contact").getRouteInfo());
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    public void match_rootPath_staticRoute() {
        router.addRoute(makeRoute("GET", "/"));

        assertNotNull(router.match("GET", "/"));
    }

    @Test
    public void match_rootPath_fallbackWhenNoStaticRoot() {
        router.addRoute(makeRoute("GET", "/{path**}"));

        assertNotNull(router.match("GET", "/"));
    }

    // -----------------------------------------------------------------------
    // Full dealer site routing scenario
    // -----------------------------------------------------------------------

    @Test
    public void dealerSite_fullRoutingScenario() {
        RouteInfo assets    = makeRoute("GET", "/assets/{file*}");
        RouteInfo apiRoute  = makeRoute("GET", "/api/inventory");
        RouteInfo invList   = makeRoute("GET", "/inventory");
        RouteInfo invNew    = makeRoute("GET", "/inventory/new");
        RouteInfo invUsed   = makeRoute("GET", "/inventory/used");
        RouteInfo invDetail = makeRoute("GET", "/inventory/{id}");
        RouteInfo fallback  = makeRoute("GET", "/{path**}");

        router.addRoute(assets);
        router.addRoute(apiRoute);
        router.addRoute(invList);
        router.addRoute(invNew);
        router.addRoute(invUsed);
        router.addRoute(invDetail);
        router.addRoute(fallback);

        // System routes
        assertEquals(assets,    router.match("GET", "/assets/css/main.css").getRouteInfo());
        assertEquals(apiRoute,  router.match("GET", "/api/inventory").getRouteInfo());
        assertEquals(invList,   router.match("GET", "/inventory").getRouteInfo());
        assertEquals(invNew,    router.match("GET", "/inventory/new").getRouteInfo());
        assertEquals(invUsed,   router.match("GET", "/inventory/used").getRouteInfo());
        assertEquals(invDetail, router.match("GET", "/inventory/abc123").getRouteInfo());

        // CMS pages — all fall through to fallback
        assertEquals(fallback,  router.match("GET", "/our-team").getRouteInfo());
        assertEquals(fallback,  router.match("GET", "/about").getRouteInfo());
        assertEquals(fallback,  router.match("GET", "/financing").getRouteInfo());

        // Param values
        assertEquals("abc123",
            router.match("GET", "/inventory/abc123").getParamValues().get("id"));
        assertEquals("our-team",
            router.match("GET", "/our-team").getParamValues().get("path"));
        assertEquals("css/main.css",
            router.match("GET", "/assets/css/main.css").getParamValues().get("file"));
    }
}
