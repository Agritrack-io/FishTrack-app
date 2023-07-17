package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.ui.custom.CustomToast.CToast;
import static io.agritrack.ui.tools.caen.ILoggerDialog.StatesEnum.INIT;
import static io.agritrack.ui.tools.caen.ILoggerDialog.StatesEnum.READ_VALUES;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.caen.common.CAENState;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.BinInfo;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.GetTempDataDialog;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.bo.LoggerReading;
import io.agritrack.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.caen.ILoggerDialog;
import io.agritrack.ui.tools.caen.InitLoggerDialogDecorator;
import io.agritrack.ui.tools.caen.LoggerDialogFragment;

public class FishingBinsActivity extends AppCompatActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private final MutableLiveData<CAENState> loggerStateObserver = new MutableLiveData<>();


    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private SingleShotScanner singleShot_runnable;
    private MobileDB db;
    private TemplateRecyclerAdapter adapterBins;
    private RecyclerView rvBins;
    private TextView tvBinsCount;
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
        setContentView(R.layout.activity_fishing_bins);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            intentForBinActivity = bundle != null ? bundle.getBoolean("BinActivity") : intentForBinActivity;
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingBins);
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
                CToast(getApplicationContext(), render("Plz select a Bin to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddBin.setOnClickListener(view -> {
            showAddDialog();
        });

        ivCheckLastTemp.setOnClickListener(view -> {
            updateState();
            this.stopScanner();
            Intent i = new Intent(getApplicationContext(), TestBinTempActivity.class);
            i.putExtra("BinActivity", true);
            startActivity(i);
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingBinsActivity.this);
            infoDialog.showDialog();
        });

        loggerReading = new ViewModelProvider(this).get(LoggerReading.class);
        loggerReading.getReading().observe(this, reading -> {
            Double temp = (Double) reading.get("LastValue");
            Long ts = (Long) reading.get("timestamp");

            recFishing.binTemperatureRecord.addRecord(binEPC, ts, temp);

            tempLoggerDialog = new GetTempDataDialog(FishingBinsActivity.this, temp, binEPC);
            tempLoggerDialog.showDialog();
        });

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
        ImageView ivNext = findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            this.stopScanner();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            this.stopScanner();
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        rvBins = findViewById(R.id.rvBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = findViewById(R.id.ivDeleteBin1);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
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
        builder.setTitle("Type bin BARCODE");

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

    private void updateState() {
        recFishing.availBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.commitFishing(db, Boolean.FALSE);
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
        singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_BIN);//TODO:: Remove
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

    private class ScanHandler extends Handler {
        private final WeakReference<FishingBinsActivity> mActivity;

        public ScanHandler(FishingBinsActivity activity) {
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
                            //Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                            if (loggerEPC != null) {
                                //binEPC = bin.rfid;//TODO:: Remove
                                //scannedBinEPCs.add(bin.rfid);
                                binEPC = loggerEPC;
                                scannedBinEPCs.add(loggerEPC);
                                tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                                adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                adapterBins.notifyDataSetChanged();
                                recFishing.availBins = new LinkedList<>(adapterBins.getValues());
                                GlobalState.commitFishing(db, Boolean.FALSE);

// ------------------------------------------
//                                //--- New implementation of Logger Dialog ---
//                                if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
//                                    FragmentManager fm = getSupportFragmentManager();
//
//                                    ILoggerDialog loggerDlg = LoggerDialogFragment.newInstance(loggerEPC, binEPC);
//                                    loggerDlg.setStateObserver(loggerStateObserver);
//                                    InitLoggerDialogDecorator initLoggerDecorator = new InitLoggerDialogDecorator(loggerDlg);
//                                    initLoggerDecorator.show(fm);
//
//                                }

                                /*FragmentManager fm = getSupportFragmentManager();
                                LoggerInitDialogFragment loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, false, true, true);
                                loggerDlg.show(fm, LoggerInitDialogFragment.TAG);*/
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
}