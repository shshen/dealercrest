package com.dealercrest.file;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;

final class RotatingFile {

    private final Path directory;
    private final String prefix;
    private final long maxFileSize;

    private FileChannel channel;
    private long currentSize;
    private int currentId;
    private String currentDateStr;

    private long nextDayThresholdMillis;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public RotatingFile(Path directory, String prefix, long maxFileSize) throws IOException {
        this.directory = directory;
        this.prefix = prefix;
        this.maxFileSize = maxFileSize;

        Files.createDirectories(directory);
        rotateIfNeeded();
    }

    public FileChannel channel() {
        return channel;
    }

    public void incrementSize(long delta) {
        currentSize += delta;
    }

    public void rotateIfNeeded() throws IOException {
        long now = System.currentTimeMillis();

        if (channel != null &&
            now < nextDayThresholdMillis &&
            currentSize < maxFileSize) {
            return;
        }

        boolean dateChanged = now >= nextDayThresholdMillis;
        performRotation(now, dateChanged);
    }

    // =============================
    // Clean rotation orchestration
    // =============================

    private void performRotation(long now, boolean dateChanged) throws IOException {
        closeCurrentChannel();

        LocalDate today = toLocalDate(now);
        String todayStr = DATE_FORMATTER.format(today);

        if (dateChanged || currentDateStr == null) {
            rotateForNewDay(today, todayStr);
        } else {
            rotateForSizeExceeded();
        }

        openCurrentFile();
    }

    // =============================
    // Rotation Scenarios
    // =============================

    private void rotateForNewDay(LocalDate today, String todayStr) throws IOException {
        int lastId = findLastFileId(todayStr);

        currentId = lastId > 0 ? lastId : 1;

        Path file = resolveFile(todayStr, currentId);

        if (Files.exists(file)) {
            currentSize = Files.size(file);
            if (currentSize >= maxFileSize) {
                currentId++;
                currentSize = 0;
            }
        } else {
            currentSize = 0;
        }

        currentDateStr = todayStr;
        nextDayThresholdMillis = computeNextDayThreshold(today);
    }

    private void rotateForSizeExceeded() {
        currentId++;
        currentSize = 0;
    }

    // =============================
    // Helpers
    // =============================

    private void closeCurrentChannel() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }

    private void openCurrentFile() throws IOException {
        Path file = resolveFile(currentDateStr, currentId);

        channel = FileChannel.open(
                file,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        );
    }

    private Path resolveFile(String dateStr, int id) {
        return directory.resolve(prefix + "_" + dateStr + "_" + id + ".log");
    }

    private LocalDate toLocalDate(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private long computeNextDayThreshold(LocalDate today) {
        return today.plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private int findLastFileId(String todayStr) throws IOException {
        int maxId = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();

                if (isMatchingLogFile(name, todayStr)) {
                    int id = extractId(name);
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
        }

        return maxId;
    }

    private boolean isMatchingLogFile(String name, String todayStr) {
        return name.startsWith(prefix + "_" + todayStr + "_")
                && name.endsWith(".log");
    }

    private int extractId(String name) {
        int idx1 = name.lastIndexOf('_');
        int idx2 = name.lastIndexOf(".log");

        if (idx1 > 0 && idx2 > idx1) {
            try {
                return Integer.parseInt(name.substring(idx1 + 1, idx2));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public void close() throws IOException {
        closeCurrentChannel();
        channel = null;
    }
}

