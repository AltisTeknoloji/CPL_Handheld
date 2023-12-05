package com.altistek.cpl_handheld.fragment;

import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.sqlite.DatabaseController;
import com.altistek.cpl_handheld.sqlite.controllers.ReaderProps;

public class RFConfigFragment extends Fragment {
    private final String TAG = "-RFConfigFragment-";

    private Context mContext;

    private Spinner mRegionSpin;
    private ArrayAdapter<CharSequence> mRegionChar;

    private EditText mDutyEditText;
    private Button mSetDutyButton;

    private EditText mPowerEditText;
    private Button mSetPowerButton;

    private EditText mRFmodeEditText;
    private Button mSetRFmodeButton;

    private EditText mRFmaskEditText;
    private Button mSetRFmaskButton;

    private Reader mReader;

    private ProgressDialog mDialog;

    private Handler mOptionHandler;

    private SelectionCriterias mCurrentSelectionCriterias;

    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedEditor;
    private DatabaseController databaseController;
    private final RFConfigHandler mRFConfigHandler = new RFConfigHandler(this);

    public static RFConfigFragment newInstance() {
        return new RFConfigFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.rf_config_fragment, container, false);

        mContext = inflater.getContext();

        //dataSource.open();
        sharedPref = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
        /*sharedEditor.putString("rfidDuty", String.valueOf(100));
        sharedEditor.putString("rfidPower", String.valueOf(6));
        sharedEditor.putString("rfidMode", String.valueOf(3));
        sharedEditor.putString("rfidMask", "C001");
        sharedEditor.apply();*/

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;
        mCurrentSelectionCriterias = new SelectionCriterias();

        mDutyEditText = (EditText) v.findViewById(R.id.duty_edit);
        mSetDutyButton = (Button) v.findViewById(R.id.set_duty_button);
        mSetDutyButton.setOnClickListener(sledListener);

        mPowerEditText = (EditText) v.findViewById(R.id.power_edit);
        mSetPowerButton = (Button) v.findViewById(R.id.set_power_button);
        mSetPowerButton.setOnClickListener(sledListener);

        mRFmodeEditText = (EditText) v.findViewById(R.id.rfmode_edit);
        mSetRFmodeButton = (Button) v.findViewById(R.id.set_rfmode_button);
        mSetRFmodeButton.setOnClickListener(sledListener);

        mRFmaskEditText = (EditText) v.findViewById(R.id.rfmask_edit);
        mSetRFmaskButton = (Button) v.findViewById(R.id.set_rfmask_button);
        mSetRFmaskButton.setOnClickListener(sledListener);

        return v;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mRFConfigHandler);
        databaseController = new DatabaseController(mContext);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
//            databaseController.open();

            ReaderProps readerProps = databaseController.getReaderProps(0);
            if (readerProps != null){
                mDutyEditText.setText(String.valueOf(readerProps.getDuty()));
                mPowerEditText.setText(String.valueOf(readerProps.getPower()));
                mRFmodeEditText.setText(String.valueOf(readerProps.getMode()));
                mRFmaskEditText.setText(String.valueOf(readerProps.getMask()));
            }

            else{
                mDutyEditText.setText(String.valueOf(mReader.RF_GetDutyCycle()));
                mPowerEditText.setText(String.valueOf(mReader.RF_GetRadioPowerState()));
                mRFmodeEditText.setText(String.valueOf(mReader.RF_GetRFMode()));
                String rfidMask = "";
                for(SelectionCriterias.Criteria scCriteria : mReader.RF_GetSelection().getCriteria()){
                    Log.d(TAG,"Mask is " + scCriteria.getSelectMask());
                    rfidMask = scCriteria.getSelectMask();
                }
                mRFmaskEditText.setText(rfidMask);
            }

            mDutyEditText.setEnabled(true);
            mPowerEditText.setEnabled(true);
            mRFmodeEditText.setEnabled(true);
            mRFmaskEditText.setEnabled(true);
            mSetDutyButton.setEnabled(true);
            mSetPowerButton.setEnabled(true);
            mSetRFmodeButton.setEnabled(true);
            mSetRFmaskButton.setEnabled(true);
//            databaseController.close();
        }
        else {
            mDutyEditText.setText(R.string.disconnected_str);
            mDutyEditText.setEnabled(false);
            mPowerEditText.setText(R.string.disconnected_str);
            mPowerEditText.setEnabled(false);
            mRFmodeEditText.setText(R.string.disconnected_str);
            mRFmodeEditText.setEnabled(false);
            mRFmaskEditText.setText(R.string.disconnected_str);
            mRFmaskEditText.setEnabled(false);

            mSetDutyButton.setEnabled(false);
            mSetPowerButton.setEnabled(false);
            mSetRFmodeButton.setEnabled(false);
            mSetRFmaskButton.setEnabled(false);
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
        Log.d(TAG, " onPause");
//        databaseController.close();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, " onStop");
        closeDialog();
//        databaseController.close();
        super.onStop();
    }

    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d(TAG, "sledListener");
            String value;
            int id = v.getId();
//            databaseController.open();

            ReaderProps readerProps = databaseController.getReaderProps(0);
            if (readerProps == null){
                Toast.makeText(mContext, "Kayıt edilmiş ayar bulunamadı, varsayılan ayarların üzerine kayıt yapabilmek için uygulamayı yeniden başlatınız",Toast.LENGTH_SHORT).show();
                return;
            }
            switch (id) {
                case R.id.set_duty_button:
                    value = mDutyEditText.getText().toString();
                    if (value != null) {
                        if (!value.equals("")) {
                            try {
                                int var = Integer.parseInt(value);
                                if (var < SDConsts.RFDutyCycle.MIN_DUTY || var > SDConsts.RFDutyCycle.MAX_DUTY) {
                                    Toast.makeText(mContext, getString(R.string.set_duty_cycle_range) + SDConsts.RFDutyCycle.MIN_DUTY + " ~ " +
                                            SDConsts.RFDutyCycle.MAX_DUTY, Toast.LENGTH_SHORT).show();
                                    break;
                                }
//                                sharedEditor.putString("rfidDuty", String.valueOf(var));
                                //                              sharedEditor.apply();

                                mReader.RF_SetDutyCycle(var);
                                readerProps.setDuty(var);
                                databaseController.saveReaderPropsInTable(readerProps);
                                Toast.makeText(mContext, getString(R.string.set_duty_cycle), Toast.LENGTH_SHORT).show();
                                return;
                            } catch (java.lang.NumberFormatException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    }
                    Toast.makeText(mContext, getString(R.string.failed_set_duty_cycle), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.set_power_button:
                    value = mPowerEditText.getText().toString();
                    if (value != null) {
                        if (!value.equals("")) {
                            try {
                                int var = Integer.parseInt(value);
                                if (var < SDConsts.RFPower.MIN_POWER || var > SDConsts.RFPower.MAX_POWER) {

                                    Toast.makeText(mContext, getString(R.string.set_power_range)+ SDConsts.RFPower.MIN_POWER + " ~ " +
                                            SDConsts.RFPower.MAX_POWER, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                //sharedEditor.putString("rfidPower", String.valueOf(var));
                                //sharedEditor.apply();
                                Log.d(TAG,"RFPOWER :" + var);
                                mReader.RF_SetRadioPowerState(var);
                                readerProps.setPower(var);
                                databaseController.saveReaderPropsInTable(readerProps);
                                Toast.makeText(mContext, getString(R.string.set_power), Toast.LENGTH_SHORT).show();
                                return;
                            } catch (java.lang.NumberFormatException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    }
                    Toast.makeText(mContext, getString(R.string.failed_set_power), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.set_rfmode_button:
                    value = mRFmodeEditText.getText().toString();
                    if (value != null) {
                        if (!value.equals("")) {
                            try {
                                int var = Integer.parseInt(value);
                                if (var < SDConsts.RFMode.DSB_ASK_1 || var > SDConsts.RFMode.DSB_ASK_2) {

                                    Toast.makeText(mContext, getString(R.string.set_rfmode_range)+ SDConsts.RFMode.DSB_ASK_1 + " ~ "
                                            + SDConsts.RFMode.DSB_ASK_2, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                //sharedEditor.putString("rfidMode", String.valueOf(var));
                                //sharedEditor.apply();
                                mReader.RF_SetRFMode(var);
                                readerProps.setMode(var);
                                databaseController.saveReaderPropsInTable(readerProps);
                                Toast.makeText(mContext, getString(R.string.set_rfmode), Toast.LENGTH_SHORT).show();
                                return;
                            } catch (java.lang.NumberFormatException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    }
                    Toast.makeText(mContext, getString(R.string.failed_set_rfmode), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.set_rfmask_button:
                    value = mRFmaskEditText.getText().toString();
                    Log.d(TAG, "Value mask : " + value);
                    Log.d(TAG, "Value mask length: " + value.length());
                    if (value != null) {
                        if (!value.equals("")) {
                            //mReader.RF_RemoveSelection();
                            //sharedEditor.putString("rfidPower", value);
                            //sharedEditor.apply();
                            Log.d(TAG,"mask value : " + value);
                            mCurrentSelectionCriterias.makeCriteria(
                                    SelectionCriterias.SCMemType.EPC,
                                    value,
                                    4,
                                    value.length() * 4,
                                    SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
                            mReader.RF_SetSelection(mCurrentSelectionCriterias);
                            readerProps.setMask(value);
                            databaseController.saveReaderPropsInTable(readerProps);
                            mCurrentSelectionCriterias.getCriteria().clear();
                            Toast.makeText(mContext, getString(R.string.set_rfmask), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "CRITERIAS : + : " + mReader.RF_GetSelection());
                        }
                    } else {
                        Toast.makeText(mContext, getString(R.string.value_cannot_be_null), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
//            databaseController.close();
        }
    };

    private static class RFConfigHandler extends Handler {
        private final WeakReference<RFConfigFragment> mExecutor;

        public RFConfigHandler(RFConfigFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("TAG", "\n\n\n\nhandleMessage " + msg);
            RFConfigFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        Log.d(TAG, "mRFConfigHandler");
        Log.d(TAG, "m arg1 = " + m.arg1 + " arg2 = " + m.arg2);
        int result = m.arg2;
        switch (m.what) {
            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    case SDConsts.RFCmdMsg.REGION_CHANGE_START:
                        if (result == SDConsts.RFResult.SUCCESS) {
                            createDialog("Region is changing...");
                        }
                        break;
                    case SDConsts.RFCmdMsg.REGION_CHANGE_END:
                        closeDialog();
                        int v = mReader.RF_GetRegion();
                        if (v == SDConsts.RFRegion.NOT_SETTED)
                            mRegionSpin.setSelection(mRegionChar.getCount() - 1);
                        else if (v < SDConsts.RFRegion.KOREA || v > SDConsts.RFRegion.NICARAGUA)//add new ISO code
                            mRegionSpin.setSelection(0);
                        else
                            mRegionSpin.setSelection(v);
                        break;
                }
                break;
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                }
                break;
        }
    }

    private void createDialog(String message) {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setTitle(message);
        mDialog.show();
    }

    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
    }
}
