package com.dealercrest.http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;

import com.dealercrest.db.DealerCacheTask;
import com.dealercrest.db.JdbcTemplate;
import com.dealercrest.page.Page;
import com.dealercrest.page.SitePages;
import com.dealercrest.page.WebResources;
import com.dealercrest.rest.ContextParam;
import com.dealercrest.rest.MapParam;
import com.dealercrest.rest.MultiValueMap;
import com.dealercrest.rest.PathParam;
import com.dealercrest.rest.Route;
import com.dealercrest.rest.http.DeferredResult;
import com.dealercrest.rest.http.HttpResult;
import com.dealercrest.rest.http.JsonResult;

import io.netty.handler.codec.http.HttpResponse;

public class PageController {

    private final WebResources webResources;
    private final JdbcTemplate jdbcTemplate;
    private final DealerCacheTask dealerCache;
    private final ExecutorService executorService;

    public PageController(WebResources appResource, JdbcTemplate jdbcTemplate) {
        this.webResources = appResource;
        this.jdbcTemplate = jdbcTemplate;
        this.dealerCache = null;
        this.executorService = null;
    }

    @Route(path = "/assets/{path*}")
    public HttpResult assets(
            @ContextParam("dealerDomain") String dealerDomain,
            @PathParam("path") String path) {
        // 1, get dealerId async
        // 2, then get pages by dealerId, and byteBuf
        // return DeferredResult
        if (dealerDomain.endsWith("dataleading.com")) {
            return null;
        }
        String dealerId = dealerCache.getDealerId(dealerDomain);
        SitePages sitePages = webResources.getDealerPages(dealerId);
        Page page = sitePages.getPage(path);
        return page.render(null);
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
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();

        // PageRenderTask pageRender = new PageRenderTask(dealerDomain, appResource, jdbcTemplate, future);

        return new DeferredResult(future);
    }

    /**
     * /vehicle/new/2026-toyota-corolla-murray-1HGBH41JXMN109186
     * 
     * @return
     */
    @Route(path = "/vehicle/{condition}/{slug*}")
    public HttpResult vehicle(
            @ContextParam("host") String host,
            @PathParam("condition") String condition,
            @PathParam("slug") String slug) {
        String vin = extractVin(slug);
        SitePages sitePages = webResources.getDealerPages("dealer");
        Page page = sitePages.getPage("/inventory");
        return page.render(null);
    }

    /**
     * 
     */
    @Route(path = "{path**}")
    public HttpResult fallback(
            @ContextParam("host") String host,
            @PathParam("path") String path, 
            @MapParam MultiValueMap queryParams) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    private String extractVin(String slug) {
        int lastHyphen = -1;
        for (int i = slug.length() - 1; i >= 0; i--) {
            if (slug.charAt(i) == '-') {
                lastHyphen = i;
                break;
            }
        }
        if (lastHyphen == -1) {
            return slug;
        }
        return slug.substring(lastHyphen + 1);
    }


}
