package com.dealercrest.page;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TemplateEngine — a lightweight, segment-based HTML template processor.
 *
 * <p>Templates are plain HTML files that use {@code th:*} attributes for dynamic
 * behaviour, similar in style to Thymeleaf. Each template is parsed once into a
 * segment tree and cached for the lifetime of the engine instance.
 *
 * <h2>Setup</h2>
 * <pre>
 *   Path templateDir = Path.of("src/main/resources/templates");
 *   TemplateEngine engine = new TemplateEngine(templateDir);
 * </pre>
 *
 * <h2>Context data</h2>
 * <p>Pass a {@code Map&lt;String, Object&gt;} whose values may be any of:
 * {@code String}, {@code Number}, {@code Boolean}, {@code List},
 * {@code Map&lt;String,Object&gt;}, {@code JSONObject}, or {@code JSONArray}.
 * As a convenience you can also pass a {@code JSONObject} directly and its
 * top-level keys become the context variables.
 *
 * <pre>
 *   // Option A — Map context (mix of static JSON and live DB data)
 *   JSONObject pageJson = new JSONObject(Files.readString(Path.of("pages/home.json")));
 *   Map&lt;String, Object&gt; ctx = TemplateEngine.jsonObjectToMap(pageJson);
 *   ctx.put("vehicles", vehicleRepo.findByDealer(dealerId));  // List from DB
 *   ctx.put("user", session.getUser());
 *   String html = engine.process("home.html", ctx);
 *
 *   // Option B — JSONObject shortcut (all data already in JSON)
 *   JSONObject data = new JSONObject();
 *   data.put("dealer", dealerJson);
 *   data.put("vehicles", vehicleArray);
 *   String html = engine.process("home.html", data);
 * </pre>
 *
 * <h2>Supported th: directives</h2>
 * <pre>
 *   th:text="${expr}"          sets element text content (HTML-escaped)
 *   th:href="${expr}"          sets the href attribute
 *   th:src="${expr}"           sets the src attribute
 *   th:alt="${expr}"           sets the alt attribute
 *   th:style="${expr}"         sets the style attribute
 *   th:classappend="${expr}"   appends a dynamic CSS class
 *   th:if="${expr}"            renders element only when expression is truthy
 *   th:unless="${expr}"        renders element only when expression is falsy
 *   th:each="v : ${list}"     repeats element once per item in a List or JSONArray
 * </pre>
 *
 * <h2>Expressions</h2>
 * <p>Expressions are written as {@code ${...}} and support dot-path navigation
 * ({@code ${user.address.city}}), string literals ({@code ${'Hello'}}),
 * arithmetic ({@code ${price * qty}}), comparisons ({@code ==}, {@code !=},
 * {@code <}, {@code >}, {@code <=}, {@code >=}), ternary
 * ({@code ${stock > 0 ? 'In Stock' : 'Sold Out'}}), and the boolean/null
 * literals {@code true}, {@code false}, {@code null}.
 *
 * <h2>Utility functions</h2>
 * <pre>
 *   ${#strings.toLowerCase(name)}
 *   ${#strings.replace(slug, '-', ' ')}
 *   ${#numbers.formatInteger(price, 1, 'COMMA')}
 *   ${#lists.isEmpty(items)}
 *   ${#lists.size(items)}
 * </pre>
 *
 * <h2>Template example</h2>
 * <pre>
 *   &lt;h1 th:text="${dealer.name}"&gt;&lt;/h1&gt;
 *   &lt;a th:href="${hero.ctaLink}" th:text="${hero.ctaText}"&gt;&lt;/a&gt;
 *
 *   &lt;div th:each="v : ${vehicles}"&gt;
 *     &lt;h3 th:text="${v.make} + ' ' + ${v.model}"&gt;&lt;/h3&gt;
 *     &lt;p  th:text="${#numbers.formatInteger(v.price, 1, 'COMMA')}"&gt;&lt;/p&gt;
 *     &lt;span th:if="${v.featured}"&gt;Featured&lt;/span&gt;
 *   &lt;/div&gt;
 * </pre>
 *
 * <h2>Dot-path resolution</h2>
 * <p>The resolver walks dot-separated segments through nested {@code Map},
 * {@code JSONObject}, and {@code JSONArray} (by numeric index) values.
 * A missing key or a {@code null} / {@code JSONObject.NULL} value silently
 * resolves to an empty string in output.
 *
 * <h2>Thread safety</h2>
 * <p>The parsed segment cache uses a {@code ConcurrentHashMap}. Once a template
 * is cached, rendering is stateless and safe to call from multiple threads
 * concurrently. Template files are read from disk only on the first request.
 */
public class TemplateEngine {

    interface Segment {}

    static final class Literal implements Segment {
        final String html;
        Literal(String html) { this.html = html; }
    }

    static final class Element implements Segment {
        final String tag;
        final boolean selfClosing;
        final String staticAttrs;
        final String thIf;
        final String thUnless;
        final String thEach;
        final String thText;
        final String thHref;
        final String thSrc;
        final String thAlt;
        final String thStyle;
        final String thClassAppend;
        final List<Segment> children;

        Element(String tag, boolean selfClosing, String staticAttrs,
                String thIf, String thUnless, String thEach,
                String thText, String thHref, String thSrc, String thAlt,
                String thStyle, String thClassAppend, List<Segment> children) {
            this.tag           = tag;
            this.selfClosing   = selfClosing;
            this.staticAttrs   = staticAttrs;
            this.thIf          = thIf;
            this.thUnless      = thUnless;
            this.thEach        = thEach;
            this.thText        = thText;
            this.thHref        = thHref;
            this.thSrc         = thSrc;
            this.thAlt         = thAlt;
            this.thStyle       = thStyle;
            this.thClassAppend = thClassAppend;
            this.children      = children;
        }
    }

    private final ConcurrentHashMap<String, List<Segment>> cache = new ConcurrentHashMap<String, List<Segment>>();
    private final Path templateDir;

    public TemplateEngine(Path templateDir) {
        this.templateDir = templateDir;
    }

    /**
     * Renders the named template with the given context map and returns the
     * resulting HTML string. The template is parsed on the first call and the
     * parsed tree is cached for all subsequent calls.
     *
     * @param templatePath relative path inside the template directory,
     *                     with or without the {@code .html} extension
     * @param ctx          context variables — values may be String, Number,
     *                     Boolean, List, Map, JSONObject, or JSONArray
     */
    public String process(String templatePath, Map<String, Object> ctx) throws Exception {
        List<Segment> segments = cache.get(templatePath);
        if (segments == null) {
            Path full = resolveTemplate(templatePath);
            String src = Files.readString(full);
            segments = new Parser(src).parse();
            cache.put(templatePath, segments);
        }
        StringBuilder sb = new StringBuilder(4096);
        renderSegments(segments, ctx, sb);
        return sb.toString();
    }

    /**
     * Convenience overload: renders the template using a {@code JSONObject} as
     * the context. The top-level keys of the object become context variables,
     * with nested {@code JSONObject} and {@code JSONArray} values kept intact
     * for dot-path resolution inside the template.
     */
    public String process(String templatePath, JSONObject ctx) throws Exception {
        return process(templatePath, jsonObjectToMap(ctx));
    }

    public boolean exists(String templatePath) {
        try { resolveTemplate(templatePath); return true; }
        catch (Exception e) { return false; }
    }

    // =========================================================================
    // PARSER
    // =========================================================================

    static final class Parser {
        private final String src;
        private int pos = 0;

        Parser(String src) { this.src = src; }

        List<Segment> parse() { return parseUntil(null); }

        private List<Segment> parseUntil(String stopTag) {
            List<Segment> out = new ArrayList<Segment>();

            while (pos < src.length()) {
                if (stopTag != null && src.startsWith("</" + stopTag, pos)) {
                    pos = src.indexOf('>', pos) + 1;
                    return out;
                }
                if (src.charAt(pos) == '<') {
                    if (src.startsWith("<!--", pos)) {
                        int end = src.indexOf("-->", pos + 4);
                        if (end < 0) end = src.length() - 3;
                        out.add(new Literal(src.substring(pos, end + 3)));
                        pos = end + 3;
                        continue;
                    }
                    if (pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                        if (stopTag == null) {
                            int end = src.indexOf('>', pos) + 1;
                            out.add(new Literal(src.substring(pos, end)));
                            pos = end;
                        } else {
                            return out;
                        }
                        continue;
                    }
                    Segment s = parseTag();
                    if (s != null) out.add(s);
                } else {
                    int next = src.indexOf('<', pos);
                    if (next < 0) next = src.length();
                    out.add(new Literal(src.substring(pos, next)));
                    pos = next;
                }
            }
            return out;
        }

        private Segment parseTag() {
            int tagStart = pos++;
            int nameStart = pos;
            while (pos < src.length()
                    && !Character.isWhitespace(src.charAt(pos))
                    && src.charAt(pos) != '>'
                    && src.charAt(pos) != '/')
                pos++;
            String tag = src.substring(nameStart, pos);

            if (tag.isEmpty()) {
                int end = src.indexOf('>', pos);
                if (end < 0) end = src.length() - 1;
                pos = end + 1;
                return new Literal(src.substring(tagStart, pos));
            }

            Map<String, String> plain = new LinkedHashMap<String, String>();
            Map<String, String> th    = new LinkedHashMap<String, String>();
            boolean selfClose = false;

            while (pos < src.length() && src.charAt(pos) != '>') {
                skipWs();
                if (pos >= src.length() || src.charAt(pos) == '>') break;
                if (src.charAt(pos) == '/') { selfClose = true; pos++; continue; }

                int aStart = pos;
                while (pos < src.length()
                        && src.charAt(pos) != '='
                        && src.charAt(pos) != '>'
                        && src.charAt(pos) != '/'
                        && !Character.isWhitespace(src.charAt(pos)))
                    pos++;
                String name = src.substring(aStart, pos).trim();
                skipWs();
                String val = "";
                if (pos < src.length() && src.charAt(pos) == '=') {
                    pos++;
                    skipWs();
                    val = readVal();
                }

                if (name.startsWith("th:")) th.put(name.substring(3), val);
                else if (!name.isEmpty())   plain.put(name, val);
            }
            if (pos < src.length() && src.charAt(pos) == '>') pos++;

            boolean isVoid = selfClose || isVoidTag(tag);
            List<Segment> children = isVoid ? new ArrayList<Segment>() : parseUntil(tag);

            StringBuilder sa = new StringBuilder();
            for (Map.Entry<String, String> entry : plain.entrySet()) {
                sa.append(' ')
                  .append(entry.getKey())
                  .append("=\"")
                  .append(entry.getValue())
                  .append('"');
            }

            return new Element(tag, isVoid, sa.toString(),
                    th.get("if"), th.get("unless"), th.get("each"),
                    th.get("text"), th.get("href"), th.get("src"),
                    th.get("alt"), th.get("style"), th.get("classappend"),
                    children);
        }

        private void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        private String readVal() {
            if (pos >= src.length()) return "";
            char q = src.charAt(pos);
            if (q == '"' || q == '\'') {
                pos++;
                int s = pos;
                while (pos < src.length() && src.charAt(pos) != q) pos++;
                String v = src.substring(s, pos);
                if (pos < src.length()) pos++;
                return v;
            }
            int s = pos;
            while (pos < src.length()
                    && !Character.isWhitespace(src.charAt(pos))
                    && src.charAt(pos) != '>')
                pos++;
            return src.substring(s, pos);
        }

        private static boolean isVoidTag(String t) {
            String lower = t.toLowerCase();
            return lower.equals("area")   || lower.equals("base")  || lower.equals("br")
                || lower.equals("col")    || lower.equals("embed") || lower.equals("hr")
                || lower.equals("img")    || lower.equals("input") || lower.equals("link")
                || lower.equals("meta")   || lower.equals("param") || lower.equals("source")
                || lower.equals("track")  || lower.equals("wbr");
        }
    }

    // =========================================================================
    // RENDERER
    // =========================================================================

    private void renderSegments(List<Segment> segs, Map<String, Object> ctx, StringBuilder sb) {
        for (Segment seg : segs) {
            if (seg instanceof Literal) {
                sb.append(((Literal) seg).html);
            } else if (seg instanceof Element) {
                renderElem((Element) seg, ctx, sb);
            }
        }
    }

    private void renderElem(Element elem, Map<String, Object> ctx, StringBuilder sb) {
        if (elem.thEach != null) { renderEach(elem, ctx, sb); return; }
        if (elem.thIf     != null && !isTruthy(eval(elem.thIf,    ctx))) return;
        if (elem.thUnless != null &&  isTruthy(eval(elem.thUnless, ctx))) return;

        sb.append('<').append(elem.tag).append(elem.staticAttrs);

        if (elem.thClassAppend != null) {
            String v = str(eval(elem.thClassAppend, ctx));
            if (!v.isEmpty()) sb.append(" class=\"").append(v).append('"');
        }
        if (elem.thHref  != null) appendAttr(sb, "href",  str(eval(elem.thHref,  ctx)));
        if (elem.thSrc   != null) appendAttr(sb, "src",   str(eval(elem.thSrc,   ctx)));
        if (elem.thAlt   != null) appendAttr(sb, "alt",   str(eval(elem.thAlt,   ctx)));
        if (elem.thStyle != null) appendAttr(sb, "style", str(eval(elem.thStyle, ctx)));

        if (elem.selfClosing) { sb.append("/>"); return; }
        sb.append('>');

        if (elem.thText != null) sb.append(escape(str(eval(elem.thText, ctx))));
        else                     renderSegments(elem.children, ctx, sb);

        sb.append("</").append(elem.tag).append('>');
    }

    private void renderEach(Element elem, Map<String, Object> ctx, StringBuilder sb) {
        String spec = elem.thEach;
        int colon = spec.indexOf(':');
        if (colon < 0) return;

        String varName  = spec.substring(0, colon).trim();
        String collExpr = spec.substring(colon + 1).trim();
        if (collExpr.startsWith("${") && collExpr.endsWith("}"))
            collExpr = collExpr.substring(2, collExpr.length() - 1);

        List<Object> items = toList(resolve(collExpr.trim(), ctx));
        Map<String, Object> child = new HashMap<String, Object>(ctx);

        for (Object item : items) {
            if (item instanceof JSONObject) {
                // Flatten all JSONObject fields into the child scope so the template
                // can reference them directly (e.g. ${make}) without a prefix.
                // The loop variable itself is also bound under varName for cases
                // where the full object is needed (e.g. ${v.price}).
                JSONObject jo = (JSONObject) item;
                Map<String, Object> joMap = jsonObjectToMap(jo);
                for (Map.Entry<String, Object> entry : joMap.entrySet()) {
                    child.put(entry.getKey(), entry.getValue());
                }
                child.put(varName, item);
            } else if (item instanceof Map) {
                // Same flattening strategy for plain Map items (e.g. rows returned
                // from a DB query as List<Map<String,Object>>). Using the wildcard
                // cast Map<?,?> avoids an unchecked cast warning.
                Map<?, ?> m = (Map<?, ?>) item;
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    child.put(entry.getKey().toString(), entry.getValue());
                }
                child.put(varName, item);
            } else {
                child.put(varName, item);
            }

            Element one = new Element(elem.tag, elem.selfClosing, elem.staticAttrs,
                    elem.thIf, elem.thUnless, null,
                    elem.thText, elem.thHref, elem.thSrc, elem.thAlt,
                    elem.thStyle, elem.thClassAppend, elem.children);
            renderElem(one, child, sb);
        }
    }

    private void appendAttr(StringBuilder sb, String name, String val) {
        sb.append(' ').append(name).append("=\"").append(escape(val)).append('"');
    }

    // =========================================================================
    // EXPRESSION EVALUATOR
    // =========================================================================

    private Object eval(String raw, Map<String, Object> ctx) {
        String e = raw.trim();
        if (e.startsWith("${") && e.endsWith("}"))
            e = e.substring(2, e.length() - 1).trim();
        return evalExpr(e, ctx);
    }

    private Object evalExpr(String e, Map<String, Object> ctx) {
        e = e.trim();

        // Ternary
        int q = findOp(e, " ? ", 0);
        if (q >= 0) {
            int c = findOp(e, " : ", q + 3);
            if (c >= 0) {
                boolean cond = isTruthy(evalExpr(e.substring(0, q).trim(), ctx));
                String branch = cond ? e.substring(q + 3, c).trim() : e.substring(c + 3).trim();
                return evalExpr(branch, ctx);
            }
        }

        // Comparison
        String[] compOps = {"==", "!=", " <= ", " >= ", " < ", " > "};
        for (String op : compOps) {
            int i = findOp(e, op, 0);
            if (i >= 0) {
                Object L = evalExpr(e.substring(0, i).trim(), ctx);
                Object R = evalExpr(e.substring(i + op.length()).trim(), ctx);
                String opTrim = op.trim();
                if ("==".equals(opTrim)) return Objects.equals(str(L), str(R));
                if ("!=".equals(opTrim)) return !Objects.equals(str(L), str(R));
                if ("<".equals(opTrim))  return toDouble(L) <  toDouble(R);
                if (">".equals(opTrim))  return toDouble(L) >  toDouble(R);
                if ("<=".equals(opTrim)) return toDouble(L) <= toDouble(R);
                if (">=".equals(opTrim)) return toDouble(L) >= toDouble(R);
            }
        }

        // Additive
        int add = findAddOp(e);
        if (add >= 0) {
            char op = e.charAt(add);
            Object L = evalExpr(e.substring(0, add).trim(), ctx);
            Object R = evalExpr(e.substring(add + 1).trim(), ctx);
            if (op == '+') {
                boolean lNum = (L instanceof Number)
                        || (L instanceof String && isNumStr((String) L));
                boolean rNum = (R instanceof Number)
                        || (R instanceof String && isNumStr((String) R));
                if (!lNum || !rNum) return str(L) + str(R);
                return toDouble(L) + toDouble(R);
            }
            return toDouble(L) - toDouble(R);
        }

        // Multiplicative
        int mul = findMulOp(e);
        if (mul >= 0) {
            char op = e.charAt(mul);
            double L = toDouble(evalExpr(e.substring(0, mul).trim(), ctx));
            double R = toDouble(evalExpr(e.substring(mul + 1).trim(), ctx));
            return op == '*' ? L * R : L / R;
        }

        if (e.startsWith("(") && e.endsWith(")") && matchParen(e, 0) == e.length() - 1)
            return evalExpr(e.substring(1, e.length() - 1), ctx);
        if (e.startsWith("'") && e.endsWith("'"))
            return e.substring(1, e.length() - 1);
        if (e.startsWith("#"))
            return evalUtil(e, ctx);
        if (e.startsWith("${") && e.endsWith("}"))
            return evalExpr(e.substring(2, e.length() - 1).trim(), ctx);

        if ("true".equals(e))  return Boolean.TRUE;
        if ("false".equals(e)) return Boolean.FALSE;
        if ("null".equals(e))  return null;

        try { return Double.parseDouble(e); } catch (NumberFormatException ex) { /* fall through */ }

        return resolve(e, ctx);
    }

    private Object evalUtil(String e, Map<String, Object> ctx) {
        if (e.startsWith("#strings.toLowerCase("))
            return str(evalExpr(argOf(e, "#strings.toLowerCase("), ctx)).toLowerCase();

        if (e.startsWith("#strings.replace(")) {
            List<String> a = splitArgs(argOf(e, "#strings.replace("));
            if (a.size() < 3) return "";
            return str(evalExpr(a.get(0).trim(), ctx))
                    .replace(unquote(a.get(1).trim()), unquote(a.get(2).trim()));
        }
        if (e.startsWith("#numbers.formatInteger(")) {
            List<String> a = splitArgs(argOf(e, "#numbers.formatInteger("));
            if (a.isEmpty()) return "0";
            long val = (long) toDouble(evalExpr(a.get(0).trim(), ctx));
            String fmt = a.size() > 2 ? unquote(a.get(2).trim()) : "";
            return "COMMA".equals(fmt) ? String.format("%,d", val) : String.valueOf(val);
        }
        if (e.startsWith("#lists.isEmpty("))
            return toList(evalExpr(argOf(e, "#lists.isEmpty("), ctx)).isEmpty();
        if (e.startsWith("#lists.size("))
            return (double) toList(evalExpr(argOf(e, "#lists.size("), ctx)).size();
        return "";
    }

    /*
     * Resolves a dot-separated path (e.g. "user.address.city") by walking the
     * context map and then descending through nested Map, JSONObject, or
     * JSONArray (using numeric index) segments. Returns null if any segment
     * along the path is missing or the value is JSONObject.NULL.
     */
    private Object resolve(String path, Map<String, Object> ctx) {
        String[] parts = path.split("\\.", -1);
        Object cur = ctx.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            if (cur == null) return null;

            if (cur instanceof JSONObject) {
                cur = jsonUnwrap(((JSONObject) cur).opt(parts[i]));
            } else if (cur instanceof JSONArray) {
                try {
                    cur = jsonUnwrap(((JSONArray) cur).opt(Integer.parseInt(parts[i])));
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else if (cur instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) cur;
                cur = m.get(parts[i]);
            } else {
                return null;
            }
        }
        return cur;
    }

    // =========================================================================
    // JSON HELPERS
    // =========================================================================

    /**
     * Converts the top-level keys of a {@code JSONObject} into a
     * {@code Map&lt;String, Object&gt;}. Nested {@code JSONObject} and
     * {@code JSONArray} values are kept as-is so the path resolver can
     * walk them natively. {@code JSONObject.NULL} is mapped to Java {@code null}.
     */
    static Map<String, Object> jsonObjectToMap(JSONObject json) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonUnwrap(json.opt(key)));
        }
        return map;
    }

    /**
     * Converts a {@code JSONArray} into a {@code List&lt;Object&gt;}, keeping
     * nested {@code JSONObject} and {@code JSONArray} elements intact.
     * {@code JSONObject.NULL} entries are mapped to Java {@code null}.
     */
    static List<Object> jsonArrayToList(JSONArray arr) {
        List<Object> list = new ArrayList<Object>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            list.add(jsonUnwrap(arr.opt(i)));
        }
        return list;
    }

    private static Object jsonUnwrap(Object v) {
        if (v == null || v == JSONObject.NULL) return null;
        return v;
    }

    /*
     * Operator search helpers — each scans the expression string for a target
     * operator while correctly skipping over string literals and nested
     * parentheses / braces, so operators inside quoted values are never matched.
     */

    private int findOp(String e, String op, int from) {
        int depth = 0;
        boolean inStr = false;
        char sc = 0;
        for (int i = from; i <= e.length() - op.length(); i++) {
            char c = e.charAt(i);
            if (inStr) { if (c == sc) inStr = false; continue; }
            if (c == '\'' || c == '"') { inStr = true; sc = c; continue; }
            if (c == '(' || c == '{') { depth++; continue; }
            if (c == ')' || c == '}') { depth--; continue; }
            if (depth == 0 && e.startsWith(op, i)) return i;
        }
        return -1;
    }

    private int findAddOp(String e) {
        int depth = 0;
        int result = -1;
        boolean inStr = false;
        char sc = 0;
        for (int i = 0; i < e.length(); i++) {
            char c = e.charAt(i);
            if (inStr) { if (c == sc) inStr = false; continue; }
            if (c == '\'' || c == '"') { inStr = true; sc = c; continue; }
            if (c == '(' || c == '{') { depth++; continue; }
            if (c == ')' || c == '}') { depth--; continue; }
            if (depth == 0 && (c == '+' || c == '-') && i > 0) result = i;
        }
        return result;
    }

    private int findMulOp(String e) {
        int depth = 0;
        int result = -1;
        boolean inStr = false;
        char sc = 0;
        for (int i = 0; i < e.length(); i++) {
            char c = e.charAt(i);
            if (inStr) { if (c == sc) inStr = false; continue; }
            if (c == '\'' || c == '"') { inStr = true; sc = c; continue; }
            if (c == '(' || c == '{') { depth++; continue; }
            if (c == ')' || c == '}') { depth--; continue; }
            if (depth == 0 && (c == '*' || c == '/')) result = i;
        }
        return result;
    }

    private int matchParen(String e, int open) {
        int depth = 0;
        for (int i = open; i < e.length(); i++) {
            if (e.charAt(i) == '(') depth++;
            else if (e.charAt(i) == ')') { if (--depth == 0) return i; }
        }
        return -1;
    }

    /*
     * Expression utility helpers — argument parsing, string unquoting,
     * numeric coercion, truthiness, HTML escaping, and template path resolution.
     */

    private String argOf(String e, String prefix) {
        return e.substring(prefix.length(), e.length() - 1).trim();
    }

    private List<String> splitArgs(String args) {
        List<String> out = new ArrayList<String>();
        int depth = 0;
        int start = 0;
        boolean inStr = false;
        char sc = 0;
        for (int i = 0; i < args.length(); i++) {
            char c = args.charAt(i);
            if (inStr) { if (c == sc) inStr = false; continue; }
            if (c == '\'' || c == '"') { inStr = true; sc = c; continue; }
            if (c == '(' || c == '{') depth++;
            else if (c == ')' || c == '}') depth--;
            else if (c == ',' && depth == 0) {
                out.add(args.substring(start, i));
                start = i + 1;
            }
        }
        out.add(args.substring(start));
        return out;
    }

    private String unquote(String s) {
        if (s.length() >= 2
                && ((s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')
                ||  (s.charAt(0) == '"'  && s.charAt(s.length() - 1) == '"')))
            return s.substring(1, s.length() - 1);
        return s;
    }

    private boolean isNumStr(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private String str(Object v) {
        if (v == null) return "";
        if (v instanceof Boolean) return v.toString();
        if (v instanceof Double) {
            double d = (Double) v;
            if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
            return Double.toString(d);
        }
        return v.toString();
    }

    private double toDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof Boolean) return ((Boolean) v) ? 1.0 : 0.0;
        try { return Double.parseDouble(v.toString()); }
        catch (Exception ex) { return 0; }
    }

    private boolean isTruthy(Object v) {
        if (v == null)               return false;
        if (v instanceof Boolean)    return (Boolean) v;
        if (v instanceof Number)     return ((Number) v).doubleValue() != 0;
        if (v instanceof String)     return !((String) v).isEmpty();
        if (v instanceof List)       return !((List<?>) v).isEmpty();
        if (v instanceof Map)        return !((Map<?, ?>) v).isEmpty();
        if (v instanceof JSONObject) return ((JSONObject) v).length() > 0;
        if (v instanceof JSONArray)  return ((JSONArray) v).length() > 0;
        return true;
    }

    private List<Object> toList(Object v) {
        if (v instanceof List) {
            List<?> src = (List<?>) v;
            List<Object> out = new ArrayList<Object>(src.size());
            for (Object item : src) {
                out.add(item);
            }
            return out;
        }
        if (v instanceof JSONArray) return jsonArrayToList((JSONArray) v);
        if (v == null) return Collections.emptyList();
        return Collections.singletonList(v);
    }

    private String escape(String s) {
        if (s == null || s.isEmpty()) return "";
        boolean needsEscape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<' || c == '>' || c == '"' || c == '&') { needsEscape = true; break; }
        }
        if (!needsEscape) return s;
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private Path resolveTemplate(String path) throws Exception {
        Path a = templateDir.resolve(path);
        if (Files.exists(a)) return a;
        Path b = templateDir.resolve(path + ".html");
        if (Files.exists(b)) return b;
        throw new Exception("Template not found: " + path + " (in " + templateDir + ")");
    }
}
