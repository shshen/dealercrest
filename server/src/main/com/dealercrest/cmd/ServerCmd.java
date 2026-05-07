package com.dealercrest.cmd;

import java.nio.file.Path;
import java.util.logging.Level;

import com.dealercrest.CleanupHook;
import com.dealercrest.HttpsWebServer;
import com.dealercrest.NettyServer;
import com.dealercrest.cli.Command;
import com.dealercrest.cli.Option;
import com.dealercrest.cli.RunContext;
import com.dealercrest.file.AsyncLogManager;
import com.dealercrest.file.JuliFileHandler;
import com.dealercrest.file.SimpleLogger;

@Command(name = "server", group = "Server", mixins = {
        HelpMixin.class }, desc = "Web server related commands")
public class ServerCmd {

    private final static SimpleLogger logger = new SimpleLogger();

    @Command(name = "start", desc = "Start web server", mixins = { HelpMixin.class,
            ServerMixin.class }, options = {
                    @Option(shortName = "p", longName = "port", desc = "Server listening port", defaultValue = "9082"),
                    @Option(shortName = "m", longName = "master", desc = "Specify one of the master servers by hostname:port"),
                    @Option(longName = "standalone", desc = "Run server in standalone mode", type = boolean.class)
            })
    public void startServer(RunContext input) throws Exception {
        NettyServer server = new HttpsWebServer();

        CleanupHook stopHook = new CleanupHook();
        stopHook.addLifecycle(server);

        if ("console".equals(input.get("output"))) {
            logger.setup(Level.INFO);
        } else {
            AsyncLogManager asyncLogger = new AsyncLogManager(
                    Path.of("logs"),
                    server.getName().toLowerCase(),
                    server.getName() + "LogDrainer",
                    50 * 1024 * 1024,
                    65536);
            asyncLogger.start();
            stopHook.addLifecycle(asyncLogger);
            logger.setup(Level.INFO, new JuliFileHandler(asyncLogger));
        }
        System.setProperty("ENVIRONMENT", input.get("environment"));
        System.setProperty("KEYSTORE_PASSWORD", input.get("keystorePassword"));

        // Runtime.getRuntime().addShutdownHook(stopHook);
        server.startAndWait();
    }

}
