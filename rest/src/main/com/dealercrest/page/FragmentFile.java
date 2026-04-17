package com.dealercrest.page;

import java.util.LinkedHashMap;
import java.util.Map;

public class FragmentFile {

    private String layout;
    private Map<String, String> attributes;
    private Map<String, String> placeholderValues;

    private FragmentFile(String layout, Map<String, String> attributes, Map<String, String> placeholderValues) {
        this.layout = layout;
        this.attributes = attributes;
        this.placeholderValues = placeholderValues;
    }

    /**
     * Parse raw HTML content into a Page.
     * Returns null if the <html> tag does not contain a layout= attribute.
     */
    public static FragmentFile parse(String html) {
        Map<String, String> attributes = parseHtmlTag(html);
        String layout = attributes.remove("layout");
        if (layout == null) {
            return null;
        }
        Map<String, String> placeholderValues = parsePlaceholders(html);
        return new FragmentFile(layout, attributes, placeholderValues);
    }

    private static Map<String, String> parseHtmlTag(String html) {
        Map<String, String> attributes = new LinkedHashMap<>();

        int start = html.indexOf("<html");
        if (start == -1) return attributes;

        int end = findTagEnd(html, start + 5);
        if (end == -1) return attributes;

        String tagContent = html.substring(start + 5, end).trim();

        int i = 0;
        while (i < tagContent.length()) {

            // Skip whitespace
            while (i < tagContent.length() && Character.isWhitespace(tagContent.charAt(i))) i++;
            if (i >= tagContent.length()) break;

            // Read key
            int keyStart = i;
            while (i < tagContent.length()
                    && tagContent.charAt(i) != '='
                    && !Character.isWhitespace(tagContent.charAt(i))) i++;
            String key = tagContent.substring(keyStart, i).trim();
            if (key.isEmpty()) break;

            // Skip whitespace and '='
            while (i < tagContent.length()
                    && (Character.isWhitespace(tagContent.charAt(i)) || tagContent.charAt(i) == '=')) i++;
            if (i >= tagContent.length()) break;

            // Read quoted value
            char quote = tagContent.charAt(i++);
            int valueStart = i;
            while (i < tagContent.length() && tagContent.charAt(i) != quote) i++;
            String value = tagContent.substring(valueStart, i);
            i++; // skip closing quote

            attributes.put(key, value);
        }

        return attributes;
    }

    /**
     * Find the closing '>' of a tag, skipping over quoted attribute values
     * so that a '>' inside a value (e.g. title="a > b") is not mistaken
     * for the tag end.
     */
    private static int findTagEnd(String html, int start) {
        int i = start;
        while (i < html.length()) {
            char c = html.charAt(i);
            if (c == '"' || c == '\'') {
                char quote = c;
                i++;
                while (i < html.length() && html.charAt(i) != quote) i++;
            } else if (c == '>') {
                return i;
            }
            i++;
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Parse placeholder blocks  <!-- $begin:name --> ... <!-- $end:name -->
    // -------------------------------------------------------------------------
    private static Map<String, String> parsePlaceholders(String html) {
        Map<String, String> placeholders = new LinkedHashMap<>();

        String beginPrefix = "<!-- $begin:";
        String endPrefix   = "<!-- $end:";
        String closeSuffix = " -->";

        int i = 0;
        while (i < html.length()) {
            int beginIdx = html.indexOf(beginPrefix, i);
            if (beginIdx == -1) break;

            // Extract the placeholder name
            int nameStart = beginIdx + beginPrefix.length();
            int nameEnd   = html.indexOf(closeSuffix, nameStart);
            if (nameEnd == -1) break;
            String name = html.substring(nameStart, nameEnd).trim();

            // Find the matching $end tag
            String endTag  = endPrefix + name + " -->";
            int contentStart = nameEnd + closeSuffix.length();
            int endIdx = html.indexOf(endTag, contentStart);
            if (endIdx == -1) break;

            String content = html.substring(contentStart, endIdx).trim();
            placeholders.put(name, content);

            i = endIdx + endTag.length();
        }

        return placeholders;
    }

    public String getLayout() { return layout; }
    public Map<String, String> getAttributes() { return attributes; }
    public Map<String, String> getPlaceholderValues() { return placeholderValues; }

    @Override
    public String toString() {
        return "Page{layout='" + layout + "', attributes=" + attributes + ", placeholders=" + placeholderValues + "}";
    }
}
