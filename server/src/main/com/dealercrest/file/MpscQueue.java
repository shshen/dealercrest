package com.dealercrest.file;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * High-performance MPSC (Multi-Producer Single-Consumer) Queue.
 * Features:
 * - Cache-line padding to prevent false sharing.
 * - VarHandle-based memory barriers (Acquire/Release).
 * - Optimized consumer signaling (only unparks if consumer is waiting).
 * - Configurable progressive backoff.
 * - Non-blocking offer() variant.
 */
abstract class MpscPadding1 { long p01,p02,p03,p04,p05,p06,p07,p08; }
abstract class MpscProducerIndex extends MpscPadding1 { protected volatile long producerIndex; }
abstract class MpscPadding2 extends MpscProducerIndex { long p11,p12,p13,p14,p15,p16,p17,p18; }
abstract class MpscConsumerIndex extends MpscPadding2 { protected long consumerIndex; }
abstract class MpscPadding3 extends MpscConsumerIndex { long p21,p22,p23,p24,p25,p26,p27,p28; }

public final class MpscQueue<T> extends MpscPadding3 {

    private final Object[] buffer;
    private final long[] sequences;
    private final int mask;
    private final int capacity;

    private volatile Thread consumerThread;
    private volatile boolean consumerWaiting = false;

    // Configurable backoff parameters
    private final int producerSpinThreshold;
    private final int producerYieldThreshold;

    private static final VarHandle QA;
    private static final VarHandle SA;
    private static final VarHandle P_IDX;

    static {
        try {
            QA = MethodHandles.arrayElementVarHandle(Object[].class);
            SA = MethodHandles.arrayElementVarHandle(long[].class);
            P_IDX = MethodHandles.lookup()
                    .findVarHandle(MpscProducerIndex.class, "producerIndex", long.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // --------------------------------------------------
    // Constructors with backoff tuning
    // --------------------------------------------------
    public MpscQueue(int capacity) {
        this(capacity, 100, 200, 1000, 2000);
    }

    public MpscQueue(int capacity,
                     int producerSpinThreshold,
                     int producerYieldThreshold,
                     int consumerSpinThreshold,
                     int consumerYieldThreshold) {
        if (Integer.bitCount(capacity) != 1) throw new IllegalArgumentException("Capacity must be power of 2");
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new Object[capacity];
        this.sequences = new long[capacity];

        for (int i = 0; i < capacity; i++) SA.set(sequences, i, (long) i);

        this.producerSpinThreshold = producerSpinThreshold;
        this.producerYieldThreshold = producerYieldThreshold;
    }

    // --------------------------------------------------
    // PRODUCER: Blocking put with backoff
    // --------------------------------------------------
    public void put(T element) {
        if (element == null) throw new NullPointerException();
        int fullSpins = 0;

        while (true) {
            long p = (long) P_IDX.getVolatile(this);
            int slot = (int) (p & mask);
            long seq = (long) SA.getAcquire(sequences, slot);
            long diff = seq - p;

            if (diff == 0) {
                if (P_IDX.compareAndSet(this, p, p + 1)) {
                    QA.setRelease(buffer, slot, element);
                    SA.setRelease(sequences, slot, p + 1);
                    // Only unpark if consumer is actually waiting
                    signalConsumer();
                    return;
                }
                fullSpins = 0; // reset on CAS failure
            } else if (diff < 0) {
                // Queue full: progressive backoff
                fullSpins++;
                if (fullSpins < producerSpinThreshold) Thread.onSpinWait();
                else if (fullSpins < producerYieldThreshold) Thread.yield();
                else LockSupport.parkNanos(1_000); // 1 microsecond park
            } else {
                // Another producer advanced
                fullSpins = 0;
                Thread.onSpinWait();
            }
        }
    }

    // --------------------------------------------------
    // PRODUCER: Non-blocking offer
    // Returns false immediately if queue is full
    // --------------------------------------------------
    public boolean offer(T element) {
        if (element == null) throw new NullPointerException();

        long p = (long) P_IDX.getVolatile(this);
        int slot = (int) (p & mask);
        long seq = (long) SA.getAcquire(sequences, slot);
        long diff = seq - p;

        if (diff == 0 && P_IDX.compareAndSet(this, p, p + 1)) {
            QA.setRelease(buffer, slot, element);
            SA.setRelease(sequences, slot, p + 1);
            signalConsumer();
            return true;
        }
        return false; // queue full
    }

    // --------------------------------------------------
    // SINGLE CONSUMER: poll (non-blocking)
    // --------------------------------------------------
    public T poll() {
        long c = consumerIndex;
        int slot = (int) (c & mask);
        long seq = (long) SA.getAcquire(sequences, slot);
        long diff = seq - (c + 1);

        if (diff == 0) {
            T e = (T) QA.getAcquire(buffer, slot);
            QA.setRelease(buffer, slot, null);
            SA.setRelease(sequences, slot, c + capacity);
            consumerIndex = c + 1;
            return e;
        }
        return null;
    }

    public void registerConsumer() {
        Thread current = Thread.currentThread();

        if (consumerThread == null) {
            consumerThread = current;
        } else if (consumerThread != current) {
            throw new IllegalStateException(
                "MpscQueue supports only one consumer thread");
        }
    }

    public boolean isEmpty() {
        // Only safe to call from the consumer thread
        return consumerIndex == (long) P_IDX.getVolatile(this);
    }

    private void signalConsumer() {
        if (consumerWaiting) { // Only pay for unpark if consumer is asleep
            Thread t = consumerThread;
            if (t != null) {
                LockSupport.unpark(t);
            }
        }
    }

    public void setConsumerWaiting(boolean waiting) {
        this.consumerWaiting = waiting;
    }

}
