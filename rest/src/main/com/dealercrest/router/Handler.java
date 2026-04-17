package com.dealercrest.router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.dealercrest.rest.ParamEntry;
import com.dealercrest.rest.http.HttpResult;

public class Handler {

    private final Method method;
    private final Object instance;
    private final List<ParamEntry> params;

    public Handler(Method method, Object handler, List<ParamEntry> params) {
        this.method = method;
        this.instance = handler;
        this.params = params;
    }

    public List<ParamEntry> getParams() {
        return params;
    }

    public HttpResult invoke(Object[] argArray)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (HttpResult) method.invoke(instance, argArray);
    }

}
