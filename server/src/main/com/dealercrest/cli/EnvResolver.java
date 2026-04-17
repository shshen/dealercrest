package com.dealercrest.cli;

public class EnvResolver {

    /**
     * Resolves a string value. Supports:
     * 1. Static: "123"
     * 2. Strict: "${PASSWORD}" (Throws if missing)
     * 3. Empty Fallback: "${PASSWORD:-}" (Throws because it's empty)
     * 4. Normal Fallback: "${PASSWORD:-12345}"
     */
    public String resolve(String input) {
        // 1. Static Value Check: If it doesn't look like ${...}, return as-is
        if (input == null || !input.startsWith("${") || !input.endsWith("}")) {
            return input;
        }
        // Extract "KEY:-FALLBACK" from "${KEY:-FALLBACK}"
        String content = input.substring(2, input.length() - 1);
        int separatorIndex = content.indexOf(":-");
        String key;
        String fallback = null;
        boolean hasSeparator = (separatorIndex != -1);
        if (hasSeparator) {
            key = content.substring(0, separatorIndex).trim();
            fallback = content.substring(separatorIndex + 2).trim();
        } else {
            key = content.trim();
        }
        // Try to fetch from System Environment
        String envValue = System.getenv(key);
        // Case: Variable found in environment
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        // Case: Strict - ${KEY} (No fallback provided)
        if (!hasSeparator) {
            return "";
        }
        // Case: Empty Fallback - ${KEY:-}
        if (fallback == null || fallback.isEmpty()) {
            return "";
        }
        // Case: Successful Fallback - ${KEY:-DEFAULT}
        return fallback;
    }
}
