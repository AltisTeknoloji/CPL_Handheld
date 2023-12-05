package com.altistek.cpl_handheld.sqlite.controllers;

import android.util.Log;

public class Company {
    // Company Structure (in SQL Server)

    //"Id": 49,
    //"Name": "xxxx",
    //"Address": "xxxx",
    //"Phone": "xxxx",
    //"CityId": "xxxxx"
    //"CompanyType": "xxxxxxxxxxxxxxxx",
    // ...

    // 3 data type (Id, Name, CompanyType) is enough to using for spinner

    private final String TAG = "-CompanyObject-";

    private int id;
    private String name;
    private int companyType;

    public Company(int id, String name, int companyType) {
        Log.d(TAG, "Creating Company Object");
        this.id = id;
        this.name = name;
        this.companyType = companyType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompanyType(int companyType) {
        this.companyType = companyType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public int getCompanyType() {
        return companyType;
    }
}
