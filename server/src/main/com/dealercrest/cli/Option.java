package com.dealercrest.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

/**
 * ${ENV_KEY:-FALLBACKVALUE}, for example: ${USER_NAME:-test123}
 * The general format is ${ENV_KEY:-FALLBACKVALUE}. The lookup order is:
 * 1, Environment variables (e.g., System.getenv("ENV_KEY"))
 * 2, The FALLBACK-VALUE if the key is not found in any of the above (e.g., test123 in the example)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Option {
    String shortName() default "";
    String longName();
    String desc() default "";
    boolean required() default false;
    Class<?> type() default String.class;
    String defaultValue() default "";
    String[] allowedValues() default {};
}
