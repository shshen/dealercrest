package com.dealercrest.cmd;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.dealercrest.cli.Option;

@Retention(RetentionPolicy.RUNTIME)
public @interface ServerMixin {
    Option[] value() default {
            @Option(shortName = "l", longName = "level", desc = "Set log level", defaultValue = "INFO", allowedValues = {
                    "FINE", "INFO", "WARNING", "SEVERE" }),
            @Option(shortName = "k", longName = "keystorePassword", desc = "Keystore password", 
                    defaultValue="${KEYSTORE_PSWD}"),
            @Option(shortName = "o", longName = "output", desc = "Log output destination", defaultValue = "file", allowedValues = {
                    "console", "file" }),
            @Option(shortName = "e", longName = "environment", desc = "Environment", defaultValue = "prod", allowedValues = {
                    "dev", "prod" }),
            @Option(shortName = "h", longName = "hostname", desc = "Local host name")
    };
}