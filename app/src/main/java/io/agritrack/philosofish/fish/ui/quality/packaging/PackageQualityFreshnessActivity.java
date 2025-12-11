package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitPackageFreshQuality;
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
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.PackageQualityRecord;
import io.agritrack.philosofish.fish.state.QualityRecord;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityFreshnessActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;
    private ToggleGroup tgEyeCondition, tgGillCondition, tgFreshCondition, tgSkinCondition;
    private int selectedEyeRating, selectedGillRating, selectedSkinRating, selectedFreshRating, overallGrade;
    private ImageView ivSupport;
    private TextView tvOverall;
    private EditText tvCurrentLot;
    private Spinner spFishLot;
    private Set<String> fishLotSet;
    private List<BinInfo> binInfos;
    private List<String> fishLots = new ArrayList<>();
    private ArrayAdapter<String> lotListAdapter;
    private SupportDialog supportDialog;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog, confirmSaveDataDialog;
    private String currentLot, bestBefore;


    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiverFresh = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (!barcode.isEmpty() && barcode.length() >= 12) {
                    currentLot = barcode.substring(barcode.length() - 11);
                    // bestBefore = barcode.substring(barcode.length() - 21, barcode.length() - 15);
                    if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (currentLot.equals(recQualityPackage.lot)) {
                            CToast(getAppContext(), String.format(getResources().getString(R.string.lot_already_loaded), currentLot), Toast.LENGTH_LONG);
                            scanning = false;
                            return;
                        } else {
                            FragmentManager fm = getSupportFragmentManager();
                            confirmNewLotDialog.setMessage(String.format(getResources().getString(R.string.box_on_another_lot), currentLot));
                            confirmNewLotDialog.showNow(fm, getString(R.string.confirm_selection));
                            return;
                        }
                    }
                }
            }
        }
    };

    private void loadBarcodeInfo(String lot) {

        PackageQualityTransaction txQuality = db.packageQualityTransactionDAO().getByLot(lot);

        binInfos = db.binInfoDAO().getAll();

        fishLotSet = new TreeSet<>(binInfos.stream().filter(x -> x.lot != null).map(x -> x.lot).collect(Collectors.toList()));

        fishLots = new ArrayList<>();
        fishLots.addAll(fishLotSet);

        lotListAdapter = new ArrayAdapter<>(PackageQualityFreshnessActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);
        if (txQuality == null) {
            String labelBB = null;
//            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
//                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
//            }
            //spFishLot.setText(labelBB);
            tvCurrentLot.setText(lot);
            recQualityPackage.bestBefore = labelBB;
            recQualityPackage.lot = currentLot;
            updateNextButtonState();
            scanning = false;
            return;

        } else if (!txQuality.isFreshSynced) {
            if (!Strings.isEmptyOrWhitespace(txQuality.fishingLot)) {
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
            //spFishLot.setText(txQuality.bestBefore.toString());
            loadStatefromDB(txQuality);
            scanning = false;
            return;
        } else {
            CToast(getAppContext(), String.format(getResources().getString(R.string.lot_package_control_done), txQuality.lot), Toast.LENGTH_LONG);
            scanning = false;
            return;
        }
    }

    private void loadStatefromDB(PackageQualityTransaction txQuality) {

        recQualityPackage.lot = txQuality.lot;
        updateNextButtonState();
        recQualityPackage.fishLot = txQuality.fishingLot;
        if (txQuality.bestBefore != null) {
            recQualityPackage.bestBefore = txQuality.bestBefore.toString();
        }
        recQualityPackage.freshGrade = txQuality.freshGrade;
        recQualityPackage.overallGrade = txQuality.overallGrade;
        recQualityPackage.skinGrade = txQuality.skinGrade;
        recQualityPackage.eyeGrade = txQuality.eyeGrade;
        recQualityPackage.gillGrade = txQuality.gillGrade;
        recQualityPackage.crookedMouth = txQuality.crookedMouth;
        recQualityPackage.lowerJaw = txQuality.lowerJaw;
        recQualityPackage.jawOver = txQuality.jawOver;
        recQualityPackage.operculum = txQuality.operculum;
        recQualityPackage.lordosis = txQuality.lordosis;
        recQualityPackage.shortening = txQuality.shortening;
        recQualityPackage.skeletical = txQuality.skeletical;
        recQualityPackage.tailDeformity = txQuality.tailDeformity;
        recQualityPackage.tailDeform = txQuality.tailDeform;
        recQualityPackage.finDeform = txQuality.finDeform;
        recQualityPackage.woundsDeform = txQuality.woundsDeform;
        recQualityPackage.hemSlight = txQuality.hemSlight;
        recQualityPackage.hemDiffuse = txQuality.hemDiffuse;
        recQualityPackage.hemSpots = txQuality.hemSpots;
        recQualityPackage.hemWounds = txQuality.hemWounds;
        recQualityPackage.eyeBlurred = txQuality.eyeBlurred;
        recQualityPackage.eyeCured = txQuality.eyeCured;
        recQualityPackage.eyeBlind = txQuality.eyeBlind;
        recQualityPackage.eyeBleed = txQuality.eyeBleed;
        recQualityPackage.gillMucus = txQuality.gillMucus;
        recQualityPackage.gillBloody = txQuality.gillBloody;
        recQualityPackage.gillBrown = txQuality.gillBrown;
        recQualityPackage.gillDiscolor = txQuality.gillDiscolor;
        recQualityPackage.headDeform = txQuality.headDeform;

        initControlsFromState();
    }

    private BarcodeScanService scanService;
    private Button btnScanBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_package_freshness_check);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageFreshCheck);
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

        lotListAdapter = new ArrayAdapter<>(PackageQualityFreshnessActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        spFishLot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recQualityPackage.fishLot = parent.getItemAtPosition(position).toString(); //this is your selected item
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        confirmNewLotDialog = YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            PackageQualityTransaction tx = commitPackageFreshQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot), Toast.LENGTH_LONG);
            }
            loadBarcodeInfo(currentLot);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiverFresh, filter);

        confirmSaveDataDialog = YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            PackageQualityTransaction tx = commitPackageFreshQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot), Toast.LENGTH_LONG);
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFresh);
            unregisterReceiver(receiverFresh);

            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFresh);
            unregisterReceiver(receiverFresh);

            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
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
            supportDialog = new SupportDialog(PackageQualityFreshnessActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityDysmorphias);

        // Disable NEXT button until LOT is scanned
        ivNext.setEnabled(false);
        ivNext.setAlpha(0.3f);

        ivNext.setOnClickListener(view -> {
            scanning = false;
            stopScanning();

            if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                CToast(getApplicationContext(), "Please scan a LOT before continuing.", Toast.LENGTH_LONG);
                return;
            }

            updateState();
            String v = validate();

            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFresh);
                unregisterReceiver(receiverFresh);

                Intent i = new Intent(getApplicationContext(), PackageQualityDysmorphiasActivity.class);
                startActivity(i);
            }
        });


        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMenu);
        ivBack.setOnClickListener(view -> {
            scanning = false;
            stopScanning();
            if (!Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                FragmentManager fm = getSupportFragmentManager();
                confirmSaveDataDialog.setMessage(getString(R.string.save_lot_quality, recQualityPackage.lot));
                confirmSaveDataDialog.showNow(fm, getString(R.string.confirm_selection));
            } else {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFresh);
                unregisterReceiver(receiverFresh);

                Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        tgEyeCondition = findViewById(R.id.tgEyeCondition);
        tgEyeCondition.setOnCheckedChangeListener(this);

        tgGillCondition = findViewById(R.id.tgGillCondition);
        tgGillCondition.setOnCheckedChangeListener(this);

        tgSkinCondition = findViewById(R.id.tgSkinCondition);
        tgSkinCondition.setOnCheckedChangeListener(this);

        tgFreshCondition = findViewById(R.id.tgFreshCondition);
        tgFreshCondition.setOnCheckedChangeListener(this);

        btnScanBox = findViewById(R.id.btnScanBin);

        tvCurrentLot = findViewById(R.id.lotNumber);

        spFishLot = findViewById(R.id.spFishLot);
        tvOverall = findViewById(R.id.overallGrade);

        //spFishLot.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});


        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PackageQualityRecord packQualityRecord = recQualityPackage;

        if (!Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
            tvCurrentLot.setText(recQualityPackage.lot);
        }

        if (!Strings.isEmptyOrWhitespace(recQualityPackage.fishLot)) {
            int position = lotListAdapter.getPosition(recQualityPackage.fishLot);
            if (position != -1) {
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            } else {
                fishLots.add(recQualityPackage.fishLot);
                lotListAdapter.notifyDataSetChanged();
                position = lotListAdapter.getPosition(recQualityPackage.fishLot);
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            }
        }
        if (!Strings.isEmptyOrWhitespace(recQualityPackage.bestBefore)) {
            tvCurrentLot.setText(recQualityPackage.bestBefore);
        }

        if (packQualityRecord.eyeGrade != null) {
            switch (packQualityRecord.eyeGrade) {
                case 1:
                    tgEyeCondition.check(R.id.tbFailEyes);
                    selectedEyeRating = 1;
                    break;
                case 2:
                    tgEyeCondition.check(R.id.tbBEyes);
                    selectedEyeRating = 2;
                    break;
                case 3:
                    tgEyeCondition.check(R.id.tbAEyes);
                    selectedEyeRating = 3;
                    break;
                case 4:
                    tgEyeCondition.check(R.id.tbExtraEyes);
                    selectedEyeRating = 4;
                    break;
                default:
                    break;
            }
        }

        if (packQualityRecord.gillGrade != null) {
            switch (packQualityRecord.gillGrade) {
                case 1:
                    tgGillCondition.check(R.id.tbFailGill);
                    selectedGillRating = 1;
                    break;
                case 2:
                    tgGillCondition.check(R.id.tbBGill);
                    selectedGillRating = 2;
                    break;
                case 3:
                    tgGillCondition.check(R.id.tbAGill);
                    selectedGillRating = 3;
                    break;
                case 4:
                    tgGillCondition.check(R.id.tbExtraGill);
                    selectedGillRating = 4;
                    break;
                default:
                    break;
            }
        }

        if (packQualityRecord.skinGrade != null) {
            switch (packQualityRecord.skinGrade) {
                case 1:
                    tgSkinCondition.check(R.id.tbFailSkin);
                    selectedSkinRating = 1;
                    break;
                case 2:
                    tgSkinCondition.check(R.id.tbBSkin);
                    selectedSkinRating = 2;
                    break;
                case 3:
                    tgSkinCondition.check(R.id.tbASkin);
                    selectedSkinRating = 3;
                    break;
                case 4:
                    tgSkinCondition.check(R.id.tbExtraSkin);
                    selectedSkinRating = 4;
                    break;
                default:
                    break;
            }
        }

        if (packQualityRecord.freshGrade != null) {

            switch (packQualityRecord.freshGrade) {
                case 1:
                    tgFreshCondition.check(R.id.tbFailFresh);
                    selectedFreshRating = 1;
                    break;
                case 2:
                    tgFreshCondition.check(R.id.tbBFresh);
                    selectedFreshRating = 2;
                    break;
                case 3:
                    tgFreshCondition.check(R.id.tbAFresh);
                    selectedFreshRating = 3;
                    break;
                case 4:
                    tgFreshCondition.check(R.id.tbExtraFresh);
                    selectedFreshRating = 4;
                    break;
                default:
                    break;
            }
        }
        if ((selectedSkinRating > 0 && selectedSkinRating <= 4)
                && (selectedGillRating > 0 && selectedGillRating <= 4) && (selectedEyeRating > 0 && selectedEyeRating <= 4)) {
            overallGrade = selectedEyeRating + selectedGillRating + selectedSkinRating;
            tvOverall.setText((overallGrade + ""));
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

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        recQualityPackage.lot = tvCurrentLot.getText().toString();
        recQualityPackage.fishLot = spFishLot.getSelectedItem() == null ? null : spFishLot.getSelectedItem().toString();
        recQualityPackage.eyeGrade = selectedEyeRating;
        recQualityPackage.gillGrade = selectedGillRating;
        recQualityPackage.skinGrade = selectedSkinRating;
        recQualityPackage.freshGrade = selectedFreshRating;
        recQualityPackage.overallGrade = (selectedSkinRating > 0 && selectedEyeRating > 0 && selectedGillRating > 0 ?
                selectedEyeRating + selectedGillRating + selectedSkinRating : null);


        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (!Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                if (recQualityPackage.eyeGrade < 1 || recQualityPackage.eyeGrade > 4) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.eye_evaluation)));
                }
                if (recQualityPackage.skinGrade < 1 || recQualityPackage.skinGrade > 4) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.skin_condition)));

                }
                if (recQualityPackage.freshGrade < 1 || recQualityPackage.freshGrade > 4) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.freshness_evaluation)));

                }
                if (recQualityPackage.gillGrade < 1 || recQualityPackage.gillGrade > 4) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.gill_condition)));

                }
            } else {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_number)));
            }

            if (Strings.isEmptyOrWhitespace(recQualityPackage.fishLot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_fishing)));
            }
        }

        return sb.toString();
    }


    private void updateNextButtonState() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityDysmorphias);
        boolean enabled = !Strings.isEmptyOrWhitespace(recQualityPackage.lot);
        ivNext.setEnabled(enabled);
        ivNext.setAlpha(enabled ? 1f : 0.3f);
    }


    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbExtraEyes) {
            selectedEyeRating = 4;
        } else if (checkedId == R.id.tbAEyes) {
            selectedEyeRating = 3;
        } else if (checkedId == R.id.tbBEyes) {
            selectedEyeRating = 2;
        } else if (checkedId == R.id.tbFailEyes) {
            selectedEyeRating = 1;
        } else if (checkedId == R.id.tbExtraGill) {
            selectedGillRating = 4;
        } else if (checkedId == R.id.tbAGill) {
            selectedGillRating = 3;
        } else if (checkedId == R.id.tbBGill) {
            selectedGillRating = 2;
        } else if (checkedId == R.id.tbFailGill) {
            selectedGillRating = 1;
        } else if (checkedId == R.id.tbExtraFresh) {
            selectedFreshRating = 4;
        } else if (checkedId == R.id.tbAFresh) {
            selectedFreshRating = 3;
        } else if (checkedId == R.id.tbBFresh) {
            selectedFreshRating = 2;
        } else if (checkedId == R.id.tbFailFresh) {
            selectedFreshRating = 1;
        } else if (checkedId == R.id.tbExtraSkin) {
            selectedSkinRating = 4;
        } else if (checkedId == R.id.tbASkin) {
            selectedSkinRating = 3;
        } else if (checkedId == R.id.tbBSkin) {
            selectedSkinRating = 2;
        } else if (checkedId == R.id.tbFailSkin) {
            selectedSkinRating = 1;
        }

        if ((selectedSkinRating > 0 && selectedSkinRating <= 4)
                && (selectedGillRating > 0 && selectedGillRating <= 4) && (selectedEyeRating > 0 && selectedEyeRating <= 4)) {
            overallGrade = selectedEyeRating + selectedGillRating + selectedSkinRating;
            tvOverall.setText(overallGrade + "");
        }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverFresh);
        super.onStop();
    }
}

