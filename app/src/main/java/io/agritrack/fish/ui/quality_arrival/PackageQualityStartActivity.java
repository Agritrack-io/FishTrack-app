package io.agritrack.fish.ui.quality_arrival;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import io.agritrack.dialog.GetTempDataDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;

public class PackageQualityStartActivity extends TriggerKeyAwareActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private MobileDB db;

    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;

    private final boolean scanning = false;

    private GetTempDataDialog tempLoggerDialog;
    private String currentBin;
    private List<String[]> values;
    private final LinkedList<String> listMeasurements = new LinkedList<>();

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
    private String binBarcode;
    private String logger_rfid;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityStart);
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
            supportDialog = new SupportDialog(PackageQualityStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

/*    private Reader.READER_ERR resetLogger(ICAEN_API cmd) {
        return cmd.Reset();
    }

    private String initializeLogger(ICAEN_API cmd) {
        try {
            Double lastTemperature = cmd.Init();
            return String.format("%.2f\u2103", lastTemperature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

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
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
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
        GlobalState.initProcessingRecord();

        GlobalState.recProcessing.qualityBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.recProcessing.tempValues = listMeasurements;
        GlobalState.recProcessing.retrievedAt = System.currentTimeMillis();
        GlobalState.recProcessing.logger_rfid = logger_rfid;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recProcessing.qualityBins == null || GlobalState.recProcessing.qualityBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//    private void displayMeasurementsDialog(List<String[]> values) {
//
//        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(PackageQualityStartActivity.this);
//        dlgBuilder.setTitle("Logger Data");
//
//        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PackageQualityStartActivity.this, R.layout.agri_list_item_12dp);
//
//        int idx = 1;
//        for (String[] value : values) {
//            arrayAdapter.add(String.format("%3d. [%s] --> %s", idx++, value[0], value[1]));
//        }
//        dlgBuilder.setAdapter(arrayAdapter, null);
//        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
//        dlgBuilder.create().show();
//    }


    @Override
    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
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
                            //binEPC = epcStr.substring(11);
                            // after bin is identified, initialize the temperatures logger.
                            IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
                            if (logger != null) {
                                scannedBinEPCs.add(epcStr.substring(11));
                                tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                                adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                                adapterBins.notifyDataSetChanged();

                                if (!Strings.isEmptyOrWhitespace(logger.rfid)) {
                                    FragmentManager fm = getSupportFragmentManager();
                                    LoggerInitDialogFragment loggerDlg = LoggerInitDialogFragment.newInstance(logger.rfid, true, false);
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

//    private String scanCloserEPC(UhfReader uhfReader) {
//        SingleShotScanner scanner = null; //new SingleShotScanner();
//        //scanner.setUhfReader(uhfReader);
//        scanner.setFilter(Filters.RFID_BIN);
//
//        try {
//            String epcStr = null; //scanner.call();
//            if (!Strings.isEmptyOrWhitespace(epcStr)) {
// after bin is identified, initialize the temperatures logger.
//                IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
//                if (logger != null) {
//                    logger_rfid = logger.rfid;
//                    scannedBinEPCs.add(epcStr.substring(11));
//                    tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
//                    adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
//                    adapterBins.notifyDataSetChanged();
//                    if (IsDemo) {
//                        return logger.rfid;
//                    } else {
//                        return epcStr;
//                    }
//                } else if (!IsDemo) {
//                    CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_LONG);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
