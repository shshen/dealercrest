package com.dealercrest.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command {
    String name();
    String group() default "General";
    String desc() default "";
    boolean hidden() default false;
    Class<? extends Annotation>[] mixins() default {}; 
    Option[] options() default {};
}