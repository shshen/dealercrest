package com.dealercrest.rest;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TempFile implements AutoCloseable {

    private final Path path;

    /**
     * Create a temp file in the given directory.
     * 
     * @param dir directory to create the temp file
     * @param prefix filename prefix
     * @param suffix filename suffix (e.g. ".tmp")
     */
    public TempFile(Path dir, String prefix, String suffix) throws IOException {
        Files.createDirectories(dir); // ensure directory exists
        this.path = Files.createTempFile(dir, prefix, suffix);
    }

    public TempFile() throws IOException {
        this(Paths.get("./data/temp"), "download", ".tmp");
    }

    /**
     * Return the Path of the temp file.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Return the File of the temp file (convenience).
     */
    public File getFile() {
        return path.toFile();
    }

    /**
     * Delete the temp file if it wasn't moved.
     */
    @Override
    public void close() throws IOException {
        Files.deleteIfExists(path);
    }

    @Override
    public String toString() {
        return "TempFile(" + path + ")";
    }
    
}
