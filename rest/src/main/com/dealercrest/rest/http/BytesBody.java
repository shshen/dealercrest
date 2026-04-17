package com.dealercrest.rest.http;

import io.netty.handler.codec.http.HttpContent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BytesBody extends HttpPart {

    private ByteArrayOutputStream body;
    private static final int MAX_JSON_SIZE = 5 * 1024 * 1024; // 5 MB

    public BytesBody() {
        body = new ByteArrayOutputStream();
    }

    @Override
    public void append(HttpContent chunk) throws IOException {
        int readable = chunk.content().readableBytes();
        if (body.size() + readable > MAX_JSON_SIZE) {
            throw new IOException("JSON body too large");
        }
        byte[] bytes = new byte[readable];
        chunk.content().readBytes(bytes);
        body.write(bytes);
    }

    public String asString() {
        return new String(body.toByteArray());
    }

    public ByteArrayOutputStream getBody() {
        return body;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(body.toByteArray());
    }

}
