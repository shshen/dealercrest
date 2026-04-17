package com.dealercrest.file;

public class FolderLock {

    public final String folder;
    public final String token;
    public final String owner;

    private volatile long acquiredAt;
    private volatile long ttlMs;

    public FolderLock(String folder, long ttlMs) {
        this.folder = folder;
        this.token = UUIDv7.generate();
        this.ttlMs = ttlMs;
        this.acquiredAt = System.nanoTime();
        this.owner = Thread.currentThread().getName();
    }

    public boolean isExpired() {
        long elapsedMs = (System.nanoTime() - acquiredAt) / 1_000_000;
        return elapsedMs > ttlMs;
    }

    void renew() {
        this.acquiredAt = System.nanoTime();
    }
}
