package com.dealercrest.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as the raw request body stream.
 * 
 * Usage:
 *   @POST
 *   @Path("/content")
 *   public Response init(@BodyStream InputStream body) {
 *       ...
 *   }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BodyStream {
}
