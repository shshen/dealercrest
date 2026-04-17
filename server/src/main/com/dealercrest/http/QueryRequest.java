package com.dealercrest.http;

import com.dealercrest.rest.MultiValueMap;

public class QueryRequest {

    private final String uri;
    private final String ifModifiedSince;
    private final MultiValueMap queryParams;

    public QueryRequest(String path, MultiValueMap queryParams) {
        this(path, "0", queryParams);
    }

    public QueryRequest(String path, String ifModifiedSince, MultiValueMap queryParams) {
        this.uri = path;
        this.ifModifiedSince = ifModifiedSince;
        this.queryParams = queryParams;
    }

    public String getUri() {
        return uri;
    }

    public String getIfModifiedSince() {
        return ifModifiedSince;
    }

    public MultiValueMap getQueryParams() {
        return queryParams;
    }

    public static final QueryRequest EMPTY = new QueryRequest("", new MultiValueMap());

}
