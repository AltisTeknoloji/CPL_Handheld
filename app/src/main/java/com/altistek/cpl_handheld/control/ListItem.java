package com.altistek.cpl_handheld.control;


public class ListItem {

    public int mIv;

    public String mUt;

    public String mDt;

    public String EPC;

    public String Barcode;

    public boolean mHasPc;

    public ListItem() {
        mIv = -1;
        mUt = null;
        mDt = null;
        mHasPc = true;
        EPC = "";
        Barcode = "";
    }
}

