package io.agritrack.kefalonia.fish.ui.initBins;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.IsOnline;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recFishing;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;
import static io.agritrack.kefalonia.ui.tools.caen.ILoggerDialog.StatesEnum.INIT;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.agritrack.kefalonia.fish.ui.FishHomeActivity;
import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.tx.TransactionApi;
import io.agritrack.kefalonia.caen.common.CAENState;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.BinInfoDTO;
import io.agritrack.kefalonia.data.model.BinInfo;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.dialog.GetTempDataDialog;
import io.agritrack.kefalonia.dialog.InfoDialog;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.FishingRecord;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.ui.bo.LoggerReading;
import io.agritrack.kefalonia.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.kefalonia.rfid.SingleShotScanner;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.sound.SoundUtil;
import io.agritrack.kefalonia.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import io.agritrack.kefalonia.ui.tools.caen.ILoggerDialog;
import io.agritrack.kefalonia.ui.tools.caen.InitLoggerDialogDecorator;
import io.agritrack.kefalonia.ui.tools.caen.LoggerDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitBinsActivity extends AppCompatActivity {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private final MutableLiveData<CAENState> loggerStateObserver = new MutableLiveData<>();


    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private SingleShotScanner singleShot_runnable;
    private MobileDB db;
    private TemplateRecyclerAdapter adapterBins;
    private RecyclerView rvBins;
    private TextView tvBinsCount, tvSelectBins;
    private Button btnScanBin;
    private LoggerReading loggerReading;
    private GetTempDataDialog tempLoggerDialog;
    private boolean intentForBinActivity = false;
    private ImageButton ivAddBin, ivDeleteBin;
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
        keyReceiver = new X9KeyReceiver(this::onClick);

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
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), true);
        rvBins.setAdapter(adapterBins);
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

            if (!Strings.isEmptyOrWhitespace(adapterBins.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", adapterBins.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterBins.getSelectedLabel());

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterBins.removeItem(barcode);
                        scannedBinEPCs.remove(barcode);
                        recFishing.binWeightRecord.getBins().remove(barcode);
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        adapterBins.clearSelectedValue();
                        recFishing.availBins = new LinkedList<>(adapterBins.getValues());
                        GlobalState.commitFishing(db, Boolean.FALSE);
                    }

                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterBins.clearSelectedValue();
                    adapterBins.notifyDataSetChanged();
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(R.string.delete_item), Toast.LENGTH_LONG);
            }
        });

        ivAddBin.setOnClickListener(view -> {
            showAddDialog();
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

        loggerReading = new ViewModelProvider(this).get(LoggerReading.class);
        loggerReading.getReading().observe(this, reading -> {
            Double temp = (Double) reading.get("LastValue");
            Long ts = (Long) reading.get("timestamp");

            recFishing.binTemperatureRecord.addRecord(binEPC, ts, temp);

            tempLoggerDialog = new GetTempDataDialog(InitBinsActivity.this, temp, binEPC);
            tempLoggerDialog.showDialog();
        });

        //-----------------------------------------------------
        // observe for state object obtained by LoggerDialog...
        //-----------------------------------------------------
        loggerStateObserver.observe(this, rs -> {
            // handle Successful operation from Logger.
            if (rs == null || !rs.canProceed) {
                CToast(getApplicationContext(), render(R.string.operation_failed), Toast.LENGTH_LONG);
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

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
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
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            this.stopScanner();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
            } else {
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
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        tvSelectBins.setVisibility(View.INVISIBLE);
    }

    private void initControlsFromState() {
        FishingRecord hvst = recFishing;

        if (hvst.availBins != null) {
            adapterBins.setValues(new LinkedList<>(hvst.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvBinsCount.setText(String.valueOf(hvst.availBins.size()));
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.type_code_of_bin);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binBarcode = input.getText().toString();
                //TODO:: Encode properly the bin barcode value, add prefix
                adapterBins.addUniqueItem(binBarcode);
                adapterBins.notifyDataSetChanged();
                tvBinsCount.setText(String.valueOf(adapterBins.getValues().size()));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private boolean updateState() {
        try {
            String token = LocalPreferences.getToken();

            recFishing.availBins = new LinkedList<>(adapterBins.getValues());

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
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.bins_to_use));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
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

    private boolean deleteTx() {
        try {
            System.out.println("About to delete bin info tx");
            db.binInfoDAO().deleteAll();
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
                                adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                adapterBins.notifyDataSetChanged();
                                recFishing.availBins = new LinkedList<>(adapterBins.getValues());
                                GlobalState.commitFishing(db, Boolean.FALSE);

                                // ------------------------------------------
                                //--- New implementation of Logger Dialog ---
                                if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                                    FragmentManager fm = getSupportFragmentManager();

                                    ILoggerDialog loggerDlg = LoggerDialogFragment.newInstance(loggerEPC, binEPC);
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
                deleteTx();
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