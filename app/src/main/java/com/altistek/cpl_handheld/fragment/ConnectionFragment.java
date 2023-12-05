package com.altistek.cpl_handheld.fragment;


import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.sqlite.DatabaseController;
import com.altistek.cpl_handheld.sqlite.controllers.ReaderProps;

import java.lang.ref.WeakReference;

public class ConnectionFragment extends Fragment {
    private final String TAG = "-ConnectionFragment-";
    private static final int LOADING_DIALOG = 0;

    private static final int PROGRESS_DIALOG = 1;

    private TextView mConnectStateTextView;
    private TextView mBatteryStateTextView;
    private TextView mBatteryStatePrefixTextView;
    private  TextView mDoubleDotTextView;

    private Button mConnectBt;
    private Button mDisconectBt;
    private Reader mReader;

    private ProgressDialog mDialog;

    private Context mContext;

    private Handler mOptionHandler;

    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedEditor;

    private DatabaseController databaseController;
    private ReaderProps readerProps;

    private final ConnectionHandler mConnectionHandler = new ConnectionHandler(this);

    public static ConnectionFragment newInstance() {
        return new ConnectionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.connection_fragment, container, false);

        mContext = inflater.getContext();
        databaseController = new DatabaseController(mContext);
        //dataSource.open();
        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        sharedPref = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();

        mConnectStateTextView = (TextView)v.findViewById(R.id.connect_state_textview);
        mBatteryStateTextView = (TextView)v.findViewById(R.id.battery_state_textview);
        mBatteryStatePrefixTextView = (TextView)v.findViewById(R.id.battery_state_textview_prefix);
        mDoubleDotTextView = (TextView)v.findViewById(R.id.tv_double_dot);

        mConnectBt = (Button)v.findViewById(R.id.bt_connect);
        mConnectBt.setOnClickListener(buttonListener);
        mDisconectBt = (Button)v.findViewById(R.id.bt_disconnect);
        mDisconectBt.setOnClickListener(buttonListener);


        return v;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mConnectionHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
            updateConnectStateTextView(true);
        else {
            updateConnectStateTextView(false);
        }
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
        //dataSource.close();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        closeDialog();
        //dataSource.close();
        super.onStop();
    }

    private OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int ret = -100;
            switch (v.getId()) {
                case R.id.bt_connect:
                    ret = mReader.SD_Wakeup();
                    if (ret == SDConsts.SDResult.SUCCESS) {
                        Activity activity = getActivity();
                        if (activity != null)
                            createDialog(LOADING_DIALOG, activity.getString(R.string.connecting_str));
                    }
                    Log.d(TAG, "wakeup result = " + ret);
                    break;
                case R.id.bt_disconnect:
                    ret = mReader.SD_Disconnect();
                    Log.d(TAG, "disconnect result = " + ret);
                    if (ret == SDConsts.SDConnectState.DISCONNECTED || ret == SDConsts.SDConnectState.ALREADY_DISCONNECTED ||
                            ret == SDConsts.SDConnectState.ACCESS_TIMEOUT) {
                        updateConnectStateTextView(false);
                    }
                    break;
            }
        }
    };


    private static class ConnectionHandler extends Handler {
        private final WeakReference<ConnectionFragment> mExecutor;
        public ConnectionHandler(ConnectionFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectionFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        Log.d(TAG, "mConnectivityHandler");
        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                    closeDialog();
                    if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                        int ret = mReader.SD_Connect();
                        if (ret == SDConsts.SDResult.SUCCESS || ret == SDConsts.SDResult.ALREADY_CONNECTED) {
                            updateConnectStateTextView(true);
                        }
                        // READER RF PROPERTIES SETTING HERE
                        createProperties();
                        //readerProperties();
                    }
                    else
                        Toast.makeText(mContext, getString(R.string.wakeup_failed), Toast.LENGTH_SHORT).show();
                }
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    updateConnectStateTextView(false);
                }
                break;
        }
    }

    private void updateConnectStateTextView(boolean b) {
        //mReader = Reader.getReader(mContext, mConnectionHandler);
        //int sled = mReader.SD_GetBatteryStatus();

        if (b) {
            mConnectStateTextView.setText(R.string.connected_str);
            mConnectStateTextView.setTextColor(ContextCompat.getColor(mContext,R.color.green));
            int sledBattery = Reader.getReader(mContext, mConnectionHandler).SD_GetBatteryStatus();
            mBatteryStateTextView.setText("%" + sledBattery);
            if (sledBattery <= 20)
                mBatteryStateTextView.setTextColor(ContextCompat.getColor(mContext,R.color.red));
            else {
                mBatteryStateTextView.setTextColor(ContextCompat.getColor(mContext,R.color.green));
            }
            mBatteryStatePrefixTextView.setVisibility(View.VISIBLE);
            mDoubleDotTextView.setVisibility(View.VISIBLE);
            mConnectBt.setEnabled(false);
            mDisconectBt.setEnabled(true);
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECTED).sendToTarget();
        }
        else {
            mConnectStateTextView.setText(R.string.disconnected_str);
            mConnectStateTextView.setTextColor(ContextCompat.getColor(mContext,R.color.red));
            mBatteryStateTextView.setText("");
            mBatteryStatePrefixTextView.setVisibility(View.INVISIBLE);
            mDoubleDotTextView.setVisibility(View.INVISIBLE);
            mConnectBt.setEnabled(true);
            mDisconectBt.setEnabled(false);
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
        }
    }

    private void createDialog(int type, String message) {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);

        mDialog.setTitle(message);
        if (type == PROGRESS_DIALOG) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        mDialog.show();
    }

    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
    }

    private void createProperties(){
        ReaderProps readerProps = null;
        Log.d(TAG, "dataSource.tableIsNull(\"ReaderProps\")" + databaseController.tableIsNull("ReaderProps"));

        if (databaseController.tableIsNull("ReaderProps")){
            String rfidMask = "";
            for(SelectionCriterias.Criteria scCriteria : mReader.RF_GetSelection().getCriteria()){
                Log.d(TAG,"Mask is " + scCriteria.getSelectMask());
                rfidMask = scCriteria.getSelectMask();
            }
            if (rfidMask.equals("") || rfidMask == null)
                rfidMask = "3714";
            readerProps = new ReaderProps(0,mReader.RF_GetDutyCycle(),mReader.RF_GetRadioPowerState(),mReader.RF_GetRFMode(),rfidMask);
            databaseController.saveReaderPropsInTable(readerProps);
        }
        else{
            //dataSource.open();
            readerProps = databaseController.getReaderProps(0);
            Log.d(TAG, "readerProps toString" + readerProps.toString());
            Log.d(TAG, "readerProps getMask" + readerProps.getMask());
            mReader.RF_SetDutyCycle(readerProps.getDuty());
            mReader.RF_SetRadioPowerState(readerProps.getPower());
            mReader.RF_SetRFMode(readerProps.getMode());
            String tempMask = readerProps.getMask();
            SelectionCriterias criterias = new SelectionCriterias();
            criterias.makeCriteria(SelectionCriterias.SCMemType.EPC,tempMask,4,tempMask.length()*4,SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
            mReader.RF_SetSelection(criterias);
            //dataSource.close();
        }
    }

}
