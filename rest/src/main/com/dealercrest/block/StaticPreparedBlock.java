package com.dealercrest.block;

import com.dealercrest.http.QueryRequest;

import io.netty.buffer.ByteBuf;

public class StaticPreparedBlock implements PreparedBlock{
    
    private final ByteBuf content;

    public StaticPreparedBlock(ByteBuf content) {
        this.content = content;
    }

    @Override
    public ByteBuf render(QueryRequest queryRequest) {
        return content.retainedDuplicate();
    }

}
