package com.dealercrest.db.vehicle;

import java.math.BigDecimal;

public class Vehicle {
 
    private String  id;
    private String  dealerId;
    private String  vin;
    private int     year;
    private String  make;
    private String  model;
    private String  trim;
    private String  exteriorColor;
    private int     mileage;
    private BigDecimal price;
    private String  status;      // "available", "sold", "pending"
    private String  condition;   // "new", "used", "certified"
 
    public String getId()            { return id; }
    public String getDealerId()      { return dealerId; }
    public String getVin()           { return vin; }
    public int    getYear()          { return year; }
    public String getMake()          { return make; }
    public String getModel()         { return model; }
    public String getTrim()          { return trim; }
    public String getExteriorColor() { return exteriorColor; }
    public int    getMileage()       { return mileage; }
    public BigDecimal getPrice()     { return price; }
    public String getStatus()        { return status; }
    public String getCondition()     { return condition; }
 
    public void setId(String id)                    { this.id = id; }
    public void setDealerId(String dealerId)        { this.dealerId = dealerId; }
    public void setVin(String vin)                  { this.vin = vin; }
    public void setYear(int year)                   { this.year = year; }
    public void setMake(String make)                { this.make = make; }
    public void setModel(String model)              { this.model = model; }
    public void setTrim(String trim)                { this.trim = trim; }
    public void setExteriorColor(String color)      { this.exteriorColor = color; }
    public void setMileage(int mileage)             { this.mileage = mileage; }
    public void setPrice(BigDecimal price)          { this.price = price; }
    public void setStatus(String status)            { this.status = status; }
    public void setCondition(String condition)      { this.condition = condition; }
 
    public String toString() {
        return year + " " + make + " " + model + " " + trim
            + " [" + vin + "] $" + price + " - " + status;
    }
}
