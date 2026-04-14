package io.agritrack.philosofish.fish.ui.binTurnover;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.caen.api.EncodingUtils.createTimestamp;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recLoggerData;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSensor;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvType;
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBSensorType;
import com.kkmcn.kbeaconlib2.KBConnState;
import com.kkmcn.kbeaconlib2.KBSensorHistoryData.KBRecordBase;
import com.kkmcn.kbeaconlib2.KBSensorHistoryData.KBRecordDataRsp;
import com.kkmcn.kbeaconlib2.KBSensorHistoryData.KBRecordHumidity;
import com.kkmcn.kbeaconlib2.KBSensorHistoryData.KBSensorReadOption;
import com.kkmcn.kbeaconlib2.KBeacon;
import com.kkmcn.kbeaconlib2.KBeaconsMgr;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.agritrack.data.repo.MeasurementRepository;
import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.sync.SyncApi;
import io.agritrack.philosofish.api.sync.SyncBinInfo;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.caen.api.EncodingUtils;
import io.agritrack.philosofish.caen.common.CAENState;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import io.agritrack.philosofish.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.TempSample;
import io.agritrack.philosofish.data.model.common.TemperatureTimeSeries;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.data.repo.BinInfoRepository;
import io.agritrack.philosofish.data.repo.IFishTrackRepository;
import io.agritrack.philosofish.data.repo.TemperatureDataRepository;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.LoggerDataRecord;
import io.agritrack.philosofish.fish.state.QualityStepsState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;
import io.agritrack.philosofish.ui.adapter.TemperatureProfileAdapter;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import io.agritrack.philosofish.ui.tools.caen.ILoggerDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BinTurnoverActivity extends AppCompatActivity implements KBeaconsMgr.KBeaconMgrDelegate, KBeacon.ConnStateDelegate, KBeacon.NotifyDataDelegate {
    private final java.util.concurrent.atomic.AtomicBoolean didNavigate = new java.util.concurrent.atomic.AtomicBoolean(false);

    private static final int PERMISSION_COARSE_LOCATION = 22;
    private static final int PERMISSION_FINE_LOCATION = 23;
    private static final int PERMISSION_SCAN = 24;
    private static final int PERMISSION_CONNECT = 25;
    private final static String TAG = "Beacon.ScanAct";//DeviceScanActivity.class.getSimpleName();
    private static final String LOG_TAG = "ScanExample";
    private static final long SYNC_TIMEOUT_MS = 20000;
    private static final long SCAN_TIMEOUT_MS = 8000;
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<CAENState> stateResult = new MutableLiveData<>();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private final Handler autoHandler = new Handler(Looper.getMainLooper());
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver = null;
    // variable to hold the dialog. Only 1 instance of ILoggerDialog may be active...
    private ILoggerDialog loggerDlg = null;
    private MobileDB db;
    private IFishTrackRepository tempDataRepo, measRepo;
    private BinInfoRepository binInfoRepo;
    private ProgressDialog progressDialog;
    private RecyclerView lvTempProfiles;
    private TemperatureProfileAdapter tempProfileAdapter;
    private BinWeightCageAdapter adapterBins;
    private ConcatAdapter concatAdapter;
    private SingleShotScanner scanner_runnable;
    private BinInfo tmpBin;
    private TextView tvLotLabel, tvLot;
    private boolean scanAllBins = false;
    private List<String> scannedBinEPCs;
    private List<String> binList;
    private List<String> adapterBinList;
    private int attemptsToGetEpcList = 0;
    private int attemptsToScanBinOutOfLot = 0;
    private long retrievedAt;
    private Spinner spProductionLine;
    private ImageButton ibShowValues;
    private String loggerEPC, binEPC;
    private String fishT, waterT, fishT2;
    private ImageView ivSupport, ivBack, ivNext;
    private Button btnScanBin;
    private SupportDialog supportDialog;
    private String callingActivity;
    private Double surfaceTemp;
    private long mNextReadReverseIndex = KBRecordDataRsp.INVALID_DATA_RECORD_POS;
    private int mTotalReverseReadIndex = 0;
    private KBeaconsMgr mBeaconsMgr;
    private KBeacon mBeacon;
    private boolean isConnecting = false;
    private String defaultProdLane = "1";
    private volatile boolean isActive = false;
    private boolean isScanning = false;
    private boolean bleRetryAllowed = false;
    private volatile boolean userLeftScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_turnover);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(BinTurnoverActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // instantiate a set to hold scanned EPCS.it will be passed to adapter shich feeds the ListView.
        scannedBinEPCs = new ArrayList<String>();

        // instantiate a set to hold expected EPCS.it will be passed to adapter shich feeds the ListView.
        binList = new ArrayList<String>();
        adapterBinList = new ArrayList<String>();

        tmpBin = new BinInfo();

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            adapterBinList = bundle.getStringArrayList("adapterBinList") != null ? getIntent().getStringArrayListExtra("adapterBinList") : adapterBinList;
            scannedBinEPCs = bundle.getStringArrayList("scannedBinList") != null ? getIntent().getStringArrayListExtra("scannedBinList") : scannedBinEPCs;
            binList = bundle.getStringArrayList("expectedBinList") != null ? getIntent().getStringArrayListExtra("expectedBinList") : binList;
            callingActivity = bundle.getString("calling_activity") != null ? getIntent().getStringExtra("calling_activity") : null;
        }

        if (callingActivity != null && callingActivity.equalsIgnoreCase("BinTurnoverActivity")) {
            // Already initialized from previous activity
        } else {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            // sync Bin Info (complete BinLedger)

            progressDialog.setMessage(render("Retrieving Data"));
            safeShowProgress("...");

            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));
        }

        // dont allow user to use the activity without bluetooth required permissions since they wont be able to do anything important
        checkBluetoothPermitAllowed();

        //initialize data logger manager
        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(this);
        if (mBeaconsMgr == null) {
            CToast(this, "make sure the phone has support ble funtion", Toast.LENGTH_LONG);
            finish();
            return;
        }
        mBeaconsMgr.delegate = this;
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);
        mBeaconsMgr.setScanMinRssiFilter(-100);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinOverturn);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        this.tempDataRepo = new TemperatureDataRepository();
        this.measRepo = new MeasurementRepository();
        this.binInfoRepo = new BinInfoRepository();
        // get  references of the controls
        assignCtrlVars();

        tvLotLabel.setVisibility(View.INVISIBLE);
        tvLot.setVisibility(View.INVISIBLE);

        String[] lines = new String[]{"1", "2", "3", "4", "5", "6"};
        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.

        ArrayAdapter<String> linesAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item, lines) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position % 2 == 0) { // we're on an even row
                    view.setBackgroundColor(getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getColor(R.color.light_grey));
                }
                return view;
            }
        };
        linesAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spProductionLine.setAdapter(linesAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BinTurnoverActivity.this);
        tempProfileAdapter = new TemperatureProfileAdapter(this);
        tempProfileAdapter.notifyDataSetChanged();

        adapterBins = new BinWeightCageAdapter(this, new ArrayList<BinWeightCageAdapter.BinDetails>());
        adapterBins.setOnBinSelectedListener(this::onBinSelected);

        if (adapterBinList != null && adapterBinList.size() > 0) {
            binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
            adapterBins.notifyDataSetChanged();
            adapterBins.markReceived(scannedBinEPCs);
            btnScanBin.setText(R.string.scan_bin);
            scanAllBins = true;
        }

        // Create a new ConcatAdapter and pass created adapters in sequence we need to show.
        concatAdapter = new ConcatAdapter(tempProfileAdapter, adapterBins);
        // Attach adapter to recyclerView.
        lvTempProfiles.setAdapter(concatAdapter);
        lvTempProfiles.setLayoutManager(layoutManager);
        lvTempProfiles.setHasFixedSize(false);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(BinTurnoverActivity.this);
            supportDialog.showDialog();
        });

        //-----------------------------------------------------
        // observe for state object obtained by LoggerDialog...
        //-----------------------------------------------------
        syncResult.observe(this, rs -> {
            safeDismissProgress();
            // handle Successful operation from Logger.
            if (rs == null) {
                finish();
            }
        });
        // ------ Logger Observer -----------------------------

        configFooter();
    }

    private void releaseBeacon() {
        try {
            if (mBeacon != null) {
                mBeacon.disconnect();
            }
        } catch (Exception ignored) {
        }

        mBeacon = null;
        isConnecting = false;
        mNextReadReverseIndex = KBRecordDataRsp.INVALID_DATA_RECORD_POS;
        mTotalReverseReadIndex = 0;
    }

    private boolean allTempsDownloaded() {
        return adapterBins.getItemCount() == 0;
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

    private void safeShowProgress(String message) {
        try {
            if (!isActive || isFinishing() || isDestroyed() || progressDialog == null) return;
            progressDialog.setMessage(message);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing progress dialog: " + e.getMessage());
        }
    }

    private void safeDismissProgress() {
        try {
            if (progressDialog == null) return;
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing progress dialog: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_SCAN) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σάρωσης BLE για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }

        if (requestCode == PERMISSION_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σύνδεσης BLE για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }

        if (requestCode == PERMISSION_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια κατά προσέγγιση τοποθεσίας για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }
        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια ακριβούς τοποθεσίας για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }
    }

    private void moveToNextScreen() {
        Boolean proceed = updateState();

        if (proceed) {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            // move to next activity.
            if (scannedBinEPCs.size() < binList.size()) {
                i = new Intent(getApplicationContext(), BinTurnoverActivity.class);
                i.putStringArrayListExtra("adapterBinList", (ArrayList<String>) convertBinDetailsToEPCs(adapterBins.getValues()));
                i.putStringArrayListExtra("scannedBinList", (ArrayList<String>) scannedBinEPCs);
                i.putStringArrayListExtra("expectedBinList", (ArrayList<String>) binList);
                i.putExtra("calling_activity", "BinTurnoverActivity");
            }
            startActivity(i);
        }
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        lvTempProfiles = findViewById(R.id.lvTempProfiles);
        tvLotLabel = findViewById(R.id.tvLotLabel);
        tvLot = findViewById(R.id.tvLot);
        ibShowValues = findViewById(R.id.ibShowValues);
        spProductionLine = findViewById(R.id.spProductionLine);
        ivSupport = findViewById(R.id.ivSupport);
        ivBack = findViewById(R.id.ivBackToMenu);
    }

    private void forceStopAllOperations() {
        try {
            // stop RFID scanning
            stopScanner();

            // stop BLE scanning + disconnect
            stopBleCompletely();

            // cancel delayed callbacks
            mScanHandler.removeCallbacksAndMessages(null);
            autoHandler.removeCallbacksAndMessages(null);

            // close progress dialog
            safeDismissProgress();

            // reset flags
            isScanning = false;
            isConnecting = false;
            bleRetryAllowed = false;

            // hard release beacon
            releaseBeacon();

        } catch (Exception ignored) {
        }
    }

    protected void configFooter() {
        ivNext = findViewById(R.id.ivToCongs);
        ivNext.setVisibility(View.VISIBLE);
        ivNext.setOnClickListener(view -> {

            forceStopAllOperations();

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(this, render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }

            QualityStepsState.completed[0] = true;
            moveToNextScreen();
        });


        ivBack.setOnClickListener(view -> {

            userLeftScreen = true;
            forceStopAllOperations();

            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
            finish();
        });
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            safeShowProgress("...");

            String token = LocalPreferences.getToken();

            for (String epc : recLoggerData.data.keySet()) {
                db.binInfoDAO().updateBinInfoSetSorted(epc);
            }
            List<TemperatureTimeSeries> measurements = GlobalState.commitMeasurements(db, null);
            List<TemperatureTimeSeriesDTO> sortingTimeSeriesDTOs = new ArrayList<>();
            for (TemperatureTimeSeries ts : measurements) {
                sortingTimeSeriesDTOs.add(TemperatureTimeSeriesDTO.convert(ts));
            }

            if (IsOnline) {
                if (!sortingTimeSeriesDTOs.isEmpty()) {
                    Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall =
                            updService.syncMeasurements(sortingTimeSeriesDTOs, "Bearer " + token);
                    autoHandler.postDelayed(() -> {
                        try {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                syncMsAsyncCall.cancel();
                                safeDismissProgress();
                                CToast(this, "Sync timeout. Data saved locally.", Toast.LENGTH_LONG);
                                goNextAfterSync();
                            }
                        } catch (Exception ignored) {
                        }
                    }, SYNC_TIMEOUT_MS);

                    syncMsAsyncCall.enqueue(new SyncMsCallBack());
                    return false;

                } else {
                    safeDismissProgress();
                    CToast(this, "No temperature data to send", Toast.LENGTH_SHORT);
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() ->
                            CToast(this,
                                    render(R.string.tx_saved_local_find_network_and_sync),
                                    Toast.LENGTH_LONG)
                    );
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    private String validate() {
        return "";
    }

    public void registerKeyReceiver() {
        if (keyReceiver == null) {
            keyReceiver = new X9KeyReceiver(this::onClick);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        userLeftScreen = false;
    }

    @Override
    protected void onStop() {
        isActive = false;

        if (keyReceiver != null) {
            try {
                unregisterReceiver(keyReceiver);
            } catch (Exception ignored) {
            }
        }

        cleanupAll();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isActive = false;
        cleanupAll();
        super.onDestroy();
    }

    private void cleanupAll() {
        stopScanner();

        mScanHandler.removeCallbacksAndMessages(null);
        autoHandler.removeCallbacksAndMessages(null);

        scanAllBins = false;

        try {
            if (mBeaconsMgr != null) {
                mBeaconsMgr.stopScanning();
                mBeaconsMgr.clearBeacons();
                mBeaconsMgr.delegate = null;
            }
        } catch (Exception ignored) {
        }

        releaseBeacon();

        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                safeDismissProgress();
            }
        } catch (Exception ignored) {
        }
    }

    protected void onClick(View view) {
        if (!isActive || userLeftScreen) return;
        if (isScanning || isConnecting) return;

        isScanning = true;

        scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        try {
            if (this.scanner_runnable != null) {
                this.scanner_runnable.stopReading();
                mScanHandler.removeCallbacks(this.scanner_runnable);
            }
        } catch (Exception ignored) {
        }

        isScanning = false;
    }

    private BinWeightCageAdapter.BinDetails loadBinInfo(String epc) {
        //Add code to retrieve bin info from local DB
        tmpBin = db.binInfoDAO().getByRFId(epc);
        if (tmpBin != null) {
            return new BinWeightCageAdapter.BinDetails(epc, tmpBin.totalWeight, tmpBin.cage, tmpBin.sorted);
        } else {
            return new BinWeightCageAdapter.BinDetails(epc);
        }
    }

    private List<String> convertBinDetailsToEPCs(List<BinWeightCageAdapter.BinDetails> epcs) {
        List<String> result = new ArrayList<>();
        for (BinWeightCageAdapter.BinDetails epc : epcs) {
            result.add(epc.epc);
        }
        return result;
    }

    public void fillTemperatureProfileAdapter() {
        List<TempSample> values = recLoggerData.getValues(binEPC);

        if (values != null) {
            Map<String, LoggerDataRecord.TemperatureModel> data = recLoggerData.data;
            tempProfileAdapter.fill(data, tmpBin, surfaceTemp);
        } else {
            tempProfileAdapter.fill(tmpBin);
        }
    }

    private void confirmScanBinOutOfLotDialog() {
        // Get custom login form view.
        final View confirmFormView = this.getLayoutInflater().inflate(R.layout.confirm_scan_bin_out_of_lot_dlg, null);

        final EditText pin = confirmFormView.findViewById(R.id.etPin);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText(Html.fromHtml("<b>" + getAppContext().getResources().getString(R.string.confirm_scanned_bin_out_of_lot) + "</b>" + "<br>" + getAppContext().getResources().getString(R.string.confirm_with_pin), HtmlCompat.FROM_HTML_MODE_LEGACY));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(confirmFormView)
                .setCustomTitle(title)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String insertedPin = pin.getText().toString().trim();
                String login = LocalPreferences.getLoggedInUser("").trim();

                if (Strings.isEmptyOrWhitespace(insertedPin)) {
                    CToast(BinTurnoverActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }

                // use typed-in PIN to compare credentials with those stored in the Local DB.
                AuthenticationService authSvc = new AuthenticationService();
                boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                if (authentication) {
                    dialog.dismiss();
                } else {
                    CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                }
            });
        });
        dialog.show();
    }

    @SuppressLint("StringFormatMatches")
    private void triggerDataLoggerDialog() {
        if (!scannedBinEPCs.contains(binEPC)) {
            scannedBinEPCs.add(binEPC);
        }
        adapterBins.addUniqueItem(loadBinInfo(binEPC));
        adapterBins.markReceived(scannedBinEPCs);
        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {

            tmpBin = db.binInfoDAO().getByRFId(binEPC);


            if (tmpBin == null) {
                CToast(this,
                        getString(R.string.sync_and_check_if_correct_bin,
                                binEPC.substring(binEPC.length() - 4)),
                        Toast.LENGTH_LONG);
                return;
            }
            if (tmpBin.sorted) {
                CToast(this, render(R.string.already_scannned_bin), Toast.LENGTH_LONG);
                return;
            }


        }
    }

    private void startLoggerScan() {
        if (isConnecting || !bleRetryAllowed) return;

        safeShowProgress("Εύρεση καταγραφικού...");
        mBeaconsMgr.startScanning();

        autoHandler.postDelayed(() -> {
            if (!isConnecting) {
                stopBleCompletely();
                safeDismissProgress();
                CToast(this, "Logger not found. Tap the bin to retry.", Toast.LENGTH_LONG);
            }
        }, SCAN_TIMEOUT_MS);
    }


    private void onBinSelected(String selectedBinEpc) {
        binEPC = selectedBinEpc;

        Asset asset = db.assetDAO().getAssetByEpc(binEPC);
        if (asset == null || Strings.isEmptyOrWhitespace(asset.loggerEPC)) {
            CToast(this, "Bin not linked to logger", Toast.LENGTH_LONG);
            return;
        }

        loggerEPC = asset.loggerEPC;
        startLoggerScan();
    }


    @Override
    public void onConnStateChange(KBeacon beacon, KBConnState state, int nReason) {
        if (!isActive || userLeftScreen) return;

        if (state == KBConnState.Disconnected) {
            stopBleCompletely();
            safeDismissProgress();

            if (!userLeftScreen) {
                CToast(this, "Logger disconnected", Toast.LENGTH_LONG);
            }
            return;
        }

        if (state == KBConnState.Connected) {
            if (userLeftScreen) return;

            Log.v(LOG_TAG, "device has connected");

            mBeacon.readSensorRecord(
                    KBSensorType.HTHumidity,
                    mNextReadReverseIndex,
                    KBSensorReadOption.ReverseOrder,
                    60,
                    (bSuccess, dataRsp, error) -> {

                        if (!isActive || userLeftScreen) return;

                        if (bSuccess) {
                            Long fishingTime = EncodingUtils.normalizeEpochTime(tmpBin.pickedAt);
                            List<TempSample> samples = new LinkedList<>();

                            for (KBRecordBase sensorRecord : dataRsp.readDataRspList) {
                                if (!isActive || userLeftScreen) return;

                                KBRecordHumidity record = (KBRecordHumidity) sensorRecord;
                                boolean isAfterFishing = fishingTime == null || record.utcTime > fishingTime;

                                samples.add(new TempSample(
                                        createTimestamp(record.utcTime * 1000),
                                        String.format("%.2f", record.temperature),
                                        isAfterFishing
                                ));
                            }

                            if (!CollectionUtils.isEmpty(samples)) {
                                Collections.reverse(samples);
                                retrievedAt = System.currentTimeMillis();

                                recLoggerData.addDataSet(loggerEPC, binEPC, defaultProdLane, retrievedAt, samples);
                                GlobalState.commitMeasurement(
                                        MobileDB.getInstance(getAppContext()),
                                        binEPC,
                                        defaultProdLane
                                );

                                if (!userLeftScreen) {
                                    fillTemperatureProfileAdapter();
                                    adapterBins.removeItem(binEPC);
                                    adapterBins.notifyDataSetChanged();
                                    bleRetryAllowed = adapterBins.getItemCount() > 0;
                                }

                                safeDismissProgress();
                                releaseBeacon();
                                return;
                            } else {
                                recLoggerData.addDataSet(loggerEPC, binEPC, defaultProdLane, System.currentTimeMillis(), null);
                                GlobalState.commitMeasurement(MobileDB.getInstance(getAppContext()), binEPC, defaultProdLane);

                                if (!userLeftScreen) {
                                    fillTemperatureProfileAdapter();
                                }
                            }
                        }

                        safeDismissProgress();
                    }
            );
        }
    }


    @Override
    public void onNotifyDataReceived(KBeacon beacon, int nEventType, byte[] sensorData) {

    }

    private void goNextAfterSync() {
        if (!didNavigate.compareAndSet(false, true)) return;

        runOnUiThread(() -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);

            if (scannedBinEPCs.size() < binList.size()) {
                i = new Intent(getApplicationContext(), BinTurnoverActivity.class);
                i.putStringArrayListExtra("adapterBinList", (ArrayList<String>) convertBinDetailsToEPCs(adapterBins.getValues()));
                i.putStringArrayListExtra("scannedBinList", (ArrayList<String>) scannedBinEPCs);
                i.putStringArrayListExtra("expectedBinList", (ArrayList<String>) binList);
                i.putExtra("calling_activity", "BinTurnoverActivity");
            }

            startActivity(i);
            finish();
        });
    }


    @Override
    public void onBeaconDiscovered(KBeacon[] beacons) {
        if (!isActive || userLeftScreen || isConnecting || !bleRetryAllowed) return;

        for (KBeacon beacon : beacons) {
            if (loggerEPC != null &&
                    beacon.getMac() != null &&
                    beacon.getMac().equalsIgnoreCase(loggerEPC)) {

                isScanning = false;
                isConnecting = true;

                mBeaconsMgr.stopScanning();
                mBeacon = beacon;

                if (userLeftScreen) return;

                mBeacon.connect(LocalPreferences.getLoggerPassword(), 20000, this);
                return;
            }
        }
    }


    @Override
    public void onCentralBleStateChang(int nNewState) {

    }

    private void stopBleCompletely() {
        isScanning = false;
        isConnecting = false;

        try {
            if (mBeaconsMgr != null) {
                mBeaconsMgr.stopScanning();
            }
        } catch (Exception ignored) {
        }

        releaseBeacon();
    }


    @Override
    public void onScanFailed(int errorCode) {
        if (!isActive || userLeftScreen) return;

        stopBleCompletely();
        safeDismissProgress();

        CToast(this, "Scan failed. Press Scan to retry.", Toast.LENGTH_LONG);
    }


    private class ScanHandler extends Handler {
        private final WeakReference<BinTurnoverActivity> mActivity;

        public ScanHandler(BinTurnoverActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            BinTurnoverActivity a = mActivity.get();
            if (a == null || !a.isActive) return;
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    if (scanAllBins) {
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                if (tempProfileAdapter.getItemCount() > 0) {
                                    CToast(a, render(getString(R.string.already_scannned_bin_first_sync_temps, tempProfileAdapter.getEpc().substring(tempProfileAdapter.getEpc().length() - 10))), Toast.LENGTH_LONG);
                                    return;
                                }

                                if (!adapterBins.contains(epcStr)) {
                                    adapterBins.addExpectedItem(loadBinInfo(epcStr));
                                    scannedBinEPCs.add(epcStr);
                                    adapterBins.notifyDataSetChanged();
                                    bleRetryAllowed = true;
                                }


                            } else {
                                CToast(a, render(R.string.scan_bin_again), Toast.LENGTH_SHORT);
                            }
                            this.removeCallbacks(scanner_runnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                List<BinInfo> binInfoList = db.binInfoDAO().getEPCListByRFId(epcStr);
                                String lot = null;


                                ///////
                                for (BinInfo bin : binInfoList) {
                                    binList.add(bin.rfid);
                                    lot = !Strings.isEmptyOrWhitespace(bin.lot) ? bin.lot : lot;
                                }
                                if (binList == null || binList.isEmpty()) {
                                    while (attemptsToGetEpcList < 3) {
                                        attemptsToGetEpcList++;
                                        return;
                                    }
                                    attemptsToGetEpcList = 0;
                                    btnScanBin.setText(R.string.scan_bin);
                                    scanAllBins = true;
                                    return;
                                }
                                binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
                                adapterBins.notifyDataSetChanged();
                                btnScanBin.setText(R.string.scan_bin);
                                bleRetryAllowed = true;
                                scanAllBins = true;

                                if (!Strings.isEmptyOrWhitespace(lot)) {
                                    tvLotLabel.setVisibility(View.VISIBLE);
                                    tvLot.setVisibility(View.VISIBLE);
                                    tvLot.setText(lot);
                                }

                                /* ============ REMOVED AUTO-PROCESSING CODE ============ */
                                // User will now manually scan each bin they want to download

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1980:
                    this.removeCallbacks(scanner_runnable);
                    break;
            }
        }
    }

    public class SyncMsCallBack implements Callback<List<TemperatureTimeSeriesDTO>> {
        @Override
        public void onResponse(Call<List<TemperatureTimeSeriesDTO>> call,
                               Response<List<TemperatureTimeSeriesDTO>> response) {
            if (!isActive || isFinishing() || isDestroyed()) return;
            safeDismissProgress();
            releaseBeacon();

            ivBack.setVisibility(View.VISIBLE);

            if (response.isSuccessful()) {
                recLoggerData.clearData();
                tempDataRepo.removeAll(db);
                db.measurementsDAO().deleteAll();
                measRepo.removeAll(db);

                runOnUiThread(() ->
                        CToast(BinTurnoverActivity.this,
                                render(R.string.tx_successfully_updated),
                                Toast.LENGTH_SHORT)
                );
            } else {
                runOnUiThread(() ->
                        CToast(BinTurnoverActivity.this,
                                render(R.string.error_temperatures_tx_update_failure),
                                Toast.LENGTH_LONG)
                );
            }

            goNextAfterSync();
        }

        @Override
        public void onFailure(Call<List<TemperatureTimeSeriesDTO>> call, Throwable error) {
            if (!isActive || isFinishing() || isDestroyed()) return;

            safeDismissProgress();
            releaseBeacon();

            ivBack.setVisibility(View.VISIBLE);
            ivNext.setVisibility(View.VISIBLE);

            runOnUiThread(() -> {
                if (error instanceof SocketTimeoutException) {
                    CToast(BinTurnoverActivity.this,
                            render(R.string.error_connection_timeout),
                            Toast.LENGTH_LONG);
                } else if (error instanceof IOException) {
                    CToast(BinTurnoverActivity.this,
                            render(R.string.tx_saved_local_find_network_and_sync),
                            Toast.LENGTH_LONG);
                } else if (call.isCanceled()) {
                    CToast(BinTurnoverActivity.this,
                            render(R.string.error_cancelled_call),
                            Toast.LENGTH_LONG);
                } else {
                    CToast(BinTurnoverActivity.this,
                            render(R.string.general_error + error.getLocalizedMessage()),
                            Toast.LENGTH_LONG);
                }

                goNextAfterSync();
            });
        }

    }
}