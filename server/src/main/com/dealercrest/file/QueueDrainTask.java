package com.dealercrest.file;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.LockSupport;
import java.io.IOException;

final class QueueDrainTask implements Runnable {

    private final MpscQueue<String> mpscQueue;
    private final RotatingFile fileManager;

    // Buffer Requirement 3: 64KB internal memory buffer
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);

    private final int maxMessageCount = 4;

    // Requirement 2: Track line count to trigger sync
    private int linesInBatch = 0;

    private volatile boolean stopRequested = false;

    public QueueDrainTask(MpscQueue<String> mpscQueue, RotatingFile fileManager) {
        this.mpscQueue = mpscQueue;
        this.fileManager = fileManager;
    }

    @Override
    public void run() {
        try {
            mpscQueue.registerConsumer();

            while (!stopRequested || !mpscQueue.isEmpty()) {
                // Batch and write all available messages
                boolean hadWork = consumeAndBatch();

                if (!hadWork && !stopRequested) {
                    parkConsumer();
                }
            }

            // Flush remaining data before closing
            flushBuffer();
            fileManager.channel().force(false);
            fileManager.close();

            System.out.println("QueueDrainTask stopped gracefully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Requirement 3: Write to buffer first, do not write to file every time.
     */
    private boolean consumeAndBatch() throws IOException {
        String event = mpscQueue.poll();
        if (event == null) {
            return false;
        }

        while (event != null) {
            byte[] bytes = event.getBytes(StandardCharsets.UTF_8);
            int totalNeeded = bytes.length + 1; // +1 for '\n'
            // Requirement 1: Handle very large messages without resizing buffer
            if (totalNeeded > buffer.capacity()) {
                // Flush existing small logs first to maintain chronological order
                flushBuffer();
                // Requirement 2: Direct write for large message
                fileManager.rotateIfNeeded();
                writeDirectly(bytes);
                // Reset counter because we just performed disk I/O
                linesInBatch = 0;
            } else {
                // Check if the current message fits in the remaining buffer space
                if (buffer.remaining() < totalNeeded) {
                    flushBuffer();
                }
                // Buffer the message
                fileManager.rotateIfNeeded();
                buffer.put(bytes);
                // buffer.put((byte) '\n');
                linesInBatch++;
                // Requirement 2: Sync to file every maxMessageCount messages
                if (linesInBatch >= maxMessageCount) {
                    flushBuffer();
                }
            }
            // Continue polling if we haven't hit the batch limit yet
            if (linesInBatch > 0 && linesInBatch < maxMessageCount) {
                event = mpscQueue.poll();
            } else {
                event = null;
            }
        }
        return true;
    }

    private void writeDirectly(byte[] bytes) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        while (wrap.hasRemaining()) {
            fileManager.channel().write(wrap);
        }
        fileManager.incrementSize(bytes.length);
    }

    private void flushBuffer() throws IOException {
        if (buffer.position() == 0) {
            return;
        }
        int bytesToWrite = buffer.position();
        buffer.flip(); // Switch to read mode for the channel
        while (buffer.hasRemaining()) {
            fileManager.channel().write(buffer);
        }
        fileManager.incrementSize(bytesToWrite);
        buffer.clear(); // Reset for next batch of writes
        linesInBatch = 0; // Reset counter after physical file write
    }

    /**
     * Park consumer when no messages are available, but allow unpark from
     * producers.
     */
    private void parkConsumer() {
        mpscQueue.setConsumerWaiting(true); // Sign on the door: "I'm sleeping"
        try {
            // Double-check to avoid missed notifications
            if (mpscQueue.isEmpty()) {
                LockSupport.park();
            }
        } finally {
            mpscQueue.setConsumerWaiting(false); // Sign off: "I'm awake"
        }
    }

    /**
     * Request the logging thread to stop gracefully.
     */
    public void requestStop() {
        stopRequested = true;
    }
}
