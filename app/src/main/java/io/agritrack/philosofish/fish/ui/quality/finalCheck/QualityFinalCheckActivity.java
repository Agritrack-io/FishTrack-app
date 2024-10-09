package io.agritrack.philosofish.fish.ui.quality.finalCheck;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitFinalQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityFinal;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityPackage;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.barcode.BarcodeScanService;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.FinalQualityRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.fish.ui.quality.packaging.PackageQualityCheckLabelActivity;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class QualityFinalCheckActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;
    private ImageView ivSupport;
    private EditText  tvCurrentLot;
    private Spinner spFishLot;
    private Set<String> fishLotSet;
    private List<BinInfo> binInfos;
    private List<String> fishLots = new ArrayList<>();
    private ArrayAdapter<String> lotListAdapter;
    private SupportDialog supportDialog;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog,  confirmSaveDataDialog;
    private String currentLot, bestBefore;
    private Integer selectedExfoRating, selectedPaletteRating, selectedBoxRating;

    private ToggleGroup tgExfo, tgPalette, tgBox;
    private CheckBox cbCylindrical, cbExpanded, cbSoft, cbHead, cbBody, cbAreas;


    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiverFinal = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (!barcode.isEmpty() && barcode.length() >= 12) {
                    currentLot = barcode.substring(barcode.length() - 11);
                    //bestBefore = barcode.substring(barcode.length() - 21, barcode.length() - 15);
                    if (Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (currentLot.equals(recQualityFinal.lot)) {
                            CToast(getAppContext(), String.format(getResources().getString(R.string.lot_already_loaded), currentLot), Toast.LENGTH_LONG);
                            scanning = false;
                        } else {
                            FragmentManager fm = getSupportFragmentManager();
                            confirmNewLotDialog.setMessage(String.format(getResources().getString(R.string.box_on_another_lot), currentLot));
                            confirmNewLotDialog.showNow(fm, getString(R.string.confirm_selection));
                        }
                    }
                }
            }
        }
    };

    private void loadBarcodeInfo(String lot) {

        FinalQualityTransaction txQuality = db.finalQualityTransactionDAO().getByLot(lot);


        binInfos = db.binInfoDAO().getAll();

        fishLotSet = new TreeSet<>(binInfos.stream().filter(x -> x.lot != null).map(x -> x.lot).collect(Collectors.toList()));

        fishLots = new ArrayList<>();
        fishLots.addAll(fishLotSet);

        lotListAdapter = new ArrayAdapter<>(QualityFinalCheckActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);

        if (txQuality == null) {
            tvCurrentLot.setText(lot);
            String labelBB = null;
//            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
//                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
//            }
            // recQualityFinal.bestBefore = labelBB;
            tvCurrentLot.setText(currentLot);
            recQualityFinal.lot = currentLot;
            scanning = false;
        } else if (!txQuality.isSynced) {
            if (!Strings.isEmptyOrWhitespace(txQuality.fishingLot))  {
                int position = lotListAdapter.getPosition(txQuality.fishingLot);
                if (position != -1) {
                    spFishLot.setSelection(position);
                    spFishLot.setClickable(false);
                } else {
                    fishLots.add(txQuality.fishingLot);
                    lotListAdapter.notifyDataSetChanged();
                    position = lotListAdapter.getPosition(txQuality.fishingLot);
                    spFishLot.setSelection(position);
                    spFishLot.setClickable(false);
                }
            }
            tvCurrentLot.setText(txQuality.lot);
            //tvBestBefore.setText(txQuality.bestBefore.toString());
            loadStateFromDB(txQuality);
            scanning = false;
        } else {
            CToast(getAppContext(),String.format(getResources().getString(R.string.lot_label_control_done), txQuality.lot), Toast.LENGTH_LONG );
            scanning = false;
        }
    }

    private void loadStateFromDB(FinalQualityTransaction txQuality) {

        recQualityFinal.lot = txQuality.lot;
//        if (txQuality.bestBefore != null) {
//        }
        recQualityFinal.fishLot = txQuality.fishingLot;

        recQualityFinal.exfoRating = txQuality.exfoRating;
        recQualityFinal.paletteRating = txQuality.paletteRating;
        recQualityFinal.boxRating = txQuality.boxRating;
        recQualityFinal.expanded = txQuality.expanded;
        recQualityFinal.soft = txQuality.soft;
        recQualityFinal.cylinrical = txQuality.cylinrical;
        recQualityFinal.head = txQuality.head;
        recQualityFinal.body = txQuality.body;
        recQualityFinal.areas = txQuality.areas;


        recQualityFinal.sample1.size = txQuality.sizeFirst;
        recQualityFinal.sample1.boxType = txQuality.boxTypeFirst;
        recQualityFinal.sample1.labelPieces = txQuality.labelPiecesFirst;
        recQualityFinal.sample1.countedPieces = txQuality.countedPiecesFirst;
        recQualityFinal.sample1.underWeight1 = txQuality.underWeightFirst1;
        recQualityFinal.sample1.underWeight2 = txQuality.underWeightFirst2;
        recQualityFinal.sample1.underWeight3 = txQuality.underWeightFirst3;
        recQualityFinal.sample1.overWeight1 = txQuality.overWeightFirst1;
        recQualityFinal.sample1.overWeight2 = txQuality.overWeightFirst2;
        recQualityFinal.sample1.overWeight3 = txQuality.overWeightFirst3;
        recQualityFinal.sample1.netWeight = txQuality.netWeightFirst;
        recQualityFinal.sample1.iceQuantity = txQuality.iceQuantityFirst;
        recQualityFinal.sample1.fishTemp = txQuality.fishTempFirst;


        recQualityFinal.sample2.size = txQuality.sizeSecond;
        recQualityFinal.sample2.boxType = txQuality.boxTypeSecond;
        recQualityFinal.sample2.labelPieces = txQuality.labelPiecesSecond;
        recQualityFinal.sample2.countedPieces = txQuality.countedPiecesSecond;
        recQualityFinal.sample2.underWeight1 = txQuality.underWeightSecond1;
        recQualityFinal.sample2.underWeight2 = txQuality.underWeightSecond2;
        recQualityFinal.sample2.underWeight3 = txQuality.underWeightSecond3;
        recQualityFinal.sample2.overWeight1 = txQuality.overWeightSecond1;
        recQualityFinal.sample2.overWeight2 = txQuality.overWeightSecond2;
        recQualityFinal.sample2.overWeight3 = txQuality.overWeightSecond3;
        recQualityFinal.sample2.netWeight = txQuality.netWeightSecond;
        recQualityFinal.sample2.iceQuantity = txQuality.iceQuantitySecond;
        recQualityFinal.sample2.fishTemp = txQuality.fishTempSecond;


        recQualityFinal.sample3.size = txQuality.sizeThird;
        recQualityFinal.sample3.boxType = txQuality.boxTypeThird;
        recQualityFinal.sample3.labelPieces = txQuality.labelPiecesThird;
        recQualityFinal.sample3.countedPieces = txQuality.countedPiecesThird;
        recQualityFinal.sample3.underWeight1 = txQuality.underWeightThird1;
        recQualityFinal.sample3.underWeight2 = txQuality.underWeightThird2;
        recQualityFinal.sample3.underWeight3 = txQuality.underWeightThird3;
        recQualityFinal.sample3.overWeight1 = txQuality.overWeightThird1;
        recQualityFinal.sample3.overWeight2 = txQuality.overWeightThird2;
        recQualityFinal.sample3.overWeight3 = txQuality.overWeightThird3;
        recQualityFinal.sample3.netWeight = txQuality.netWeightThird;
        recQualityFinal.sample3.iceQuantity = txQuality.iceQuantityThird;
        recQualityFinal.sample3.fishTemp = txQuality.fishTempThird;

        recQualityFinal.foreignBody = txQuality.foreignBody;
        recQualityFinal.lotAccepted  = txQuality.lotAccepted;
        recQualityFinal.corrAction = txQuality.corrAction;
        recQualityFinal.discardedQty = txQuality.discardedQty;

        initControlsFromState();
    }

    private BarcodeScanService scanService;
    private Button btnScanBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_final_check_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFinalQuality);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // get  references of the controls
        assignCtrlVars();


        binInfos = db.binInfoDAO().getAll();

        fishLotSet = new TreeSet<>(binInfos.stream().filter(x -> x.lot != null).map(x -> x.lot).collect(Collectors.toList()));

        fishLots = new ArrayList<>();
        fishLots.addAll(fishLotSet);

        lotListAdapter = new ArrayAdapter<>(QualityFinalCheckActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);

        spFishLot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recQualityFinal.fishLot = parent.getItemAtPosition(position).toString(); //this is your selected item
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });



        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiverFinal, filter);

        confirmNewLotDialog= YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            FinalQualityTransaction tx = commitFinalQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityFinal.lot),Toast.LENGTH_LONG);
            }
            loadBarcodeInfo(currentLot);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });

        confirmSaveDataDialog= YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            FinalQualityTransaction tx = commitFinalQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityFinal.lot),Toast.LENGTH_LONG);
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinal);
            unregisterReceiver(receiverFinal);

            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
            unregisterReceiver(receiverFinal);

            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinal);

            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });

        btnScanBox.setOnClickListener(view -> {
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(QualityFinalCheckActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToFinalQualityConfirm);
        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinal);
                unregisterReceiver(receiverFinal);

                Intent i = new Intent(getApplicationContext(), QualityFinalCheckSecondActivity.class);
                startActivity(i);

            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToQualityMenu);
        ivBack.setOnClickListener(view -> {
            scanning = false;
            stopScanning();
            if (!Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
                FragmentManager fm = getSupportFragmentManager();
                confirmSaveDataDialog.setMessage(getString(R.string.save_lot_quality, recQualityFinal.lot));
                confirmSaveDataDialog.showNow(fm, getString(R.string.confirm_selection));
            } else {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinal);
                unregisterReceiver(receiverFinal);

                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        btnScanBox = findViewById(R.id.btnScanBin);

        tvCurrentLot = findViewById(R.id.lotNumber);
        spFishLot = findViewById(R.id.spFishLot);

        tgBox = findViewById(R.id.tgBoxCondition);
        tgBox.setOnCheckedChangeListener(this);

        tgPalette = findViewById(R.id.tgPaletteCondition);
        tgPalette.setOnCheckedChangeListener(this);

        tgExfo = findViewById(R.id.tgExfoliation);
        tgExfo.setOnCheckedChangeListener(this);

        cbAreas = findViewById(R.id.cbAreas);
        cbBody = findViewById(R.id.cbBody);
        cbHead = findViewById(R.id.cbHead);
        cbCylindrical = findViewById(R.id.cbCylindrical);
        cbExpanded = findViewById(R.id.cbExpanded);
        cbSoft = findViewById(R.id.cbSoft);

        ivSupport = findViewById(R.id.ivSupport);

    }

    private void initControlsFromState() {

        if (!Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
            tvCurrentLot.setText(recQualityFinal.lot);
        }


        if (!Strings.isEmptyOrWhitespace(recQualityFinal.fishLot)) {
            int position = lotListAdapter.getPosition(recQualityFinal.fishLot);
            if (position != -1) {
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            } else {
                fishLots.add(recQualityFinal.fishLot);
                lotListAdapter.notifyDataSetChanged();
                position = lotListAdapter.getPosition(recQualityFinal.fishLot);
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            }
        }
        FinalQualityRecord qltRecord = GlobalState.recQualityFinal;

        if (qltRecord.exfoRating != null) {
            switch (qltRecord.exfoRating) {
                case 1:
                    tgExfo.check(R.id.tbHighExfo);
                    selectedExfoRating = 1;
                    break;
                case 2:
                    tgExfo.check(R.id.tbMidExfo);
                    selectedExfoRating = 2;
                    break;
                case 3:
                    tgExfo.check(R.id.tbLightExfo);
                    selectedExfoRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.paletteRating != null) {
            switch (qltRecord.paletteRating) {
                case 1:
                    tgPalette.check(R.id.tbPaletteC);
                    selectedPaletteRating = 1;
                    break;
                case 2:
                    tgPalette.check(R.id.tbPaletteB);
                    selectedPaletteRating = 2;
                    break;
                case 3:
                    tgPalette.check(R.id.tbPaletteA);
                    selectedPaletteRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.boxRating != null) {
            switch (qltRecord.boxRating) {
                case 1:
                    tgBox.check(R.id.tbBoxC);
                    selectedBoxRating = 1;
                    break;
                case 2:
                    tgBox.check(R.id.tbBoxB);
                    selectedBoxRating = 2;
                    break;
                case 3:
                    tgBox.check(R.id.tbBoxA);
                    selectedBoxRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.areas != null && qltRecord.areas) {
            cbAreas.setChecked(true);
        }
        if (qltRecord.body != null && qltRecord.body) {
            cbBody.setChecked(true);
        }
        if (qltRecord.head != null && qltRecord.head) {
            cbHead.setChecked(true);
        }
        if (qltRecord.cylinrical != null && qltRecord.cylinrical) {
            cbCylindrical.setChecked(true);
        }
        if (qltRecord.expanded != null && qltRecord.expanded) {
            cbExpanded.setChecked(true);
        }
        if (qltRecord.soft != null &&qltRecord.soft) {
            cbSoft.setChecked(true);
        }

    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbLightExfo) {
            selectedExfoRating = 3;
        } else if (checkedId == R.id.tbMidExfo) {
            selectedExfoRating = 2;
        } else if (checkedId == R.id.tbHighExfo) {
            selectedExfoRating = 1;
        } else if (checkedId == R.id.tbPaletteA) {
            selectedPaletteRating = 3;
        }else if (checkedId == R.id.tbPaletteB) {
            selectedPaletteRating = 2;
        } else if (checkedId == R.id.tbPaletteC) {
            selectedPaletteRating = 1;
        } else if (checkedId == R.id.tbBoxA) {
            selectedBoxRating = 3;
        } else if (checkedId == R.id.tbBoxB) {
            selectedBoxRating = 2;
        }else if (checkedId == R.id.tbBoxC) {
            selectedBoxRating = 1;
        }
    }


    private void startScanning() {
        if (scanService != null) {
            scanning = true;
            scanService.scan();
        }
    }

    private void stopScanning() {
        if (scanService != null) {
            scanService.stopScan();
            scanning = false;
        }
    }

    private void updateState() {

        recQualityFinal.lot = Strings.isEmptyOrWhitespace(tvCurrentLot.getText().toString()) ? null : tvCurrentLot.getText().toString();

        recQualityFinal.fishLot = spFishLot.getSelectedItem() == null ? null : spFishLot.getSelectedItem().toString();

        recQualityFinal.exfoRating = selectedExfoRating;
        recQualityFinal.paletteRating = selectedPaletteRating;
        recQualityFinal.boxRating = selectedBoxRating;

        if(cbSoft.isChecked()) {
            recQualityFinal.soft = true;
        } else {
            recQualityFinal.soft = false;
        }
        if(cbBody.isChecked()) {
            recQualityFinal.body = true;
        } else {
            recQualityFinal.body = false;
        }
        if(cbHead.isChecked()) {
            recQualityFinal.head = true;
        } else {
            recQualityFinal.head = false;
        }
        if(cbCylindrical.isChecked()) {
            recQualityFinal.cylinrical = true;
        } else {
            recQualityFinal.cylinrical = false;
        }
        if(cbExpanded.isChecked()) {
            recQualityFinal.expanded = true;
        } else {
            recQualityFinal.expanded = false;
        }
        if(cbAreas.isChecked()) {
            recQualityFinal.areas = true;
        } else {
            recQualityFinal.areas = false;
        }

    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_number)));
            }

            if (Strings.isEmptyOrWhitespace(recQualityFinal.fishLot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_fishing)));
            }
        }

        return sb.toString();
    }


    @Override
    protected void onResume() {
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFinal);
        super.onStop();
    }
}
