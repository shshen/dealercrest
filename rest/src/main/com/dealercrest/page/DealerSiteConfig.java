package com.dealercrest.page;

import java.util.Map;

import org.json.JSONObject;

public class DealerSiteConfig {

    private final String dealerId;
    private final JSONObject commonData;
    private final Map<String, JSONObject> pageData;

    public DealerSiteConfig(String dealerId, String manifest, 
            JSONObject commonData, Map<String, JSONObject> pageData) {
        this.dealerId = dealerId;
        this.commonData = commonData;
        this.pageData = pageData;
    }
    public String getDealerId() {
        return dealerId;
    }
    public JSONObject getCommonData() {
        return commonData;
    }
    public JSONObject getPageData(String pagePath) {
        return pageData.get(pagePath);
    }
    
}
