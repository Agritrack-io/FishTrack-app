package io.agritrack.fish.ui.quality_arrival;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.ui.bo.LoggerReading;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;

public class PackageQualityStartPackageActivity extends TriggerKeyAwareActivity {

    // Local handler that receives the RFID scanner results.
    private final PackageQualityStartPackageActivity.ScanHandler mScanHandler = new PackageQualityStartPackageActivity.ScanHandler(this);
    private boolean intentForProcessing = false; //TODO: Check why savedInstance is null
    private final LinkedList<String[]> listMeasurements = new LinkedList<>();
    private SingleShotScanner scanner_runnable;
    private MobileDB db;
    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;
    private TemplateRecyclerAdapter adapterBins;

    private ImageButton ivDeleteBin;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    // Instantiate a clickListener to be passed to adapterBins.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };
    private Set<String> scannedBinEPCs;
    private String logger_rfid;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;
    private LoggerReading loggerReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_start_package);

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
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
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
            clearSelectedItem();

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterBins.removeItem(barcode);
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Bin to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityStartPackageActivity.this);
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

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
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
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackageQualityTemperatureProfilesActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), PackageQualitySelectStepsActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        /*if (prcRecord.availBins != null) {
            adapterBins.setValues(new LinkedList<String>(prcRecord.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }*/
    }

    private void updateState() {
        GlobalState.initQualityRecord();

        GlobalState.recQuality.qualityBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.recQuality.tempValues = listMeasurements;
        GlobalState.recQuality.retrievedAt = System.currentTimeMillis();
        GlobalState.recQuality.logger_rfid = logger_rfid;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recQuality.qualityBins == null || GlobalState.recQuality.qualityBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        stopScanner();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        stopScanner();
        super.onStop();
    }

    @Override
    protected void onClick(View view) {
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

    private class ScanHandler extends Handler {
        private final WeakReference<PackageQualityStartPackageActivity> mActivity;

        public ScanHandler(PackageQualityStartPackageActivity activity) {
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
                            //binEPC = epcStr.substring(11);
                            // after bin is identified, initialize the temperatures logger.
                            IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
                            if (logger != null) {
                                scannedBinEPCs.add(epcStr.substring(11));
                                tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                                adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                adapterBins.notifyDataSetChanged();

                                if (!Strings.isEmptyOrWhitespace(logger.rfid)) {
                                    logger_rfid = epcStr.substring(11);
                                    FragmentManager fm = getSupportFragmentManager();
                                    LoggerInitDialogFragment loggerDlg = LoggerInitDialogFragment.newInstance(logger.rfid, true, false, intentForProcessing);
                                    loggerDlg.show(fm, LoggerInitDialogFragment.TAG);
                                }
                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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