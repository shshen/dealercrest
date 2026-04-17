package com.dealercrest.http;

import com.dealercrest.db.DealerCacheTask;
import com.dealercrest.page.Page;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.ThemeFiles;
import com.dealercrest.resource.WebResource;
import com.dealercrest.rest.ContextParam;
import com.dealercrest.rest.HeaderParam;
import com.dealercrest.rest.MapParam;
import com.dealercrest.rest.MultiValueMap;
import com.dealercrest.rest.PathParam;
import com.dealercrest.rest.Route;
import com.dealercrest.rest.http.HttpResult;
import com.dealercrest.storage.Storage;

public class PageController {

    private final WebResource webResource;
    private final DealerCacheTask dealerCache;

    public PageController(Storage storage, WebResource webResource, DealerCacheTask dealerCache) {
        this.webResource = webResource;
        this.dealerCache = dealerCache;
    }

    @Route(path = "/themes/{path*}")
    public HttpResult themes(@PathParam("path") String path, 
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        String themeName = "";
        ThemeFiles themeTemplate = webResource.getTheme(themeName);
        return themeTemplate.buildHttpResult(path, ifModifiedSince);
    }

    /**
     * /inventory/used/toyota/camry?yearMin=2020&priceMax=30000&sort=price_asc&page=2&limit=24
     * 
     * PATH: condition/make/model  (high SEO value, page identity) 
     * QUERY STRING: everything else — secondary filters,sort, and pagination
     */
    @Route(path = "/inventory/{path*}")
    public HttpResult inventory(
            @ContextParam("host") String host,
            @PathParam("path") String path,
            @MapParam MultiValueMap queryParams) {
        QueryRequest queryResource = new QueryRequest(path, queryParams);
        return renderPage(host, "/inventory", queryResource);
    }

    /**
     * /vehicle/new/2026-toyota-corolla-murray-1HGBH41JXMN109186
     * 
     * @return
     */
    @Route(path = "/vehicle/{condition}/{detail*}")
    public HttpResult vehicle(
            @ContextParam("host") String host,
            @PathParam("condition") String condition,
            @PathParam("detail") String detail) {
        String fullPath = "/vehicle/" + condition + "/" + detail;
        QueryRequest requestContext = new QueryRequest(fullPath, new MultiValueMap());
        return renderPage(host, "/vehicle", requestContext);
    }

    /**
     * Fallback route for all other paths, can be used to render a 404 page or redirect to homepage
     * 
     * 1, predefined set of dealer paths. 
     *    such as /about, /contact, /service etc. that can be rendered with static pages or simple templates.
     * 2, my own saas pages
     * 
     */
    @Route(path = "{path**}")
    public HttpResult fallback(
            @ContextParam("host") String host,
            @PathParam("path") String path, 
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @MapParam MultiValueMap queryParams) {
        QueryRequest queryRequest = new QueryRequest(path, ifModifiedSince, queryParams);
        return renderPage(host, path, queryRequest);
    }

    private HttpResult renderPage(String host, String path, QueryRequest queryRequest) {
        String dealerId = dealerCache.getDealerId(host);
        if (dealerId == null) {
            return null; // dealer not found page
        }
        SitePages dealerSite = webResource.getDealerPages(dealerId);
        if (dealerSite == null) {
            return null; // dealer not found page
        }
        Page page = dealerSite.getPage(path);
        if (page == null) {
            page = dealerSite.getPage("/404");
        }
        return page.render(queryRequest);
    }

    private HttpResult buildNotFoundResult() {
        return null;
    }

}
