package com.dealercrest.cmd;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.dealercrest.cli.Option;

@Retention(RetentionPolicy.RUNTIME)
public @interface HelpMixin {
    Option[] value() default {
        @Option(shortName = "h", longName = "help", desc = "Display help", type=boolean.class),
        @Option(longName = "preview", desc = "Print resolved config without executing", type=boolean.class)
    };
}
