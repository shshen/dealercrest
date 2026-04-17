package com.dealercrest.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {
    /**
     * Defines the name of the URI template parameter whose value will be used
     * to initialize the value of the annotated method parameter, class field or
     * property. See {@link Path#value()} for a description of the syntax of
     * template parameters.
     * 
     * <p>E.g. a class annotated with: <code>&#64;Path("widgets/{id}")</code>
     * can have methods annotated whose arguments are annotated
     * with <code>&#64;PathParam("id")</code>.
     */
    String value();
}
