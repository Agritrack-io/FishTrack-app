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
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.barcode.BarcodeScanService;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.PackageQualityRecord;
import io.agritrack.philosofish.fish.state.QualityRecord;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityFreshnessActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;
    private ToggleGroup tgEyeCondition, tgGillCondition, tgFreshCondition, tgSkinCondition;
    private int selectedEyeRating, selectedGillRating, selectedSkinRating, selectedFreshRating, overallGrade;
    private ImageView ivSupport;
    private TextView  tvOverall;
    private EditText tvCurrentLot, tvBestBefore;
    private SupportDialog supportDialog;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog,  confirmSaveDataDialog;
    private String currentLot, bestBefore;


    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (!barcode.isEmpty() && barcode.length() >= 10) {
                     currentLot = barcode.substring(barcode.length() - 11);
                     bestBefore = barcode.substring(barcode.length() - 21, barcode.length() - 15);
                    if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (barcode.equals(recQualityPackage.lot)) {
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

        if (txQuality == null) {
            String labelBB = null;
            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
            }
            tvBestBefore.setText(labelBB);
            tvCurrentLot.setText(lot);
            recQualityPackage.bestBefore = labelBB;
            recQualityPackage.lot = currentLot;
            scanning = false;
            return;
        } else if (!txQuality.isFreshSynced) {
            tvCurrentLot.setText(txQuality.lot);
            tvBestBefore.setText(txQuality.bestBefore.toString());
            loadStatefromDB(txQuality);
            scanning = false;
            return;
        } else {
            CToast(getAppContext(),String.format(getResources().getString(R.string.lot_package_control_done), txQuality.lot), Toast.LENGTH_LONG );
            scanning = false;
            return;
        }
    }

    private void loadStatefromDB(PackageQualityTransaction txQuality) {

        recQualityPackage.lot = txQuality.lot;
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        confirmNewLotDialog= YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            PackageQualityTransaction tx = commitPackageFreshQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot),Toast.LENGTH_LONG);
            }
            loadBarcodeInfo(currentLot);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });

        confirmSaveDataDialog= YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            PackageQualityTransaction tx = commitPackageFreshQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot),Toast.LENGTH_LONG);
            }
            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
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
        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
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
        tvBestBefore = findViewById(R.id.tvBB);
        tvOverall = findViewById(R.id.overallGrade);

        tvBestBefore.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});


        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PackageQualityRecord packQualityRecord = recQualityPackage;

        if (!Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
            tvCurrentLot.setText(recQualityPackage.lot);
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
        if ( (selectedSkinRating > 0 && selectedSkinRating <= 4)
                && (selectedGillRating > 0 && selectedGillRating <= 4) && (selectedEyeRating > 0 && selectedEyeRating <= 4) ) {
            overallGrade =  selectedEyeRating + selectedGillRating + selectedSkinRating;
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
        recQualityPackage.bestBefore = tvBestBefore.getText().toString();
        recQualityPackage.eyeGrade = selectedEyeRating;
        recQualityPackage.gillGrade = selectedGillRating;
        recQualityPackage.skinGrade = selectedSkinRating;
        recQualityPackage.freshGrade = selectedFreshRating;
        recQualityPackage.overallGrade = (selectedSkinRating > 0 && selectedEyeRating > 0 &&selectedGillRating > 0 ?
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

        }

        return sb.toString();
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
        }else if (checkedId == R.id.tbExtraGill) {
            selectedGillRating = 4;
        } else if (checkedId == R.id.tbAGill) {
            selectedGillRating = 3;
        } else if (checkedId == R.id.tbBGill) {
            selectedGillRating = 2;
        } else if (checkedId == R.id.tbFailGill) {
            selectedGillRating = 1;
        }else if (checkedId == R.id.tbExtraFresh) {
            selectedFreshRating = 4;
        } else if (checkedId == R.id.tbAFresh) {
            selectedFreshRating = 3;
        } else if (checkedId == R.id.tbBFresh) {
            selectedFreshRating = 2;
        } else if (checkedId == R.id.tbFailFresh) {
            selectedFreshRating = 1;
        }else if (checkedId == R.id.tbExtraSkin) {
            selectedSkinRating = 4;
        } else if (checkedId == R.id.tbASkin) {
            selectedSkinRating = 3;
        } else if (checkedId == R.id.tbBSkin) {
            selectedSkinRating = 2;
        } else if (checkedId == R.id.tbFailSkin) {
            selectedSkinRating = 1;
        }

        if ((selectedSkinRating > 0 && selectedSkinRating <= 4)
                && (selectedGillRating > 0 && selectedGillRating <= 4) && (selectedEyeRating > 0 && selectedEyeRating <= 4) ) {
            overallGrade =  selectedEyeRating + selectedGillRating + selectedSkinRating;
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}

