package com.dealercrest.file;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class SimpleLogger {

    private static final String[] LEVELS = { "", "", "", "FINR", "FINT", "FINE", "", "CONF", "INFO", "WARN", "FAIL" };

    public void setup(Level level, JuliFileHandler h) {
        Logger rootLogger = Logger.getLogger("com.dataleading");
        rootLogger.setUseParentHandlers(false);

        try {
            h.setLevel(level);
            rootLogger.setLevel(level);
            h.setFormatter(new MyFormatter());
            rootLogger.addHandler(h);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void setup(Level level) {
        StdConsoleHandler consoleHandler = new StdConsoleHandler();
        consoleHandler.setLevel(level);
        consoleHandler.setFormatter(new MyFormatter());
        setLogger("com.dataleading", level, consoleHandler);
    }

    private void setLogger(String pkg, Level level, StdConsoleHandler consoleHandler) {
        Logger appLogger = Logger.getLogger(pkg);
        appLogger.setLevel(level);
        appLogger.setUseParentHandlers(false);
        appLogger.addHandler(consoleHandler);
    }

    // refer to java.util.logging.SimpleFormatter;
    static class MyFormatter extends Formatter {
        private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        private static final ZoneId ZONE = ZoneId.systemDefault();
        private final StringBuilder buffer = new StringBuilder(256);

        public String format(LogRecord record) {
            buffer.setLength(0);

             Instant instant = record.getInstant();
            buffer.append(DTF.format(instant.atZone(ZONE)));

            buffer.append(" [");
            int value = record.getLevel().intValue();
            value = value / 100;
            String name = record.getLevel().getName();
            if (value > 1 && value < 11) {
                name = LEVELS[value];
            }
            buffer.append(name);

            buffer.append(" ");
            String fullClassName = record.getSourceClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            buffer.append(className);
            buffer.append(".");
            buffer.append(record.getSourceMethodName());
            buffer.append("] ");
            buffer.append(formatMessage(record));
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter(256);
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            buffer.append(throwable);
            buffer.append("\n");
            return buffer.toString();
        }
    }

    static class StdConsoleHandler extends ConsoleHandler {
        protected void setOutputStream(OutputStream out) {
            super.setOutputStream(out);
        }
    }
}
