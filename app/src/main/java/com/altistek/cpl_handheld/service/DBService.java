package com.altistek.cpl_handheld.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.altistek.cpl_handheld.helpers.Logger;
import com.altistek.cpl_handheld.sqlite.DatabaseController;
import com.altistek.cpl_handheld.sqlite.controllers.Pallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DBService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private final String TAG = "BACKGROUND SYNC SERVICE";
    private final int MILLIS = 10000; //  ms - 10 sec
    private Runnable runnable;
    private final ApiServiceOkHttp apiService;
    private DatabaseController databaseController;
    private Logger logger;
    private boolean isTaskRunning = false;
    public DBService() {
        super("DBService");
        apiService = new ApiServiceOkHttp();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Log.d(TAG,"handleIntent Method in IntentService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Method in IntentService");
        Log.d(TAG, "onStartCommand");
        final Handler handler = new Handler();
        final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        databaseController = new DatabaseController(getApplicationContext());
        Log.d(TAG, "Data source created. line 51");
        //apiService = new ApiService(this,getApplicationContext());
        logger = new Logger();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Runnable started. line 56");
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                // That variable status checks for Internet connection.
                boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
//                Log.d(TAG, "Network info handled. line 60 " + networkInfo.isConnectedOrConnecting());
                if (isConnected && !isTaskRunning) {
                    //updateWarehouseTable();
                    Log.d(TAG, "WebApiTask starting");
                    //logger.addRecordToLog(Logger.LogType.INFO, TAG, "Sending to API task starting...");
                    new DBService.WebApiTask().execute();
                }
                handler.postDelayed(runnable, MILLIS);
            }
        };
        handler.postDelayed(runnable, MILLIS);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //Log.d(TAG,"onDestroy Method in IntentService");
        super.onDestroy();
    }


    class WebApiTask extends AsyncTask<String, Void, Void> {

        private Exception exception;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                //updatePalletTable();
                Log.d(TAG, "WebApiTask starting...");
                isTaskRunning = true;
//                updateWarehouseTable();
                isTaskRunning = false;
            } catch (Exception e) {
                Log.d(TAG, "Exception is " + e);
                exception = e;
            }
            return null;
        }

        protected void onPostExecute() {
            // Check exception
            Log.d(TAG, "Exception in onPostExecute" + exception);
        }
    }



}

