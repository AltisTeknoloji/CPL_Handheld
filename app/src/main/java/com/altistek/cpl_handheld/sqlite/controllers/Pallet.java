package com.altistek.cpl_handheld.sqlite.controllers;

// Two table for that object
// 'TempPallets' have a non-network connection api requests,
// 'Pallets' have all pallets in remote server

public class Pallet {
    // Pallet Structure (in SQL Server)

    //"Id": 1,
    //"EPC": "xxx",
    //"MouldCode": "xxx",
    //"PoolCode": "xxx",
    //"ProducerCompanyId": 1
    //"LastSeenCompanyId": 1,
    //"LastSeenWarehouseId": 1
    //"GRAI_Full": null,
    //"GRAI_SerialNumber": null,
    //"GRAI_AssetType": null,
    //"GRAI_CompanyPrefix": null,
    // ...

    // 3 data type (Id, LastSeenCompanyId, LastSeenWarehouseId) is enough to change the position of the pallet

    // isSended 1 = TRUE
    // isSended 0 = FALSE

    private final String TAG = "-PalletObject-";

//    private int id;
//    private String Barcode;
//    private String EPC;
//    private int lastSeenCompanyId;
//    private int lastSeenWarehouseId;
//    private int jobOrderId;
//    private int shipmentType;
//    private String gateName;
//    private String movement;
//    private int isSend;

    private int PN;
    private String Barcode;
    private String SN;
    private String EPC;
    private String URI;
    private String RSSI1;
    private String RSSI2;
    private String RSSI3;
    private String RSSI4;
//    private String Direction;
    private String WC;


    public Pallet(String barcode, String EPC, int PN,String SN, String URI, String RSSI1, String RSSI2, String RSSI3,String RSSI4,String WC) {
        this.Barcode = barcode;
        this.EPC = EPC;
        this.PN=PN;
        this.SN = SN;
        this.URI = URI;
        this.RSSI1 = RSSI1;
        this.RSSI2 = RSSI2;
        this.RSSI3 = RSSI3;
        this.RSSI4 = RSSI4;
//        this.Direction = Direction;
        this.WC = WC;
    }

//    public Pallet(int id,String barcode, String EPC, int lastSeenCompanyId, int lastSeenWarehouseId, int jobOrderId, int shipmentType, String gateName,String movement, int isSend) {
//        this.id = id;
//        this.Barcode = barcode;
//        this.EPC = EPC;
//        this.lastSeenCompanyId = lastSeenCompanyId;
//        this.lastSeenWarehouseId = lastSeenWarehouseId;
//        this.jobOrderId = jobOrderId;
//        this.shipmentType = shipmentType;
//        this.gateName = gateName;
//        this.movement = movement;
//        this.isSend = isSend;
//    }

//    public void setId(int id){
//        this.id = id;
//    }

    public void setBarcode(String barcode) {
        this.Barcode = barcode;
    }

    public void setEPC(String EPC) {
        this.EPC = EPC;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public void setRSSI1(String RSSI1) {
        this.RSSI1 = RSSI1;
    }

    public void setRSSI2(String RSSI2) {
        this.RSSI2 = RSSI2;
    }

    public void setRSSI3(String RSSI3) {
        this.RSSI3 = RSSI3;
    }

    public void setRSSI4(String RSSI4) {
        this.RSSI4 = RSSI4;
    }

    public void setWC(String WC) {
        this.WC = WC;
    }
    public void setPN(int PN) {
        this.PN = PN;
    }
//    public void setDirection(String Direction) {
//        this.Direction = Direction;
//    }

    public String getBarcode() {
        return Barcode;
    }

    public int getPN() {
        return PN;
    }

    public String getEPC() {
        return EPC;
    }

    public String getSN() {
        return SN;
    }

    public String getURI() {
        return URI;
    }

    public String getRSSI1() {
        return RSSI1;
    }

    public String getRSSI2() {
        return RSSI2;
    }

    public String getRSSI3() {
        return RSSI3;
    }

    public String getRSSI4() {
        return RSSI4;
    }

//    public String getDirection() {
//        return Direction;
//    }
    public String getWC() {
        return WC;
    }

    @Override
    public String toString() {
        return "Pallet{" +
                ", PN=" + PN +
                ", Barcode='" + Barcode + '\'' +
                ", EPC='" + EPC + '\'' +
                ", SN=" + SN +'\'' +
                ", URI=" + URI +'\'' +
                ", RSSI1=" + RSSI1 +'\'' +
                ", RSSI2=" + RSSI2 +'\'' +
                ", RSSI3='" + RSSI3 + '\'' +
                ", RSSI4='" + RSSI4 + '\'' +
                ", WC='" + WC + '\'' +
                '}';
    }
}
