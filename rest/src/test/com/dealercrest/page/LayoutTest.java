package com.dealercrest.page;

import io.netty.buffer.*;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.dealercrest.page.Layout;

import static com.dealercrest.page.Layout.Placeholders;

import java.util.HashMap;
import java.util.Map;

public class LayoutTest {

    private final ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

    @After
    public void cleanup() {
        // optional: enable leak detection externally if needed
    }

    @Test
    public void testStaticOnly() {
        Layout layout = new Layout("path", 1, "hello world");

        Placeholders ctx = new Placeholders(new HashMap<>());

        CompositeByteBuf buf = layout.apply(ctx);

        String result = buf.toString(CharsetUtil.UTF_8);
        Assert.assertEquals("hello world", result);

        buf.release();
        layout.release();
    }

    @Test
    public void testSinglePlaceholder() {
        Layout layout = new Layout("path", 1, "hello <!-- $placeholder:name -->");

        ByteBuf nameBuf = alloc.buffer();
        nameBuf.writeBytes("john".getBytes());

        Map<String, ByteBuf> map = new HashMap<>();
        map.put("name", nameBuf);

        Placeholders ctx = new Placeholders(map);

        CompositeByteBuf buf = layout.apply(ctx);

        String result = buf.toString(CharsetUtil.UTF_8);
        Assert.assertEquals("hello john", result);

        buf.release();
        nameBuf.release();
        layout.release();
    }

    @Test
    public void testMultiplePlaceholders() {
        Layout layout = new Layout("path", 1,
                "A <!-- $placeholder:x --> B <!-- $placeholder:y --> C");

        ByteBuf x = alloc.buffer();
        x.writeBytes("1".getBytes());

        ByteBuf y = alloc.buffer();
        y.writeBytes("2".getBytes());

        Map<String, ByteBuf> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);

        Placeholders ctx = new Placeholders(map);

        CompositeByteBuf buf = layout.apply(ctx);

        String result = buf.toString(CharsetUtil.UTF_8);
        Assert.assertEquals("A 1 B 2 C", result);

        buf.release();
        x.release();
        y.release();
        layout.release();
    }

    @Test
    public void testRetainBehavior() {
        Layout layout = new Layout("path", 1, "X <!-- $placeholder:v -->");

        ByteBuf v = alloc.buffer();
        v.writeBytes("data".getBytes());

        Map<String, ByteBuf> map = new HashMap<>();
        map.put("v", v);

        Placeholders ctx = new Placeholders(map);

        int before = v.refCnt();

        CompositeByteBuf buf = layout.apply(ctx);

        // composite holds retained duplicate
        Assert.assertTrue(v.refCnt() > before);

        buf.release();

        // after release, should return to original
        Assert.assertEquals(before, v.refCnt());

        v.release();
        layout.release();
    }
}
