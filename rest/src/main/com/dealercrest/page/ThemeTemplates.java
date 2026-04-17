package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class ThemeTemplates {

    private final String themeName;
    private final Map<String, BlockTemplate> blocks;
    private final Map<String, Layout> layouts;

    public ThemeTemplates(String themeName) {
        this.themeName = themeName;
        this.blocks = new HashMap<>();
        this.layouts = new HashMap<>();
    }

    public String getThemeName() {
        return themeName;
    }
    public void addBlock(BlockTemplate block) {
        blocks.put(block.getName(), block);
    }
    public BlockTemplate getBlock(String name) {
        return blocks.get(name);
    }
    public Layout getLayout(String name) {
        return layouts.get(name);
    }
    public void addLayout(String path, String layout) {
        // TODO
    }

}
