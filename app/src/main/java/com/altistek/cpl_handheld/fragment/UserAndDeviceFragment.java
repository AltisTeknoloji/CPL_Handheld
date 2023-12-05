package com.altistek.cpl_handheld.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import java.lang.ref.WeakReference;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;


import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.service.AuthService;

import org.json.JSONObject;

public class UserAndDeviceFragment extends Fragment {

    private final String TAG = "-UserAndDeviceFragment-";

    private TextView tvUsername;
    private TextView tvNameSurname;

    private TextView tvSLEDBattery;
    private TextView tvDeviceBattery;

    private Button btnExit;
    private Button btnSendPallets;

    private Handler mOptionHandler;
    private Context mContext;
    private Reader mReader;
    private AuthService authService;
    private Intent batteryStatus;
    private IntentFilter ifilter;

    private final UserAndDeviceHandler mUserAndDeviceHandler = new UserAndDeviceHandler(this);
    public static UserAndDeviceFragment newInstance() {
        return new UserAndDeviceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.user_and_device_fragment,container,false);

        mContext = inflater.getContext();
        authService = new AuthService(mContext);
        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;
        mReader = Reader.getReader(mContext, mUserAndDeviceHandler);

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = mContext.registerReceiver(null, ifilter);

//        tvUsername = (TextView) v.findViewById(R.id.tv_username);
//        tvNameSurname = (TextView) v.findViewById(R.id.tv_name_surname);
//        JSONObject temp = authService.GetCurrentUser();
//        try {
//            tvUsername.setText((String) temp.get("username"));
//            tvNameSurname.setText((String) temp.get("name") +" "+ temp.get("surname"));
//        }catch (Exception ignored){
//
//        }



        tvSLEDBattery = (TextView) v.findViewById(R.id.tv_sled_battery);
        tvDeviceBattery = (TextView) v.findViewById(R.id.tv_device_battery);

        btnSendPallets = (Button) v.findViewById(R.id.btn_send_pallet);
        btnSendPallets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getActivity().getSystemService(DBService.class).onDestroy();
//                getActivity().startService(new Intent(mContext, DBService.class));
                Toast.makeText(mContext, getString(R.string.service_started), Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, getString(R.string.service_already_started), Toast.LENGTH_SHORT).show();
            }
        });


        btnExit = (Button) v.findViewById(R.id.btn_user_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"User exit button click event");
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("");
                builder.setMessage("Are you sure ?");
                builder.setNegativeButton("No",null);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"Logout");
                        authService.Logout();
                        getActivity().finish();
                        Intent intent = new Intent(mContext,MainActivity.class);
                        getActivity().startActivity(intent);
                    }
                });
                builder.show();
            }
        });

        return v;
    }

    private void updateBatteryStatus(int SLED, int Device){
        if ( SLED < 0 || SLED > 100) {
            tvSLEDBattery.setText(getString(R.string.disconnected_str));
            tvSLEDBattery.setTextColor(ContextCompat.getColor(mContext,R.color.red));
        }
        else {
            if (SLED <= 20)
                tvSLEDBattery.setTextColor(ContextCompat.getColor(mContext,R.color.red));
            else
                tvSLEDBattery.setTextColor(ContextCompat.getColor(mContext,R.color.green));
            tvSLEDBattery.setText("%"+String.format("%s", SLED));
        }
        if (Device <= 20)
            tvDeviceBattery.setTextColor(ContextCompat.getColor(mContext,R.color.red));
        else
            tvDeviceBattery.setTextColor(ContextCompat.getColor(mContext,R.color.green));
        tvDeviceBattery.setText("%"+String.format("%s", Device));
    }

    private static class UserAndDeviceHandler extends Handler{
        private final WeakReference<UserAndDeviceFragment> mExecutor;
        public UserAndDeviceHandler(UserAndDeviceFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            UserAndDeviceFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
                Log.d("TAG","handleMessage");
            }
        }
    }

    public void handleMessage(Message m) {
        if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED){
            // Battery status (int)
            int temp = mReader.SD_GetBatteryStatus();
            updateBatteryStatus(temp,1);

        }
        if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
        }
    }

    @Override
    public void onStart() {
        mReader = Reader.getReader(mContext, mUserAndDeviceHandler);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float)scale;
        int sled = mReader.SD_GetBatteryStatus();

        updateBatteryStatus(sled, (int) batteryPct);
        super.onStart();
    }

    @Override
    public void onPause() {
        //mContext.unregisterReceiver(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        //mContext.unregisterReceiver(null);
        super.onStop();
    }

}

