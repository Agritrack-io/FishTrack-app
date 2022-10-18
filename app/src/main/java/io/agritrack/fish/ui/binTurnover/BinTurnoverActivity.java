package io.agritrack.fish.ui.binTurnover;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.IsOnline;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.fish.state.GlobalState.recQuality;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncBinInfo;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.BinInfoDTO;
import io.agritrack.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.data.dto.tx.FishingTxDTO;
import io.agritrack.data.model.BinInfo;
import io.agritrack.data.model.common.TemperatureTimeSeries;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.data.repo.IFishTrackRepository;
import io.agritrack.data.repo.MeasurementRepository;
import io.agritrack.data.repo.TemperatureDataRepository;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.LoggerDataRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.fishing.FishingConfirmActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.IInformedActivity;
import io.agritrack.ui.adapter.BinWeightCageAdapter;
import io.agritrack.ui.adapter.TemperatureProfileAdapter;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BinTurnoverActivity extends AppCompatActivity implements IInformedActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private IFishTrackRepository tempDataRepo, measRepo;
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
    private LoggerDataRecord.TemperatureModel data;
    private Spinner spProductionLine;
    private ImageButton ibShowValues;
    private String loggerEPC, binEPC;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_turnover);

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
        }

        SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
        String token = LocalPreferences.getToken();
        // sync Bin Info (complete BinLedger)
        Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
        syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinOverturn);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        this.tempDataRepo = new TemperatureDataRepository();
        this.measRepo = new MeasurementRepository();

        // get  references of the controls
        assignCtrlVars();

        tvLotLabel.setVisibility(View.INVISIBLE);
        tvLot.setVisibility(View.INVISIBLE);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(BinTurnoverActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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

        if (adapterBinList!=null && adapterBinList.size()>0){
            binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
            adapterBins.notifyDataSetChanged();
            adapterBins.markReceived(scannedBinEPCs);
            btnScanBin.setText(R.string.scan_one_to_one_bins);
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

        configFooter();
    }

    private void moveToNextScreen() {
        Boolean proceed = updateState();

        if (proceed) {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            // move to next activity.
            if(scannedBinEPCs.size()<binList.size()) {
                i = new Intent(getApplicationContext(), BinTurnoverActivity.class);
                i.putStringArrayListExtra("adapterBinList", (ArrayList<String>) convertBinDetailsToEPCs(adapterBins.getValues()));
                i.putStringArrayListExtra("scannedBinList", (ArrayList<String>) scannedBinEPCs);
                i.putStringArrayListExtra("expectedBinList", (ArrayList<String>) binList);
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
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            stopScanner();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                moveToNextScreen();
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();
            //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

            db.binInfoDAO().updateBinInfoSetSorted(binEPC);

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
                for (int i=0; i < 3; i++) {
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
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void onClick(View view) {
        if (adapterBins.getItemCount() < 1 && IsOnline && !scanAllBins) {
            scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_LOGGER);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        }
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

    private List<BinWeightCageAdapter.BinDetails> convertEPCsToBinDetails(List<String> epcs) {
        List<BinWeightCageAdapter.BinDetails> result = new ArrayList<>();
        for (String epc : epcs) {
            result.add(new BinWeightCageAdapter.BinDetails(epc));
        }
        return result;
    }

    private List<String> convertBinDetailsToEPCs(List<BinWeightCageAdapter.BinDetails> epcs) {
        List<String> result = new ArrayList<>();
        for (BinWeightCageAdapter.BinDetails epc : epcs) {
            result.add(epc.epc);
        }
        return result;
    }

    @Override
    public void inform() {
        List<String[]> values = recLoggerData.getValues(binEPC);

        if (values != null) {
            Map<String, LoggerDataRecord.TemperatureModel> data = recLoggerData.data;
            tempProfileAdapter.fill(data, tmpBin);
        }

        adapterBins.removeItem(tmpBin.rfid);
        adapterBins.notifyDataSetChanged();
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
                                loggerEPC = epcStr;

                                // after bin is identified, initialize the temperatures logger.
                                Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                                if (bin != null) {
                                    binEPC = bin.rfid;

                                    if (!binEPC.equalsIgnoreCase(epcStr)){
                                        if (tempProfileAdapter.getItemCount()>0){
                                            CToast(getApplicationContext(), render(getString(R.string.already_scannned_bin_first_sync_temps, tempProfileAdapter.getEpc().substring(tempProfileAdapter.getEpc().length()-10))), Toast.LENGTH_LONG);
                                            return;
                                        }
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
                                } else if (!IsDemo) {
                                    CToast(getApplicationContext(), render(R.string.no_logger_found_linked_to_bin), Toast.LENGTH_SHORT);
                                }
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
                                    btnScanBin.setText(R.string.scan_one_to_one_bins);
                                    scanAllBins = true;
                                    return;
                                }
                                binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
                                adapterBins.notifyDataSetChanged();
                                btnScanBin.setText(R.string.scan_one_to_one_bins);
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

    private void triggerDataLoggerDialog(){
        if (!scannedBinEPCs.contains(binEPC)) {
            scannedBinEPCs.add(binEPC);
        }
        adapterBins.addUniqueItem(loadBinInfo(binEPC));
        adapterBins.markReceived(scannedBinEPCs);

        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            tmpBin = db.binInfoDAO().getByRFId(binEPC);
            if (tmpBin.sorted){
                CToast(getApplicationContext(), render(R.string.already_scannned_bin), Toast.LENGTH_LONG);
                return;
            }
            FragmentManager fm = getSupportFragmentManager();
            String productionLane = spProductionLine.getSelectedItem().toString();
            LoggerInitDialogFragment loggerDlg;
            if (tmpBin != null && tmpBin.initedAt != null) {
                loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, productionLane, tmpBin.initedAt, true, true, false);
            } else {
                loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, productionLane, true, true, false);
            }
            loggerDlg.setInformedActivity(BinTurnoverActivity.this);
            loggerDlg.show(fm, LoggerInitDialogFragment.TAG);
        }
    }

    private void confirmScanBinOutOfLotDialog() {
        // Get custom login form view.
        final View confirmFormView = this.getLayoutInflater().inflate(R.layout.confirm_scan_bin_out_of_lot_dlg, null);

        final EditText pin = confirmFormView.findViewById(R.id.etPin);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText(Html.fromHtml("<b>"+ getAppContext().getResources().getString(R.string.confirm_scanned_bin_out_of_lot) +"</b>" + "<br>" + getAppContext().getResources().getString(R.string.confirm_with_pin), HtmlCompat.FROM_HTML_MODE_LEGACY));
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

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String insertedPin = pin.getText().toString().trim();
                        String login = LocalPreferences.getLoggedInUser("").trim();

                        if (Strings.isEmptyOrWhitespace(insertedPin)) {
                            CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG);
                            return;
                        }

                        // use typed-in PIN to compare credentials with those stored in the Local DB.
                        AuthenticationService authSvc = new AuthenticationService();
                        boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                        if (authentication){
                            triggerDataLoggerDialog();
                            dialog.dismiss();
                        } else {
                            CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG);
                            return;
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public class SyncMsCallBack implements Callback<List<TemperatureTimeSeriesDTO>> {
        @Override
        public void onResponse(Call<List<TemperatureTimeSeriesDTO>> call, Response<List<TemperatureTimeSeriesDTO>> response) {
            List<TemperatureTimeSeriesDTO> rs = response.body();

            if (rs != null || IsDemo) {
                // reset existing Temperature values in stateRecord.
                recLoggerData.clearData();
                tempDataRepo.removeAll(db);
                measRepo.removeAll(db);
                //db.temperatureDataDAO().deleteAll();
                //db.measurementsDAO().deleteAll();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_temperatures_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<TemperatureTimeSeriesDTO>> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                for (int i=0; i < 3; i++) {
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