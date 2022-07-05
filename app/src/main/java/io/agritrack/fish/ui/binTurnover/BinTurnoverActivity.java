package io.agritrack.fish.ui.binTurnover;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.data.dto.tx.QualityTxDTO;
import io.agritrack.data.model.common.SortingTimeSeries;
import io.agritrack.data.model.common.TemperatureTimeSeries;
import io.agritrack.data.model.tx.QualityTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.LoggerDataRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.quality.receipt.ReceiptQualityConfirmActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.TemperatureProfileAdapter;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BinTurnoverActivity extends AppCompatActivity {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private ProgressDialog progressDialog;
    private RecyclerView lvTempProfiles;
    private TemperatureProfileAdapter tempProfileAdapter;
    private SingleShotScanner scanner_runnable;
    private LoggerDataRecord.TemperatureModel data;
    private Spinner spProductionLine;
    private ImageButton ibShowValues;
    private String loggerEPC, binEPC;
    private TextView tvCurrentBin;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin_turnover);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinOverturn);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(BinTurnoverActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        String[] lines = new String[]{"1", "2", "3", "4", "5", "6"};
        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.

        ArrayAdapter<String> linesAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, lines);
        linesAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spProductionLine.setAdapter(linesAdapter);

        tempProfileAdapter = new TemperatureProfileAdapter(this);
        lvTempProfiles.setAdapter(tempProfileAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BinTurnoverActivity.this);
        lvTempProfiles.setLayoutManager(layoutManager);
        lvTempProfiles.setHasFixedSize(false);

        tempProfileAdapter.notifyDataSetChanged();

        // configure image button to display last measurements set.
        ibShowValues.setOnClickListener(v -> {
            if(!Strings.isEmptyOrWhitespace(binEPC)) {
                List<String[]> values = recLoggerData.getValues(binEPC);

                if(values!=null) {
                    Map<String, LoggerDataRecord.TemperatureModel> data = recLoggerData.data;
                    tempProfileAdapter.refill(data);

                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(BinTurnoverActivity.this);
                    dlgBuilder.setTitle("Logger Data");

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BinTurnoverActivity.this, R.layout.agri_list_item_12dp);

                    int idx = 1;
                    for (String[] value : values) {
                        arrayAdapter.add(String.format("%04d. [%s] --> %s", idx++, value[0], value[1]));
                    }
                    dlgBuilder.setAdapter(arrayAdapter, null);
                    dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                    dlgBuilder.create().show();
                }
            } else {
                CToast(getApplicationContext(), render("No Bin Tag was scanned!!"), Toast.LENGTH_SHORT);
            }
        });

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

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        lvTempProfiles = findViewById(R.id.lvTempProfiles);
        ibShowValues = findViewById(R.id.ibShowValues);
        spProductionLine = findViewById(R.id.spProductionLine);
        tvCurrentBin = findViewById(R.id.tvCurrentBin);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            stopScanner();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                startActivity(i);
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

            // persist Measurements Record data to local DB.
            List<SortingTimeSeries> measurements = GlobalState.commitSortingMeasurements(db);
            List<TemperatureTimeSeriesDTO> temperatureTimeSeriesDTOs = new ArrayList<>();
            for (SortingTimeSeries ts : measurements) {
                //TemperatureTimeSeriesDTO measurementDTO = TemperatureTimeSeriesDTO.convert(ts);
                //measurementDTO.lot = tx.plot;
                //temperatureTimeSeriesDTOs.add(measurementDTO);
            }

            // sync Measurements records
            if (!temperatureTimeSeriesDTOs.isEmpty()) {
                Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall = updService.syncMeasurements(temperatureTimeSeriesDTOs, "Bearer " + token);
                syncMsAsyncCall.enqueue(new BinTurnoverActivity.SyncMsCallBack());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
        //GlobalState.recQuality.qualityBins = new LinkedList<>(adapterBins.getValues());
        //GlobalState.recQuality.qualityBinsCnt = adapterBins.getItemCount();

        //GlobalState.recQuality.retrievedAt = System.currentTimeMillis();
        //GlobalState.recQuality.logger_rfid = loggerEPC;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recQuality.logger_rfid == null || GlobalState.recQuality.logger_rfid.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Bin to overturn'"));
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
        if(keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the receiver
        if(keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void onClick(View view) {
        // reset existing Temperature values in stateRecord.
        recLoggerData.clearData();

        scanner_runnable = new SingleShotScanner(mScanHandler);
        tvCurrentBin.setText("");
        scanner_runnable.setFilter(Filters.RFID_LOGGER);
        scanner_runnable.LowEnergy();
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
        scanner_runnable.HighEnergy();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<BinTurnoverActivity> mActivity;

        public ScanHandler(BinTurnoverActivity activity) {
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
                            loggerEPC = epcStr;

                            // after bin is identified, initialize the temperatures logger.
                            Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                            if (bin != null) {
                                binEPC = bin.rfid;
                                tvCurrentBin.setText(binEPC.substring(binEPC.length() - 10));
                                //adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                //adapterBins.notifyDataSetChanged();

                                if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                                    FragmentManager fm = getSupportFragmentManager();
                                    LoggerInitDialogFragment loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC,true, true, false);
                                    loggerDlg.show(fm, LoggerInitDialogFragment.TAG);
                                }
                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                            }
                        } else {
                            CToast(getApplicationContext(), render("No bin was found!! Please scan again!"), Toast.LENGTH_SHORT);
                        }
                        this.removeCallbacks(scanner_runnable);
                    } catch (Exception e) {
                        e.printStackTrace();
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

            if (rs != null || IsDemo) {
                // reset existing Temperature values in stateRecord.
                recLoggerData.clearData();
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_SHORT));
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
        }
    }
}