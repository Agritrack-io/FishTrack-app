package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.bo.BinLoadsMap;
import io.agritrack.fish.ui.bo.BinWeightRecord;
import io.agritrack.scale.diniargeo.MCWScale;
import io.agritrack.ui.adapter.BinLoadAdapter;
import io.agritrack.ui.adapter.BinLoadAdapter.BinLoadItem;
import io.agritrack.ui.service.LocalPreferences;


public class FishingFillBinsActivity extends AppCompatActivity implements ISummaryActivity {

    protected BroadcastReceiver keyReceiver;
    private TextView tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;
    private RecyclerView rvWeightBatchesBin;
    private BinLoadAdapter adapterCatches;
    private boolean intentForFillBinActivity = false;
    private String mCatchWeight = "";
    private String currentBin;
    private Integer weightOfBin;
    private BinLoadsMap loadsMap;
    private long epochFrom;
    private ImageView ivSupport, ivInfo, ivCheckLastTemp;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private boolean isClicked = true;

    // Bluetooth variables
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private MCWScale scale = null;
    private ProgressBar pbBluetooth;
    private String currState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        loadsMap = new BinLoadsMap();

        ArrayList<BinLoadItem> list = recFishing.availBins != null
                ? recFishing.availBins.stream()
                .map(BinLoadItem::new)
                .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        rvWeightBatchesBin.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterCatches = new BinLoadAdapter(this, list);
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

//        // =================================
//        // Adding bin load completion functionality
//        btnFillBin.setOnClickListener(view -> {
//            isClicked = true;
//            if (weightOfBin != null) {
//                BinWeightRecord.BinRecord currRec = recFishing.binWeightRecord.getRecordForEPC(currentBin);
//                if (currRec.from != null) {
//                    GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, currRec.init, currRec.from, System.currentTimeMillis() / 1000l);
//                } else {
//                    GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, currRec.init, epochFrom, System.currentTimeMillis() / 1000l);
//                }
//                weightOfBin = null;
//            }
//            btnCurrentBinScan.setEnabled(true);
//            btnCurrentBinScan.setTextColor(getColor(R.color.aqua));
//            btnAddCatch.setEnabled(false);
//            btnAddCatch.setTextColor(Color.DKGRAY);
//            btnDeleteCatch.setEnabled(false);
//            btnDeleteCatch.setTextColor(Color.DKGRAY);
//            view.setEnabled(false);
//            ((Button) view).setTextColor(Color.DKGRAY);
//            epochFrom = 0;
//        });
//
//        btnDeleteCatch.setOnClickListener(view -> {
//            if (!Strings.isEmptyOrWhitespace(adapterCatches.getSelectedValue())) {
//                // instantiate Site selection confirm dialog
//                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
//                confirmSiteSelectionDlg.args().putString("selectedCatch", adapterCatches.getSelectedValue());
//                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterCatches.getSelectedValue() + " kg");
//
//                confirmSiteSelectionDlg.onConfirm(bundle -> {
//                    String aCatch = bundle.getString("selectedCatch");
//                    if (aCatch != null) {
//                        adapterCatches.removeItem(aCatch);
//                        adapterCatches.notifyDataSetChanged();
//
//                        tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
//                        tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));//TODO:: Remove
//                        tvTotalWeightCount.setText(String.format("%s", loadsMap.totalWeight().toString()));
//                        adapterCatches.clearSelectedValue();
//                        btnDeleteCatch.setEnabled(false);
//                        btnDeleteCatch.setTextColor(Color.DKGRAY);
//                    }
//                });
//
//                confirmSiteSelectionDlg.onReject(bundle -> {
//                    adapterCatches.clearSelectedValue();
//                    btnDeleteCatch.setEnabled(false);
//                    btnDeleteCatch.setTextColor(Color.DKGRAY);
//                });
//
//                FragmentManager fm = getSupportFragmentManager();
//                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
//            } else {
//                // <delete> Button was pressed without selecting a Catch first.
//                CToast(getApplicationContext(), render("Plz select a Catch to delete!!"), Toast.LENGTH_LONG);
//            }
//        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

//        ivCheckLastTemp.setOnClickListener(view -> {
//            this.stopScanner();
//            updateState();
//            Intent i = new Intent(getApplicationContext(), TestBinTempActivity.class);
//            i.putExtra("FillBinActivity", true);
//            startActivity(i);
//        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingFillBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingFillBinsActivity.this);
            infoDialog.showDialog();
        });

        // ============
        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);

        /*//############ Bluetooth initialization ###############################################
        // get instance of BT Adapter. Will be used to search dor BT devices.
        this.bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        // register handlers for BT events.
        bluetooth_RegisterHandlers();

        // enable bluetooth
        BluetoothUtils.Switch(true);
        // start discovering
        if (this.bluetoothAdapter != null && !this.bluetoothAdapter.isDiscovering()) {
            ((Runnable) () -> this.bluetoothAdapter.startDiscovery()).run();
        }
        //#####################################################################################*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregister the receiver, pairs with registration in onStart()!!!
        if (keyReceiver != null) {
            unregisterReceiver(keyReceiver);
        }

        // dispose bluetooth handlers
        //Bluetooth_DisposeHandlers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            if (!isClicked) {
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            if (!isClicked) { // && loadsMap.hasLoads()
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        // bluetooth progress bar
        pbBluetooth = findViewById(R.id.pbBluetooth);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (recFishing.totalFishWeight != null) {
            tvTotalWeightCount.setText(recFishing.totalFishWeight.toString());
        }

        if (recFishing.totalBinsUsed != null) {
            tvUsedBinsCount.setText(recFishing.totalBinsUsed.toString());
        }


        // sometimes 'hvst.availBins' is null!!!
        int availBinsCnt = hvst.availBins != null ? hvst.availBins.size() : 0;
        tvAvailableBinsCount.setText(String.valueOf(availBinsCnt));

        if (!recFishing.binWeightRecord.isEmpty()) {
            for (BinWeightRecord.BinRecord bin : recFishing.binWeightRecord.getBinsData()) {
                loadsMap.addLoad(bin.binEPC, bin.weight + "");
            }
            adapterCatches.setValues(recFishing.binWeightRecord.getBinsData().stream().map(x -> new BinLoadItem(x.binEPC, x.weight, x.temp)).collect(Collectors.toList()));
            if (recFishing.reqWeight != null) {
                tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
            } else {
                tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), "N/A"));
            }
            isClicked = true;
        }

        if (loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBinsData();
            tvUsedBinsCount.setText(loadsMap.loadsCnt());
        }
    }

    @Override
    public void refreshSummary() {
        if (adapterCatches.getValues() != null) {
            for (BinLoadItem binLoad : adapterCatches.getValues()) {
                loadsMap.addLoad(binLoad.epc, binLoad.weight + "");
            }
        }
        if (recFishing.reqWeight != null) {
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
        } else {
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), "N/A"));
        }

        if (loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBinsData();
            tvUsedBinsCount.setText(loadsMap.loadsCnt());
        }
    }

    private FishingRecord updateState() {
        // get an instance of local DB
        MobileDB db = MobileDB.getInstance(getAppContext());

        if (adapterCatches.getValues() != null) {
            for (BinLoadItem binLoad : adapterCatches.getValues())
                recFishing.binWeightRecord.addRecord(binLoad.epc, binLoad.weight, binLoad.temperature, null, null, null);
        }
        if (tvTotalWeightCount.getText() != null) {
            recFishing.totalFishWeight = loadsMap.totalWeight();
        }

        if (tvUsedBinsCount.getText() != null && !Strings.isEmptyOrWhitespace(tvUsedBinsCount.getText().toString())) {
            recFishing.totalBinsUsed = Short.valueOf(tvUsedBinsCount.getText().toString());
        }
        GlobalState.commitFishing(db, Boolean.FALSE);

        return recFishing;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
//            if (GlobalState.recFishing.totalBinsUsed == null) {
//                sb.append(String.format("\n%s is missing", "'Harvest bins'"));
//            }
        }
        return sb.toString();
    }

//    private void showBTScaleCatchDialog() {
//        // Store the created AlertDialog instance.
//        // Because only AlertDialog has cancel method.
//        AlertDialog alertDialog = null;
//
//        // Create a alert dialog builder.
//        final AlertDialog.Builder builder = new AlertDialog.Builder(FishingFillBinsActivity.this);
//        // Set icon value.
//        builder.setIcon(R.mipmap.ic_launcher);
//        // Set title value.
//        builder.setTitle(R.string.type_weight);
//
//        // Get custom login form view.
//        final View btScaleWeightFormView = getLayoutInflater().inflate(R.layout.bt_scale_reading_alert_dlg, null);
//
//        // assign variables to ui controls.
//        final EditText etWeight = btScaleWeightFormView.findViewById(R.id.etFishCatchWeight);
//        final TextView tvStatus = btScaleWeightFormView.findViewById(R.id.etBTScaleStatus);
//        //final Button ivRefreshScale = btScaleWeightFormView.findViewById(R.id.ivRefreshScale);
//        /*ivRefreshScale.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ClassREAD reading = getScaleReading();
//                if (reading != null) {
//                    tvStatus.setText(reading.getWeigthState().name());
//                    if (reading.getNet() > 0) {
//                        etWeight.setText(String.format("%.0f", reading.getNet()));
//                    }
//                } else {
//                    tvStatus.setText("N/A");
//                }
//            }
//        });*/
//
////        ClassREAD reading = getScaleReading();
////        if (reading != null) {
////            tvStatus.setText(reading.getWeigthState().name());
////            etWeight.setText(String.format("%.0f", reading.getNet()));
////        } else {
////            tvStatus.setText("N/A");
////        }
//
//        // Set above view in alert dialog.
//        builder.setView(btScaleWeightFormView);
//
//        // Register button click listener.
//        builder.setPositiveButton("OK", (dialog, which) -> {
//            mCatchWeight = etWeight.getText().toString();
//            if (Strings.isEmptyOrWhitespace(mCatchWeight)) {
//                CToast(getApplicationContext(), render(R.string.type_weight), Toast.LENGTH_LONG);
//                return;
//            }
//            adapterCatches.addItem(mCatchWeight);
//            adapterCatches.notifyDataSetChanged();
//
//            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
//            weightOfBin = loadsMap.weightOf(currentBin);
//            tvUsedBinsCount.setText(loadsMap.loadsCnt());
//            if (recFishing.reqWeight!=null) {
//                tvTotalWeightCount.setText(String.format("%s", loadsMap.totalWeight().toString()));
//                //tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
//            } else {
//                tvTotalWeightCount.setText(String.format("%s", loadsMap.totalWeight().toString()));
//                //tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), "N/A"));
//            }
//        });
//
//        // Reset button click listener.
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            // Close Alert Dialog.
//            dialog.cancel();
//        });
//
//        builder.setCancelable(true);
//        alertDialog = builder.create();
//        alertDialog.show();
//    }

//    private ClassREAD getScaleReading() {
//        // Checks if Bluetooth Adapter is present
//        if (bluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
//            return null;
//        }
//
//        if (scale == null) {
//            if(bluetoothDevice!=null) {
//                this.scale = new MCWScale(bluetoothDevice);
//            } else {
//                Toast.makeText(getApplicationContext(), "No Scale was found!", Toast.LENGTH_SHORT).show();
//                return null;
//            }
//        }
//
//        boolean connected = scale.Connect();
//        if (connected) {
//            boolean sentReadCmd = scale.Send("READ");
//            String read = scale.ReadString();
//            if (!Strings.isEmptyOrWhitespace(read)) {
//                return new ClassREAD(read);
//            }
//        }
//
//        return null;
//    }

//    protected void onClick(View view) {
//        isClicked = false;
//        scanner_runnable.setFilter(Filters.RFID_BIN);
//        scanner_runnable.startReading();
//        mScanHandler.postDelayed(scanner_runnable, 0);
//    }

//    // ###################################################
//    private void stopScanner() {
//        if (this.scanner_runnable != null) {
//            this.scanner_runnable.stopReading();
//            mScanHandler.removeCallbacks(this.scanner_runnable);
//        }
//    }
//
//    // ###################################################
//    private class ScanHandler extends Handler {
//        private final WeakReference<FishingFillBinsActivity> mActivity;
//
//        public ScanHandler(FishingFillBinsActivity activity) {
//            mActivity = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
////            FishingFillBinsActivity activity = mActivity.get();
////            if (activity != null) {
////            }
//            switch (msg.what) {
//                case 1:
//                    try {
//                        String epcStr = msg.getData().getString("epc");
//                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
//                            new Handler(Looper.getMainLooper()).post(() -> {
//                                String epc = epcStr.substring(14);
//                                tvCurrentBin.setText(epc);
//                                currentBin = epcStr;
//
//                                BinWeightRecord.BinRecord currRec = recFishing.binWeightRecord.getRecordForEPC(currentBin);
//                                if (currRec == null || currRec.init == null) {
//                                    CToast(getApplicationContext(), render(R.string.data_logger_not_initialized), Toast.LENGTH_LONG);
//                                    GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, null, epochFrom, null);
//                                    loadsMap.addLoad(currentBin, "0");
//                                }
//                                List<String> loadForBin = loadsMap.getLoads(currentBin);
//                                if (loadForBin == null) {
//                                    loadForBin = new ArrayList<>();
//                                }
//                                adapterCatches.setValues(loadForBin);
//                                if (loadsMap.getLoads(currentBin) != null) {
//                                    isClicked = true;
//                                }
//                                tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
//                                adapterCatches.notifyDataSetChanged();
//                                tvUsedBinsCount.setText(loadsMap.loadsCnt());
//                                epochFrom = System.currentTimeMillis() / 1000l;
//                            });
//
//                            btnAddCatch.setEnabled(true);
//                            btnAddCatch.setTextColor(getColor(R.color.aqua));
//                            if (adapterCatches.getItemCount() != 0) {
//                                btnFillBin.setEnabled(true);
//                                btnFillBin.setTextColor(getColor(R.color.aqua));
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        saveCrashInfo2File(e);
//                    }
//                    break;
//                case 1980:
//                    if (!IsDemo) {
//                        //CToast(getApplicationContext(), render("No BIN was found!!"), Toast.LENGTH_SHORT);
//                    }
//                    break;
//            }
//        }
//    }
//
//
//    //#######################################
//    //####   Private BLUETOOTH methods   ####
//    //#######################################
//    private void bluetooth_RegisterHandlers() {
//        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_STARTED"));
//        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
//        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
//        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
//        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
//    }
//
//    private void Bluetooth_DisposeHandlers() {
//        unregisterReceiver(this.bluetoothReceiver);
//    }
//
//    private void addDevice(BluetoothDevice bluetoothDevice) {
//        this.scale = new MCWScale(bluetoothDevice);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Log.wtf("DEVICE FOUND", String.format("%s [%s]", bluetoothDevice.getName(), bluetoothDevice.getAddress()));
//    }
//
//    // Broadcast receiver that handles BlueTooth events.
//    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
//                pbBluetooth.setVisibility(VISIBLE);
//            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
//                pbBluetooth.setVisibility(View.INVISIBLE);
//                //btnGetReading.setVisibility(VISIBLE);
//            } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
//                if (bluetoothAdapter.isEnabled()) {
//                    currState = null;
//                }
//            } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
//                //changeState("BOND_STATE_CHANGED");
//            } else if ("android.bluetooth.device.action.FOUND".equals(action)) {
//                BluetoothDevice bluetoothDevice = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
//                String trim = ((bluetoothDevice == null || bluetoothDevice.getName() == null) ? "" : bluetoothDevice.getName()).trim();
//                if (trim.length() > 0 && trim.startsWith("BTDA")) {
//                    addDevice(bluetoothDevice);
//                }
//            }
//        }
//    };
}