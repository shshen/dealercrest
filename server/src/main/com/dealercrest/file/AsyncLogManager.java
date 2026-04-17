package com.dealercrest.file;

import java.nio.file.Path;
import java.time.Duration;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncLogManager {

    private final MpscQueue<String> mpscQueue;
    private final Thread writerThread;
    private final QueueDrainTask queueTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AsyncLogManager(Path directory,
                       String prefix,
                       String threadName,
                       long maxFileSizeBytes,
                       int ringSizePowerOfTwo) throws IOException {
        RotatingFile rotateFile =
                new RotatingFile(directory, prefix, maxFileSizeBytes);

        this.mpscQueue = new MpscQueue<>(ringSizePowerOfTwo);
        this.queueTask = new QueueDrainTask(mpscQueue, rotateFile);
        this.writerThread = new Thread(queueTask, threadName);
    }

    /**
     * Starts the logger thread safely. Can be called multiple times without effect.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            writerThread.start();
        }
    }

    public void log(String message) {
        // Only accept messages if running
        if (!running.get()) return;
        mpscQueue.put(message);
    }

    public String getName() {
        return writerThread.getName();
    }

    public void shutdown() {
        if (!running.get()) return; // already stopped
        running.set(false); // stop accepting
        queueTask.requestStop();
        try {
            writerThread.join(Duration.ofSeconds(5).toMillis());
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}