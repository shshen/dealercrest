package com.dealercrest;

import java.io.IOException;

public abstract class NettyServer {

    public abstract String getName();
    public abstract void start() throws IOException;
    public abstract void shutdown();
    public abstract void block();
    public abstract void startAndWait() throws IOException;

}
