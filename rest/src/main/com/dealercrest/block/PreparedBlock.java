package com.dealercrest.block;

import com.dealercrest.http.QueryRequest;

import io.netty.buffer.ByteBuf;

public interface PreparedBlock {

    ByteBuf render(QueryRequest queryRequest);

}
