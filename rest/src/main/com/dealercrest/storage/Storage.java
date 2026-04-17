package com.dealercrest.storage;

public abstract class Storage {

    public abstract void put(String path, byte[] data);

    public abstract String getUrl(String path);
    
}
