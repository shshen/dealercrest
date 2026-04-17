package com.dealercrest.page;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a raw .html source file with data-layout, data-* metadata,
 * and named slot content via data-slot="slotName" divs.
 *
 * Format:
 * <pre>
 * <html data-layout="/layout/classic.html" data-title="Welcome">
 *     <div data-slot="body1">
 *         <h2>Content</h2>
 *     </div>
 *     <div data-slot="body2">
 *         <h2>More</h2>
 *     </div>
 *     <h2>ignored — no data-slot</h2>
 * </html>
 * </pre>
 */
public class HtmlPageSource {

    private final long lastModified;
    private final String layoutPath; // from data-layout
    private final Map<String, String> metadata; // all other data-* attrs (e.g. data-title)
    private final Map<String, String> slots; // data-slot name -> inner HTML

    private HtmlPageSource(
            long lastModified,
            String layoutPath,
            Map<String, String> metadata,
            Map<String, String> slots) {
        this.lastModified = lastModified;
        this.layoutPath = layoutPath;
        this.metadata = metadata;
        this.slots = slots;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getLayoutPath() {
        return layoutPath;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    // Convenience: common metadata keys
    public String getTitle() {
        return metadata.get("title");
    }

    public static HtmlPageSource parse(String html, long lastModified) {
        String layoutPath = null;
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        Map<String, String> slots = new LinkedHashMap<String, String>();

        // 1. Parse <html ...> attributes
        int htmlTagStart = html.indexOf("<html");
        int htmlTagEnd = html.indexOf('>', htmlTagStart);
        if (htmlTagStart >= 0 && htmlTagEnd > htmlTagStart) {
            String htmlTag = html.substring(htmlTagStart, htmlTagEnd + 1);
            Map<String, String> attrs = parseAttributes(htmlTag);

            layoutPath = attrs.remove("data-layout");
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
                if (entry.getKey().startsWith("data-")) {
                    // Strip the "data-" prefix for the metadata key
                    metadata.put(entry.getKey().substring(5), entry.getValue());
                }
            }
        }

        // 2. Find all <div data-slot="..."> blocks and capture inner HTML
        int searchFrom = htmlTagEnd + 1;
        int divStart = indexOfSlotDiv(html, searchFrom);
        while (divStart >= 0) {
            int attrEnd = html.indexOf('>', divStart);
            String divTag = html.substring(divStart, attrEnd + 1);
            Map<String, String> divAttrs = parseAttributes(divTag);
            String slotName = divAttrs.get("data-slot");

            // Find matching </div> — simple depth counter
            int innerStart = attrEnd + 1;
            int innerEnd = findClosingDiv(html, innerStart);

            if (slotName != null && innerEnd >= innerStart) {
                String innerHtml = html.substring(innerStart, innerEnd).trim();
                slots.put(slotName, innerHtml);
            }

            searchFrom = innerEnd + "</div>".length();
            divStart = indexOfSlotDiv(html, searchFrom);
        }

        return new HtmlPageSource(lastModified, layoutPath, metadata, slots);
    }

    /**
     * Finds the next <div that has a data-slot attribute.
     */
    private static int indexOfSlotDiv(String html, int from) {
        int pos = from;
        while (pos < html.length()) {
            int divPos = html.indexOf("<div", pos);
            if (divPos < 0)
                return -1;
            int tagEnd = html.indexOf('>', divPos);
            if (tagEnd < 0)
                return -1;
            String tag = html.substring(divPos, tagEnd + 1);
            if (tag.contains("data-slot"))
                return divPos;
            pos = tagEnd + 1;
        }
        return -1;
    }

    /**
     * Finds the index of the closing </div> that matches the opening <div>
     * starting just after its >. Handles nested divs.
     */
    private static int findClosingDiv(String html, int from) {
        int depth = 1;
        int pos = from;
        while (pos < html.length() && depth > 0) {
            int nextOpen = html.indexOf("<div", pos);
            int nextClose = html.indexOf("</div>", pos);
            if (nextClose < 0)
                break;
            if (nextOpen >= 0 && nextOpen < nextClose) {
                depth++;
                pos = nextOpen + 4;
            } else {
                depth--;
                if (depth == 0)
                    return nextClose;
                pos = nextClose + 6;
            }
        }
        return -1;
    }

    /**
     * Parses key="value" (or key='value') attribute pairs from a tag string.
     * Returns a mutable LinkedHashMap.
     */
    private static Map<String, String> parseAttributes(String tag) {
        Map<String, String> attrs = new LinkedHashMap<String, String>();
        int len = tag.length();
        int i = 0;

        // Skip past tag name
        while (i < len && tag.charAt(i) != ' ' && tag.charAt(i) != '>')
            i++;

        while (i < len) {
            // Skip whitespace
            while (i < len && Character.isWhitespace(tag.charAt(i)))
                i++;
            if (i >= len || tag.charAt(i) == '>' || tag.charAt(i) == '/')
                break;

            // Read attribute name
            int nameStart = i;
            while (i < len && tag.charAt(i) != '=' && tag.charAt(i) != ' '
                    && tag.charAt(i) != '>' && tag.charAt(i) != '/')
                i++;
            String name = tag.substring(nameStart, i).trim();
            if (name.isEmpty()) {
                i++;
                continue;
            }

            // Skip whitespace and '='
            while (i < len && (tag.charAt(i) == ' ' || tag.charAt(i) == '='))
                i++;

            // Read quoted value
            String value = "";
            if (i < len && (tag.charAt(i) == '"' || tag.charAt(i) == '\'')) {
                char quote = tag.charAt(i++);
                int valStart = i;
                while (i < len && tag.charAt(i) != quote)
                    i++;
                value = tag.substring(valStart, i);
                i++; // skip closing quote
            }

            attrs.put(name, value);
        }
        return attrs;
    }
}