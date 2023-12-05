package com.altistek.cpl_handheld.helpers;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
    public enum LogType {
        INFO,
        WARN,
        ERROR
    }

    private final String LOG_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/RTITracker";
    private String LOG_FILE_PATH = LOG_FOLDER_PATH + "/" + calculateTime("dd_MM_yyyy") + "_log.txt";
    private FileIO fileIO;

    public Logger() {
        fileIO = new FileIO();
    }

    public void createLogFiles(){
        fileIO.createFolder(LOG_FOLDER_PATH);
        fileIO.createFile(LOG_FILE_PATH);
    }

    public void addRecordToLog(LogType logType, String errorSource, String message){
        LOG_FILE_PATH = LOG_FOLDER_PATH + "/" + calculateTime("dd_MM_yyyy") + "_log.txt";
        String typicalMessage =
                calculateTime("dd/MM/yyyy HH:mm:ss") + "; " +
                        logType + "; " +
                        errorSource + "; " +
                        message;

        fileIO.append(LOG_FILE_PATH, typicalMessage);
    }

    @SuppressLint("SimpleDateFormat")
    private static String calculateTime(String pattern) {
        Date currentTime = Calendar.getInstance().getTime();
        return new SimpleDateFormat(pattern).format(currentTime);
    }
}


