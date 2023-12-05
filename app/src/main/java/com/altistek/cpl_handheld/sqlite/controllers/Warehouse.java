package com.altistek.cpl_handheld.sqlite.controllers;

import android.util.Log;

public class Warehouse {
    // Company Structure (in SQL Server)

    //"Id": 49,
    //"CompanyId": "xxxx",
    //"Lat": "xxxx",
    //"Lng": "xxxx",
    //"Name": "xxxxx"
    //...

    // 3 data type (Id, CompanyId, Name) is enough to using for spinner

    private final String TAG = "-WarehouseObject-";

    private int id;
    private int companyId;
    private String name;

    public Warehouse(int id, int companyId, String name) {
        Log.d(TAG,"Creating Warehouse Object");
        this.id = id;
        this.companyId = companyId;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getName() {
        return this.name;
    }
}
