package com.altistek.cpl_handheld.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.control.TagListAdapter;
import com.altistek.cpl_handheld.grai.Grai;
import com.altistek.cpl_handheld.helpers.Logger;
import com.altistek.cpl_handheld.service.ApiServiceOkHttp;
import com.altistek.cpl_handheld.service.AuthService;
import com.altistek.cpl_handheld.sqlite.DatabaseController;
import com.altistek.cpl_handheld.sqlite.controllers.JobOrder;
import com.altistek.cpl_handheld.sqlite.controllers.Pallet;
import com.altistek.cpl_handheld.sqlite.controllers.ShipmentType;
import com.altistek.cpl_handheld.sqlite.controllers.Warehouse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

// OUT COMPANY MOVEMENT
public class ManuelPalletFragment extends Fragment {

    private final String TAG = "-ManuelPalletFragment-";

    public enum Mode {
        RFID,
        BARCODE
    }

    public static Mode workMode;
    private String mClearButtonText;
    private   Reader mReader;
    private Context mContext;
    private Handler mOptionHandler;
    private DatabaseController databaseController;
    private AlertDialog dialogForJobOrder;
    private boolean mInventory = false;

    private ListView mListView;
    private Button mClearButton;
    private Button mCompleteButton;
    private Button mDeleteButton;
    private TextView mTvJobDetails;
    private Spinner spinner;

    private AuthService authService;
    private ApiServiceOkHttp apiService;

    private int choosenShipmentType = 0;
    private int choosenWarehouseId = 0;
    private int choosenGateId = 0;
    private String choosenGateName = "";
    private int choosenJobOrder = 0;
    private SpinAdapterShipTypes adapterShipTypes;
    private SpinAdapterWarehouses adapterWarehouses;
    private SpinAdapterJobOrders adapterJobOrders;
    private TagListAdapter mAdapter;
    private static Logger logger;

    private final EPCHandler mEPCHandler = new EPCHandler(this);
    private final BarcodeHandler mBarcodeHandler = new BarcodeHandler(this);

    public static ManuelPalletFragment newInstance() {
        return new ManuelPalletFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.manuel_pallet_fragment, container, false);





        mContext = inflater.getContext();
        logger = new Logger();
        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;
        authService = new AuthService(mContext);
        apiService = new ApiServiceOkHttp();
        databaseController = new DatabaseController(mContext);

        //region FRAGMENT UI Components and events
        // UI Components
        mTvJobDetails = v.findViewById(R.id.txt_job_details);
        mCompleteButton = v.findViewById(R.id.complete_button);
        mDeleteButton = v.findViewById(R.id.delete_button);
        mClearButton = v.findViewById(R.id.clear_button);
        mListView = v.findViewById(R.id.barcode_list);
        mAdapter = new TagListAdapter(mContext);
        mListView.setAdapter(mAdapter);

        mTvJobDetails.setText("");
        mClearButtonText = mClearButton.getText().toString();
        //updateCompleteButtonText();
        mCompleteButton.setOnClickListener(v1 -> {
            Log.d(TAG, "mCompleteButton");
            if (mAdapter.getCount() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("");
                builder.setMessage(R.string.r_u_sure);
                builder.setNegativeButton(R.string.no_str, null);
                builder.setPositiveButton(R.string.yes_str, (dialog, which) -> {
                    Log.d(TAG, "Complete Job");

                    savePallets(choosenShipmentType);
                });
                builder.show();

            } else {
                Toast.makeText(mContext, getString(R.string.no_pallet_found), Toast.LENGTH_SHORT).show();
            }

        });

        mDeleteButton.setOnClickListener(v13 -> {
            Log.d(TAG, "mDeleteButton");
            if (mAdapter.getCount() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("");
                builder.setMessage(R.string.r_u_sure);
                builder.setNegativeButton(R.string.no_str, null);
                builder.setPositiveButton(R.string.yes_str, (dialog, which) -> {
                    Log.d(TAG, "Complete Job");
                    //Log.d(TAG, "SEVKIYAT");
                    //        choosenShipmentType = 1;
                    //Log.d(TAG, "MAL KABUL");
                    //      choosenShipmentType = 2;
                    if (choosenShipmentType == 1)
                        savePallets(2);
                    else if (choosenShipmentType == 2)
                        savePallets(1);
                    else {
                        Log.d(TAG, "Wrong shipmentType");
                    }

                });
                builder.show();

            } else {
                Toast.makeText(mContext, getString(R.string.no_pallet_found), Toast.LENGTH_SHORT).show();
            }
        });

//        AlertDialog.Builder alertBuilder1 = new AlertDialog.Builder(mContext);
//        LayoutInflater alertInflater1 = LayoutInflater.from(mContext);
//        View alertView1 = alertInflater1.inflate(R.layout.alert_transfer, null);
//        Spinner spinnerDoor = alertView1.findViewById(R.id.spinner_door_selection);
//
//        new FetchGatewayNamesTask(spinnerDoor).execute();
////       spinner = new Spinner(mContext, android.R.layout.simple_spinner_dropdown_item);
////       spinnerDoor.setAdapter(spinner);
//        spinnerDoor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                // Bu kısımda Spinner'a tıklandığında yapılmasını istediğiniz işlemi tanımlayabilirsiniz.
//                // Örneğin, seçilen öğeyi alabilir ve buna göre bir işlem yapabilirsiniz.
//                String selectedValue = spinnerDoor.getSelectedItem().toString();
//                Log.d(TAG, "Seçilen değer: " + selectedValue);
//                // Seçilen değere göre işlemler yapabilirsiniz.
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//                // Bir şey seçilmediğinde yapılmasını istediğiniz işlemi buraya ekleyebilirsiniz.
//            }
//        });
//        AlertDialog alert = alertBuilder1.create();
//        alert.show();

        mClearButton.setOnClickListener(v12 -> {
            Log.d(TAG, "mClearButton");
            mAdapter.removeAllItem();
            updateCompleteButtonText();
        });

        //region CHOOSE FOR JOB ORDER AND MODE

        if ((choosenShipmentType == 0 || choosenWarehouseId == 0 || choosenJobOrder == 0)) {
            new ShipmentTypeTask().execute();

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
            LayoutInflater alertInflater = LayoutInflater.from(mContext);

            View alertView = alertInflater.inflate(R.layout.alert_job_order, null);

            Spinner spinnerShipTypes = alertView.findViewById(R.id.spinner_shipment_types);
            Spinner spinnerWarehouses = alertView.findViewById(R.id.spinner_warehouse_list);
            Spinner spinnerJobOrders = alertView.findViewById(R.id.spinner_job_order_list);
            Switch switchMode = alertView.findViewById(R.id.switch_mode);


            //region SHIPMENT TYPE DROPDOWN
            adapterShipTypes = new SpinAdapterShipTypes(mContext, android.R.layout.simple_list_item_1);
            spinnerShipTypes.setAdapter(adapterShipTypes);
            spinnerShipTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected : parent : " + parent);
                    Log.d(TAG, "onItemSelected : view : " + view);
                    Log.d(TAG, "onItemSelected : position : " + position);
                    Log.d(TAG, "onItemSelected : id : " + id);
                    Log.d(TAG, "onItemSelected : view.id : " + view.getId());
                    // ID 1 SEVKIYAT, API ICIN 1
                    // ID 2 MAL KABUL, API ICIN 2

                    adapterWarehouses.clear();
                    adapterJobOrders.clear();

                    switch ((int) id) {
                        case 1:
                            //Log.d(TAG, "SEVKIYAT");
                            choosenShipmentType = 1;
                            break;
                        case 2:
                            //Log.d(TAG, "MAL KABUL");
                            choosenShipmentType = 2;
                            break;
                    }
                    new WarehouseTask().execute();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            //endregion

            adapterWarehouses = new SpinAdapterWarehouses(mContext, android.R.layout.simple_list_item_1);
            spinnerWarehouses.setAdapter(adapterWarehouses);
            spinnerWarehouses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected : parent : " + parent);
                    Log.d(TAG, "onItemSelected : view : " + view);
                    Log.d(TAG, "onItemSelected : position : " + position);
                    Log.d(TAG, "onItemSelected : id : " + id);
                    Log.d(TAG, "onItemSelected : view.id : " + view.getId());
                    choosenWarehouseId = (int) id;
                    adapterJobOrders.clear();
                    new JobOrderTask().execute();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //DO NOTHING
                }
            });

            adapterJobOrders = new SpinAdapterJobOrders(mContext, android.R.layout.simple_list_item_1);
            spinnerJobOrders.setAdapter(adapterJobOrders);
            spinnerJobOrders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected : parent : " + parent);
                    Log.d(TAG, "onItemSelected : view : " + view);
                    Log.d(TAG, "onItemSelected : position : " + position);
                    Log.d(TAG, "onItemSelected : id : " + id);
                    Log.d(TAG, "onItemSelected : view.id : " + view.getId());
                    choosenJobOrder = (int) id;
                    choosenGateId = adapterJobOrders.getItem(position).getGateId();
                    mTvJobDetails.setText(
                            getString(R.string.bill_of_lading)
                                    + adapterJobOrders.getItem(position).getBillOfLadingNo()
                                    + " - "
                                    + getString(R.string.company)
                                    + adapterJobOrders.getItem(position).getCompanyName());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    choosenJobOrder = 0;
                    mTvJobDetails.setText("");
                    //DO NOTHING
                }
            });


            switchMode.setChecked(false);
            workMode = Mode.RFID;
            switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // isChecked false RFID
                // isChecked true Barcode
                Log.d(TAG, "Choosed item is " + isChecked);
                if (isChecked)
                    workMode = Mode.BARCODE;
                else
                    workMode = Mode.RFID;


            });

            alertBuilder
                    .setCancelable(false)
                    .setView(alertView)
                    .setNegativeButton(R.string.cancel_str, ((dialog, which) -> {
                        changeFragmentAndBar(UserAndDeviceFragment.newInstance(), R.id.navigation_user);
                        dialog.dismiss();
                    }))
                    .setPositiveButton(R.string.start_str, (dialog, which) -> {
                        if (choosenJobOrder == 0) {
                            Log.d(TAG, "Secilen iş emri yok");
                            changeFragmentAndBar(UserAndDeviceFragment.newInstance(), R.id.navigation_user);
                        } else {
                            Log.d(TAG, "Secilen is emri baslatılıyor");
                            logger.addRecordToLog(Logger.LogType.INFO, TAG, "Manuel pallet operation starting...(OUT_COMPANY)");
                            new GateNameTask().execute();
                            dialogForJobOrder.dismiss();
                            startReader();
                        }
                    });
            dialogForJobOrder = alertBuilder.create();
        }
        //endregion
        return v;
    }

    private class FetchGatewayNamesTask extends AsyncTask<Void, Void, List<String>> {
        private Spinner spinner;
        private Context context;

        public FetchGatewayNamesTask(Spinner spinner) {
            this.spinner = spinner;
            this.context = spinner.getContext();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> data = new ArrayList<>();

//            try {
//                URL url = new URL("http://192.168.1.111:5046/api/Job/GatewayNames"); // API'nizin URL'sini buraya ekleyin.
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                // Bağlantıyı gerçekleştir
//                connection.connect();
//
//                // Yanıtı oku
//                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    String line;
//                    StringBuilder response = new StringBuilder();
//
//                    while ((line = bufferedReader.readLine()) != null) {
//                        response.append(line);
//                    }
//
//                    // JSON yanıtını işle ve gatewayNames listesine ekleyin
//                    JSONArray jsonArray = new JSONArray(response.toString());
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        gatewayNames.add(jsonArray.getString(i));
//                    }
//
//                    bufferedReader.close();
//                }
//
//                connection.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            data.add("Veri 1");
            data.add("Veri 2");
            data.add("Veri 3");

            return data;
        }

        @Override
        protected void onPostExecute(List<String> data) {
            super.onPostExecute(data);

            // Spinner'a verileri ekleyin
            ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, data);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
    @Override
    public void onStart() {
        mReader = Reader.getReader(mContext, mEPCHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            dialogForJobOrder.show();
        } else {
            dialogForConnection();
        }
        super.onStart();
    }

    private void startReader() {
        if (workMode == Mode.BARCODE)
            mReader = Reader.getReader(mContext, mBarcodeHandler);
        else
            mReader = Reader.getReader(mContext, mEPCHandler);
        //dataSource = new DataSource(mContext);
        //dataSource.open();
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            int mode;
            if (workMode == Mode.RFID)
                mode = SDConsts.SDTriggerMode.RFID;
            else
                mode = SDConsts.SDTriggerMode.BARCODE;
            Log.d(TAG, "Reader trigger mode " + mReader.SD_GetTriggerMode());
            if (mReader.SD_GetTriggerMode() != mode) {
                Log.d(TAG, "Barcode MODE");
                mReader.SD_SetTriggerMode(mode);
                //enableControl(true);
            }
            Log.d(TAG, "Reader trigger mode " + mReader.SD_GetTriggerMode());
        } else {
            Toast.makeText(mContext, getString(R.string.reader_not_connected), Toast.LENGTH_SHORT).show();
        }
        mInventory = false;
        updateCompleteButtonText();
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
        dialogForJobOrder.dismiss();
        super.onStop();
    }

    //region BARCODE HANDLER
    private static class BarcodeHandler extends Handler {
        private final WeakReference<ManuelPalletFragment> mExecutor;

        public BarcodeHandler(ManuelPalletFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            ManuelPalletFragment executor = mExecutor.get();
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
                        Log.d(TAG, "Barcode is " + m.obj.toString());
                        String tmp[] = m.obj.toString().split(";");
                        Grai grai = new Grai(tmp[0], Grai.Tag.BARCODE);
                        boolean res = mAdapter.addItem(tmp[0], "barcode_mode", true);

                        if (!res)
                            Toast.makeText(mContext, getString(R.string.barcode_already_listed), Toast.LENGTH_SHORT).show();
                            //MyToast.showToast(mContext,getString(R.string.barcode_already_listed),Toast.LENGTH_SHORT);
                        else {
                            updateCompleteButtonText();
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

    //region EPC HANDLER
    private static class EPCHandler extends Handler {
        private final WeakReference<ManuelPalletFragment> mExecutor;

        public EPCHandler(ManuelPalletFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            ManuelPalletFragment executor = mExecutor.get();
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
                                //enableControl(!mInventory);
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
                        //enableControl(!mInventory);
                        //pauseStopwatch();
                        // In case of low battery on inventory, reason value is LOW_BATTERY
                        Toast.makeText(mContext, getString(R.string.stop_inven_reason) + m.arg2, Toast.LENGTH_SHORT).show();
                        break;

                    case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                        if (mReader.RF_StopInventory() == SDConsts.SDResult.SUCCESS) {
                            mInventory = false;
                            //enableControl(!mInventory);
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
                        //enableControl(false);
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
                            // DO NOTHING
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
        if (data.contains(";")) {
            Log.d(TAG, "Full Tag : " + data);
            // 3000 is pc word
            //full tag example(with optional value)
            //1) RF_PerformInventory => "3000123456783333444455556666;rssi:-54.8"
            //2) RF_PerformInventoryWithLocating => "3000123456783333444455556666;loc:64"
            int infoTagPoint = data.indexOf(';');
            Log.d(TAG, "info tag = " + info);
            data = data.substring(0, infoTagPoint);
            Log.d(TAG, "data tag = " + data);
        }

        Log.d(TAG, "DATA :  " + data);


        Grai grai = new Grai(data, Grai.Tag.EPC);
        mAdapter.addItem(grai.getBarcode(), data, true);
        mListView.setSelection(mListView.getAdapter().getCount() - 1);
        updateCompleteButtonText();
    }

    private void processReadDataCustom(String data) {
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String customData = "";
        if (data.contains(";")) {
            Log.d(TAG, "full tag = " + data);
            //full tag example = "3000123456783333444455556666;rssi:-54.8^custom=2348920384"
            int customdDataPoint = data.indexOf('^');
            data = data.substring(0, customdDataPoint);
            int rssiTagPoint = data.indexOf(';');
            data = data.substring(0, rssiTagPoint);
            Log.d(TAG, "data tag = " + data);
            data = data + "\n" + customData;
        }
        Grai grai = new Grai(data, Grai.Tag.EPC);
        mAdapter.addItem(grai.getBarcode(), data, true);
        mListView.setSelection(mListView.getAdapter().getCount() - 1);
    }
    //endregion

    private void savePallets(int shipmentType) {
//        databaseController.open();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Pallet pallet = new Pallet(mAdapter.getBarcode(i), mAdapter.getEPC(i), -1, "0", "0", "0", "0", "0", "0", "0");
            logger.addRecordToLog(Logger.LogType.INFO, TAG, "Manuel pallet operation pallet: " + pallet.toString());
            databaseController.savePalletInTable(pallet);
        }
//        databaseController.close();
        logger.addRecordToLog(Logger.LogType.INFO, TAG, "Manuel pallet operation completed.(OUT_COMPANY)");
        mAdapter.removeAllItem();
        updateCompleteButtonText();
        Toast.makeText(mContext, getString(R.string.pallets_saved), Toast.LENGTH_SHORT).show();
        //dialogForJobOrder.show();
    }

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


    public void killAll() {
//        databaseController.close();
        logger.addRecordToLog(Logger.LogType.INFO, TAG, "Manuel pallet operation stopping...(OUT_COMPANY)");
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            if (workMode == Mode.RFID) {
                mReader.RF_StopInventory();
                //pauseStopwatch();
                mInventory = false;
                //stopStopwatch();
                //unbindStopwatchSvc();
            }
            mReader = Reader.getReader(mContext, null);
        }
    }

    private void updateCompleteButtonText() {

        String t2;
        if (mAdapter != null)
            t2 = mClearButtonText + "(" + mAdapter.getCount() + " palet)";
        else {
            t2 = mClearButtonText + "(0 palet)";
        }
        mClearButton.setText(t2);
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


    //region ASYNC TASKS FOR ShipmentType, Warehouse, GateName, API and JobOrder
    class ShipmentTypeTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                if (authService.GetCurrentUser() != null) {

                    String shipmentTypes = apiService.GET("/ShipmentType");

                    if (shipmentTypes.equals("null")) {
                        Toast.makeText(mContext, R.string.expected_data_null_tryagain, Toast.LENGTH_SHORT).show();
                        getActivity().runOnUiThread(() -> {
                            if (mContext != null)
                                Toast.makeText(mContext, getString(R.string.api_resp_error), Toast.LENGTH_SHORT).show();
                        });
                        return false;
                    } else if (shipmentTypes.equals("no token")) {
                        if (getActivity() != null) {
                            Intent intent = getActivity().getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getActivity().finish();
                            startActivity(intent);
                        } else
                            Log.d(TAG, "activity null");
                        return false;
                    } else {
                        JSONArray tmp = new JSONArray(shipmentTypes);
                        for (int i = 0; i < tmp.length(); i++) {
                            JSONObject tmp2 = tmp.getJSONObject(i);
                            ShipmentType type = new ShipmentType(tmp2.getInt("id"), tmp2.getString("type"));
                            getActivity().runOnUiThread(() -> {
                                adapterShipTypes.add(type);
                            });
                        }
                        return true;
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        if (mContext != null)
                            Toast.makeText(mContext, getString(R.string.api_resp_error), Toast.LENGTH_SHORT).show();
                    });
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception is " + e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
            if (!aBoolean) {
                Log.d(TAG, "Execute error");
            }
        }
    }

    class WarehouseTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                if (authService.GetCurrentUser() != null) {
                    String warehouses = apiService.GET("/Warehouse/ByShipmentType/" + choosenShipmentType);
                    Log.d(TAG, "Warehouses : " + warehouses);
                    if (warehouses.equals("null")) {
                        Toast.makeText(mContext, R.string.expected_data_null_tryagain, Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (warehouses.equals("no token")) {
                        if (getActivity() != null) {
                            Intent intent = getActivity().getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getActivity().finish();
                            startActivity(intent);
                        } else
                            Log.d(TAG, "activity null");
                        return false;
                    } else {
                        JSONArray tmp = new JSONArray(warehouses);
                        for (int i = 0; i < tmp.length(); i++) {

                            JSONObject tmp2 = tmp.getJSONObject(i);
                            if (tmp2.getString("name").equals("Hepsi"))
                                continue;
                            Warehouse type = new Warehouse(tmp2.getInt("id"), tmp2.getInt("companyId"), tmp2.getString("name"));
                            getActivity().runOnUiThread(() -> {
                                adapterWarehouses.add(type);
                            });
                        }
                        return true;
                    }

                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception is " + e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
            if (!aBoolean) {
                getActivity().runOnUiThread(() -> {
                    if (mContext != null)
                        Toast.makeText(mContext, getString(R.string.api_resp_error_or_token), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    class JobOrderTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                if (authService.GetCurrentUser() != null) {
                    String jobOrders = apiService.GET("/JobOrder/GetUncompleteJobOrdersByShipmentTypeAndLocalWarehouseId/" + choosenShipmentType + "/" + choosenWarehouseId);
                    Log.d(TAG, "Job Orders : " + jobOrders);
                    if (jobOrders.equals("null")) {
                        Toast.makeText(mContext, R.string.expected_data_null_tryagain, Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (jobOrders.equals("no token")) {
                        if (getActivity() != null) {
                            Intent intent = getActivity().getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getActivity().finish();
                            startActivity(intent);
                        } else
                            Log.d(TAG, "activity null");
                        return false;
                    } else {
                        JSONArray tmp = new JSONArray(jobOrders);

                        for (int i = 0; i < tmp.length(); i++) {

                            JSONObject tmp2 = tmp.getJSONObject(i);
                            JSONObject tmp3 = new JSONObject(tmp2.getString("company"));
                            JobOrder type = new JobOrder(tmp2.getInt("id"), tmp2.getString("billOfLadingNo"), tmp2.getInt("amount"), tmp2.getInt("companyId"), tmp3.getString("name"), tmp2.getInt("warehouseId"), tmp2.getInt("gateId"));

                            getActivity().runOnUiThread(() -> {
                                adapterJobOrders.add(type);
                            });
                        }
                        return true;
                    }

                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception is " + e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
            if (!aBoolean) {
                (getActivity()).runOnUiThread(() -> {
                    Toast.makeText(mContext, getString(R.string.api_resp_error), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    class GateNameTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                if (authService.GetCurrentUser() != null) {
                    String gates = apiService.GET("/Gate/" + choosenGateId);
                    Log.d(TAG, "Gates : " + gates);
                    if (gates.equals("null")) {
                        Toast.makeText(mContext, R.string.expected_data_null_tryagain, Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (gates.equals("no token")) {
                        if (getActivity() != null) {
                            Intent intent = getActivity().getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getActivity().finish();
                            startActivity(intent);
                        } else
                            Log.d(TAG, "activity null");
                        return false;
                    } else {
                        JSONObject tmp = new JSONObject(gates);
                        choosenGateName = tmp.getString("name");
                        Log.d(TAG, "Choosen gate name" + choosenGateName);
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception is " + e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
            if (!aBoolean) {
                (getActivity()).runOnUiThread(() -> {
                    if (mContext != null)
                        Toast.makeText(mContext, getString(R.string.api_resp_error), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    //endregion

    //region SPINNERS FOR ShipmentType, Warehouse and JobOrder
    public class SpinAdapterShipTypes extends ArrayAdapter<ShipmentType> {

        private ArrayList<ShipmentType> mItemList;

        public SpinAdapterShipTypes(@NonNull Context context, int resource) {
            super(context, resource);
            mItemList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public ShipmentType getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItemList.get(position).getId();
        }

        public String getName(int position) {
            return mItemList.get(position).getType();
        }

        // And the "magic" goes here
        // This is for the "passive" state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            TextView label = (TextView) super.getView(position, convertView, parent);
            //label.setTextColor(Color.BLACK);
            // Then you can get the current item using the values array (Users array) and the current position
            // You can NOW reference each method you has created in your bean object (User class)
            label.setText(mItemList.get(position).getType());
            //label.setId(values[position].getId());
            // And finally return your dynamic (or custom) view for each spinner item
            return label;
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            //label.setTextColor(Color.BLACK);
            label.setText(mItemList.get(position).getType());
            //label.setId(values[position].getId());
            return label;
        }

        @Override
        public void add(@Nullable ShipmentType object) {
            //super.add(object);
            mItemList.add(object);
            notifyDataSetChanged();
        }

        @Override
        public void clear() {
            //super.clear();
            mItemList.clear();
            notifyDataSetChanged();
        }
    }

    public class SpinAdapterWarehouses extends ArrayAdapter<Warehouse> {

        private ArrayList<Warehouse> mItemList;

        public SpinAdapterWarehouses(@NonNull Context context, int resource) {
            super(context, resource);
            mItemList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Warehouse getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItemList.get(position).getId();
        }

        public String getName(int position) {
            return mItemList.get(position).getName();
        }

        // And the "magic" goes here
        // This is for the "passive" state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            TextView label = (TextView) super.getView(position, convertView, parent);
            //label.setTextColor(Color.BLACK);
            // Then you can get the current item using the values array (Users array) and the current position
            // You can NOW reference each method you has created in your bean object (User class)
            label.setText(mItemList.get(position).getName());
            //label.setId(values[position].getId());
            // And finally return your dynamic (or custom) view for each spinner item
            return label;
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            //label.setTextColor(Color.BLACK);
            label.setText(mItemList.get(position).getName());
            //label.setId(values[position].getId());
            return label;
        }

        @Override
        public void add(@Nullable Warehouse object) {
            //super.add(object);
            mItemList.add(object);
            notifyDataSetChanged();
        }

        @Override
        public void clear() {
            //super.clear();
            mItemList.clear();
            notifyDataSetChanged();
        }

    }

    public class SpinAdapterJobOrders extends ArrayAdapter<JobOrder> {

        private ArrayList<JobOrder> mItemList;

        public SpinAdapterJobOrders(@NonNull Context context, int resource) {
            super(context, resource);
            mItemList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public JobOrder getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItemList.get(position).getId();
        }


        // And the "magic" goes here
        // This is for the "passive" state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(String.valueOf(mItemList.get(position).getBillOfLadingNo() + "-" + mItemList.get(position).getCompanyName()));
            return label;
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            //label.setTextColor(Color.BLACK);
            label.setText(String.valueOf(mItemList.get(position).getBillOfLadingNo() + "-" + mItemList.get(position).getCompanyName()));
            //label.setId(values[position].getId());
            return label;
        }

        @Override
        public void add(@Nullable JobOrder object) {
            //super.add(object);
            mItemList.add(object);
            notifyDataSetChanged();
        }

        @Override
        public void clear() {
            //super.clear();
            mItemList.clear();
            notifyDataSetChanged();
        }
    }
    //endregion

}

