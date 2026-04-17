package com.dealercrest.rest.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.List;

public class FileUploads extends HttpPart {
    
    private final HttpPostRequestDecoder decoder;
    private final Multipart multipart;

    public FileUploads(HttpPostRequestDecoder decoder) {
        this.decoder = decoder;
        this.multipart = new Multipart();
    }

    @Override
    public void append(HttpContent chunk) throws IOException {
        decoder.offer(chunk);
        if(chunk instanceof LastHttpContent) {
            List<InterfaceHttpData> dataList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData data : dataList) {
                multipart.add(data);
            }
        }
    }

    public Multipart multipart() {
        return multipart;
    }

}