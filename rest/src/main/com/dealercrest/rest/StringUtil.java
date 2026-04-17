package com.dealercrest.rest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringUtil {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static byte[] truncateToBytes(String str, int maxBytes) {
        if (str == null) {
            return null;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return bytes;
        }
        // Find the last valid character index within the byte limit
        int index = maxBytes - 1;
        while (index >= 0 && (bytes[index] & 0x80) != 0) {
            index--; // Move backwards until you find a non-continuation byte
        }
        return Arrays.copyOf(bytes, index + 1);
        // return new String(bytes, 0, index + 1, StandardCharsets.UTF_8);
    }

    public static String convert(InputStream inputStream) {
        try {
            if (inputStream == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
