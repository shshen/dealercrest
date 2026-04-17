package com.dealercrest.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a DELETE request includes an entity body, the body is ignored
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {

    String GET = "GET";

    String POST = "POST";

    String PATCH = "PATCH";

    String DELETE = "DELETE";

    String PUT = "PUT";

    String value();

}
