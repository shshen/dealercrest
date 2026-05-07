package com.dealercrest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CleanupHook extends Thread {

    private final List<Lifecycle> lifecycleComponents;
    private static final Logger logger = Logger.getLogger(CleanupHook.class.getName());

    public CleanupHook () {
        this.setName("GracefulShutdown");
        this.lifecycleComponents = new ArrayList<>();
    }

    @Override
    public void run() {
        logger.info("Shutdown hook triggered,stopping server...");
        for (Lifecycle component: lifecycleComponents) {
            try {
                component.shutdown();
            } catch (Exception e) {
                logger.warning("Failed to stop " + component.getName());
            }
        }
    }

    public void addLifecycle(Lifecycle asyncLogger) {
        lifecycleComponents.add(asyncLogger);
    }
}
