package io.agritrack.philosofish.fish.ui.wh.correlation;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBCfgBase;
import com.kkmcn.kbeaconlib2.KBCfgPackage.KBCfgCommon;
import com.kkmcn.kbeaconlib2.KBConnState;
import com.kkmcn.kbeaconlib2.KBErrorCode;
import com.kkmcn.kbeaconlib2.KBException;
import com.kkmcn.kbeaconlib2.KBeacon;
import com.kkmcn.kbeaconlib2.KBeaconsMgr;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import common.Assert;
import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.common.Constants;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.crypto.Crypto;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.CorrelationTxDTO;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;
import io.agritrack.philosofish.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.ui.LocationAwareActivity;
import io.agritrack.philosofish.ui.adapter.FilterableAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrelationBinActivity extends LocationAwareActivity implements KBeaconsMgr.KBeaconMgrDelegate, KBeacon.ConnStateDelegate, KBeacon.NotifyDataDelegate{

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private Button btnScanAssetTag, btnClearEpcs;
    private TextView tvCorrBinBarcode, tvCorrTempLoggerBarcode;
    private FilterableAdapter adapterAssets;
    private SearchView svSearchAsset;
    private RecyclerView rvBins;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg, confirmBinRfidDlg, confirmRfidReplacement;;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;
    private String epcStr, label, binCode, loggerMac;

    private ProgressBar progressBar;

    private Boolean scanBLE = false;
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

    private Boolean isScanning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_bin);

        scanBLE = false;

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        svSearchAsset.setIconifiedByDefault(false);

        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(this);
        if (mBeaconsMgr == null)
        {
            CToast(getAppContext(),"make sure the phone has support ble funtion", Toast.LENGTH_LONG);
            finish();
            return;
        }
        mBeaconsMgr.delegate =  this;
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);

        confirmRfidReplacement = YesNoDialogFragment.instance(); //dialog in the case where a specific net is already corr
        //confirmRfidReplacement.setMessage(getText(R.string.proceed_with_replacement));
        confirmRfidReplacement.onConfirm(bundle -> {
            GlobalState.recWHCorrelation.assetRFID = epcStr;
            runOnUiThread(() -> tvCorrBinBarcode.setText(label));
            switchToBLEScan();
        });
        confirmRfidReplacement.onReject(bundle -> {
            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
        });


        confirmBinRfidDlg = YesNoDialogFragment.instance();
        confirmBinRfidDlg.onConfirm(bundle -> {
            showConfirmDialog();
        });
        confirmBinRfidDlg.onReject(bundle -> {
            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
        });

        confirmGPSSelectionDlg = YesNoDialogFragment.instance();
        confirmGPSSelectionDlg.setMessage(getText(R.string.procced_without_location));
        confirmGPSSelectionDlg.onConfirm(bundle -> {
            proceedWithoutLocation = true;
            moveToNextScreen();
        });
        confirmGPSSelectionDlg.onReject(bundle -> {
            mLastLocation = findLocation();
            proceedWithoutLocation = false;
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationBinActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        loadBinsFromLocalDB(Constants.ftBin);

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationBinActivity.this);
            supportDialog.showDialog();
        });

        btnClearEpcs.setOnClickListener(view -> {
            clearAllScanned();
        });

        configFooter();
    }

    private void clearAllScanned() {
        tvCorrBinBarcode.setText(null);
        tvCorrTempLoggerBarcode.setText(null);
        btnClearEpcs.setTextColor(Color.DKGRAY);
        btnClearEpcs.setEnabled(false);
        recWHCorrelation.assetRFID = null;
        progressBar.setVisibility(View.GONE);
        recWHCorrelation.rfid = null;
        btnScanAssetTag.setText(getString(R.string.scan_tag));
        scanBLE = false;
        isScanning = false;
    }

    @SuppressLint("StringFormatMatches")
    private void showConfirmDialog() {
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle(getString(R.string.confirm_with_pin))
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean wantToCloseDialog;
                        String pin = input.getText().toString();
                        String userPin = null;
                        try {
                            userPin = Crypto.decodeAndDecrypt(db.userDAO().getByUsername(LocalPreferences.getLoggedInUser(null)).pin);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }

                        if (pin.equalsIgnoreCase(userPin)) {
                            input.getShowSoftInputOnFocus();
                            recWHCorrelation.assetRFID = epcStr;
                            runOnUiThread(() -> tvCorrBinBarcode.setText(label));
                            wantToCloseDialog = true;
                            switchToBLEScan();
                        } else {
                            dialog.setTitle(getString(R.string.invalid_password));
                            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog) {
                            dialog.dismiss();
                        } else {
                            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
                        }

                        input.setText("");

                        /*//Dismiss once everything is OK.
                        dialog.dismiss();*/
                    }
                });
            }
        });
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {

            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Connecting to Logger..."));
            progressDialog.show();
            isScanning = true;

            //connect to specific ble sensor
            mBeacon = mBeaconsMgr.getBeacon(loggerMac);
            mBeacon.connect(LocalPreferences.getLoggerPassword(),
                    20 * 1000,
                    this);


        }
    }

    private void loadBinsFromLocalDB(String assetType) {
        // load assets for current Site and filter by asset type (if selected).
        this.rvBins.setAdapter(null);
        this.rvBins.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<Asset> assetsList = db.assetDAO().getAssetsForType(assetType.toUpperCase(Locale.ROOT));
        Asset testBin = new Asset();
        assetsList.add(testBin);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid, x.code, x.netEyeGirth, x.perimeter)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            adapterAssets.notifyDataSetChanged();
            this.rvBins.setAdapter(adapterAssets);
        }
    }

    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
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
        stopScanner();
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);

        if (mBeacon != null && (mBeacon.getState() == KBConnState.Connected
                || mBeacon.getState() == KBConnState.Connecting)){
            mBeacon.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanner();
    }

    protected void configFooter() {
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            //i.putExtra("uid", 2);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();
            GlobalState.recWHCorrelation.assetType = Constants.ftBin;
            GlobalState.recWHCorrelation.assetCode = adapterAssets.getSelectedValue();
            GlobalState.recWHCorrelation.type = Constants.ftDataLogger;

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
                return;
            }
            if (mLastLocation != null) {
                recWHCorrelation.longitude = mLastLocation.getLongitude();
                recWHCorrelation.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });
    }

    private void assignCtrlVars() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvBins = findViewById(R.id.rvBins);
        tvCorrBinBarcode = findViewById(R.id.tvCorrBinBarcode);
        tvCorrTempLoggerBarcode = findViewById(R.id.tvCorrTempLoggerBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        btnClearEpcs = findViewById(R.id.btnClearEPCs);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);

        rvBins.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvBins.setItemAnimator(new DefaultItemAnimator());

        svSearchAsset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAssets.getFilter().filter(newText);
                return false;
            }
        });
        svSearchAsset.setOnClickListener(view -> {
            int kk = 0;
        });
    }

    private boolean correlate() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {


            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);


            // sync WH Correlation Tx
            ArrayList<CorrelationTxDTO> dtos = new ArrayList<>();
            dtos.add(CorrelationTxDTO.convert(tx));
            Call<ResponseBody> syncTxAsyncCall = updService.syncAssetWithAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new CorrelationBinActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);

            return false;
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetCode)) {
                sb.append(String.format("\n%s είναι άδειο", "'Κωδικός Βούτας'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetRFID)) {
                sb.append(String.format("\n%s είναι άδειο", "'Ετικέτα'"));
            }
            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s είναι άδειο", "'Καταγραφικό'"));
            }
        }
        return sb.toString();
    }

    private boolean deleteCorrelationTx() {
        try {
            System.out.println("About to delete correlate tx");
            CorrelationTransaction delObj = new CorrelationTransaction();
            delObj.id = recWHCorrelation.txKey;
            db.correlationTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    protected void onClick(View view) {
        //if bluetooth is scanning dont allow further clicks, it will cuase faulty behavior
        if (isScanning) {
            return;
        }

        if (!scanBLE) {
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            handleStartScan();
        }
    }

    private void handleStartScan(){

        isScanning = true;

        mBeaconsMgr.setScanMinRssiFilter(mRssiFilterValue);
        if (!checkBluetoothPermitAllowed())
        {
            return;
        }

        int nStartScan = mBeaconsMgr.startScanning();
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
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σάρωσης BLE για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);

            }
        }

        if (requestCode == PERMISSION_CONNECT){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια σύνδεσης BLE για την εύρεση BLE", Toast.LENGTH_LONG);

            }
        }

        if (requestCode == PERMISSION_COARSE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια κατά προσέγγιση τοποθεσίας για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);
            }
        }
        if (requestCode == PERMISSION_FINE_LOCATION){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                CToast(getAppContext(), "Η εφαρμογή χρειάζεται άδεια ακριβούς τοποθεσίας για να ξεκινήσει τη σάρωση BLE", Toast.LENGTH_LONG);
            }
        }
    }

    public void onBeaconDiscovered(KBeacon[] beacons)
    {

        if (beacons == null || beacons.length == 0) {
            return; // No beacons found, exit early
        }

        KBeacon strongestBeacon = null;
        int maxRssi = Integer.MIN_VALUE; // Start with the lowest possible RSSI

        for (KBeacon beacon : beacons) {
            if (beacon != null && beacon.getRssi() > maxRssi) {
                maxRssi = beacon.getRssi();
                strongestBeacon = beacon;
            }
        }

        // If a valid strongest beacon was found, process it
        if (strongestBeacon != null) {
            Log.d("BeaconDiscovery", "Strongest Beacon: " + strongestBeacon.getMac() +
                    " RSSI: " + maxRssi);
            tvCorrTempLoggerBarcode.setText(strongestBeacon.getName());
            recWHCorrelation.rfid = strongestBeacon.getMac();
            loggerMac = strongestBeacon.getMac();
            boolean loggerIsCorrelated = db.assetDAO().getAssetByLoggerEpc(strongestBeacon.getMac()) != null;
            if (loggerIsCorrelated) {
                CToast(getAppContext(), render(R.string.logger_already_correlated), Toast.LENGTH_LONG);
            }
        }

        mBeaconsMgr.stopScanning();
        progressBar.setVisibility(View.GONE);
        isScanning = false;
    }

    public void onCentralBleStateChang(int nNewState)
    {
        Log.e(TAG, "centralBleStateChang：" + nNewState);
    }

    public void onScanFailed(int errorCode)
    {
        if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER){
            CToast(getAppContext(),"Scan encountered error, error time:" + mScanFailedContinueNum, Toast.LENGTH_SHORT);
            isScanning = false;

        }
        mScanFailedContinueNum++;
    }

    protected void switchToBLEScan() {
        btnClearEpcs.setTextColor((getColor(R.color.yellow)));
        btnClearEpcs.setEnabled(true);
        btnScanAssetTag.setEnabled(true);
        btnScanAssetTag.setText(getString(R.string.scan_logger));
        scanBLE = true;
    }

    @Override
    public void onConnStateChange(KBeacon beacon, KBConnState state, int nReason) {
        if (state == KBConnState.Connected) {
            Log.v(LOG_TAG, "device has connected");
            //change parameters
            KBCfgCommon newCommomCfg = new KBCfgCommon();

            //set device name
            newCommomCfg.setName(tvCorrBinBarcode.getText().toString());

            ArrayList<KBCfgBase> cfgList = new ArrayList<>(1);
            cfgList.add(newCommomCfg);
            mBeacon.modifyConfig(cfgList, new KBeacon.ActionCallback() {
                @Override
                public void onActionComplete(boolean bConfigSuccess, KBException error) {
                    if (bConfigSuccess) {

                        Log.e(LOG_TAG, "Logger Successfully Renamed");

                        //toastShow("config data to beacon success");
                    } else {
                        if (error.errorCode == KBErrorCode.CfgBusy) {
                            Log.e(LOG_TAG, "Device was busy, Maybe another configuration is not complete");
                        } else if (error.errorCode == KBErrorCode.CfgTimeout) {
                            Log.e(LOG_TAG, "Sending parameters to device timeout");

                        }

                        //toastShow("config failed for error:" + error.errorCode);
                    }

                    progressDialog.setMessage("Συγχρονισμός Δεδομένων");
                    isScanning = false;
                    //CToast(getAppContext(), "Logger Successfully Renamed", Toast.LENGTH_LONG);

                    correlate();
                }
            });
        }
    }

    @Override
    public void onNotifyDataReceived(KBeacon beacon, int nEventType, byte[] sensorData) {

    }

    public class SyncTxCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (response.isSuccessful()) {
                Asset oldBinRfid = db.assetDAO().getAssetByEpc(recWHCorrelation.assetRFID);
                if (oldBinRfid != null) {
                    oldBinRfid.rfid = null;
                    db.assetDAO().update(oldBinRfid);
                }
                Asset oldBinLogger = db.assetDAO().getAssetByLoggerEpc(recWHCorrelation.rfid);
                if (oldBinLogger != null) {
                    oldBinLogger.rfid = null;
                    db.assetDAO().update(oldBinLogger);
                }
                Asset bin = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                bin.rfid = recWHCorrelation.assetRFID;
                bin.loggerEPC = recWHCorrelation.rfid;
                db.assetDAO().update(bin);
                loadBinsFromLocalDB(Constants.ftBin);
                deleteCorrelationTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
            clearAllScanned();
            endActivity();
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_timeout), Toast.LENGTH_LONG));
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render("Network Error :: " + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
            clearAllScanned();
            endActivity();
        }
    }

    private void endActivity() {
        progressDialog.dismiss();
        isScanning = false;
        Intent i = new Intent(getApplicationContext(), CorrelationBinActivity.class);
        startActivity(i);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<CorrelationBinActivity> mActivity;

        public ScanHandler(CorrelationBinActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String tag = msg.getData().getString("epc");
                    try {
                        if (!Strings.isEmptyOrWhitespace(tag)) {

                            String epc = tag.toString();
                            epcStr = epc;
                            label = epc.length() > 15 ? epc.substring(16) : epc;

                            if (epc.contains(Filters.RFID_BIN)) {
                                // label = epc.length() > 15 ? epc.substring(14) : epc;

                                if (adapterAssets.getSelectedValue() != null) {
                                    Asset bin = db.assetDAO().getAssetByEpc(epc);
                                    if (bin == null) {
                                        bin = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                                        if (bin != null && bin.rfid != null) {
                                            String code = bin.code;
                                            FragmentManager fm = getSupportFragmentManager();
                                            confirmRfidReplacement.setMessage(String.format(getResources().getString(R.string.bin_already_assigned_to_other_rfid), code, label));
                                            confirmRfidReplacement.showNow(fm, getString(R.string.confirm_selection));
                                            return;

                                        } else {
                                            recWHCorrelation.assetRFID = epcStr;
                                            runOnUiThread(() -> tvCorrBinBarcode.setText(label));
                                            switchToBLEScan();
                                            break;
                                        }

                                    } else {
                                        binCode = bin.code;
                                        FragmentManager fm = getSupportFragmentManager();
                                        confirmBinRfidDlg.setMessage(String.format(getResources().getString(R.string.rfid_already_assigned_to_other_bin), label, binCode));
                                        confirmBinRfidDlg.showNow(fm, getString(R.string.confirm_selection));
                                        return;
                                    }
                                } else {
                                    CToast(getApplicationContext(), render(R.string.select_asset), Toast.LENGTH_LONG);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render(String.format("No item of type %s was found!", selectedAssetType)), Toast.LENGTH_LONG);
                    }
                    break;
            }
        }
    }
}