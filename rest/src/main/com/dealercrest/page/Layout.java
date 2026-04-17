package com.dealercrest.page;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;

public class Layout {

    private final String path;
    private final long lastModified;
    private final List<Token> tokens;
    private final ByteBufAllocator alloc;

    private final static String START = "<!--";
    private final static String END = "-->";
    private final static String PREFIX = "$placeholder:";

    public Layout(String path, long lastModified, String content) {
        this(path, lastModified, content, PooledByteBufAllocator.DEFAULT);
    }

    public Layout(String path, long lastModified, String content, ByteBufAllocator alloc) {
        this.path = path;
        this.lastModified = lastModified;
        this.alloc = alloc;
        this.tokens = compile(content, alloc);
    }

    public CompositeByteBuf apply(Placeholders ctx) {
        CompositeByteBuf composite = alloc.compositeBuffer(tokens.size());
        for (Token t : tokens) {
            t.writeTo(composite, ctx);
        }
        return composite;
    }

    public void release() {
        for (Token t : tokens) {
            t.release();
        }
    }

    private List<Token> compile(String content, ByteBufAllocator alloc) {
        List<Token> tokens = new ArrayList<>();

        int index = 0;
        int start;

        while ((start = content.indexOf(START, index)) != -1) {
            int end = content.indexOf(END, start);
            if (end == -1) break;

            int innerStart = start + 4;
            int innerEnd = end;

            while (innerStart < innerEnd && Character.isWhitespace(content.charAt(innerStart))) innerStart++;
            while (innerEnd > innerStart && Character.isWhitespace(content.charAt(innerEnd - 1))) innerEnd--;

            boolean isPlaceholder = startsWith(content, innerStart, innerEnd, PREFIX);

            if (!isPlaceholder) {
                index = end + 3;
                continue;
            }

            if (start > index) {
                tokens.add(staticToken(content, index, start, alloc));
            }

            int nameStart = innerStart + PREFIX.length();
            int nameEnd = innerEnd;

            while (nameStart < nameEnd && Character.isWhitespace(content.charAt(nameStart))) nameStart++;
            while (nameEnd > nameStart && Character.isWhitespace(content.charAt(nameEnd - 1))) nameEnd--;

            String name = content.substring(nameStart, nameEnd);
            tokens.add(new PlaceholderToken(name));

            index = end + 3;
        }

        if (index < content.length()) {
            tokens.add(staticToken(content, index, content.length(), alloc));
        }
        return tokens;
    }

    private StaticToken staticToken(String content, int start, int end, ByteBufAllocator alloc) {
        ByteBuf buf = ByteBufUtil.encodeString(
                alloc,
                CharBuffer.wrap(content, start, end),
                CharsetUtil.UTF_8
        );
        return new StaticToken(buf.asReadOnly());
    }

    private static boolean startsWith(String s, int start, int end, String prefix) {
        if (end - start < prefix.length()) return false;
        for (int i = 0; i < prefix.length(); i++) {
            if (s.charAt(start + i) != prefix.charAt(i)) return false;
        }
        return true;
    }

    public interface Token {
        void writeTo(CompositeByteBuf out, Placeholders ctx);
        void release();
    }

    static class StaticToken implements Token {
        private final ByteBuf buf;
        StaticToken(ByteBuf buf) {
            this.buf = buf;
        }
        @Override
        public void writeTo(CompositeByteBuf out, Placeholders ctx) {
            out.addComponent(true, buf.retainedDuplicate());
        }
        @Override
        public void release() {
            buf.release();
        }
    }

    static class PlaceholderToken implements Token {
        private final String name;
        PlaceholderToken(String name) {
            this.name = name;
        }
        @Override
        public void writeTo(CompositeByteBuf out, Placeholders ctx) {
            ByteBuf dynamic = ctx.resolve(name);
            if (dynamic != null) {
                out.addComponent(true, dynamic.retainedDuplicate());
            }
        }
        @Override
        public void release() {
            // nothing
        }
    }

    public static class Placeholders {
        private final Map<String, ByteBuf> data;
        public Placeholders(Map<String, ByteBuf> data) {
            this.data = data;
        }
        public ByteBuf resolve(String name) {
            return data.get(name);
        }
    }

    public String getPath() { return path; }
    public long getLastModified() { return lastModified; }

    @Override
    public String toString() {
        return "Layout{path='" + path + "', lastModified=" + lastModified + "}";
    }
}
