package io.agritrack.kefalonia.fish.ui.quality.packaging;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.state.QualityRecord;
import io.agritrack.kefalonia.fish.ui.bo.LoggerReading;
import io.agritrack.kefalonia.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.kefalonia.rfid.SingleShotScanner;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.sound.SoundUtil;
import io.agritrack.kefalonia.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class PackageQualityStartActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final boolean intentForProcessing = true;
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private SingleShotScanner scanner_runnable;
    private MobileDB db;
    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;
    private TemplateRecyclerAdapter adapterBins;

    private ImageButton ivDeleteBin;
    private Set<String> scannedBinEPCs;
    private String loggerEPC, binEPC;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;
    private LoggerReading loggerReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_start);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityStartPackageActivity);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        rvBinsForTransport.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), true);
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        // instantiate a set to hold scanned EPCS.it will be passed to adapter shich feeds the ListView.
        scannedBinEPCs = new LinkedHashSet<>();

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
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        adapterBins.clearSelectedValue();
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(R.string.delete_item), Toast.LENGTH_LONG);
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityStartActivity.this);
            supportDialog.showDialog();
        });

        // ------- instantiate a ViewModel to fetch Temperature measurements ----
        loggerReading = new ViewModelProvider(this).get(LoggerReading.class);
        loggerReading.getReading().observe(this, reading -> {
            List<String[]> values = (List<String[]>) reading.get("Measurements");
            String epc = (String) reading.get("EPC");
            Long ts = (Long) reading.get("timestamp");

            //recLoggerData.addDataSet(this.loggerEPC, System.currentTimeMillis() / 1000L, values);

            //TODO: check if dialog display will be invoked here or in fragment
//            tempLoggerDialog = new GetTempDataDialog(FishingBinsActivity.this, temp, binEPC);
//            tempLoggerDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        ivDeleteBin = findViewById(R.id.ivDeleteBin);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityTempProfiles);
        ivNext.setOnClickListener(view -> {
            stopScanner();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackageQualityTemperatureProfilesActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        /*if (qualityRecord.qualityBins != null) {
            adapterBins.setValues(new LinkedList<String>(qualityRecord.qualityBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(qualityRecord.qualityBins.size()));
        }*/
    }

    private void updateState() {

        //GlobalState.recQuality.qualityBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.recQuality.retrievedAt = System.currentTimeMillis();
        GlobalState.recQuality.logger_rfid = loggerEPC;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recQuality.qualityBins == null || GlobalState.recQuality.qualityBins.isEmpty()) {
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.received_bins));

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
    }

    protected void onClick(View view) {
        scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_LOGGER);
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

    private class ScanHandler extends Handler {
        private final WeakReference<PackageQualityStartActivity> mActivity;

        public ScanHandler(PackageQualityStartActivity activity) {
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
                            //String binEPC = epcStr.substring(11);
                            loggerEPC = epcStr;

                            // after bin is identified, initialize the temperatures logger.
                            //IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
                            Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                            if (bin != null) {
                                binEPC = bin.rfid;
                                scannedBinEPCs.add(bin.rfid);
                                tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                                adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                adapterBins.notifyDataSetChanged();

                                /*if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
                                    FragmentManager fm = getSupportFragmentManager();
                                    LoggerInitDialogFragment loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, true, intentForProcessing, intentForProcessing);
                                    loggerDlg.show(fm, LoggerInitDialogFragment.TAG);
                                }*/
                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                            }
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
}