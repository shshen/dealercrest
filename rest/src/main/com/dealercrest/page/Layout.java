package com.dealercrest.page;

public class Layout {

    private final String path;
    private final long lastModified;
    private final String content;

    public Layout(String path, long lastModified, String content) {
        this.path = path;
        this.lastModified = lastModified;
        this.content = content;
    }

    public String getPath() {
        return path;
    }
    public long getLastModified() {
        return lastModified;
    }
    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Layout [path=" + path + ", lastModified=" + lastModified + ", content=" + content + "]";
    }

}
