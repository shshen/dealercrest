package com.dealercrest.rest;

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpRequest;

public class RequestContext {

    private final HttpRequest request;
    private final Map<String, Object> attributes;

    public RequestContext(HttpRequest  request, Map<String,Object> attributes) {
        this.request = request;
        this.attributes = new HashMap<>(attributes);
    }

    public HttpRequest getRequest() {
        return request;
    }
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
}
