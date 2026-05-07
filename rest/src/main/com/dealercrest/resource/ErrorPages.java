package com.dealercrest.resource;

import java.util.HashMap;
import java.util.Map;

import com.dealercrest.page.Page;
import com.dealercrest.page.StaticPage;

public class ErrorPages {

    private final Map<Integer, StaticPage> errorPages;

    public ErrorPages() {
        this.errorPages = new HashMap<>();
    }

    public Page get(int statusCode) {
        return errorPages.get(statusCode);
    }

    public void addErrorPage(int statusCode, StaticPage page) {
        errorPages.put(statusCode, page);
    }
    
}
