package com.dealercrest.page;

import java.util.HashMap;
import java.util.Map;

public class ThemeStore {
    
    private final Map<String, DealerTheme> themes;

    public ThemeStore() {
        this(new HashMap<>());
    }

    public ThemeStore(Map<String, DealerTheme> themes) {
        this.themes = themes;
    }

    public void addTheme(DealerTheme theme) {
        themes.put(theme.getThemeName(), theme);
    }

    public DealerTheme getTheme(String themeName) {
        return themes.get(themeName);
    }

    

}
