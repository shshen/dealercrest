package com.dealercrest.rest;

import java.io.IOException;
import java.io.RandomAccessFile;

import com.dealercrest.rest.http.HttpResult;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class FileResult extends HttpResult {

    private final String path;

    public FileResult(String path) {
        this.path = path;
    }

    @Override
    public void write(ChannelHandlerContext ctx) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(path, "r");
        try {
            long fileLength = raf.length();
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.headers().set("Content-Length", fileLength);
            response.headers().set("Content-Type", "application/octet-stream");
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.write(response);
    
            DefaultFileRegion fileRegion = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
            ChannelFuture sendFileFuture = ctx.writeAndFlush(fileRegion);

            final RandomAccessFile rafFinal = raf;
            sendFileFuture.addListener(
                new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        try {
                            rafFinal.close();
                            future.channel().close();
                        } catch (Exception e) {
                            // log error, do not rethrow
                        }
                    }
                }
            );
            raf = null; // prevent close in finally
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    // log error, do not rethrow
                }
            }
        }
    }

}
