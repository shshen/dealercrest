package com.dealercrest.page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a parsed website builder block.
 *
 * <p>
 * Use {@link #from(String)} to create an instance from a block HTML string.
 * No external dependencies, no regex, no lambdas.
 *
 * <p>
 * Example input:
 * 
 * <pre>{@code
 * <footer
 *   data-schema-name="Footer — Standard"
 *   data-schema-fields="logo,copyright"
 *   data-schema-layout="layout.html"
 *   data-schema-preview="themes/modern/footer/preview.jpg">
 *   <img data-field="logo" src="{{logo}}" />
 *   <p data-field="copyright">{{copyright}}</p>
 * </footer>
 * }</pre>
 *
 * <p>
 * Required: {@code data-schema-name}, {@code data-schema-fields},
 * {@code data-schema-layout}, {@code data-schema-preview}.
 *
 * <p>
 * Optional: {@code data-schema-description},
 * {@code data-schema-render} (default: "static"), {@code data-schema-order}.
 */
public class BlockConfig implements Comparable<BlockConfig> {

    private final String name;
    private final String description;
    private final String layout;
    private final String preview;
    private final String render;
    private final List<String> fields;
    private final Integer order;
    private final String htmlContent;

    private BlockConfig(String name, String description, String layout, String preview,
            String render, List<String> fields, Integer order, String htmlContent) {
        this.name = name;
        this.description = description;
        this.layout = layout;
        this.preview = preview;
        this.render = render;
        this.fields = fields;
        this.order = order;
        this.htmlContent = htmlContent;
    }

    public static BlockConfig from(String html) {
        if (html == null || html.isBlank()) {
            throw new ParseException("Block HTML must not be empty.");
        }

        String trimmed = html.trim();
        if (trimmed.charAt(0) != '<') {
            throw new ParseException("Block HTML must start with an element tag.");
        }

        // ── Locate opening tag
        int openTagEnd = indexOfOpenTagClose(trimmed);
        if (openTagEnd < 0) {
            throw new ParseException("Could not find closing '>' of root opening tag.");
        }

        String openTag = trimmed.substring(0, openTagEnd + 1);

        // ── Extract tag name
        int nameStart = 1;
        int nameEnd = nameStart;
        while (nameEnd < openTag.length()
                && !Character.isWhitespace(openTag.charAt(nameEnd))
                && openTag.charAt(nameEnd) != '>'
                && openTag.charAt(nameEnd) != '/') {
            nameEnd++;
        }
        String tagName = openTag.substring(nameStart, nameEnd);
        if (tagName.isBlank()) {
            throw new ParseException("Could not determine root element tag name.");
        }

        // ── Ensure exactly one root element
        assertSingleRoot(trimmed, tagName);

        // ── Parse attributes
        String attrText = openTag.substring(nameEnd, openTag.length() - 1).trim();
        Map<String, String> attrs = parseAttributes(attrText);

        // ── Required attributes
        String name = requireAttr(attrs, "data-schema-name", tagName);
        String layout = requireAttr(attrs, "data-schema-layout", tagName);
        String preview = requireAttr(attrs, "data-schema-preview", tagName);
        String rawFields = requireAttr(attrs, "data-schema-fields", tagName);

        // ── Optional attributes
        String description = nullIfBlank(attrs.get("data-schema-description"));
        String render = nullIfBlank(attrs.get("data-schema-render"));
        String orderStr = nullIfBlank(attrs.get("data-schema-order"));

        if (render == null)
            render = "static";

        Integer order = null;
        if (orderStr != null) {
            try {
                order = Integer.parseInt(orderStr);
            } catch (NumberFormatException e) {
                throw new ParseException(
                        "data-schema-order must be an integer, got: \"" + orderStr + "\".");
            }
        }

        // ── Parse fields list
        List<String> fields = splitFields(rawFields);
        if (fields.isEmpty()) {
            throw new ParseException("data-schema-fields must contain at least one field key.");
        }
        // List<String> unknownKeys = FieldRegistry.getInstance().findUnknownKeys(fields);
        // if (!unknownKeys.isEmpty()) {
        //     throw new ParseException(
        //             "Unknown field keys in <" + tagName + ">: " + unknownKeys);
        // }

        // ── Strip data-schema-* attrs → clean htmlContent
        String cleanOpenTag = stripSchemaAttrs(tagName, attrs);
        String htmlContent = cleanOpenTag + trimmed.substring(openTagEnd + 1);

        return new BlockConfig(name, description, layout, preview, render, fields, order, htmlContent);
    }

    /**
     * Returns the index of the '>' that closes the root opening tag,
     * skipping '>' characters inside quoted attribute values.
     */
    private static int indexOfOpenTagClose(String html) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 1; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote && c == '>')
                return i;
        }
        return -1;
    }

    /**
     * Parses key="value" pairs from the attribute portion of an opening tag.
     * Handles both single and double quoted values.
     */
    private static Map<String, String> parseAttributes(String attrText) {
        Map<String, String> attrs = new LinkedHashMap<String, String>();
        int i = 0;
        int len = attrText.length();

        while (i < len) {
            // Skip whitespace
            while (i < len && Character.isWhitespace(attrText.charAt(i)))
                i++;
            if (i >= len)
                break;

            // Read key
            int keyStart = i;
            while (i < len
                    && attrText.charAt(i) != '='
                    && !Character.isWhitespace(attrText.charAt(i)))
                i++;
            String key = attrText.substring(keyStart, i).trim();
            if (key.isEmpty()) {
                i++;
                continue;
            }

            // Skip to '='
            while (i < len && Character.isWhitespace(attrText.charAt(i)))
                i++;
            if (i >= len || attrText.charAt(i) != '=')
                continue;
            i++; // skip '='
            while (i < len && Character.isWhitespace(attrText.charAt(i)))
                i++;

            // Read quoted value
            if (i >= len)
                break;
            char quote = attrText.charAt(i);
            if (quote != '"' && quote != '\'')
                continue;
            i++; // skip opening quote

            int valueStart = i;
            while (i < len && attrText.charAt(i) != quote)
                i++;
            String value = attrText.substring(valueStart, i);
            if (i < len)
                i++; // skip closing quote

            attrs.put(key, value);
        }

        return attrs;
    }

    /**
     * Splits a comma-separated fields string into a trimmed list,
     * ignoring empty entries.
     */
    private static List<String> splitFields(String raw) {
        List<String> result = new ArrayList<String>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Rebuilds the opening tag keeping only non-schema attributes.
     */
    private static String stripSchemaAttrs(String tagName, Map<String, String> attrs) {
        StringBuilder sb = new StringBuilder("<").append(tagName);
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            if (!entry.getKey().startsWith("data-schema-")) {
                sb.append(' ')
                        .append(entry.getKey())
                        .append("=\"")
                        .append(entry.getValue())
                        .append('"');
            }
        }
        sb.append('>');
        return sb.toString();
    }

    /**
     * Walks the full HTML character by character tracking tag depth.
     * Counts how many top-level (depth == 0) opening tags are encountered.
     * Any tag name at depth 0 counts as a root — not just the first tag name.
     */
    private static void assertSingleRoot(String html, String rootTagName) {
        int roots = 0;
        int depth = 0;
        int i = 0;
        int len = html.length();

        while (i < len) {
            // Skip until we hit a '<'
            if (html.charAt(i) != '<') {
                i++;
                continue;
            }

            // Peek ahead to determine tag type
            int j = i + 1;

            // Skip whitespace after '<'
            while (j < len && Character.isWhitespace(html.charAt(j)))
                j++;
            if (j >= len)
                break;

            boolean isClosing = html.charAt(j) == '/';
            if (isClosing)
                j++; // skip '/'

            // Read tag name
            int nameStart = j;
            while (j < len
                    && !Character.isWhitespace(html.charAt(j))
                    && html.charAt(j) != '>'
                    && html.charAt(j) != '/') {
                j++;
            }
            String tagName = html.substring(nameStart, j).toLowerCase();
            if (tagName.isEmpty()) {
                i++;
                continue;
            }

            // Find closing '>' of this tag, respecting quoted attributes
            int tagClose = indexOfOpenTagClose(html.substring(i));
            boolean isSelfClosing = tagClose > 0
                    && html.charAt(i + tagClose - 1) == '/';

            if (isClosing) {
                depth--;
            } else if (isSelfClosing) {
                // self-closing tag: <img /> — counts as root if at depth 0
                if (depth == 0)
                    roots++;
                // depth unchanged
            } else {
                if (depth == 0)
                    roots++;
                depth++;
            }

            if (roots > 1) {
                throw new ParseException(
                        "Block HTML must have exactly one root element, found more than one.");
            }

            // Advance past this tag
            i = (tagClose >= 0) ? i + tagClose + 1 : j;
        }
    }

    private static String requireAttr(Map<String, String> attrs, String attr, String tagName) {
        String value = attrs.get(attr);
        if (value == null || value.isBlank()) {
            throw new ParseException(
                    "Missing required attribute \"" + attr + "\" on <" + tagName + ">.");
        }
        return value.trim();
    }

    private static String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLayout() {
        return layout;
    }

    public String getPreview() {
        return preview;
    }

    public String getRender() {
        return render;
    }

    public List<String> getFields() {
        return fields;
    }

    public Integer getOrder() {
        return order;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", layout='" + layout + '\'' +
                ", preview='" + preview + '\'' +
                ", render='" + render + '\'' +
                ", fields=" + fields +
                ", order=" + order +
                '}';
    }

    @Override
    public int compareTo(BlockConfig o) {
        return Integer.compare(
                this.order != null ? this.order : Integer.MAX_VALUE,
                o.order != null ? o.order : Integer.MAX_VALUE);
    }

}
