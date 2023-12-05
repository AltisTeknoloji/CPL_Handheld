package com.altistek.cpl_handheld.sqlite.controllers;

public class ShipmentType {

    private int id;
    private String type;

    public ShipmentType(int id, String type){
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
