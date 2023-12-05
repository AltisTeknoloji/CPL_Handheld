package com.altistek.cpl_handheld.sqlite.controllers;

public class JobOrder {

    private final int id;
    private String billOfLadingNo;
    private String poolCode;
    private String companyName;
    private final int amount;
    private final int companyId;
    private final int warehouseId;
    private final int gateId;

    public JobOrder(int id, String billOfLadingNo,int amount, int companyId, String companyName, int warehouseId, int gateId){
        this.id = id;
        this.billOfLadingNo = billOfLadingNo;
        this.amount = amount;
        this.companyId = companyId;
        this.companyName = companyName;
        this.warehouseId = warehouseId;
        this.gateId = gateId;
    }

    public int getId() {
        return id;
    }

    public String getBillOfLadingNo() {
        return billOfLadingNo;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public int getAmount() {
        return amount;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public int getGateId() {
        return gateId;
    }

}
