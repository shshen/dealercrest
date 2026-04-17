package com.dealercrest.file;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LockManager {

    private final long defaultTtlMs;
    private final ConcurrentHashMap<String, FolderLock> locks;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition freed = lock.newCondition();
    private static final long DEFAULT_TTL_MS = 60 * 1000L; // 1 minute
    private static final Logger logger = Logger.getLogger(LockManager.class.getName());

    public LockManager() {
        this(DEFAULT_TTL_MS);
    }

    public LockManager(long defaultTtlMs) {
        this.defaultTtlMs = defaultTtlMs;
        this.locks = new ConcurrentHashMap<>();
    }

    public FolderLock acquire(String folder, long timeoutMs)
            throws TimeoutException, InterruptedException {

        long waitMs = timeoutMs > 0 ? timeoutMs : 5_000L;

        lock.lock();
        try {
            // First attempt
            FolderLock folderLock = tryAcquire(folder);
            if (folderLock != null)
                return folderLock;

            // Wait once for any lock to be released
            freed.await(waitMs, TimeUnit.MILLISECONDS);

            // Second attempt
            folderLock = tryAcquire(folder);
            if (folderLock != null)
                return folderLock;

            throw new TimeoutException(String.format(
                    "Could not acquire lock on '%s' — still held after %dms", folder, waitMs));

        } finally {
            lock.unlock();
        }
    }

    public FolderLock acquire(String folder) throws InterruptedException {
        try {
            return acquire(folder, 0);
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean renew(String folder, String token) {
        lock.lock();
        try {
            FolderLock existing = locks.get(folder);
            if (existing == null || !existing.token.equals(token)) {
                logger.warning(String.format(
                        "Renew failed on '%s': token is stale or lock gone.", folder));
                return false;
            }
            existing.renew();
            logger.fine(String.format("Lock RENEWED on '%s'", folder));
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean release(String folder, String token) {
        lock.lock();
        try {
            FolderLock existing = locks.get(folder);
            if (existing == null) {
                logger.warning(String.format("Release on '%s' but no lock exists.", folder));
                return false;
            }
            if (!existing.token.equals(token)) {
                logger.warning(String.format(
                        "Stale release on '%s': token mismatch. Ignoring.", folder));
                return false;
            }
            locks.remove(folder);
            freed.signalAll(); // wake up anyone waiting in acquire()
            logger.info(String.format("Lock RELEASED on '%s'", folder));
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isValid(String folder, String token) {
        lock.lock();
        try {
            FolderLock existing = locks.get(folder);
            return existing != null
                    && existing.token.equals(token)
                    && !existing.isExpired();
        } finally {
            lock.unlock();
        }
    }

    private FolderLock tryAcquire(String folder) {
        FolderLock existing = locks.get(folder);
        if (existing == null || existing.isExpired()) {
            if (existing != null) {
                logger.warning(String.format(
                        "Lock on '%s' expired (owner='%s'). Auto-releasing.",
                        folder, existing.owner));
            }
            FolderLock newLock = new FolderLock(folder, defaultTtlMs);
            locks.put(folder, newLock);
            logger.info(String.format(
                    "Lock ACQUIRED on '%s' (token=%s, owner='%s')",
                    folder, newLock.token, newLock.owner));
            return newLock;
        }
        return null;
    }

}
