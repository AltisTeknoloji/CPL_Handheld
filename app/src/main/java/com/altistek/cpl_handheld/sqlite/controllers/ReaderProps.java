package com.altistek.cpl_handheld.sqlite.controllers;

public class ReaderProps {

    private final String TAG = "-ReaderPropsObject-";

    private int id;
    private int duty;
    private int power;
    private int mode;
    private String mask;

    public ReaderProps(int id,int duty, int power, int mode, String mask) {
        this.id = id;
        this.duty = duty;
        this.power = power;
        this.mode = mode;
        this.mask = mask;
    }

    // SETTERS
    public void setId(int id){
        this.id = id;
    }

    public void setDuty(int duty) {
        if (0 <= duty  && duty <= 1000)
            this.duty = duty;
        else
            this.duty = 100;
    }

    public void setPower(int power) {
        if (5 <= power && power <=30)
            this.power = power;
        else
            this.power = 30;
    }

    public void setMode(int mode) {
        if ( 0 <= mode && mode <= 3)
            this.mode = mode;
        else
            this.mode = 3;
    }

    public void setMask(String mask) {
        if (mask.length() != 0 || mask.equals(""))
            this.mask = mask;
        else
            this.mask = "3714";
    }

    // GETTERS
    public int getId() {
        return id;
    }

    public int getDuty() {
        return duty;
    }

    public int getPower() {
        return power;
    }

    public int getMode() {
        return mode;
    }

    public String getMask() {
        return mask;
    }
}
