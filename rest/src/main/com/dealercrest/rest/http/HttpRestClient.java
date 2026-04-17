package com.dealercrest.rest.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.dealercrest.rest.TempFile;


public class HttpRestClient {

    // private final String EMPTY = "";
    private int TIME_OUT = 3 * 1000;
    // private static final int BUFFER_SIZE = 16 * 1024; // 16 KB
    // private static final Logger logger = Logger.getLogger(HttpRestClient.class.getName());

    public RestResponse get(String urlStr, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "GET", headers);
        return new RestResponse(conn);
    }

    public RestResponse post(String urlStr, String body, Map<String, String> headers) throws IOException {
        return service(urlStr, "POST", body, headers);
    }

    public RestResponse put(String urlStr, String body, Map<String, String> headers) throws IOException {
        return service(urlStr, "PUT", body, headers);
    }

    // ---------- SERVICE ----------
    public RestResponse service(String urlStr, String httpMethod, String body, 
            Map<String, String> headers) throws IOException {
        return sendWithBody(urlStr, httpMethod, body.getBytes(StandardCharsets.UTF_8), headers);
    }

    public RestResponse service(String urlStr, String httpMethod, InputStream body, long length, 
            Map<String, String> headers)
            throws IOException {
        return sendWithStream(urlStr, body, length, httpMethod, headers);
    }

    // ---------- DELETE ----------
    public RestResponse delete(String urlStr) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "DELETE", Map.of());
        return new RestResponse(conn);
    }

    public TempFile download(String urlStr) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "GET", Map.of());
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to download:" + urlStr + ",status:" + conn.getResponseCode());
        }
        TempFile tempFile = new TempFile();
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile.getFile())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    public ByteBuffer downloadAsBytes(String urlStr) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, "GET", Map.of());
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to download file: HTTP code " + conn.getResponseCode());
        }
        try (InputStream in = conn.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return ByteBuffer.wrap(out.toByteArray());
        }
    }

    private HttpURLConnection openConnection(String urlStr, String method, Map<String, String> headers)
            throws IOException {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod(method);

        conn.setConnectTimeout(TIME_OUT);
        conn.setReadTimeout(TIME_OUT);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        conn.setRequestProperty("User-Agent", "Analytics-WebClient");
        conn.setRequestProperty("Connection", "close");

        Set<String> keys = headers.keySet();
        for (String k : keys) {
            conn.setRequestProperty(k, headers.get(k));
        }
        return conn;
    }

    private RestResponse sendWithBody(String urlStr, String method, byte[] body,
            Map<String, String> headers) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, method, headers);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));
        if (body.length > 0) {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body);
            }
        }
        return new RestResponse(conn);
    }

    private RestResponse sendWithStream(String urlStr, InputStream body, long length, String httpMethod,
            Map<String, String> headers) throws IOException {
        HttpURLConnection conn = openConnection(urlStr, httpMethod, headers);
        conn.setDoOutput(true);
        if (length >= 0) {
            conn.setRequestProperty("Content-Length", String.valueOf(length));
        } else {
            conn.setChunkedStreamingMode(8192); // fallback for unknown length
        }

        try (OutputStream out = conn.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = body.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        return new RestResponse(conn);
    }

    public RestResponse upload(String urlStr, String fileName, InputStream fileInputStream)
            throws IOException {
        HttpURLConnection connection = null;
        DataOutputStream outputStream;

        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";

        long bytesRead;
        int bufferSize;
        long bytesAvailable = fileInputStream.available();
        byte[] buffer;
        int maxBufferSize = 8192;

        URL url = URI.create(urlStr).toURL();
        connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);

        connection.setRequestMethod("POST");
        connection.setChunkedStreamingMode(maxBufferSize); // Enables chunked transfer
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("User-Agent", "Tiny Multipart HTTP Client 1.0 by shane");
        connection.setRequestProperty("Content-Length", Long.toString(bytesAvailable));
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);

        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);
        outputStream
                .writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
        outputStream.writeBytes(lineEnd);

        bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
            outputStream.write(buffer, 0, bufferSize);
            bytesAvailable = bytesAvailable - bytesRead;
            bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        return new RestResponse(connection);
    }

}
