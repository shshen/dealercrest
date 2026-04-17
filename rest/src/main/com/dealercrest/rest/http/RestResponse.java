package com.dealercrest.rest.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;

import com.dealercrest.rest.StringUtil;

public class RestResponse implements AutoCloseable {

    private final int status;
    private final Map<String, String> headers;
    private final InputStream inputStream;
    private final HttpURLConnection connection;

    public RestResponse(HttpURLConnection conn) throws IOException {
        this.status = conn.getResponseCode();
        this.connection = conn;
        Map<String, List<String>> headerFields = conn.getHeaderFields();
        this.headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if (key != null && value != null && !value.isEmpty()) {
                headers.put(key.toLowerCase(), String.join(", ", value));
            }
        }
        InputStream in;
        if (status >= 200 && status < 400) {
            in = conn.getInputStream();
        } else {
            in = conn.getErrorStream();
        }
        this.inputStream = in;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return headers.get("content-type");
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContent() {
        return StringUtil.convert(inputStream);
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
    }
}
