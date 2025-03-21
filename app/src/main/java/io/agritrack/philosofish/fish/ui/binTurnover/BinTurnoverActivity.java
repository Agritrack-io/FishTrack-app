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
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

public class BinTurnoverActivity extends AppCompatActivity implements  KBeaconsMgr.KBeaconMgrDelegate, KBeacon.ConnStateDelegate, KBeacon.NotifyDataDelegate {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private final MutableLiveData<CAENState> stateResult = new MutableLiveData<>();

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();

    // variable to hold the dialog. Only 1 instance of ILoggerDialog may be active...
    private ILoggerDialog loggerDlg = null;

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver = null;
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

    private static final int PERMISSION_COARSE_LOCATION = 22;
    private static final int PERMISSION_FINE_LOCATION = 23;
    private static final int PERMISSION_SCAN = 24;
    private static final int PERMISSION_CONNECT = 25;
    private final static String TAG = "Beacon.ScanAct";//DeviceScanActivity.class.getSimpleName();
    private static final String LOG_TAG = "ScanExample";
    private int mRssiFilterValue = -40;
    private int mScanFailedContinueNum = 0;
    private final static int  MAX_ERROR_SCAN_NUMBER = 2;
    private HashMap<String, KBeacon> mBeaconsDictory;
    private KBeacon[] mBeaconsArray;
    private KBeaconsMgr mBeaconsMgr;
    private KBeacon mBeacon;
    private boolean isScanning = false;
    private int findBeaconAttempts = 0;

    private String defaultProdLane = "1";

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

        } else {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            // sync Bin Info (complete BinLedger)

            ComponentName callActivity = getCallingActivity();

            progressDialog.setMessage(render("Retrieving Data"));
            progressDialog.show();

            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));
        }
        // dont allow user to use the activity without bluetooth required permissions since they wont be able to do anything important
        checkBluetoothPermitAllowed();

        //initialize data logger manager
        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(this);
        if (mBeaconsMgr == null)
        {
            CToast(getAppContext(),"make sure the phone has support ble funtion", Toast.LENGTH_LONG);
            finish();
            return;
        }
        mBeaconsMgr.delegate =  this;
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);

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
            progressDialog.dismiss();
            // handle Successful operation from Logger.
            if (rs == null ) {
                //CToast(getAppContext(), "Αποτυχία σύνδεσης, ελέγξτε τη σύνδεσή σας ή επικοινωνήστε με την υποστήριξη", Toast.LENGTH_LONG);
                finish();
            }
        });
        // ------ Logger Observer -----------------------------

        configFooter();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_SCAN){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σάρωσης BLE για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }

        if (requestCode == PERMISSION_CONNECT){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σύνδεσης BLE για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }

        if (requestCode == PERMISSION_COARSE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια κατά προσέγγιση τοποθεσίας για να προχωρήσει με τη διαδικασία", Toast.LENGTH_LONG);
                finish();
            }
        }
        if (requestCode == PERMISSION_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
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

    protected void configFooter() {
        ivNext = findViewById(R.id.ivToCongs);
        ivNext.setVisibility(View.INVISIBLE);
        ivNext.setOnClickListener(view -> {
            stopScanner();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                moveToNextScreen();
            }
        });

        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            db.binInfoDAO().updateBinInfoSetSorted(binEPC);

            BinInfo bin = db.binInfoDAO().getByRFId(binEPC);

            // persist Measurements Record data to local DB.
            List<TemperatureTimeSeries> measurements = GlobalState.commitMeasurements(db, null);
            List<TemperatureTimeSeriesDTO> sortingTimeSeriesDTOs = new ArrayList<>();
            for (TemperatureTimeSeries ts : measurements) {
                sortingTimeSeriesDTOs.add(TemperatureTimeSeriesDTO.convert(ts));
            }

            if (IsOnline) {
                // sync Measurements records
                if (!sortingTimeSeriesDTOs.isEmpty()) {
                    Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall = updService.syncMeasurements(sortingTimeSeriesDTOs, "Bearer " + token);
                    syncMsAsyncCall.enqueue(new BinTurnoverActivity.SyncMsCallBack());
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (binEPC == null) {
                sb.append(String.format("\n%s is missing", "'Bin to turnover'"));
            }
        }
        return sb.toString();
    }

    public void registerKeyReceiver() {
        if (keyReceiver == null){
            keyReceiver = new X9KeyReceiver(this::onClick);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        registerKeyReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null) {
            unregisterReceiver(keyReceiver);
            keyReceiver = null;
        }

        if (mBeacon != null && (mBeacon.getState() == KBConnState.Connected
                || mBeacon.getState() == KBConnState.Connecting)){
            mBeacon.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopScanner();
    }

    protected void onClick(View view) {
        if (progressDialog.isShowing()) {
            CToast(getApplicationContext(), render(R.string.temp_downloading_in_progress), Toast.LENGTH_SHORT);
            return;
        }

        scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);

    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
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

        adapterBins.removeItem(tmpBin.rfid);
        adapterBins.notifyDataSetChanged();
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
                    CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }

                // use typed-in PIN to compare credentials with those stored in the Local DB.
                AuthenticationService authSvc = new AuthenticationService();
                boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                if (authentication) {
                    triggerDataLoggerDialog();
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
                CToast(getApplicationContext(), getString(R.string.sync_and_check_if_correct_bin, binEPC.substring(binEPC.length()-4)), Toast.LENGTH_LONG);
                return;
            }
            if (tmpBin.sorted) {
                CToast(getApplicationContext(), render(R.string.already_scannned_bin), Toast.LENGTH_LONG);
                return;
            }

            int nStartScan = mBeaconsMgr.startScanning();

            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Εύρεση καταγραφικού..."));
            progressDialog.show();
            if (nStartScan == 0)
            {
                Log.v(TAG, "start scan success");
            }
            else if (nStartScan == KBeaconsMgr.SCAN_ERROR_BLE_NOT_ENABLE)
            {
                CToast(getAppContext(),"Το Bluetooth δεν είναι ενεργοποιημένο", Toast.LENGTH_LONG);
            }
            else if (nStartScan == KBeaconsMgr.SCAN_ERROR_UNKNOWN)
            {
                CToast(getAppContext(),"Παρακαλώ επιβεβαιώστε ότι η εφαρμογή έχει πρόσβαση στο Bluetooth", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onConnStateChange(KBeacon beacon, KBConnState state, int nReason) {
        if (state == KBConnState.Connected) {
            Log.v(LOG_TAG, "device has connected");

                mBeacon.readSensorRecord(KBSensorType.HTHumidity,
                        mNextReadReverseIndex, //read from last pos
                        KBSensorReadOption.ReverseOrder,  //read direction type
                        100,   //number of records the app want to read
                        (bSuccess, dataRsp, error) -> {
                            if (bSuccess)
                            {
                                Long fishingTime = EncodingUtils.normalizeEpochTime(tmpBin.pickedAt);
                                List<TempSample> samples = new LinkedList<>();
                                //mNextReadReverseIndex = dataRsp.readDataNextPos;
                                for (KBRecordBase sensorRecord: dataRsp.readDataRspList)
                                {
                                    boolean isAfterFishing;
                                    KBRecordHumidity record = (KBRecordHumidity)sensorRecord;
                                    Log.v(LOG_TAG, mTotalReverseReadIndex
                                            +": utc time:" + record.utcTime
                                            + ",temperature:" + record.temperature
                                            + ",humidity:" + record.humidity);
                                    mTotalReverseReadIndex++;

                                    if (fishingTime == null) {
                                        isAfterFishing = true;
                                    } else {
                                        if (record.utcTime > fishingTime) {
                                            isAfterFishing = true;
                                        } else {
                                            isAfterFishing = false;
                                        }
                                    }
                                    samples.add(new TempSample(createTimestamp(record.utcTime * 1000), String.format("%.2f", record.temperature), isAfterFishing));
                                }
                                if (dataRsp.readDataNextPos == KBRecordDataRsp.INVALID_DATA_RECORD_POS)
                                {
                                    Log.v(LOG_TAG, "Read data complete");
                                }
                                else
                                {
                                    Log.v(LOG_TAG, "next read position:" + dataRsp.readDataNextPos);
                                }
                                if (!CollectionUtils.isEmpty(samples)) {
                                    //put them in chronological order
                                    Collections.reverse(samples);
                                    retrievedAt = System.currentTimeMillis();
                                    recLoggerData.addDataSet(loggerEPC, binEPC, defaultProdLane, retrievedAt, samples);
                                    GlobalState.commitMeasurement(MobileDB.getInstance(getAppContext()), binEPC, defaultProdLane);
                                    fillTemperatureProfileAdapter();
                                    ivBack.setVisibility(View.INVISIBLE);
                                    ivNext.setVisibility(View.VISIBLE);
                                } else {
                                    recLoggerData.addDataSet(loggerEPC, binEPC, defaultProdLane, System.currentTimeMillis(), null);
                                    GlobalState.commitMeasurement(MobileDB.getInstance(getAppContext()), binEPC, defaultProdLane);
                                    fillTemperatureProfileAdapter();
                                }
                            }
                            progressDialog.dismiss();
                        });
        }
    }

    @Override
    public void onNotifyDataReceived(KBeacon beacon, int nEventType, byte[] sensorData) {

    }

    @Override
    public void onBeaconDiscovered(KBeacon[] beacons) {
        if (beacons == null || beacons.length == 0) {
            return; // No beacons found, exit early
        }
        KBeacon beaconFound = null; // Initialize as null

        for (KBeacon beacon : beacons) {
            if (beacon.getMac().equals(loggerEPC)) {
                beaconFound = beacon; // Store the found beacon
                //KBSensor info
                try {
                    KBAdvPacketSensor kSensor = (KBAdvPacketSensor) beaconFound.getAdvPacketByType(KBAdvType.Sensor);
                    surfaceTemp = Double.valueOf(kSensor.getTemperature());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break; // Exit loop once found
            }
        }

        // Check if a beacon was found
        if (beaconFound == null) {
            // No matching beacon found
            findBeaconAttempts++;
            if (findBeaconAttempts > 5) {
                findBeaconAttempts = 0;
                mBeaconsMgr.stopScanning();
                progressDialog.dismiss();
                CToast(getAppContext(), "Το καταγραφικό δεν βρέθηκε, βεβαιωθείτε ότι βρίσκεται μέσα στη βούτα",Toast.LENGTH_LONG);
                return;
            }
            return;
        }
        findBeaconAttempts = 0;
        mBeaconsMgr.stopScanning();
        progressDialog.setMessage(render("Σύνδεση με καταγραφικό"));

        //connect to specific ble sensor
        mBeacon = mBeaconsMgr.getBeacon(loggerEPC);
        mBeacon.connect(LocalPreferences.getLoggerPassword(),
                20 * 1000,
                this);
    }

    @Override
    public void onCentralBleStateChang(int nNewState) {

    }

    @Override
    public void onScanFailed(int errorCode) {
        if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER){
            CToast(getAppContext(),"Scan encountered error, error time:" + mScanFailedContinueNum, Toast.LENGTH_SHORT);
            progressDialog.dismiss();
            mBeaconsMgr.stopScanning();
        }
        mScanFailedContinueNum++;

    }

    private class ScanHandler extends Handler {
        private final WeakReference<BinTurnoverActivity> mActivity;

        public ScanHandler(BinTurnoverActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    if (scanAllBins) {
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                if (tempProfileAdapter.getItemCount() > 0) {
                                    CToast(getApplicationContext(), render(getString(R.string.already_scannned_bin_first_sync_temps, tempProfileAdapter.getEpc().substring(tempProfileAdapter.getEpc().length() - 10))), Toast.LENGTH_LONG);
                                    return;
                                }
                                //loggerEPC = epcStr;
                                binEPC = epcStr;
                                //new code here
                                //#########################
                                Asset bin = db.assetDAO().getAssetByEpc(binEPC);
//                                bin = new Asset();
//                                bin.rfid = epcStr;
//                                bin.loggerEPC = "BC:57:29:13:FF:CD";
                                if (bin == null) {
                                    CToast(getApplicationContext(), render(getString(R.string.epc_not_correlated_to_bin, binEPC.substring(binEPC.length() - 10))), Toast.LENGTH_LONG);
                                    return;
                                } else {
                                    loggerEPC = bin.loggerEPC;
                                    if (Strings.isEmptyOrWhitespace(bin.loggerEPC)) {
                                        CToast(getApplicationContext(), render(getString(R.string.bin_not_correlated_to_logger, bin.code ,binEPC.substring(binEPC.length() - 10))), Toast.LENGTH_LONG);
                                        return;
                                    }

                                    if (!binList.contains(binEPC)) {
                                        while (attemptsToScanBinOutOfLot < 1) {
                                            attemptsToScanBinOutOfLot++;
                                            CToast(getApplicationContext(), render(R.string.scanned_bin_out_of_lot), Toast.LENGTH_LONG);
                                            return;
                                        }
                                        confirmScanBinOutOfLotDialog();
                                        adapterBins.markReceived(Collections.singletonList(binEPC));
                                        attemptsToScanBinOutOfLot = 0;
                                        return;
                                    }
                                    triggerDataLoggerDialog();
                                    return;


                                }
                                //########################
                            } else {
                                CToast(getApplicationContext(), render(R.string.scan_bin_again), Toast.LENGTH_SHORT);
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

                                for (BinInfo bin : binInfoList) {
                                    binList.add(bin.rfid);
                                    lot = !Strings.isEmptyOrWhitespace(bin.lot) ? bin.lot : lot;
                                }
                                if (binList == null || binList.isEmpty()) {
                                    while (attemptsToGetEpcList < 3) {
                                        attemptsToGetEpcList++;
                                        CToast(getApplicationContext(), render(getString(R.string.no_epc_list_returned)), Toast.LENGTH_LONG);
                                        return;
                                    }
                                    attemptsToGetEpcList = 0;
                                    CToast(getApplicationContext(), render(getString(R.string.scan_all_bins)), Toast.LENGTH_LONG);
                                    btnScanBin.setText(R.string.scan_bin);
                                    scanAllBins = true;
                                    return;
                                }
                                binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
                                adapterBins.notifyDataSetChanged();
                                btnScanBin.setText(R.string.scan_bin);
                                scanAllBins = true;
                                if (!Strings.isEmptyOrWhitespace(lot)) {
                                    tvLotLabel.setVisibility(View.VISIBLE);
                                    tvLot.setVisibility(View.VISIBLE);
                                    tvLot.setText(lot);
                                }
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
        public void onResponse(Call<List<TemperatureTimeSeriesDTO>> call, Response<List<TemperatureTimeSeriesDTO>> response) {
            List<TemperatureTimeSeriesDTO> rs = response.body();
            ivBack.setVisibility(View.VISIBLE);
            ivNext.setVisibility(View.INVISIBLE);

            if (rs != null || !IsDemo) {
                // reset existing Temperature values in stateRecord.
                recLoggerData.clearData();
                tempDataRepo.removeAll(db);
                db.measurementsDAO().deleteAll();
                measRepo.removeAll(db);
               // binInfoRepo.removeOneBin(db, tmpBin);
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_temperatures_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<TemperatureTimeSeriesDTO>> call, Throwable error) {
            ivBack.setVisibility(View.VISIBLE);
            ivNext.setVisibility(View.INVISIBLE);
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }
}