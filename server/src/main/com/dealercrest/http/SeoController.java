package com.dealercrest.http;

import com.dealercrest.rest.Route;

public class SeoController {
    
    @Route(path = "/sitemap.xml")
    public HttpResult sitemap() {
        // query all active vehicles for this dealer
        // generate XML response
        // return with content-type application/xml
        return null;
    }

    @Route(path = "/robots.txt")
    public HttpResult robotsTxt() {
        // String domain = resolveDomainFromRequest();
        // String body = buildRobotsTxt(domain);
        // return HttpResult.ok(body, "text/plain");
        return null;
    }
}
