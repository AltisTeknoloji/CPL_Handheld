package com.altistek.cpl_handheld.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.control.TagListAdapter;
import com.altistek.cpl_handheld.grai.Grai;
import com.altistek.cpl_handheld.helpers.Logger;
import com.altistek.cpl_handheld.service.ApiServiceOkHttp;
import com.altistek.cpl_handheld.sqlite.controllers.Pallet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import okhttp3.Response;

// IN COMPANY MOVEMENT
public class TransferFragment extends Fragment {

    private final String TAG = "-TransferFragment-";
    private enum Mode {
        RFID,
        BARCODE
    }
    private Mode workMode;
    //private StopwatchService mStopwatchSvc;
    private TagListAdapter mAdapter;
    private ListView mRfidList;
    private Button mAddPalletButton;
    private Button mCompleteButton;
    private Button mClearButton;
    private Spinner mWarehouseSpinner;
    private Reader mReader;
    private Context mContext;
    private boolean mInventory = false;
    private Handler mOptionHandler;
    private ProgressBar mTagLocateProgress;

    private int choosenWarehouseId = 0;
    private String choosenWarehouseName = "";
    private int choosenCompanyId = 0;
    private AlertDialog dialogForWorkMode;
    ArrayAdapter<String> adapterDoor;
    private Logger logger;
    private  Spinner spinner;
    //private UpdateStopwatchHandler mUpdateStopwatchHandler = new UpdateStopwatchHandler(this);
    private EPCHandler mEPCHandler = new EPCHandler(this);
    private BarcodeHandler mBarcodeHandler = new BarcodeHandler(this);
    private String selectedSN="";
    public static TransferFragment newInstance() {
        return new TransferFragment();
    }
    private String sortedData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.transfer_fragment, container, false);

        mContext = inflater.getContext();
        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;
        logger = new Logger();
        // Choosen Item Click event
        mRfidList = (ListView) v.findViewById(R.id.rfid_list);
        //mRfidList.setOnItemClickListener(listItemClickListener);

        mTagLocateProgress = (ProgressBar) v.findViewById(R.id.tag_locate_progress);


//        mCompleteButton = (Button) v.findViewById(R.id.complete_button);
        mAddPalletButton=(Button) v.findViewById(R.id.add_pallet_warehouse_button);
        mCompleteButton=(Button) v.findViewById(R.id.remove_pallet_button);
        mAddPalletButton.setOnClickListener(sledListener);
        mCompleteButton.setOnClickListener(sledListener);
        //  mCompleteButton.setText(choosenWarehouseName + " " + getString(R.string.complete_job) + " (0 palet)");
        //updateCompleteButtonText();

        mClearButton = (Button) v.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(sledListener);

        mAdapter = new TagListAdapter(mContext);
        mRfidList.setAdapter(mAdapter);



        //region CHOOSE FOR MODE

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater alertInflater = LayoutInflater.from(mContext);

        View alertView = alertInflater.inflate(R.layout.alert_transfer, null);
        Spinner spinner = alertView.findViewById(R.id.spinner_door_selection);
        fillSpinner(spinner);
        Switch switchMode = alertView.findViewById(R.id.switch_mode);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

               selectedSN = (String) parentView.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Herhangi bir şey seçilmediğinde yapılacaklar
            }
        });

        if (dialogForWorkMode != null) {
            dialogForWorkMode.show();
        }
        switchMode.setChecked(false);
        workMode = Mode.RFID;
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked false RFID
                // isChecked true Barcode
                Log.d(TAG,"Choosed item is " + isChecked);
                if(isChecked)
                    workMode = Mode.BARCODE;
                else
                    workMode = Mode.RFID;
            }
        });

//        String[] strArray = new String[2];
//        strArray[0] = "asdaddasdsaasadasds";
//        strArray[1] = "asddasaadsddsdasddd";
//        ArrayAdapter adapterDoor = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, strArray);
//        adapterDoor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapterDoor.notifyDataSetChanged();
//
//        spinner.setAdapter(adapterDoor);

        alertBuilder
                .setCancelable(false)
                .setView(alertView)
                .setNegativeButton(R.string.cancel_str, ((dialog, which) -> {
                    changeFragmentAndBar(UserAndDeviceFragment.newInstance(), R.id.navigation_user);
                    dialog.dismiss();
                }))
                .setPositiveButton(R.string.start_str, (dialog, which) -> {
                    Log.d(TAG, "Secilen is emri baslatılıyor");
                    logger.addRecordToLog(Logger.LogType.INFO, TAG, "Transfer operation starting.(IN_COMPANY)");
                    dialogForWorkMode.dismiss();
                    //if (workMode == Mode.RFID)
                    //    bindStopwatchSvc();
                    startReader();
                });
        dialogForWorkMode = alertBuilder.create();

        //dialogForWorkMode.show();
        //endregion
        return v;
    }
//    private void showSpinnerDialog() {
//        if (dialogForWorkMode != null) {
//            dialogForWorkMode.show();
//        }
//    }

    @Override
    public void onStart() {
        mReader = Reader.getReader(mContext, mEPCHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            dialogForWorkMode.show();
        }
        else
        {
            dialogForConnection();
        }
        super.onStart();
    }

    private void startReader(){
        if (workMode == Mode.BARCODE)
            mReader = Reader.getReader(mContext, mBarcodeHandler);
        else
            mReader = Reader.getReader(mContext, mEPCHandler);
        //dataSource = new DataSource(mContext);
        //dataSource.open();


        int mode;
        if(workMode == Mode.RFID )
            mode = SDConsts.SDTriggerMode.RFID;
        else
            mode = SDConsts.SDTriggerMode.BARCODE;
        Log.d(TAG, "Reader trigger mode " + mReader.SD_GetTriggerMode());
        if (mReader.SD_GetTriggerMode() != mode) {
            Log.d(TAG, "RFID MODE");
            mReader.SD_SetTriggerMode(mode);
        }
        Log.d(TAG, "Reader trigger mode " + mReader.SD_GetTriggerMode());

        mInventory = false;
        //updateCompleteButtonText();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        // DO NOTHING
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        killAll();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        killAll();
        super.onStop();
    }

    private void clearAll() {
        if (!mInventory) {
            mAdapter.removeAllItem();
            //stopStopwatch();
        }
    }

    public void killAll() {
//        databaseController.close();
        logger.addRecordToLog(Logger.LogType.INFO, TAG, "Transfer operation stopping...(IN_COMPANY)");
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            if (workMode == Mode.RFID){
                mReader.RF_StopInventory();
                //pauseStopwatch();
                mInventory = false;
                //stopStopwatch();
                //unbindStopwatchSvc();
            }
            mReader = Reader.getReader(mContext, null);
        }
    }

    private View.OnClickListener sledListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "sledListener");
            Log.d(TAG, "stopwatchListener");
            int id = v.getId();
            switch (id) {
                case R.id.add_pallet_warehouse_button:
                    try
                    {
                        savePallets("+");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case R.id.remove_pallet_button:
                    try
                    {
                        savePallets("-");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    break;

                case R.id.clear_button:
                    clearAll();
                    //updateCompleteButtonText();
                    updateUIWithPalletCount();
                    break;
            }

        }
    };

//    private void saveDoor() {
//        new ApiTask().execute();
//    }
    private class ApiTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> resultList = new ArrayList<>();
            try {

                ApiServiceOkHttp apiServiceOkHttp = new ApiServiceOkHttp();
                String result = apiServiceOkHttp.GET("/api/Job/GatewayNames");


                if (result != null) {

                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        resultList.add(jsonArray.getString(i));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<String> resultList) {
            try
            {
                if (resultList != null) {
                    ArrayAdapter<String> adapterDoor = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, resultList);
                    adapterDoor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapterDoor);
                } else {
                    // Hata durumunu ele alabilirsiniz.
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    private void fillSpinner(Spinner spinner) {
        this.spinner=spinner;
        ApiTask task = new ApiTask();
        task.execute();
    }

    private ApiServiceOkHttp apiServiceOkHttp;
    private void savePallets(String direction)throws Exception{
        try
        {

            JSONArray jsonArray = new JSONArray();
        /*for (int i = 0; i < mRfidList.getCount(); i++) {
            Log.d(TAG, "Pallet list madapter barcode: " + mAdapter.getBarcode(i));
            Pallet pallet = new Pallet(mAdapter.getBarcode(i), mAdapter.getEPC(i), -1, "0", "0", "0", "0", "0", "0","0");
            logger.addRecordToLog(Logger.LogType.INFO, TAG, "Transfer operation pallet: " + pallet.toString());*/


//            mp.put("barcode","");

//            mp.put("JobId",JobId);
//            mp.put("JobType",JobType);
            for (int j = 0; j < mAdapter.getCount(); j++)
            {
                JSONObject obj = new JSONObject();
                obj.put("Mode",workMode.toString());
                obj.put("barcode", mAdapter.getBarcode(j));
                obj.put("direction", direction);
                obj.put("SN",selectedSN);
                obj.put("EPC", mAdapter.getEPC(j));
                obj.put("PN", -1);
                obj.put("URI", "0");
                obj.put("RSSI1", "0");
                obj.put("RSSI2", "0");
                obj.put("RSSI3", "0");
                obj.put("RSSI4", "0");
                obj.put("WC", String.valueOf(mAdapter.getCount()));
                jsonArray.put(obj);
            }
        //}
            apiServiceOkHttp = new ApiServiceOkHttp();
            Response result = apiServiceOkHttp.POSTArray("/Handheld/Handheld", jsonArray);
            if (result != null) {
                int responceCode = result.code();
                if (responceCode==400) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Hata")
                            .setMessage("Bu kapıda iş emri başlatılmamıştır.")
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                else {
                    // Uyarı mesajını oluştur
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Uyarı");
                    builder.setMessage(mContext.getString(R.string.pallets_saved));

                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Uyarıyı kapat
                            dialog.dismiss();

                            updateUIWithPalletCount();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
            else
            {
                new AlertDialog.Builder(mContext)
                        .setTitle("Hata")
                        .setMessage("API'den geçersiz yanıt alındı")
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        logger.addRecordToLog(Logger.LogType.INFO, TAG, "Transfer operation completed.(IN_COMPANY)");
        //clearAll();
        //updateCompleteButtonText();
        //dialogForWorkMode.show();
    }catch (Exception ex) {
            // Hata durumunda burada işleyin

            Toast.makeText(mContext, "Hata: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //region EPC HANDLER
    private static class EPCHandler extends Handler {
        private final WeakReference<TransferFragment> mExecutor;

        public EPCHandler(TransferFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            TransferFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleEPC(msg);
            }
        }
    }

    public void handleEPC(Message m) {
        Log.d(TAG, "handle Message : " + m.what);
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                switch (m.arg1) {
                    case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                        if (!mInventory) {
                            //clearAll();
                            int ret = mReader.RF_PerformInventory(true, true, true);

                            if (ret == SDConsts.RFResult.SUCCESS) {
                                //startStopwatch();
                                mInventory = true;
                            } else if (ret == SDConsts.RFResult.MODE_ERROR) {
                                Toast.makeText(mContext, getString(R.string.start_inven_failed_check_rfr), Toast.LENGTH_SHORT).show();
                            } else if (ret == SDConsts.RFResult.LOW_BATTERY) {
                                Toast.makeText(mContext, getString(R.string.start_inven_failed_check_battery), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "Start Inventory failed");
                            }
                        }
                        break;
                    case SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED:
                        mInventory = false;
                        //pauseStopwatch();
                        // In case of low battery on inventory, reason value is LOW_BATTERY
                        Toast.makeText(mContext, getString(R.string.stop_inven_reason) + m.arg2, Toast.LENGTH_SHORT).show();
                        break;

                    case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                        if (mReader.RF_StopInventory() == SDConsts.SDResult.SUCCESS) {
                            mInventory = false;
                        }
                        //pauseStopwatch();
                        break;

                    case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                        //This message contain DETACHED event.
                        Log.d(TAG, "sled disconnected");
                        dialogForConnection();
                        if (mInventory) {
                            //pauseStopwatch();
                            mInventory = false;
                        }
                        if (mOptionHandler != null)
                            mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                        break;

                    case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                        //Toast.makeText(mContext, "Battery state = " + m.arg2, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Battery state = " + m.arg2);
                        break;
                }
                break;
            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    //+RF_PerformInventoryCustom
                    case SDConsts.RFCmdMsg.INVENTORY_CUSTOM_READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            String data = (String) m.obj;
                            if (data != null)
                                processReadDataCustom(data);
                        }
                        break;
                    //RF_PerformInventoryCustom+
                    //---NTNS
                    case SDConsts.RFCmdMsg.INVENTORY:
                    case SDConsts.RFCmdMsg.READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof String) {
                                String data = (String) m.obj;
                                if (data != null) {
                                    processReadData(data);
                                }
                            }
                        }
                        break;
                    case SDConsts.RFCmdMsg.LOCATE:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof Integer) {
                                //startLocateTimer();
                                //mLocateValue = (int) m.obj;
                            }
                        }
                        break;
                }
                break;
        }
    }
    //endregion

    //region EPCHandler Requirements
    private void processReadData(String data) {
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String orginalData = data;
        if (data.contains(";")) {
            Log.d(TAG, "Full Tag : " + data);
            // 3000 is pc word
            //full tag example(with optional value)
            //1) RF_PerformInventory => "3000123456783333444455556666;rssi:-54.8"
            //2) RF_PerformInventoryWithLocating => "3000123456783333444455556666;loc:64"
            int infoTagPoint = data.indexOf(';');
            info = data.substring(infoTagPoint);
            int infoPoint = info.indexOf(':') + 1;
            info = info.substring(infoPoint);
            Log.d(TAG, "info tag = " + info);
            data = data.substring(0, infoTagPoint);
            Log.d(TAG, "data tag = " + data);
        }

        Log.d(TAG, "DATA :  " + data);
        sortedData = sortData(data);

        Grai grai = new Grai(data, Grai.Tag.EPC);
        mAdapter.addItem(grai.getBarcode(), data, true);
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        //updateCompleteButtonText();
        updateUIWithPalletCount();
    }

    private void processReadDataCustom(String data) {
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String rssi = "";
        String customData = "";
        if (data.contains(";")) {
            Log.d(TAG, "full tag = " + data);
            //full tag example = "3000123456783333444455556666;rssi:-54.8^custom=2348920384"
            int customdDataPoint = data.indexOf('^');
            customData = data.substring(customdDataPoint, data.length());
            int customPoint = customData.indexOf('=') + 1;
            customData = customData.substring(customPoint, customData.length());
            Log.d(TAG, "custom data = " + customData);
            data = data.substring(0, customdDataPoint);

            int rssiTagPoint = data.indexOf(';');
            rssi = data.substring(rssiTagPoint, data.length());
            int rssiPoint = rssi.indexOf(':') + 1;
            rssi = rssi.substring(rssiPoint, rssi.length());
            Log.d(TAG, "rssi tag = " + rssi);
            data = data.substring(0, rssiTagPoint);

            Log.d(TAG, "data tag = " + data);
            data = data + "\n" + customData;
        }
        if (rssi != "") {
            Activity activity = getActivity();
            if (activity != null)
                rssi = activity.getString(R.string.rssi_str) + rssi;
        }
        sortedData = sortData(data);
        Grai grai = new Grai(data, Grai.Tag.EPC);
        mAdapter.addItem(grai.getBarcode(), data, true);
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        updateUIWithPalletCount();

    }
    private String sortData(String data) {
        String numericPart = data.substring(data.indexOf('B') + 1);

        String[] numericStrings = numericPart.split("\\D+");

        List<Integer> numericList = new ArrayList<>();
        for (String numericString : numericStrings) {
            if (!numericString.isEmpty()) {
                numericList.add(Integer.parseInt(numericString));
            }
        }
        Collections.sort(numericList);

        StringBuilder sortedData = new StringBuilder();
        sortedData.append(data, 0, data.indexOf('B') + 1);
        for (int numericValue : numericList) {
            sortedData.append(numericValue);
        }

        return sortedData.toString();
    }

    //endregion
    private void updateUIWithPalletCount() {
        int palletCount = mAdapter.getCount();
        TextView palletCountView = getView().findViewById(R.id.pallet_count_view);

        if (palletCount == 0) {
            palletCountView.setText("0 Palet Okundu");
        } else {
            palletCountView.setText(String.valueOf(palletCount) + " Palet Okundu.");
        }
    }

    //region BARCODE HANDLER
    private static class BarcodeHandler extends Handler {
        private final WeakReference<TransferFragment> mExecutor;

        public BarcodeHandler(TransferFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            TransferFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleBarcode(msg);
            }
        }
    }

    public void handleBarcode(Message m) {
        Log.d(TAG, "handle Message : " + m.what);
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED)
                    Log.d(TAG, "Trigger_pressed");
                else if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED)
                    Log.d(TAG, "Trigger_released");
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_MODE_CHANGED)
                    Log.d(TAG, "SLED_mode_changed : " + m.arg2);
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                }
                break;
            case SDConsts.Msg.BCMsg:
                //StringBuilder readData = new StringBuilder();
                if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                    Log.d(TAG, "Barcode_trigger_pressed");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                    Log.d(TAG, "Barcode_trigger_released");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                    Log.d(TAG, "BARCODE_READ");
                    if (m.arg2 == SDConsts.BCResult.SUCCESS) {
                        //readData.append(" " + "BARCODE READ SUCCESS");
                        Log.d(TAG, "BARCODE_READ_SUCCESS");
                    } else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT) {
                        //readData.append(" " + "BARCODE READ ACCESS TIMEOUT");
                        Log.d(TAG, "BARCODE_READ_ACCESS_TIMEOUT");
                    }
                    if (m.obj != null && m.obj instanceof String) {
                        //readData.append("\n" + m.obj.toString());
                        String fullData = m.obj.toString();

                        // Extract the barcode value from the full data string
                        String barcodeValue = fullData.split(";")[0];


                        // Check if the barcode has 26 characters
                        if (barcodeValue.length() != 26) {
                            Log.d(TAG, "Skipping barcode with invalid length: " + barcodeValue);

                            // Create an AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("Geçersiz barkod uzunluğu: " + barcodeValue.length())
                                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            dialog.dismiss();
                                        }
                                    });


                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            return;
                        }
                        Log.d(TAG, "Barcode is " + m.obj.toString());
                        String tmp[] = m.obj.toString().split(";");
                        Grai grai = new Grai(tmp[0], Grai.Tag.BARCODE);
                        boolean res = mAdapter.addItem(tmp[0],"barcode_mode",true);

                        if (!res)
                            Toast.makeText(mContext, getString(R.string.barcode_already_listed), Toast.LENGTH_SHORT).show();
                            //MyToast.showToast(mContext,getString(R.string.barcode_already_listed),Toast.LENGTH_SHORT);
                        else {
                            //updateCompleteButtonText();
                            updateUIWithPalletCount();
                        }
                    }
                } else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_ERROR) {
                    Log.e(TAG, "BARCODE ERROR");
                    if (m.arg2 == SDConsts.BCResult.LOW_BATTERY) {
                        //readData.append(" " + "BARCODE ERROR LOW BATTERY");
                        Log.e(TAG, "BARCODE ERROR LOW BATTERY");
                    } else {
                        Log.e(TAG, "BARCODE ERROR SOMETHING ELSE");
                    }
                }
                break;

        }
    }
    //endregion


    private void dialogForConnection() {
        //Toast.makeText(mContext,"Check the module connection",Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.connect_str));
        builder.setMessage(getString(R.string.check_module_connection));
        builder.setPositiveButton(getString(R.string.okey_str), (dialog, which) -> {
            changeFragmentAndBar(ConnectionFragment.newInstance(), R.id.navigation_connect);
        });
        builder.show();
    }





    private void changeFragmentAndBar(Fragment fragment, int barId) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
        BottomNavigationView bottom = getActivity().findViewById(R.id.bottom_navigation);
        bottom.setSelectedItemId(barId);
    }

//    private void updateCompleteButtonText() {
//        String t = choosenWarehouseName + " " + getString(R.string.complete_job);
//        String t2;
//        if (mAdapter != null)
//            t2 = t + " (" + mAdapter.getCount() + " palet)";
//        else {
//            t2 = t + " (0 palet)";
//        }
//        mCompleteButton.setText(t2);
//    }

}

