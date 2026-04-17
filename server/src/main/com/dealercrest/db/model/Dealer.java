package com.dealercrest.db.model;

public class Dealer {
    private final String id;
    private final String name;
    private final String address;
    private final String phone;
    private final long lastUpdate;

    public Dealer(String id, String name, String address, String phone, long lastUpdate) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }
    public String getname() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getPhone() {
        return phone;
    }
    public long getLastUpdate() {
        return lastUpdate;
    }
}
