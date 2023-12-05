package com.altistek.cpl_handheld;

import com.altistek.cpl_handheld.fragment.*;
import com.altistek.cpl_handheld.helpers.Logger;
import com.altistek.cpl_handheld.service.ApiServiceOkHttp;
import com.altistek.cpl_handheld.service.AuthService;


import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

//import android.app.Fragment;
import android.Manifest;
import android.os.StrictMode;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import com.altistek.cpl_handheld.service.DBService;
import com.altistek.cpl_handheld.sqlite.DatabaseController;
import com.altistek.cpl_handheld.sqlite.controllers.ReaderProps;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "-MainActivity-";

    public static final int MSG_OPTION_DISCONNECTED = 0;
    public static final int MSG_OPTION_CONNECTED = 1;
    public static final int MSG_BACK_PRESSED = 2;

    private Reader serialReader;
    private Context mContext;
    private boolean isConnected;
    public static DatabaseController databaseController;
    private AuthService authService;

    private final MainHandler mMainHandler = new MainHandler(this);

    public final UpdateConnectHandler mUpdateConnectHandler = new UpdateConnectHandler(this);

    private BottomNavigationView bottomNavigation;
    private ApiServiceOkHttp apiServiceOkHttp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Permissions
        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        ActivityCompat.requestPermissions(this, permissions, 1);
        //endregion

        Logger logger = new Logger();
        logger.createLogFiles();

        // Requirements for pages and Connection status
        mContext = this;
        isConnected = false;
        authService = new AuthService( mContext);
        // SQLite
        //SQLite sqLite = new SQLite(mContext);
        databaseController = new DatabaseController(this);
        if(databaseController.tableIsNull("ReaderProps"))
            databaseController.saveReaderPropsInTable(new ReaderProps(0,100, 30, 1, "3714"));
        // API service
        apiServiceOkHttp = new ApiServiceOkHttp();


        // TopBar and Settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setLogo(R.drawable.rti_logo_beyaz_160_32);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.brown)));
        }

        // BottomBar and Settings
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        if (bottomNavigation != null) {
            //bottomNavigation.setItemBackground(new ColorDrawable(getResources().getColor(R.color.brown)));
            bottomNavigation.setItemBackground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.brown)));
        }

        // Set initialize fragment
//        String token = AuthService.GetAccessToken();
//        if (token != null) {
            bottomNavigation.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            //this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            loadFragment(UserAndDeviceFragment.newInstance() );

//            //getWarehouses();
//            new WebApiTask().execute();
//
//            // DBService service
//            Intent dbService = new Intent(mContext,DBService.class);
//            startService(dbService);
//
//        } else {
//            bottomNavigation.setVisibility(View.GONE);
//            toolbar.setVisibility(View.GONE);
//            //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            //loadFragment(LoginFragment.newInstance());
//            //loadFragment(LoginFragment.newInstance());
//            loadLoginFragment();
//        }
    }

    // Initialize choosen fragment
    public void loadFragment(Fragment fragment) {
//        if ( AuthService.GetCurrentUser() == null || AuthService.GetAccessToken() == null){
//            Intent intent = getIntent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            finish();
//            startActivity(intent);
//        }
//        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container,fragment)
                    .addToBackStack(null)
                    .commit();
//        }
    }

    private void loadLoginFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager
                .beginTransaction()
                .replace(R.id.container,LoginFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_user:
                            //loadFragment(ServiceFragment.newInstance());
                            loadFragment(UserAndDeviceFragment.newInstance());
                            break;
                        case R.id.navigation_transfer:
                            loadFragment(TransferFragment.newInstance());
                            break;
//                        case R.id.navigation_add_palet:
////                            loadFragment(ManuelPalletFragment.newInstance());
//                            break;
                        case R.id.navigation_connect:
                            loadFragment(ConnectionFragment.newInstance());
                            break;
                        case R.id.navigation_settings:
                            loadFragment(RFConfigFragment.newInstance());
                            break;
                    }
                    return true;
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connected) {
            Toast.makeText(this, getString(R.string.sled_connected_str), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, " onStart");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Serial Reader
        boolean openResult = false;
        boolean isConnected = false;
        serialReader = Reader.getReader(mContext, mMainHandler);
        if (serialReader != null)
            openResult = serialReader.SD_Open();
        if (openResult == SDConsts.RF_OPEN_SUCCESS) {
            Log.i(TAG, "Reader opened");
            if (serialReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
                isConnected = true;
        } else if (openResult == SDConsts.RF_OPEN_FAIL)
            Log.e(TAG, "Reader open failed");

        updateConnectState(isConnected);

        Log.d(TAG, "onStart end");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Log.d(TAG, "getWindow : " + getWindow());
        //Log.d(TAG, "getWindow Context: " + getWindow().getContext());
        //Log.d(TAG, "getWindow is Active : " + getWindow().isActive());
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "NOTHING ONBACKPRESSED");
        super.onBackPressed();
    }


    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;

        public MainHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message m) {
        Log.d(TAG, "handleMessage");
        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                break;
            case SDConsts.Msg.RFMsg:
                break;
            case SDConsts.Msg.BCMsg:
                break;
        }
    }

    private void updateConnectState(boolean b) {
        isConnected = b;
        invalidateOptionsMenu();
    }

    private static class UpdateConnectHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;

        public UpdateConnectHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleUpdateConnectHandler(msg);
            }
        }
    }

    public void handleUpdateConnectHandler(Message m) {
        if (m.what == MSG_OPTION_DISCONNECTED)
            updateConnectState(false);
        else if (m.what == MSG_OPTION_CONNECTED)
            updateConnectState(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (isConnected)
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class WebApiTask extends AsyncTask<String, Void, Boolean> {
        private Exception exception;

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                if (authService.GetCurrentUser() != null) {
                    Log.d(TAG, "before");

                    String companyString = apiServiceOkHttp.GET("/Company?orderBy=companyTypeId&pageSize=1");
                    Log.d(TAG, "after");
                    Log.d(TAG, "ARRAY : " + companyString);

                    companyString = companyString.substring(1,companyString.length()-1);
                    JSONObject companyJSON = new JSONObject(companyString);
                    Log.d(TAG, "object : " + companyJSON);
                    int id = companyJSON.getInt("id");

                    String warehouseString = apiServiceOkHttp.GET("/Warehouse/ByCompany/" + id);
                    //warehouseString = warehouseString.substring(1,warehouseString.length()-1);
                    JSONArray warehouseJSON = new JSONArray(warehouseString);
                    Log.d(TAG, "warehouse JSON " + warehouseJSON);
                    Log.d(TAG, "warehouse JSON length " + warehouseJSON.length());
                    for (int i=0;i<warehouseJSON.length();i++){
                        JSONObject temp  = (JSONObject) warehouseJSON.get(i);
//                        Warehouse warehouse =
//                                new Warehouse(
//                                        temp.getInt("id"),
//                                        temp.getInt("companyId"),
//                                        temp.getString("name"));
//                        databaseController.saveWarehouseInTable(warehouse);
                    }

                    JSONObject tempJSON = companyJSON.getJSONObject("entity");
                    // Save company in sqlite
//                    Company company = new Company(tempJSON.getInt("Id"),tempJSON.getString("Name"),tempJSON.getInt("CompanyTypeId"));
//                    databaseController.saveCompanyInTable(company);
                }
            } catch (Exception e) {
                this.exception = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
        }
    }

}