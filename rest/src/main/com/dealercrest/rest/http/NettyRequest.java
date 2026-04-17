package com.dealercrest.rest.http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import static io.netty.handler.codec.http.HttpHeaderValues.MULTIPART_FORM_DATA;

public class NettyRequest implements HttpRequest {

    private HttpVersion protocolVersion;
    private HttpHeaders headers;
    private HttpMethod method;
    private String uri;
    private HttpPart bodyPart; 
    private DecoderResult decoderResult;
    private static final int MIN_16K = 16384;
    private static final String TEMP_DIR = "./data/temp";

    public NettyRequest(HttpRequest request) {
        this.protocolVersion = request.protocolVersion();
        this.headers = request.headers();
        this.method = request.method();
        this.uri = request.uri();

        String mimeType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (mimeType!=null && mimeType.startsWith(MULTIPART_FORM_DATA.toString())) {
            File temp = new File(TEMP_DIR);
            if (!temp.exists()) {
                temp.mkdirs();
            }
            DefaultHttpDataFactory factory = new DefaultHttpDataFactory(MIN_16K);
            factory.setBaseDir(TEMP_DIR);

            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, request, StandardCharsets.UTF_8);
            decoder.setDiscardThreshold(0); 
            this.bodyPart = new FileUploads(decoder);
        } else {
            this.bodyPart = new BytesBody();
        }
    }

    public void parse(HttpContent chunk) throws IOException {
        bodyPart.append(chunk);
        if (chunk instanceof LastHttpContent) {
            decoderResult = DecoderResult.SUCCESS;
        }
    }

    public HttpPart body() {
        return bodyPart;
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpVersion protocolVersion() {
        return protocolVersion;
    }

    @Override
    public DecoderResult getDecoderResult() {
        return decoderResult;
    }

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult arg0) {
        this.decoderResult = arg0;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public HttpRequest setMethod(HttpMethod arg0) {
        this.method = arg0;
        return this;
    }

    @Override
    public HttpRequest setProtocolVersion(HttpVersion arg0) {
        this.protocolVersion = arg0;
        return this;
    }

    @Override
    public HttpRequest setUri(String arg0) {
        this.uri = arg0;
        return this;
    }

    @Override
    public String uri() {
        return uri;
    }
    
}
