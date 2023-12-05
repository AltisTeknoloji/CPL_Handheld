package com.altistek.cpl_handheld.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

// First create SQLite DB with all properties and Version Control in that section
public class DatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = "-SQLite-";
    private static DatabaseHelper _instance;

    public static synchronized DatabaseHelper getInstance(Context context){
        if (_instance == null){
            _instance = new DatabaseHelper(context.getApplicationContext());
        }
        return _instance;
    }

    public DatabaseHelper(@Nullable Context context) {
        super(context, "PalletTracker_CPL.db", null, 1);
        Log.d(TAG,"Constructed.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Created.");
        // Pallet Tables Create
        String palletTableSQL = " CREATE TABLE Pallets (Id INTEGER PRIMARY KEY AUTOINCREMENT,Barcode TEXT, EPC TEXT, LastSeenCompanyId INTEGER, LastSeenWarehouseId INTEGER, JobOrderId INTEGER, ShipmentType INTEGER, GateName TEXT,Movement TEXT NOT NULL, IsSend INTEGER NOT NULL) ";
        db.execSQL(palletTableSQL);
        // User Table Create
        String userTableSQL = " CREATE TABLE Users (Id INTEGER PRIMARY KEY, Username TEXT NOT NULL, UserRoleId INTEGER) ";
        db.execSQL(userTableSQL);
        // Companies Table Create
        String companiesTableSQL = " CREATE TABLE Companies (Id INTEGER PRIMARY KEY, Name TEXT NOT NULL, CompanyType INTEGER NOT NULL) ";
        db.execSQL(companiesTableSQL);
        // Warehouses Table Create
        String warehousesTableSQL = " CREATE TABLE Warehouses (Id INTEGER PRIMARY KEY, CompanyId INTEGER NOT NULL, Name TEXT NOT NULL) ";
        db.execSQL(warehousesTableSQL);
        // Reader Properties Table Create
        String readerTableSQL = " CREATE TABLE ReaderProps (Id INTEGER PRIMARY KEY, RfidDuty INTEGER, RfidPower INTEGER, RfidMode INTEGER, RfidMask TEXT) ";
        db.execSQL(readerTableSQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgraded.");
        db.execSQL("DROP TABLE IF EXISTS Pallets");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Companies");
        db.execSQL("DROP TABLE IF EXISTS Warehouses");
        db.execSQL("DROP TABLE IF EXISTS ReaderProps");
    }
}

