package com.dealercrest.rest.http;

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.multipart.InterfaceHttpData;

public class Multipart {

    private final Map<String, InterfaceHttpData> values;

    public Multipart() {
        this.values = new HashMap<>();
    }

    public void add(InterfaceHttpData obj) {
        this.values.put(obj.getName(), obj);
    }

    public InterfaceHttpData get(String name) {
        return values.get(name);
    }

}
