package com.dealercrest.http;

import com.dealercrest.rest.MultiValueMap;

public class QueryRequest {

    private final String path;
    private final String ifModifiedSince;
    private final MultiValueMap queryParams;

    public QueryRequest(String path, MultiValueMap queryParams) {
        this(path, null, queryParams);
    }

    public QueryRequest(String path, String ifModifiedSince, MultiValueMap queryParams) {
        this.path = path;
        this.ifModifiedSince = ifModifiedSince;
        this.queryParams = queryParams;
    }

    public String getPath() {
        return path;
    }

    public String getIfModifiedSince() {
        return ifModifiedSince;
    }

    public MultiValueMap getQueryParams() {
        return queryParams;
    }

}
