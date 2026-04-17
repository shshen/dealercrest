package com.dealercrest.page;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * VehicleRecord — one vehicle row returned from the database.
 * toMap() converts it for the template engine context.
 */
public class Vehicle {

    private String vin;
    private int year;
    private String make;
    private String model;
    private String trim;
    private String condition;
    private int price;
    private int mileage;
    private String image;
    private String badge;

    public Vehicle(String vin, int year, String make, String model,
            String trim, String condition, int price, int mileage,
            String image, String badge) {
        this.vin = vin;
        this.year = year;
        this.make = make;
        this.model = model;
        this.trim = trim;
        this.condition = condition;
        this.price = price;
        this.mileage = mileage;
        this.image = image;
        this.badge = badge;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getVin() {
        return vin;
    }

    public int getYear() {
        return year;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getTrim() {
        return trim;
    }

    public String getCondition() {
        return condition;
    }

    public int getPrice() {
        return price;
    }

    public int getMileage() {
        return mileage;
    }

    public String getImage() {
        return image;
    }

    public String getBadge() {
        return badge;
    }

    /**
     * Convert to Map so the template engine can access ${v.make}, ${v.price} etc.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vin", vin);
        m.put("year", year);
        m.put("make", make);
        m.put("model", model);
        m.put("trim", trim);
        m.put("condition", condition);
        m.put("price", price);
        m.put("mileage", mileage);
        m.put("image", image);
        m.put("badge", badge);
        return m;
    }
}
