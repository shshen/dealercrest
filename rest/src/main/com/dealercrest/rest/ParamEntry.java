package com.dealercrest.rest;

import java.lang.annotation.Annotation;

public class ParamEntry {

    private final Annotation param;
    private final Class<?> clazz;

    public ParamEntry(Annotation param, Class<?> clazz) {
        this.param = param;
        this.clazz = clazz;
    }

    public Annotation getParam() {
        return param;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
