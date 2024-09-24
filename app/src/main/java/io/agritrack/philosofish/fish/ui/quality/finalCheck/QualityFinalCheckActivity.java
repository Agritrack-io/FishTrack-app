package io.agritrack.philosofish.fish.ui.quality.finalCheck;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitFinalQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityFinal;
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
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class QualityFinalCheckActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private EditText size1, size2, size3, type1, type2, type3, number1, number2, number3, actual1, actual2, actual3,
        under1, under2, under3, over1, over2, over3,  net1, net2, net3, ice1, ice2, ice3, temp1, temp2, temp3, tvCurrentLot, tvBestBefore;
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
                    if (Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (barcode.equals(recQualityFinal.lot)) {
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

        if (txQuality == null) {
            tvCurrentLot.setText(lot);
            String labelBB = null;
            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
            }
            tvBestBefore.setText(labelBB);
            recQualityFinal.bestBefore = labelBB;
            recQualityFinal.lot = currentLot;
            scanning = false;
        } else if (!txQuality.isSynced) {
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
        recQualityFinal.sample1.underWeight = txQuality.underWeightFirst;
        recQualityFinal.sample1.overWeight = txQuality.overWeightFirst;
        recQualityFinal.sample1.netWeight = txQuality.netWeightFirst;
        recQualityFinal.sample1.iceQuantity = txQuality.iceQuantityFirst;
        recQualityFinal.sample1.fishTemp = txQuality.fishTempFirst;


        recQualityFinal.sample2.size = txQuality.sizeSecond;
        recQualityFinal.sample2.boxType = txQuality.boxTypeSecond;
        recQualityFinal.sample2.labelPieces = txQuality.labelPiecesSecond;
        recQualityFinal.sample2.countedPieces = txQuality.countedPiecesSecond;
        recQualityFinal.sample2.underWeight = txQuality.underWeightSecond;
        recQualityFinal.sample2.overWeight = txQuality.overWeightSecond;
        recQualityFinal.sample2.netWeight = txQuality.netWeightSecond;
        recQualityFinal.sample2.iceQuantity = txQuality.iceQuantitySecond;
        recQualityFinal.sample2.fishTemp = txQuality.fishTempSecond;


        recQualityFinal.sample3.size = txQuality.sizeThird;
        recQualityFinal.sample3.boxType = txQuality.boxTypeThird;
        recQualityFinal.sample3.labelPieces = txQuality.labelPiecesThird;
        recQualityFinal.sample3.countedPieces = txQuality.countedPiecesThird;
        recQualityFinal.sample3.underWeight = txQuality.underWeightThird;
        recQualityFinal.sample3.overWeight = txQuality.overWeightThird;
        recQualityFinal.sample3.netWeight = txQuality.netWeightThird;
        recQualityFinal.sample3.iceQuantity = txQuality.iceQuantityThird;
        recQualityFinal.sample3.fishTemp = txQuality.fishTempThird;

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


        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

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
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
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
                    Intent i = new Intent(getApplicationContext(), QualityFinalCheckConfirmActivity.class);
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
                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        btnScanBox = findViewById(R.id.btnScanBin);

        tvCurrentLot = findViewById(R.id.lotNumber);
        tvBestBefore = findViewById(R.id.etBB);

        size1 = findViewById(R.id.etSize1);
        type1 = findViewById(R.id.etType1);
        number1 = findViewById(R.id.etNumber1);
        actual1 = findViewById(R.id.etActual1);
        under1 = findViewById(R.id.etUnder1);
        over1 = findViewById(R.id.etOver1);
        net1 = findViewById(R.id.etNet1);
        ice1 = findViewById(R.id.etIce1);
        temp1 = findViewById(R.id.etTemp1);
        size2 = findViewById(R.id.etSize2);
        type2 = findViewById(R.id.etType2);
        number2 = findViewById(R.id.etNumber2);
        actual2 = findViewById(R.id.etActual2);
        under2 = findViewById(R.id.etUnder2);
        over2 = findViewById(R.id.etOver2);
        net2 = findViewById(R.id.etNet2);
        ice2 = findViewById(R.id.etIce2);
        temp2 = findViewById(R.id.etTemp2);
        size3 = findViewById(R.id.etSize3);
        type3 = findViewById(R.id.etType3);
        number3 = findViewById(R.id.etNumber3);
        actual3 = findViewById(R.id.etActual3);
        under3 = findViewById(R.id.etUnder3);
        over3 = findViewById(R.id.etOver3);
        net3 = findViewById(R.id.etNet3);
        ice3 = findViewById(R.id.etIce3);
        temp3 = findViewById(R.id.etTemp3);

        ivSupport = findViewById(R.id.ivSupport);

        tvBestBefore.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

    }

    private void initControlsFromState() {

        if (!Strings.isEmptyOrWhitespace(recQualityFinal.sample1.size)) {
            size1.setText(recQualityFinal.sample1.size);
        }
        if (recQualityFinal.sample1.boxType != null) {
            type1.setText(String.valueOf(recQualityFinal.sample1.boxType));
        }
        if (recQualityFinal.sample1.labelPieces != null) {
            number1.setText(String.valueOf(recQualityFinal.sample1.labelPieces));
        }
        if (recQualityFinal.sample1.countedPieces != null) {
            actual1.setText(String.valueOf(recQualityFinal.sample1.countedPieces));
        }
        if (recQualityFinal.sample1.underWeight != null) {
            under1.setText(String.valueOf(recQualityFinal.sample1.underWeight));
        }
        if (recQualityFinal.sample1.overWeight != null) {
            over1.setText(String.valueOf(recQualityFinal.sample1.overWeight));
        }
        if (recQualityFinal.sample1.netWeight != null) {
            net1.setText(String.valueOf(recQualityFinal.sample1.netWeight));
        }
        if (recQualityFinal.sample1.iceQuantity != null) {
            ice1.setText(String.valueOf(recQualityFinal.sample1.iceQuantity));
        }
        if (recQualityFinal.sample1.fishTemp != null) {
            temp1.setText(String.valueOf(recQualityFinal.sample1.fishTemp));
        }


        if (Strings.isEmptyOrWhitespace(recQualityFinal.sample2.size)) {
            size2.setText(recQualityFinal.sample2.size);
        }
        if (recQualityFinal.sample2.boxType != null) {
            type2.setText(String.valueOf(recQualityFinal.sample2.boxType));
        }
        if (recQualityFinal.sample2.labelPieces != null) {
            number2.setText(String.valueOf(recQualityFinal.sample2.labelPieces));
        }
        if (recQualityFinal.sample2.countedPieces != null) {
            actual2.setText(String.valueOf(recQualityFinal.sample2.countedPieces));
        }
        if (recQualityFinal.sample2.underWeight != null) {
            under2.setText(String.valueOf(recQualityFinal.sample2.underWeight));
        }
        if (recQualityFinal.sample2.overWeight != null) {
            over2.setText(String.valueOf(recQualityFinal.sample2.overWeight));
        }
        if (recQualityFinal.sample2.netWeight != null) {
            net2.setText(String.valueOf(recQualityFinal.sample2.netWeight));
        }
        if (recQualityFinal.sample2.iceQuantity != null) {
            ice2.setText(String.valueOf(recQualityFinal.sample2.iceQuantity));
        }
        if (recQualityFinal.sample2.fishTemp != null) {
            temp2.setText(String.valueOf(recQualityFinal.sample2.fishTemp));
        }


        if (Strings.isEmptyOrWhitespace(recQualityFinal.sample3.size)) {
            size3.setText(recQualityFinal.sample3.size);
        }
        if (recQualityFinal.sample3.boxType != null) {
            type3.setText(String.valueOf(recQualityFinal.sample3.boxType));
        }
        if (recQualityFinal.sample3.labelPieces != null) {
            number3.setText(String.valueOf(recQualityFinal.sample3.labelPieces));
        }
        if (recQualityFinal.sample3.countedPieces != null) {
            actual3.setText(String.valueOf(recQualityFinal.sample3.countedPieces));
        }
        if (recQualityFinal.sample3.underWeight != null) {
            under3.setText(String.valueOf(recQualityFinal.sample3.underWeight));
        }
        if (recQualityFinal.sample3.overWeight != null) {
            over3.setText(String.valueOf(recQualityFinal.sample3.overWeight));
        }
        if (recQualityFinal.sample3.netWeight != null) {
            net3.setText(String.valueOf(recQualityFinal.sample3.netWeight));
        }
        if (recQualityFinal.sample3.iceQuantity != null) {
            ice3.setText(String.valueOf(recQualityFinal.sample3.iceQuantity));
        }
        if (recQualityFinal.sample3.fishTemp != null) {
            temp3.setText(String.valueOf(recQualityFinal.sample3.fishTemp));
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
        recQualityFinal.sample1.size = Strings.isEmptyOrWhitespace(size1.getText().toString()) ? null : size1.getText().toString();
        recQualityFinal.sample1.boxType = Strings.isEmptyOrWhitespace(type1.getText().toString()) ? null : Integer.valueOf(type1.getText().toString());
        recQualityFinal.sample1.labelPieces = Strings.isEmptyOrWhitespace(number1.getText().toString()) ? null : Integer.valueOf(number1.getText().toString());
        recQualityFinal.sample1.countedPieces = Strings.isEmptyOrWhitespace(actual1.getText().toString()) ? null : Integer.valueOf(actual1.getText().toString());
        recQualityFinal.sample1.underWeight = Strings.isEmptyOrWhitespace(under1.getText().toString()) ? null : Integer.valueOf(under1.getText().toString());
        recQualityFinal.sample1.overWeight = Strings.isEmptyOrWhitespace(over1.getText().toString()) ? null : Integer.valueOf(over1.getText().toString());
        recQualityFinal.sample1.netWeight = Strings.isEmptyOrWhitespace(net1.getText().toString()) ? null : Integer.valueOf(net1.getText().toString());
        recQualityFinal.sample1.iceQuantity = Strings.isEmptyOrWhitespace(ice1.getText().toString()) ? null : Integer.valueOf(ice1.getText().toString());
        recQualityFinal.sample1.fishTemp = Strings.isEmptyOrWhitespace(temp1.getText().toString()) ? null : Integer.valueOf(temp1.getText().toString());

        recQualityFinal.sample2.size = Strings.isEmptyOrWhitespace(size2.getText().toString()) ? null : size2.getText().toString();
        recQualityFinal.sample2.boxType = Strings.isEmptyOrWhitespace(type2.getText().toString()) ? null : Integer.valueOf(type2.getText().toString());
        recQualityFinal.sample2.labelPieces = Strings.isEmptyOrWhitespace(number2.getText().toString()) ? null : Integer.valueOf(number2.getText().toString());
        recQualityFinal.sample2.countedPieces = Strings.isEmptyOrWhitespace(actual2.getText().toString()) ? null : Integer.valueOf(actual2.getText().toString());
        recQualityFinal.sample2.underWeight = Strings.isEmptyOrWhitespace(under2.getText().toString()) ? null : Integer.valueOf(under2.getText().toString());
        recQualityFinal.sample2.overWeight = Strings.isEmptyOrWhitespace(over2.getText().toString()) ? null : Integer.valueOf(over2.getText().toString());
        recQualityFinal.sample2.netWeight = Strings.isEmptyOrWhitespace(net2.getText().toString()) ? null : Integer.valueOf(net2.getText().toString());
        recQualityFinal.sample2.iceQuantity = Strings.isEmptyOrWhitespace(ice2.getText().toString()) ? null : Integer.valueOf(ice2.getText().toString());
        recQualityFinal.sample2.fishTemp = Strings.isEmptyOrWhitespace(temp2.getText().toString()) ? null : Integer.valueOf(temp2.getText().toString());

        recQualityFinal.sample3.size = Strings.isEmptyOrWhitespace(size3.getText().toString()) ? null : size3.getText().toString();
        recQualityFinal.sample3.boxType = Strings.isEmptyOrWhitespace(type3.getText().toString()) ? null : Integer.valueOf(type3.getText().toString());
        recQualityFinal.sample3.labelPieces = Strings.isEmptyOrWhitespace(number3.getText().toString()) ? null : Integer.valueOf(number3.getText().toString());
        recQualityFinal.sample3.countedPieces = Strings.isEmptyOrWhitespace(actual3.getText().toString()) ? null : Integer.valueOf(actual3.getText().toString());
        recQualityFinal.sample3.underWeight = Strings.isEmptyOrWhitespace(under3.getText().toString()) ? null : Integer.valueOf(under3.getText().toString());
        recQualityFinal.sample3.overWeight = Strings.isEmptyOrWhitespace(over3.getText().toString()) ? null : Integer.valueOf(over3.getText().toString());
        recQualityFinal.sample3.netWeight = Strings.isEmptyOrWhitespace(net3.getText().toString()) ? null : Integer.valueOf(net3.getText().toString());
        recQualityFinal.sample3.iceQuantity = Strings.isEmptyOrWhitespace(ice3.getText().toString()) ? null : Integer.valueOf(ice3.getText().toString());
        recQualityFinal.sample3.fishTemp = Strings.isEmptyOrWhitespace(temp3.getText().toString()) ? null : Integer.valueOf(temp3.getText().toString());


    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_number)));
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
}
