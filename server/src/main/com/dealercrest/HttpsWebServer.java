package com.dealercrest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.Mapping;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.dealercrest.db.DataSourceFactory;
import com.dealercrest.db.DealerCacheTask;
import com.dealercrest.db.JdbcTemplate;
import com.dealercrest.http.CertificateManager;
import com.dealercrest.http.InventoryController;
import com.dealercrest.http.NettyHttpsHandler;
import com.dealercrest.http.PageController;
import com.dealercrest.http.PrefixThreadFactory;
import com.dealercrest.http.RedirectHttpsHandler;
import com.dealercrest.page.RenderedBlockCacheTask;
import com.dealercrest.page.ResourceLoader;
import com.dealercrest.page.WebResources;
import com.dealercrest.rest.NettyRouters;

public class HttpsWebServer extends NettyServer {

    private final int httpPort;
    private final int httpsPort;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final static int maxContentLength = 65536; // 64*1024 = 64K
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(HttpsWebServer.class.getName());

    public HttpsWebServer() throws UnknownHostException {
        this(80, 443);
    }

    public HttpsWebServer(int httpPort, int httpsPort) throws UnknownHostException {
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        // Configure the bootstrap.
        this.bossGroup = new MultiThreadIoEventLoopGroup(1, new PrefixThreadFactory("HttpsWebServer:" + httpsPort),
                KQueueIoHandler.newFactory());
        this.workerGroup = new MultiThreadIoEventLoopGroup(1, new PrefixThreadFactory("HttpsWebServer:" + httpsPort),
                KQueueIoHandler.newFactory());
    }

    @Override
    public void start() throws IOException {
        startHttpsServer(httpsPort);
        startHttpServer(httpPort);
        logger.log(Level.INFO, "console server started at http port {0},https port {1}",
                new String[] { Integer.toString(httpPort), Integer.toString(httpsPort) });
    }

    private void startHttpServer(int port) {
        ServerBootstrap httpBootstrap = new ServerBootstrap();
        httpBootstrap.group(bossGroup, workerGroup).channel(KQueueServerSocketChannel.class)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new RedirectHttpsHandler());
                    }
                });
        try {
            Channel httpChannel = httpBootstrap.bind(port).sync().channel();
            channelGroup.add(httpChannel);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to start ConsoleServer on Port " + port);
        }
    }

    private NettyRouters buildRouters() throws IOException {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/dealer";
        DataSource dataSource = DataSourceFactory.build(jdbcUrl, "username", "password");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        NettyRouters nettyRouters = new NettyRouters();
        ResourceLoader scanner = new ResourceLoader();
        WebResources appResource = scanner.scan();
        PageController pageController = new PageController(appResource, jdbcTemplate);
        nettyRouters.addHandler(pageController);

        InventoryController inventoryController = new InventoryController(jdbcTemplate);
        nettyRouters.addHandler(inventoryController);

        RenderedBlockCacheTask pageRefresh = new RenderedBlockCacheTask(appResource, jdbcTemplate);
        pageRefresh.rebuildDealerPages();
        DealerCacheTask dealerTask = new DealerCacheTask(jdbcTemplate);
        ThreadFactory factory = new PrefixThreadFactory("HttpExecutor");
        ScheduledExecutorService scheduledExecutor =  Executors.newScheduledThreadPool(1, factory);
        scheduledExecutor.scheduleWithFixedDelay(pageRefresh, 60, 60, TimeUnit.SECONDS);
        scheduledExecutor.scheduleWithFixedDelay(dealerTask, 60, 60, TimeUnit.SECONDS);
        return nettyRouters;
    }

    private void startHttpsServer(int port) throws IOException {
        NettyRouters nettyRouters = buildRouters();
        CertificateManager certificateManager = new CertificateManager();
        Mapping<String, SslContext> sniMapping = certificateManager.getMapping();
        ServerBootstrap httpsBootstrap = new ServerBootstrap();
        httpsBootstrap.group(bossGroup, workerGroup).channel(KQueueServerSocketChannel.class)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new SniHandler(sniMapping));
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(maxContentLength));
                        // p.addLast(new ChunkedWriteHandler());
                        p.addLast(new HttpContentCompressor());
                        p.addLast(new NettyHttpsHandler(nettyRouters));
                    }
                });

        try {
            Channel httpsChannel = httpsBootstrap.bind(port).sync().channel();
            channelGroup.add(httpsChannel);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to start HttpServer on Port " + port);
        }
    }

    @Override
    public void block() {
        try {
            channelGroup.newCloseFuture().sync();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "failed to start http", e);
        }
    }

    @Override
    public void shutdown() {
        if (channelGroup != null) {
            channelGroup.close().syncUninterruptibly();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    @Override
    public void startAndWait() throws IOException {
        Thread.currentThread().setName("ConsoleServerMain");
        start();
        block();
    }

    @Override
    public String getName() {
        return "Https";
    }

}
