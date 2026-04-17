package com.dealercrest.page;

import org.json.JSONObject;

public class PageDefinition {

    private final String path;
    private final ThemeTemplates themeTemplates;
    private final JSONObject blockConfig;
    private final Layout layout;

    public PageDefinition(String path, ThemeTemplates themeTemplates, JSONObject blockConfig, Layout layout) {
        this.path = path;
        this.layout = layout;
        this.blockConfig = blockConfig;
        this.themeTemplates = themeTemplates;
    }

    public String getPath() {
        return path;
    }
    public ThemeTemplates getThemeTemplates() {
        return themeTemplates;
    }
    public JSONObject getBlockConfig() {
        return blockConfig;
    }
    public Layout getLayout() {
        return layout;
    }

}
