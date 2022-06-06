package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.bo.BinLoadsMap;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;


public class FishingFillBinsActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    private Button btnCurrentBinScan, btnAddCatch, btnDeleteCatch, btnFillBin;
    private TextView tvCurrentBin, tvBinWeight, tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;
    private ImageView ivBT;

    private RecyclerView rvWeightBatchesBin;
    private TemplateRecyclerAdapter adapterCatches;
    private boolean isClickable;
    private String mCatchWeight = "";
    private String currentBin;
    private Integer weightOfBin;
    private BinLoadsMap loadsMap;
    private long epochFrom;
    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private boolean isClicked = false;


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        break;
                }

            }
        }
    };

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    //
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("Activity result", "OK");
                    // There are no request codes
                    Intent data = result.getData();
                }
            });

/*
    View.OnClickListener ivBTListener = v -> {
        switch (v.getId()) {

            // Turn on Bluetooth btn click
            case R.id.ivBT:

                if (!bluetoothAdapter.isEnabled()) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        ActivityCompat.requestPermissions(FishingFillBinsActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT);
                        // here to request the missing permissions, and then overriding
                        // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    // Intent to On Bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    //  startActivityForResult(intent, REQUEST_ENABLE_BT);

                    activityResultLauncher.launch(intent);
                } else {
                    CToast(this, render("Bluetooth is already ON."), Toast.LENGTH_SHORT);
                }
                break;

            // Discover bluetooth btn click
            case R.id.discoverableBtn:

                if (!bluetoothAdapter.isDiscovering()) {
                    CToast(this, render("Making Your BT Device Discoverable."), Toast.LENGTH_SHORT);
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    //startActivityForResult(intent ,REQUEST_DISCOVER_BT);
                    activityResultLauncher.launch(intent);
                }
                break;

            // Turn off Bluetooth btn click
            case R.id.offBtn:

                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                    CToast(this, render("Turning Bluetooth Off."), Toast.LENGTH_SHORT);
                    ivBT.setColorFilter(getColor(R.color.agri_red));
                } else {
                    CToast(this, render("Bluetooth is already off."), Toast.LENGTH_SHORT);
                }

                break;
            // Get Paired devices button click
            case R.id.pairedBtn:

                if (bluetoothAdapter.isEnabled()) {
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedTv.append("\nDevice: " + device.getName() + ", " + device);
                    }
                } else {
                    //bluetooth is off so can't get paired devices
                    CToast(this, render("Turn ON Bluetooth to get paired devices."), Toast.LENGTH_SHORT);
                }
                break;
        }
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // enable Bluetooth Features
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ivBT.setVisibility(View.INVISIBLE);
            CToast(this, render("Bluetooth is NOT Available"), Toast.LENGTH_SHORT);
        } else {
            ivBT.setVisibility(View.VISIBLE);
            CToast(this, render("Bluetooth is Available"), Toast.LENGTH_SHORT);

            // Set image according to bluetooth status (on/off)
            if (bluetoothAdapter.isEnabled()) {
                ivBT.setColorFilter(getColor(R.color.agri_blue));
            } else {
                ivBT.setColorFilter(getColor(R.color.agri_red));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(FishingFillBinsActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT);
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                // Intent to On Bluetooth
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //  startActivityForResult(intent, REQUEST_ENABLE_BT);

                activityResultLauncher.launch(intent);

//            mOnBtn.setOnClickListener(this);          // Turn on Bluetooth btn click
//            mDiscoverBtn.setOnClickListener(this);    // Discover bluetooth btn click
//            mOffBtn.setOnClickListener(this);         // Turn off Bluetooth btn click
//            mPairedBtn.setOnClickListener(this);      // Get Paired devices button click
            }


            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        }
        //**************************************************


        // initialize the map for each bin's loads.
        loadsMap = new BinLoadsMap();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        rvWeightBatchesBin.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterCatches = new TemplateRecyclerAdapter(this, new ArrayList<>(), false);
        isClickable = adapterCatches.isClickable;
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

        // initially only scan button is active.
        btnCurrentBinScan.setEnabled(true);
        btnAddCatch.setEnabled(false);
        btnAddCatch.setTextColor(Color.DKGRAY);
        btnDeleteCatch.setEnabled(false);
        btnDeleteCatch.setTextColor(Color.DKGRAY);
        btnFillBin.setEnabled(false);
        btnFillBin.setTextColor(Color.DKGRAY);

        // =================================
        // RFID scanning functionality
        btnCurrentBinScan.setOnClickListener(this::onClick);

        // =================================
        // Adding fish catch functionality
        btnAddCatch.setOnClickListener(view -> {
            btnCurrentBinScan.setEnabled(false);
            btnCurrentBinScan.setTextColor(Color.DKGRAY);
            btnFillBin.setEnabled(true);
            btnFillBin.setTextColor(getColor(R.color.aqua));
            btnDeleteCatch.setEnabled(true);
            btnDeleteCatch.setTextColor(getColor(R.color.aqua));

            //show Message box
            showCatchDialog();
        });

        // =================================
        // Adding bin load completion functionality
        btnFillBin.setOnClickListener(view -> {
            isClicked = true;
            GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, epochFrom, System.currentTimeMillis() / 1000l);
            isClickable = false;
            btnCurrentBinScan.setEnabled(true);
            btnCurrentBinScan.setTextColor(getColor(R.color.aqua));
            btnAddCatch.setEnabled(false);
            btnAddCatch.setTextColor(Color.DKGRAY);
            btnDeleteCatch.setEnabled(false);
            btnDeleteCatch.setTextColor(Color.DKGRAY);
            view.setEnabled(false);
            ((Button) view).setTextColor(Color.DKGRAY);
            epochFrom = 0;
        });

        btnDeleteCatch.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(adapterCatches.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedCatch", adapterCatches.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterCatches.getSelectedValue() + " kg");

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String aCatch = bundle.getString("selectedCatch");
                    if (aCatch != null) {
                        adapterCatches.removeItem(aCatch);
                        adapterCatches.notifyDataSetChanged();

                        tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                        tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));

                        adapterCatches.clearSelectedValue();
                        btnDeleteCatch.setEnabled(false);
                        btnDeleteCatch.setTextColor(Color.DKGRAY);
                    }
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterCatches.clearSelectedValue();
                    btnDeleteCatch.setEnabled(false);
                    btnDeleteCatch.setTextColor(Color.DKGRAY);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Catch first.
                CToast(getApplicationContext(), render("Plz select a Catch to delete!!"), Toast.LENGTH_LONG);
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

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
    }

    @Override
    protected void onStop() {
        super.onStop();

        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);

        /*if (btReceiver != null)
            unregisterReceiver(btReceiver);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void onClick(View view) {
        isClicked = false;
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.HighEnergy();
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
        isClickable = true;
        scanner_runnable.LowEnergy();
    }

    private void assignCtrlVars() {
        btnCurrentBinScan = findViewById(R.id.btnScanCurrentBin);
        btnAddCatch = findViewById(R.id.btnAddCatch);
        btnDeleteCatch = findViewById(R.id.btnDeleteCatch);
        btnFillBin = findViewById(R.id.btnEndBin);
        tvCurrentBin = findViewById(R.id.tvBinName);
        tvBinWeight = findViewById(R.id.tvBinWeight);
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        ivBT = findViewById(R.id.ivBT);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (recFishing.totalFishWeight != null) {
            tvTotalWeightCount.setText(recFishing.totalFishWeight.toString());
        }

        if (recFishing.totalBinsUsed != null) {
            tvUsedBinsCount.setText(recFishing.totalBinsUsed.toString());
        }

        if (!loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBins();
            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
        }

        tvAvailableBinsCount.setText(String.valueOf(hvst.availBins.size()));
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
            if (!isClicked) {
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }

    private FishingRecord updateState() {
        // get an instance of local DB
        MobileDB db = MobileDB.getInstance(getAppContext());

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
            if (GlobalState.recFishing.totalBinsUsed == null) {
                sb.append(String.format("\n%s is missing", "'Harvest bins'"));
            }
        }
        return sb.toString();
    }

    private void showCatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.fish_catch_weight);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) FishingFillBinsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));
        input.requestFocus();
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mCatchWeight = input.getText().toString();
            if (Strings.isEmptyOrWhitespace(mCatchWeight) || mCatchWeight == null) {
                CToast(getApplicationContext(), render(R.string.type_weight), Toast.LENGTH_LONG);
                return;
            }
            adapterCatches.addItem(mCatchWeight);
            adapterCatches.notifyDataSetChanged();

            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
            weightOfBin = loadsMap.weightOf(currentBin);
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<FishingFillBinsActivity> mActivity;

        public ScanHandler(FishingFillBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
//            FishingFillBinsActivity activity = mActivity.get();
//            if (activity != null) {
//            }
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    if (!Strings.isEmptyOrWhitespace(epcStr)) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            String epc = epcStr.substring(14);
                            tvCurrentBin.setText(epc);
                            currentBin = epcStr;
                            adapterCatches.setValues(loadsMap.getLoads(currentBin));
                            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                            adapterCatches.notifyDataSetChanged();
                            tvUsedBinsCount.setText(loadsMap.loadsCnt());
                            epochFrom = System.currentTimeMillis() / 1000l;
                        });

                        btnAddCatch.setEnabled(true);
                        btnAddCatch.setTextColor(getColor(R.color.aqua));
                        if (adapterCatches.getItemCount() != 0) {
                            btnFillBin.setEnabled(true);
                            btnFillBin.setTextColor(getColor(R.color.aqua));
                        }
                    }

                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No BIN was found!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}