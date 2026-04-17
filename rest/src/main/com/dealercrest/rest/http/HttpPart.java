package com.dealercrest.rest.http;

import io.netty.handler.codec.http.HttpContent;
import java.io.IOException;

public abstract class HttpPart {
    /** Append a chunk of content */
    public abstract void append(HttpContent chunk) throws IOException;

}
