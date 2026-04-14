package io.agritrack.philosofish.fish.ui.fishing;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.fish.ui.FishHomeActivity.uhfReader;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;
import static io.agritrack.philosofish.ui.tools.caen.ILoggerDialog.StatesEnum.INIT;

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
import androidx.core.content.ContextCompat;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.caen.api.BX6100Commander;
import io.agritrack.philosofish.caen.common.CAENState;
import io.agritrack.philosofish.common.AlphanumericComparator;
import io.agritrack.philosofish.common.Constants;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.CageDetails;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.dialog.CageListDialog;
import io.agritrack.philosofish.dialog.GetTempDataDialog;
import io.agritrack.philosofish.dialog.InfoDialog;
import io.agritrack.philosofish.dialog.PowerLevelDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.FishingRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.bo.LoggerReading;
import io.agritrack.philosofish.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.philosofish.rfid.ScanInventoryThread;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class FishingBinsActivity extends AppCompatActivity implements PowerLevelDialog.PowerLevelListener {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private final MutableLiveData<CAENState> loggerStateObserver = new MutableLiveData<>();

    private final MutableLiveData<String> currentCageSelection = new MutableLiveData<>();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ScanInventoryThread scanner_runnable;
    private MobileDB db;
    private TemplateRecyclerAdapter adapterBins;
    private RecyclerView rvBins;
    private TextView tvBinsCount;
    private Button btnScanBin, btnChooseCage;
    private LoggerReading loggerReading;
    private GetTempDataDialog tempLoggerDialog;
    private boolean intentForBinActivity = false;
    private ImageButton ivAddBin, ivDeleteBin;
    private Set<String> scannedBinEPCs;
    private String binBarcode = "", binEPC, loggerEPC;
    private ImageView ivSupport, ivCheckLastTemp;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    private CageListDialog chooseCageDialog;

    private ImageView ivPowerLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_bins_and_cage);

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
        System.out.println("DEBUG BINS -> recFishing.availBins = " + recFishing.availBins);
        assignCtrlVars();

        Integer pr = LocalPreferences.getCurrentPower();

        if (pr < 25) {
            ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.low_power_green));
        } else if (pr < 30) {
            ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.med_power_green));
        } else {
            ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.high_power_green));
        }

        ivPowerLevel.setOnClickListener(v -> showPowerLevelDialog());


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
            recFishing.availBins.sort(Comparator.comparing(o -> o.substring(o.length() - 10)));
            scannedBinEPCs = new LinkedHashSet<>(recFishing.availBins);
        }

        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteBin.setOnClickListener(view -> {
            List<TemplateRecyclerAdapter.BinEpc> selectedBins = adapterBins.getValues().stream().filter(x -> x.isSelected).collect(Collectors.toList());
            if (!selectedBins.isEmpty()) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", adapterBins.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_selected_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    for (TemplateRecyclerAdapter.BinEpc bin : selectedBins) {
                        adapterBins.removeItem(bin);
                        recFishing.binWeightRecord.getBins().remove(bin.epc);

                    }
                    adapterBins.notifyDataSetChanged();
                    tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                    adapterBins.clearSelectedValue();
                    recFishing.availBins = adapterBins.getValues().stream().map(x -> x.epc).collect(Collectors.toList());
                    adapterBins.setAllUnselected();
                    adapterBins.notifyDataSetChanged();


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
            i.putExtra("BinActivity", true);
            startActivity(i);
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingBinsActivity.this);
            supportDialog.showDialog();
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
                CToast(getApplicationContext(), render(getString(R.string.operation_failed)), Toast.LENGTH_LONG);
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
                }
            }
        });

        List<String> cageCodes = new ArrayList<>();
        //get all available cages from local database
        List<Asset> cages = db.assetDAO().getAssetsForType(Constants.ftCage.toUpperCase(Locale.ROOT));

        if (cages != null && !cages.isEmpty()) {
            //get the cage codes
            cageCodes = cages.stream().map(cage -> cage.code).collect(Collectors.toList());
            cageCodes.sort(new AlphanumericComparator());
        }

        List<String> finalCageCodes = cageCodes;
        //if choose cage button is selected, show cage list dialog
        btnChooseCage.setOnClickListener(view -> {
            chooseCageDialog = new CageListDialog(FishingBinsActivity.this, finalCageCodes, currentCageSelection, R.string.choose_cage);
            chooseCageDialog.showDialog();
        });

        //handle cage selection from cage list dialog
        currentCageSelection.observe(this, response -> {
            if (response != null) {
                btnChooseCage.setText(response);
                recFishing.cageCode = response;
                chooseCageDialog.dismiss();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    //in another thread, set cage rfid of the state to be the rfid matching to the cage code, if it exists
                    CageDetails cage = db.cageDetailsDAO().getByCode(response);
                    if (cage != null && !Strings.isEmptyOrWhitespace(cage.cageCode)) {
                        recFishing.cageRFID = cage.rfid;
                    }
                });
            }
        });

        // ------ Logger Observer -----------------------------

        // create Footer
        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (uhfReader == null) {
            uhfReader = new BX6100Commander();
            Integer pr = LocalPreferences.getCurrentPower();
            if (pr < 25) {
                uhfReader.LowPowerLevel();
            } else if (pr < 30) {
                uhfReader.MedPowerLevel();
            } else {
                uhfReader.HighPowerLevel();
            }
        }
    }


    private void showPowerLevelDialog() {
        new PowerLevelDialog().show(getSupportFragmentManager(), "PowerLevelDialog");
    }


    @Override
    public void onPowerLevelSelected(int powerLevel) {
        int saveValue = 33; // default High

        switch (powerLevel) {
            case 1:
                saveValue = 10;
                uhfReader.LowPowerLevel();
                ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.low_power_green));
                break;

            case 2:
                saveValue = 26;
                uhfReader.MedPowerLevel();
                ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.med_power_green));
                break;

            case 3:
                saveValue = 33;
                uhfReader.HighPowerLevel();
                ivPowerLevel.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.high_power_green));
                break;
        }

        LocalPreferences.setCurrentPower(saveValue);
        CToast(getApplicationContext(), render("Power Level Updated"), Toast.LENGTH_SHORT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (keyReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.rfid.FUN_KEY");   // Example — replace with actual event
            registerReceiver(keyReceiver, filter);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (keyReceiver != null) {
            try {
                unregisterReceiver(keyReceiver);
            } catch (IllegalArgumentException ignored) {
            }
        }
        stopScanner();
    }


    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            clearBinSession();


            Intent i = new Intent(getApplicationContext(), HarvestRequestsActivity.class);
            startActivity(i);
        });
    }


    private void clearBinSession() {
        // UI
        adapterBins.setValues(new ArrayList<>());
        adapterBins.notifyDataSetChanged();
        tvBinsCount.setText("0");

        // STATE
        recFishing.availBins = null;
        recFishing.binWeightRecord.clear();

        stopScanner();
    }


    private void assignCtrlVars() {
        btnChooseCage = findViewById(R.id.btnChooseCage);
        btnScanBin = findViewById(R.id.btnScanBin);
        rvBins = findViewById(R.id.rvBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = findViewById(R.id.ivDeleteBin1);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        ivPowerLevel = findViewById(R.id.ivPowerLevel);

    }

    private void initControlsFromState() {
        FishingRecord hvst = recFishing;

        if (hvst.cageCode != null) {
            btnChooseCage.setText(hvst.cageCode);
        }

        if (hvst.availBins != null) {
            adapterBins.setValues(hvst.availBins.stream().map(x -> new TemplateRecyclerAdapter.BinEpc(x)).collect(Collectors.toList()));
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
                adapterBins.addUniqueItem(new TemplateRecyclerAdapter.BinEpc(binBarcode));
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

        List<String> newBins = adapterBins.getValues()
                .stream()
                .map(x -> x.epc)
                .collect(Collectors.toList());

        recFishing.availBins = newBins;
        recFishing.binWeightRecord.clear();

        for (String bin : newBins) {
            recFishing.binWeightRecord.addRecord(bin, 0, null, null, null);
        }

    }


    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recFishing.availBins == null || recFishing.availBins.isEmpty()) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.bins_to_use)));
            }

            if (recFishing.cageCode == null || recFishing.cageCode.isEmpty()) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.cage)));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            btnScanBin.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else if (!scanner_runnable.isReading()) {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            btnScanBin.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            btnScanBin.setText(R.string.scan_bin);
            mScanHandler.removeCallbacks(scanner_runnable);
        }
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
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
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    if (epcList != null && !epcList.isEmpty()) {
                        epcList.forEach(x ->
                                adapterBins.addUniqueItem(new TemplateRecyclerAdapter.BinEpc(x.toString()))
                        );

                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        adapterBins.notifyDataSetChanged();
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