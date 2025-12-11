package io.agritrack.philosofish.fish.ui.testBinTemperature;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.util.Strings;
import com.kkmcn.kbeaconlib2.KBeacon;
import com.kkmcn.kbeaconlib2.KBeaconsMgr;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.fish.ui.fishing.FishingBinsActivity;
import io.agritrack.philosofish.fish.ui.fishing.FishingFillBinsActivity;
import io.agritrack.philosofish.fish.ui.initBins.InitBinsActivity;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.LeDeviceListAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class TestBinTempActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
        KBeaconsMgr.KBeaconMgrDelegate, LeDeviceListAdapter.ListDataSource {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ProgressBar progressBar;
    private Button btnScanBin;

    private final static String TAG = "Beacon.ScanAct";//DeviceScanActivity.class.getSimpleName();

    private static final String LOG_TAG = "ScanExample";

    private boolean intentForBinActivity = false;
    private boolean intentForFillBinActivity = false;
    private boolean intentForInitBinActivity = false;
    private SingleShotScanner singleShot_runnable;
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private static final int PERMISSION_COARSE_LOCATION = 22;
    private static final int PERMISSION_FINE_LOCATION = 23;
    private static final int PERMISSION_SCAN = 24;
    private static final int PERMISSION_CONNECT = 25;
    private Handler autoStopHandler = new Handler();


    private ListView mListView;
    private LeDeviceListAdapter mDevListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout mLayoutFilterName, mLayoutFilterRssi;
    private SeekBar mSeekBarRssi;
    private TextView mTxtViewRssi, tvBinsCount;
    private int mRssiFilterValue;

    private int mScanFailedContinueNum = 0;

    private final static int MAX_ERROR_SCAN_NUMBER = 2;
    private HashMap<String, KBeacon> mBeaconsDictory;
    private KBeacon[] mBeaconsArray;
    private KBeaconsMgr mBeaconsMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bin_temp);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.getBoolean("BinActivity")) {
                intentForBinActivity = bundle.getBoolean("BinActivity");
            } else if (bundle.getBoolean("FillBinActivity")) {
                intentForFillBinActivity = bundle.getBoolean("FillBinActivity");
            } else if (bundle.getBoolean("BinInitActivity")) {
                intentForInitBinActivity = bundle.getBoolean("BinInitActivity");
            }
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTestTemp);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        mRssiFilterValue = -100;

        mBeaconsDictory = new HashMap<>(50);
        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(this);
        if (mBeaconsMgr == null) {
            CToast(getAppContext(), "make sure the phone has support ble funtion", Toast.LENGTH_LONG);
            finish();
            return;
        }
        mBeaconsMgr.delegate = this;
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);

        // get  references of the controls
        assignCtrlVars();

        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TestBinTempActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop scanning if running
        if (mBeaconsMgr != null && mBeaconsMgr.isScanning()) {
            mBeaconsMgr.stopScanning();
        }

        // Unregister receiver safely
        try {
            unregisterReceiver(keyReceiver);
        } catch (Exception ignored) {
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Kill all pending callbacks
        mScanHandler.removeCallbacksAndMessages(null);

        // Stop scan to avoid background BLE overload
        if (mBeaconsMgr != null && mBeaconsMgr.isScanning()) {
            mBeaconsMgr.stopScanning();
        }
    }


    @Override
    protected void onStart() {
        // instantiate Reader Module
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        ContextCompat.registerReceiver(this, keyReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // instantiate Reader Module
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBeaconsMgr.clearBeacons();
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToMainMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent();
            if (intentForBinActivity) {
                i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                i.putExtra("BinActivity", true);
            } else if (intentForFillBinActivity) {
                i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
                i.putExtra("FillBinActivity", true);
            } else if (intentForInitBinActivity) {
                i = new Intent(getApplicationContext(), InitBinsActivity.class);
                i.putExtra("BinInitActivity", true);
            } else {
                i = new Intent(getApplicationContext(), FishHomeActivity.class);
            }
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvBinsCount = findViewById(R.id.tvBinsCount);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnScanBin = findViewById(R.id.btnScanBin);
        mListView = (ListView) findViewById(R.id.listview);
        mDevListAdapter = new LeDeviceListAdapter(this, getApplicationContext());
        mListView.setAdapter(mDevListAdapter);
        mListView.setOnItemClickListener(this);
        mLayoutFilterRssi = (LinearLayout) findViewById(R.id.layRssiFilter);
        mSeekBarRssi = (SeekBar) findViewById(R.id.seekBarRssiFilter);
        mSeekBarRssi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRssiFilterValue = progress - 100;
                String strRssiValue = String.valueOf(mRssiFilterValue) + getString(R.string.BEACON_RSSI_UINT);
                mTxtViewRssi.setText(strRssiValue);
                enableFilterSetting();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mTxtViewRssi = (TextView) findViewById(R.id.txtViewRssiValue);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rvBinsForTransport);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        swipeRefreshLayout.setRefreshing(false);
                        if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER) {
                            mScanFailedContinueNum = 0;
                            new AlertDialog.Builder(TestBinTempActivity.this)
                                    .setTitle(R.string.common_error_title)
                                    .setMessage(R.string.bluetooth_error_need_reboot)
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            clearAllData();
                            mDevListAdapter.notifyDataSetChanged();
                        }
                    }
                }, 500);
            }
        });

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/digital-7.ttf");
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void enableFilterSetting() {
        //filter
        boolean bChangeFilter = false;

        if (mRssiFilterValue != mBeaconsMgr.getScanMinRssiFilter()) {
            mBeaconsMgr.setScanMinRssiFilter(mRssiFilterValue);
            bChangeFilter = true;
        }
        if (bChangeFilter) {
            clearAllData();
            mDevListAdapter.notifyDataSetChanged();
        }

    }

    public void clearAllData() {
        mBeaconsDictory.clear();
        mBeaconsArray = null;
        mBeaconsMgr.clearBeacons();
        tvBinsCount.setText("0");
    }

    public void onBeaconDiscovered(KBeacon[] beacons) {
        for (KBeacon pBeacons : beacons) {
            mBeaconsDictory.put(pBeacons.getMac(), pBeacons);
        }

        // Prevent memory overload - CRASH FIX
        if (mBeaconsDictory.size() > 300) {
            mBeaconsDictory.clear();
        }

        mBeaconsArray = new KBeacon[mBeaconsDictory.size()];
        mBeaconsDictory.values().toArray(mBeaconsArray);
        mDevListAdapter.notifyDataSetChanged();
        tvBinsCount.setText(getCount() + "");
    }

    public void onCentralBleStateChang(int nNewState) {
        Log.e(TAG, "centralBleStateChang：" + nNewState);
    }

    public void onScanFailed(int errorCode) {
        if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER) {
            CToast(getAppContext(), "Scan encountered error, error time:" + mScanFailedContinueNum, Toast.LENGTH_SHORT);
        }
        mScanFailedContinueNum++;
    }

    private void handleStartScan() {

        enableFilterSetting();
        if (!checkBluetoothPermitAllowed()) {
            return;
        }

        int nStartScan = mBeaconsMgr.startScanning();
        if (nStartScan == 0) {
            Log.v(TAG, "start scan success");
        } else if (nStartScan == KBeaconsMgr.SCAN_ERROR_BLE_NOT_ENABLE) {
            CToast(getAppContext(), "Το Bluetooth δεν είναι ενεργοποιημένο", Toast.LENGTH_LONG);
        } else if (nStartScan == KBeaconsMgr.SCAN_ERROR_UNKNOWN) {
            CToast(getAppContext(), "Παρακαλώ επιβεβαιώστε ότι η εφαρμογή έχει πρόσβαση στο Bluetooth", Toast.LENGTH_LONG);
        }
    }

    private boolean checkBluetoothPermitAllowed() {
        boolean bHasPermission = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_FINE_LOCATION);
            bHasPermission = false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_COARSE_LOCATION);
            bHasPermission = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        PERMISSION_SCAN);
                bHasPermission = false;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        PERMISSION_CONNECT);
                bHasPermission = false;
            }
        }

        return bHasPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_SCAN) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σάρωσης BLE για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);

            }
        }

        if (requestCode == PERMISSION_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σύνδεσης BLE για την εύρεση BLE", Toast.LENGTH_LONG);

            }
        }

        if (requestCode == PERMISSION_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια κατά προσέγγιση τοποθεσίας για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);
            }
        }
        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια ακριβούς τοποθεσίας για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);
            }
        }
    }

    public KBeacon getBeaconDevice(int nIndex) {
        if (mBeaconsArray != null && mBeaconsArray.length > nIndex) {
            return mBeaconsArray[nIndex];
        } else {
            return null;
        }
    }

    public int getCount() {
        if (mBeaconsArray == null) {
            return 0;
        } else {
            return mBeaconsArray.length;
        }
    }


    @Override
    public void onClick(View view) {

        if (mBeaconsMgr.isScanning()) {
            stopScanSafe();
        } else {
            handleStartScan();
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            btnScanBin.setText(R.string.stop_scan);
            progressBar.setVisibility(View.VISIBLE);

            autoStopHandler.postDelayed(this::stopScanSafe, 30000); // auto stop in 30s
        }
    }

    private void stopScanSafe() {
        if (mBeaconsMgr.isScanning()) {
            mBeaconsMgr.stopScanning();
            progressBar.setVisibility(View.GONE);
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            btnScanBin.setText(R.string.scan_all_bins);
        }
    }


    // ###################################################
    private void stopScanner() {
        if (singleShot_runnable != null) {
            singleShot_runnable.stopReading();
            mScanHandler.removeCallbacks(singleShot_runnable);
            singleShot_runnable = null;      // avoid leaked runnable
        }
    }


    private class ScanHandler extends Handler {
        private final WeakReference<TestBinTempActivity> mActivity;

        public ScanHandler(TestBinTempActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            // after bin is identified, initialize the temperatures logger.
                            Asset bin = db.assetDAO().getByLoggerEPC(epcStr);
                            if (bin != null) {
                                String binEPC = bin.rfid;
                                //tvCurrentBin.setText(binEPC.substring(binEPC.length() - 10));
                                progressBar.setVisibility(ProgressBar.VISIBLE);
                                progressBar.setProgress(0);
                                //cmd.setFilterEPC(epcStr);
                                //mScanHandler.post(readLastSampleThread);
                            } else if (!IsDemo) {
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                CToast(getApplicationContext(), render(R.string.no_logger_found_linked_to_bin), Toast.LENGTH_SHORT);
                            }
                        } else {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            CToast(getApplicationContext(), render(R.string.scan_bin_again), Toast.LENGTH_SHORT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveCrashInfo2File(e);
                    }
                    break;
                case 200:
                    Double lastTemp = msg.getData().getDouble("value");
                    //tvCurrentTemp.setText(String.format(new DecimalFormat("##.##").format(lastTemp) + "°C"));
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}