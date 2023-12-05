package com.altistek.cpl_handheld.grai;


import android.util.Log;

import java.math.BigInteger;

public class Grai {
    /*
    CPL'de kullanılan GRAI düzeninin EPC'yi binary formata çevirip açtığımızda

    00110111    filter  101 gs1companyprefix    assettype   serial
                3 bit       24 bit              20 bit      118 bit

     Burada filter, companyprefix ve assetType'i elde ederken
     bitler hex formata geçirilir. Elde edilen değerler istediğimiz sonuçlardır.

     Serial format elde edilirken bitler 7'şerli olarak gruplanır
     ve ASCII karşılıkları serial değeri olur.

     */

    public enum Tag {
        BARCODE,
        EPC
    }

    private final String TAG = "-Grai-";

    private final String tag;
    private String filter;
    private String companyPrefix;
    private String assetType;
    private String checkDigit;
    private String serialNo;
    private String barcode;

    public Grai(String tag, Tag type) {
        this.tag = tag;
        if(type == Tag.BARCODE){
            barcodeToEpc();
        }
        else if (type == Tag.EPC){
            epcToBarcode();
        }
    }

    public void barcodeToEpc(){
        // EMPTY FOR NOW
        // BECAUSE DONT KNOW HOW TO PRODUCE
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setCompanyPrefix(String companyPrefix) {
        this.companyPrefix = companyPrefix;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public void setCheckDigit(String checkDigit) {
        this.checkDigit = checkDigit;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTag() {
        return tag;
    }

    public String getCompanyPrefix() {
        return companyPrefix;
    }

    public String getFilter() {
        return filter;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getCheckDigit() {
        return checkDigit;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getBarcode() {
        return barcode;
    }

    private void epcToBarcode() {
        String rfidTagBinary = hexToBinary(tag);
        Log.d(TAG, "rfidTagBinary " + rfidTagBinary);

        // Filter setting
        String tempFilter = rfidTagBinary.substring(6, 9);
        this.filter = binaryToDecimal(tempFilter);

        // companyPrefix setting
        String companyPre = rfidTagBinary.substring(12, 36);
        this.companyPrefix = binaryToDecimal(companyPre);

        // AssetType setting
        StringBuilder tempAsset = new StringBuilder(binaryToDecimal(rfidTagBinary.substring(36, 56)));
        int diff1 = 5 - tempAsset.length();
        for (int i=0;i<diff1;i++){
            tempAsset.insert(0, "0");
        }
        this.assetType = tempAsset.toString();

        // serialNo setting
        StringBuilder tempSerial = new StringBuilder(rfidTagBinary.substring(56, rfidTagBinary.length()));
        int diff2 = 7 - (tempSerial.length() % 7);
        if (diff2 < 7) {
            for (int i = 0; i < diff2; i++) {
                tempSerial.insert(tempSerial.length(), "0");
            }
        }

        int strPieces = tempSerial.length() / 7;
        String[] serialResult = new String[strPieces];
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strPieces; i++) {
            try {
                serialResult[i] = tempSerial.substring(i * 7, (i * 7) + 7);
                result.append(binaryToASCII(tempSerial.substring(i * 7, (i * 7) + 7)));
            } catch (Exception e) {
                Log.d(TAG, " Exception is " + e);
            }

        }
        this.serialNo = result.substring(0,8);

        // check Digit setting
        checkDigit = String.valueOf(checkDigit(companyPrefix+assetType));

        // barcode setting
        this.barcode = "8003"+filter+companyPrefix+assetType+checkDigit+serialNo;
    }

    private long checkDigit(String s){
        int length = s.length();
        long sum = Long.parseLong(s);
        long result=0;
        for (int i=length;i>0;i--){
            long power = (long) Math.pow(10,i-1);
            long temp = sum / power;
            sum = sum - temp * power;
            if (i%2==0){
                result = result + temp; // temp*1
            }
            else{
                result = result + (temp *3);
            }
        }
        if (result%10 == 0){
            return 1;
        }
        else {
            return 10 - (result%10);
        }
    }

    private String hexToBinary(String hex) {
        return new BigInteger(hex, 16).toString(2);
    }
    private String binaryToDecimal(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return Integer.toString(decimal);
    }
    private String binaryToASCII(String binary) {
        int nb = Integer.parseInt(binary, 2);
        return String.valueOf(Character.toChars(nb));
    }

}
