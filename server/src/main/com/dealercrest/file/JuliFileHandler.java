package com.dealercrest.file;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class JuliFileHandler extends Handler {

    private final AsyncLogManager asyncLogger;

    public JuliFileHandler(AsyncLogManager asyncLogger) {
        this.asyncLogger = asyncLogger;
        setLevel(Level.ALL);
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        // Format the log message
        String msg;
        try {
            msg = getFormatter() != null ? getFormatter().format(record)
                                         : record.getMessage();
        } catch (Exception e) {
            msg = record.getMessage();
        }
        asyncLogger.log(msg);
    }

    @Override
    public void flush() {
        // Optional: AsyncFileLogger flushes itself
    }

    @Override
    public void close() throws SecurityException {

    }
}
