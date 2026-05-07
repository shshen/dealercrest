package com.dealercrest;

public abstract class Lifecycle {
    public abstract String getName();
    public abstract void start() throws Exception;
    public abstract void shutdown();
}
