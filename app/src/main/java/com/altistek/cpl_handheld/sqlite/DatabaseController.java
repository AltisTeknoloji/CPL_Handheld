package com.altistek.cpl_handheld.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.altistek.cpl_handheld.sqlite.controllers.Company;
import com.altistek.cpl_handheld.sqlite.controllers.ReaderProps;
import com.altistek.cpl_handheld.sqlite.controllers.User;
import com.altistek.cpl_handheld.sqlite.controllers.Warehouse;
import com.altistek.cpl_handheld.sqlite.controllers.Pallet;

import java.util.ArrayList;
import java.util.List;

// LIKE A SOCKET PROGRAMMING
// Data management on that section
public class DatabaseController {
    private final String TAG = "-DataSource-";
    SQLiteDatabase sqLiteDB;
    DatabaseHelper dbHelper;

    public DatabaseController(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }


    // Check table is null
    public boolean tableIsNull(String table) {
        //Log.d(TAG, "tableIsNull");
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        boolean empty = true;
        Cursor cur = sqLiteDB.rawQuery("SELECT COUNT(*) FROM " + table, null);
        if (cur != null && cur.moveToFirst()) {
            empty = (cur.getInt(0) == 0);
        }
        cur.close();
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
        return empty;
    }

    // Check choosen table row is exist
    private boolean isRowExist(String tableName, int rowId) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        boolean result;
        Cursor cur = sqLiteDB.rawQuery("SELECT 1 FROM " + tableName + " WHERE Id = " + rowId, null);
        if (cur != null && cur.moveToFirst()) {
            //result = (cur.getInt (0) == 0);
            result = true;
        } else
            result = false;
        cur.close();
        Log.d(TAG, "result is " + result);
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
        return result;
    }

    //region Save the object in the corresponding table
    // Save pallet in Pallets table
    public void savePalletInTable(Pallet pallet) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "save pallet in Pallets table");
        // Value for Query
        ContentValues values = new ContentValues();
        //values.put("Id", pallet.getId());
        values.put("EPC", pallet.getEPC());
        values.put("Barcode", pallet.getBarcode());
        sqLiteDB.insert("Pallets", null, values);
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
    }

    // Save company in Companies table
    public void saveCompanyInTable(Company company) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "save company in Company table");
        // Value for Query
        ContentValues values = new ContentValues();
        values.put("Id", company.getId());
        values.put("Name", company.getName());
        values.put("CompanyType", company.getCompanyType());
        if (isRowExist("Companies", company.getId())) {
            sqLiteDB.update("Companies", values, "Id=" + company.getId(), null);
        } else {
            sqLiteDB.insert("Companies", null, values);
        }
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
    }


    // Save warehouse in Warehouses table
    public void saveWarehouseInTable(Warehouse warehouse) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "save warehouse in Warehouses table");
        // Value for Query
        ContentValues values = new ContentValues();
        values.put("Id", warehouse.getId());
        values.put("CompanyId", warehouse.getCompanyId());
        values.put("Name", warehouse.getName());

        if (isRowExist("Warehouses", warehouse.getId())) {
            sqLiteDB.update("Warehouses", values, "Id=" + warehouse.getId(), null);
        } else {
            sqLiteDB.insert("Warehouses", null, values);
        }
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
    }

    // Save reader in Warehouses table
    public void saveReaderPropsInTable(ReaderProps readerProps) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "new/update readerProps in ReaderProps table");
        // Value for Query
        ContentValues values = new ContentValues();
        values.put("Id", readerProps.getId());
        values.put("RfidDuty", readerProps.getDuty());
        values.put("RfidPower", readerProps.getPower());
        values.put("RfidMode", readerProps.getMode());
        values.put("RfidMask", readerProps.getMask());
        Log.d(TAG, "mask value " + readerProps.getMask());
        if (!tableIsNull("ReaderProps")) {
            sqLiteDB.update("ReaderProps", values, "Id=" + readerProps.getId(), null);
        } else {
            sqLiteDB.insert("ReaderProps", null, values);
        }
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
    }
    //endregion



    public ReaderProps getReaderProps(int id) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        ReaderProps readerProps = null;
        Cursor cursor = sqLiteDB.query("ReaderProps", new String[]{"Id", "RfidDuty", "RfidPower", "RfidMode", "RfidMask"}, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int Id = cursor.getInt(0);
            if (id == Id) {
                int Duty = cursor.getInt(1);
                int Power = cursor.getInt(2);
                int Mode = cursor.getInt(3);
                String Mask = cursor.getString(4);
                readerProps = new ReaderProps(Id, Duty, Power, Mode, Mask);
                break;
            } else {
                cursor.moveToNext();
            }
        }
        cursor.close();
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
        return readerProps;
    }
    //endregion



    public List<Warehouse> getAllWarehouses() {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        // Like a searcher(pointer) in DB
        Log.d(TAG, "getWarehouses");
        List<Warehouse> warehouses = new ArrayList<>();
        //Warehouse[] warehouses = new Warehouse;
        Cursor cursor = sqLiteDB.query("Warehouses", new String[]{"Id", "CompanyId", "Name"}, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int Id = cursor.getInt(0);
            int companyId = cursor.getInt(1);
            String name = cursor.getString(2);
            Warehouse tempWarehouse = new Warehouse(Id, companyId, name);
            warehouses.add(tempWarehouse);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
        return warehouses;
    }

    //region Independent
    public int getCPLId() {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "getCPLId");
        Cursor cursor = sqLiteDB.query("Companies", new String[]{"Id", "Name", "CompanyType"}, null, null, null, null, null, null);
        cursor.moveToFirst();
        int temp = 0;
        while (!cursor.isAfterLast()) {
            temp = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
        return temp;
    }


    public void updateRowInPallet(Pallet pallet) {
        sqLiteDB = dbHelper.getWritableDatabase();
        sqLiteDB.beginTransaction();
        Log.d(TAG, "update pallet in Pallets table");
        // Value for Query
        ContentValues values = new ContentValues();
        //values.put("Id",pallet.getId());
        values.put("EPC", pallet.getEPC());
        values.put("Barcode", pallet.getBarcode());

        sqLiteDB.setTransactionSuccessful();
        sqLiteDB.endTransaction();
    }



    //endregion


}

