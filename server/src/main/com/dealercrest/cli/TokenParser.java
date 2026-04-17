package com.dealercrest.cli;

import java.util.*;

public class TokenParser {

    public Map<String,String> parse(List<String> tokens) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (!isOption(token)) {
                continue;
            }
            String key;
            String value;
            int eqIndex = token.indexOf('=');
            // Case: --key=value or -k=value
            if (eqIndex > 0) {
                key = normalizeKey(token.substring(0, eqIndex));
                value = token.substring(eqIndex + 1);
                result.put(key, value);
                continue;
            }
            // Case: --key value
            key = normalizeKey(token);
            if (hasNextValue(tokens, i)) {
                value = tokens.get(++i);
            } else {
                value = "";
            }
            result.put(key, value);
        }
        return result;
    }

    private boolean hasNextValue(List<String> tokens, int index) {
        return index + 1 < tokens.size()
                && !isOption(tokens.get(index + 1));
    }

    private boolean isOption(String token) {
        return token.startsWith("-") && token.length() > 1;
    }

    private String normalizeKey(String rawKey) {
        if (rawKey.startsWith("--")) {
            return rawKey.substring(2);
        } else if (rawKey.startsWith("-")) {
            return rawKey.substring(1);
        }
        return rawKey;
    }

}
