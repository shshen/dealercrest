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
import java.net.URISyntaxException;
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
import com.dealercrest.domain.AcmeChallengeStore;
import com.dealercrest.domain.RedirectHttpsHandler;
import com.dealercrest.http.CertificateManager;
import com.dealercrest.http.InventoryController;
import com.dealercrest.http.NettyHttpsHandler;
import com.dealercrest.http.PageController;
import com.dealercrest.http.PrefixThreadFactory;
import com.dealercrest.resource.WebResources;
import com.dealercrest.rest.NettyRouters;
import com.dealercrest.storage.LocalStorage;
import com.dealercrest.storage.Storage;
import com.dealercrest.template.DirectiveRegistry;
import com.dealercrest.template.EachDirective;
import com.dealercrest.template.IfDirective;
import com.dealercrest.template.ReplaceDirective;
import com.dealercrest.template.TemplateEngine;

public class HttpsWebServer extends NettyServer {

    private final AppConfig appConfig;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final DataSource dataSource;
    private final static int maxContentLength = 65536; // 64*1024 = 64K
    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/dealerbase";
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Logger logger = Logger.getLogger(HttpsWebServer.class.getName());

    public HttpsWebServer() throws UnknownHostException {
        this(new AppConfig());
    }

    public HttpsWebServer(AppConfig appConfig) throws UnknownHostException {
        this.appConfig = appConfig;
        this.dataSource = DataSourceFactory.build(jdbcUrl, "dealerbase_app", "zhu88jie");
        // Configure the bootstrap.
        this.bossGroup = new MultiThreadIoEventLoopGroup(1, new PrefixThreadFactory("DealerCrestBossGroup"),
                KQueueIoHandler.newFactory());
        this.workerGroup = new MultiThreadIoEventLoopGroup(1, new PrefixThreadFactory("DealerCrestWorkerGroup"),
                KQueueIoHandler.newFactory());
    }

    @Override
    public void start() throws Exception {
        int httpPort = appConfig.getHttpPort();
        int httpsPort = appConfig.getHttpsPort();
        startHttpServer(httpPort);
        startHttpsServer(httpsPort);
        logger.log(Level.INFO, "console server started at http port {0},https port {1}",
                new String[] { Integer.toString(httpPort), Integer.toString(httpsPort) });
    }

    private void startHttpServer(int port) {
        AcmeChallengeStore challengeStore = new AcmeChallengeStore(dataSource);
        ServerBootstrap httpBootstrap = new ServerBootstrap();
        httpBootstrap.group(bossGroup, workerGroup).channel(KQueueServerSocketChannel.class)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpServerCodec());
                        p.addLast(new RedirectHttpsHandler(challengeStore));
                    }
                });
        try {
            Channel httpChannel = httpBootstrap.bind(port).sync().channel();
            channelGroup.add(httpChannel);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to start ConsoleServer on Port " + port);
        }
    }

    private NettyRouters buildRouters() throws IOException, URISyntaxException {
        AppConfig appConfig = new AppConfig();
        ThreadFactory factory = new PrefixThreadFactory("DealerCrestExecutor");
        ScheduledExecutorService scheduledExecutor =  Executors.newScheduledThreadPool(1, factory);
        DealerCacheTask dealerTask = new DealerCacheTask(dataSource, appConfig.getDomain());
        scheduledExecutor.scheduleWithFixedDelay(dealerTask, 60, 60, TimeUnit.MINUTES);

        Storage storage = new LocalStorage();
        DirectiveRegistry reg = new DirectiveRegistry();
        reg.register(new ReplaceDirective());
        reg.register(new IfDirective());
        reg.register(new EachDirective());
        TemplateEngine templateEngine = new TemplateEngine(reg);
        WebResources webResource = WebResources.load(appConfig.getDomain(), templateEngine);

        NettyRouters nettyRouters = new NettyRouters();
        PageController pageController = new PageController(storage, webResource, dealerTask);
        nettyRouters.addHandler(pageController);

        InventoryController inventoryController = new InventoryController(dataSource);
        nettyRouters.addHandler(inventoryController);
        return nettyRouters;
    }

    private void startHttpsServer(int port) throws IOException, URISyntaxException {
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
    public void startAndWait() throws Exception {
        start();
        block();
    }

    @Override
    public String getName() {
        return "Https";
    }

}
