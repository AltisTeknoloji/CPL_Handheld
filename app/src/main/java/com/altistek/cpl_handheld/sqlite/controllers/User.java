package com.altistek.cpl_handheld.sqlite.controllers;

import android.util.Log;

public class User {
    // User Structure (in SQL Server)

    //"Id": 49,
    //"Name": "xxxx",
    //"Surname": "xxxx",
    //"Username": "xxxx",
    //"Password": "xxxxx"
    //"RefreshToken": "xxxxxxxxxxxxxxxx",
    //"RefreshTokenValidUntil": "2020-10-20 14:45:13.212"
    //"UserRoleId": 1,
    // ...

    // 3 data type (Id, UserName, UserRoleId) is enough to using in app

    private final String TAG = "-UserObject-";

    private int id;
    private String userName;
    private int userRoleId;

    public User(int id, String userName, int userRoleId) {
        Log.d(TAG, "Creating User Object");
        this.id = id;
        this.userName = userName;
        this.userRoleId = userRoleId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserRoleId(int userRoleId) {
        this.userRoleId = userRoleId;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserRoleId() {
        return userRoleId;
    }
}
