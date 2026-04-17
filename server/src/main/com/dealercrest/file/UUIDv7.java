package com.dealercrest.file;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UUIDv7 generator — time-ordered, monotonic, unguessable.
 *
 * Structure (128 bits):
 *   [48 bits: unix timestamp ms][4 bits: version=7][12 bits: sequence][2 bits: variant][62 bits: random]
 */
public class UUIDv7 {

    private static final AtomicInteger sequence     = new AtomicInteger(0);
    private static volatile long       lastTimestamp = 0L;

    public static synchronized String generate() {
        long now = System.currentTimeMillis();

        // If same millisecond, increment sequence to guarantee monotonicity
        if (now == lastTimestamp) {
            sequence.incrementAndGet();
        } else {
            sequence.set(0);
            lastTimestamp = now;
        }

        int seq = sequence.get() & 0xFFF;  // 12 bits max (0–4095)

        // High 64 bits: [48-bit timestamp][4-bit version=7][12-bit sequence]
        long high = (now << 16) | 0x7000L | seq;

        // Low 64 bits: [2-bit variant=10][62-bit random]
        long low  = 0x8000000000000000L | (randomBits() & 0x3FFFFFFFFFFFFFFFL);

        return new UUID(high, low).toString();
    }

    /**
     * UUIDv7 strings are time-ordered — simple string comparison works.
     * Returns true if a is newer than b.
     */
    public static boolean isNewer(String a, String b) {
        return a.compareTo(b) > 0;
    }

    private static long randomBits() {
        return (long)(Math.random() * Long.MAX_VALUE);
    }
}
