package com.dealercrest.http;

import java.util.Map;

import javax.sql.DataSource;

import org.json.JSONObject;

import com.dealercrest.rest.MapParam;
import com.dealercrest.rest.QueryParam;
import com.dealercrest.rest.Route;

public class InventoryController {
    
    public InventoryController(DataSource dataSource) {
        
    }

    @Route(path = "/api/inventory")
    public HttpResult listInventory(@MapParam Map<String,String> filter) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    @Route(path = "/api/inventory", method="POST")
    public HttpResult addVehicle() {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    @Route(path = "/api/inventory/{vin}")
    public HttpResult getVehicle(@QueryParam("vin") String vin) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    @Route(path = "/api/inventory/{vin}", method="DELETE")
    public HttpResult deleteVehicle(@QueryParam("vin") String vin) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    // partial update (price change, status)
    @Route(path = "/api/inventory/{vin}", method="PATCH")
    public HttpResult patchVehicle(@QueryParam("vin") String vin) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }

    // full update
    @Route(path = "/api/inventory/{vin}", method="PUT")
    public HttpResult putVehicle(@QueryParam("vin") String vin) {
        JSONObject result = new JSONObject().put("code", 200);
        return new JsonResult(result);
    }
    
}

