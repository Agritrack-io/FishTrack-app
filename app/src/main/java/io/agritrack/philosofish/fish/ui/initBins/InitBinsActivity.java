package io.agritrack.philosofish.fish.ui.initBins;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;
import static io.agritrack.philosofish.ui.tools.caen.ILoggerDialog.StatesEnum.INIT;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.caen.common.CAENState;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.dialog.GetTempDataDialog;
import io.agritrack.philosofish.dialog.InfoDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.FishingRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import io.agritrack.philosofish.ui.tools.caen.IDialogCloseListener;
import io.agritrack.philosofish.ui.tools.caen.ILoggerDialog;
import io.agritrack.philosofish.ui.tools.caen.InitLoggerDialogDecorator;
import io.agritrack.philosofish.ui.tools.caen.LoggerDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitBinsActivity extends AppCompatActivity implements IDialogCloseListener {
    // REST API to interact with the backend
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    // State POJO that keeps everything related to CAEN logger.
    private final MutableLiveData<CAENState> loggerStateObserver = new MutableLiveData<>();

    // variable to hold the dialog. Only 1 instance of ILoggerDialog may be active...
    private ILoggerDialog loggerDlg = null;

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver = null;
    private SingleShotScanner singleShot_runnable;
    private MobileDB db;
    private TemplateRecyclerAdapter rcAdapterBins;
    private RecyclerView rvBins;
    private TextView tvBinsCount, tvSelectBins;
    private Button btnScanBin;
    //private LoggerReading loggerReading;
    private GetTempDataDialog tempLoggerDialog;
    private boolean intentForBinActivity = false;
    private ImageButton ivDeleteBin;
    private Set<String> scannedBinEPCs;
    private String binBarcode = "", binEPC, loggerEPC;
    private ImageView ivSupport, ivCheckLastTemp;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private ImageView ivInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_bins);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            intentForBinActivity = bundle != null ? bundle.getBoolean("BinInitActivity") : intentForBinActivity;
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        //keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInitBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBins.setLayoutManager(layoutManager);
        rvBins.setItemAnimator(new DefaultItemAnimator());
        rvBins.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rcAdapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), true);
        rvBins.setAdapter(rcAdapterBins);
        rvBins.setNestedScrollingEnabled(false);

        if (!intentForBinActivity && CollectionUtils.isEmpty(recFishing.availBins)) {
            // instantiate a set to hold scanned EPCS.it will be passed to adapter which feeds the ListView.
            scannedBinEPCs = new LinkedHashSet<>();
        } else {
            scannedBinEPCs = new LinkedHashSet<>(recFishing.availBins);
        }

        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteBin.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(rcAdapterBins.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmLoggerBinSelectionDlg = YesNoDialogFragment.instance();
                confirmLoggerBinSelectionDlg.args().putString("selectedBarcode", rcAdapterBins.getSelectedValue());
                confirmLoggerBinSelectionDlg.setMessage(getText(R.string.delete_selected_item) + rcAdapterBins.getSelectedLabel());

                confirmLoggerBinSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        rcAdapterBins.removeItem(barcode);
                        scannedBinEPCs.remove(barcode);
                        recFishing.binWeightRecord.getBins().remove(barcode);
                        rcAdapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(rcAdapterBins.getItemCount()));
                        rcAdapterBins.clearSelectedValue();
                        recFishing.availBins = rcAdapterBins.getValues().stream().map(x -> x.epc).collect(Collectors.toList());

                        // TODO:: add component in GlobalState for Bins Initialization, should not use the Fishing state.
                        GlobalState.commitFishing(db, Boolean.FALSE);
                    }
                });

                confirmLoggerBinSelectionDlg.onReject(bundle -> {
                    rcAdapterBins.clearSelectedValue();
                    rcAdapterBins.notifyDataSetChanged();
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmLoggerBinSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(R.string.select_bin_to_delete), Toast.LENGTH_LONG);
            }
        });

        ivCheckLastTemp.setOnClickListener(view -> {
            updateState();
            this.stopScanner();
            Intent i = new Intent(getApplicationContext(), TestBinTempActivity.class);
            i.putExtra("BinInitActivity", true);
            startActivity(i);
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(InitBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(InitBinsActivity.this);
            infoDialog.showDialog();
        });


        // ????????????????????????????
//        loggerReading = new ViewModelProvider(this).get(LoggerReading.class);
//        loggerReading.getReading().observe(this, reading -> {
//            Double temp = (Double) reading.get("LastValue");
//            Long ts = (Long) reading.get("timestamp");
//
//            recFishing.binTemperatureRecord.addRecord(binEPC, ts, temp);
//
//            tempLoggerDialog = new GetTempDataDialog(InitBinsActivity.this, temp, binEPC);
//            tempLoggerDialog.showDialog();
//        });

        //-----------------------------------------------------
        // observe for state object obtained by LoggerDialog...
        //-----------------------------------------------------
        loggerStateObserver.observe(this, rs -> {
            // handle Successful operation from Logger.
            if (rs == null || !rs.canProceed) {
                CToast(getApplicationContext(), render("Operation Failed!"), Toast.LENGTH_LONG);
                return;
            }
            // handle READ and INIT events...
            if (rs.canProceed) {
                if (INIT.equals(rs.state)) {
                    if (rs.getInitTS() != null) {
                        recFishing.binWeightRecord.addRecord(binEPC, 0, rs.getInitTS() / 1000L, null, null);
                    } else {
                        recFishing.binWeightRecord.addRecord(binEPC, 0, System.currentTimeMillis() / 1000L, null, null);
                    }
                    GlobalState.commitFishing(db, Boolean.FALSE);
                }
            }
        });
        // ------ Logger Observer -----------------------------

        // create Footer
        configFooter();
    }

    // Logger Dialog is dismissed.
    // release the singleton that points to the dialog.
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
        this.loggerDlg = null;
        registerKeyReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        registerKeyReceiver();
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
    protected void onDestroy() {
        super.onDestroy();
        this.stopScanner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null) {
            unregisterReceiver(keyReceiver);
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            this.stopScanner();
            boolean proceed = false;
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                proceed = updateState();

            }
            if (proceed) {
                Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            this.stopScanner();
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        rvBins = findViewById(R.id.rvBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        tvSelectBins = findViewById(R.id.tvSelectBins);
        ivDeleteBin = findViewById(R.id.ivDeleteBin1);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        tvSelectBins.setVisibility(View.INVISIBLE);
    }

    private void initControlsFromState() {
        FishingRecord hvst = recFishing;

        if (hvst.availBins != null) {
            rcAdapterBins.setValues(hvst.availBins.stream().map(x -> new TemplateRecyclerAdapter.BinEpc(x)).collect(Collectors.toList()));
            rcAdapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvBinsCount.setText(String.valueOf(hvst.availBins.size()));
        }
    }

    private boolean updateState() {
        try {
            String token = LocalPreferences.getToken();

            recFishing.availBins = rcAdapterBins.getValues().stream().map(x -> x.epc).collect(Collectors.toList());

            // persist Fishing Record data to local DB.
            List<BinInfo> tx = GlobalState.commitBinInfoTx(db);

            if (IsOnline) {
                // sync fish tx
                Call<List<BinInfoDTO>> syncTxAsyncCall = updService.syncBinInfoTx(BinInfoDTO.convert(tx), "Bearer " + token);
                syncTxAsyncCall.enqueue(new InitBinsActivity.SyncTxCallBack());
            } else {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            saveCrashInfo2File(e);
            return false;
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recFishing.availBins == null || recFishing.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Bins for usage'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        if (this.loggerDlg != null) {
            CToast(getApplicationContext(), render(R.string.init_in_progress), Toast.LENGTH_SHORT);
            return;
        }
        tvSelectBins.setVisibility(View.VISIBLE);
        singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_LOGGER);
        singleShot_runnable.startReading();
        mScanHandler.postDelayed(singleShot_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.singleShot_runnable != null) {
            this.singleShot_runnable.stopReading();
            mScanHandler.removeCallbacks(this.singleShot_runnable);
        }
    }

    private boolean deleteTx(BinInfoDTO bin) {
        try {
            System.out.println("About to delete bin info tx");
            db.binInfoDAO().deleteByRfid(bin.rfid);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<InitBinsActivity> mActivity;

        public ScanHandler(InitBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr) && epcStr != null) {
                            loggerEPC = epcStr;
                            // after bin is identified, initialize the temperatures logger.
                            Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                            if (bin != null) {
                                binEPC = bin.rfid;
                                scannedBinEPCs.add(binEPC);
                                tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                                // TODO: clean up this mess...
                                rcAdapterBins.setValues(scannedBinEPCs.stream().map(x -> new TemplateRecyclerAdapter.BinEpc(x)).collect(Collectors.toList()));
                                rcAdapterBins.notifyDataSetChanged();
                                recFishing.availBins = rcAdapterBins.getValues().stream().map(x -> x.epc).collect(Collectors.toList());
                                GlobalState.commitFishing(db, Boolean.FALSE);

                                // ------------------------------------------
                                //--- New implementation of Logger Dialog ---
                                if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                                    if (keyReceiver != null) {
                                        unregisterReceiver(keyReceiver);
                                        keyReceiver = null;
                                    }
                                    FragmentManager fm = getSupportFragmentManager();

                                    loggerDlg = LoggerDialogFragment.newInstance(loggerEPC, binEPC);
                                    loggerDlg.setStateObserver(loggerStateObserver);
                                    InitLoggerDialogDecorator initLoggerDecorator = new InitLoggerDialogDecorator(loggerDlg);
                                    initLoggerDecorator.show(fm);
                                }

                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render(R.string.no_logger_found_linked_to_bin), Toast.LENGTH_SHORT);
                            }
                        } else {
                            CToast(getApplicationContext(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveCrashInfo2File(e);
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

    public class SyncTxCallBack implements Callback<List<BinInfoDTO>> {
        @Override
        public void onResponse(Call<List<BinInfoDTO>> call, Response<List<BinInfoDTO>> response) {
            if (response.isSuccessful() || IsDemo) {
                for (BinInfoDTO bin : response.body()) {
                    deleteTx(bin);
                }
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_fishing_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<BinInfoDTO>> call, Throwable error) {
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