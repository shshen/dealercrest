package com.dealercrest.db.vehicle;

import java.util.function.BiConsumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class VehicleLookupCallback implements BiConsumer<Vehicle, Throwable> {

    private final ChannelHandlerContext ctx;

    public VehicleLookupCallback(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void accept(Vehicle dealer, Throwable error) {
        if (error != null) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }
        if (dealer == null) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        // send response
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        // your error response logic
    }
}
