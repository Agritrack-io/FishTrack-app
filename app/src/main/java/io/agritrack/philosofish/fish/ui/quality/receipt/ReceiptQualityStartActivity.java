package io.agritrack.philosofish.fish.ui.quality.receipt;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitReceiptQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityReceipt;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.sync.SyncApi;
import io.agritrack.philosofish.api.sync.SyncBinInfo;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.caen.common.CAENState;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.QualityStepsState;
import io.agritrack.philosofish.fish.state.ReceiptQualityRecord;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import io.agritrack.philosofish.ui.tools.caen.ILoggerDialog;
import retrofit2.Call;

public class ReceiptQualityStartActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<CAENState> stateResult = new MutableLiveData<>();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private boolean scanAllBins = false;
    private int attemptsToGetEpcList = 0;
    private int attemptsToScanBinOutOfLot = 0;
    private List<String> binList;
    private SingleShotScanner scanner_runnable;
    private MobileDB db;
    private RecyclerView rvBinsForTransport;
    private EditText tvFarm, tvCage, tvSpecies, tvFishDate, tvPlant, tvLot;
    private EditText etArrival, etStart;
    private SwitchCompat swSealed;
    private BinWeightCageAdapter adapterBins;
    private ImageButton ivDeleteBin;
    private List<String> scannedBinEPCs;
    private String loggerEPC, binEPC;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;
    private String epcStr, epcShort;
    // variable to hold the dialog. Only 1 instance of ILoggerDialog may be active...
    private ILoggerDialog loggerDlg = null;
    private YesNoDialogFragment confirmNewLotDialog, confirmSaveDataDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_receipt_start);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
        String token = LocalPreferences.getToken();
        // sync Bin Info (complete BinLedger)
        Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
        syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityInfo);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        confirmNewLotDialog= YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            ReceiptQualityTransaction tx = commitReceiptQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityReceipt.lot),Toast.LENGTH_LONG);
            }
            loadLotInfo(epcStr);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });


        confirmSaveDataDialog= YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            ReceiptQualityTransaction tx = commitReceiptQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityReceipt.lot),Toast.LENGTH_LONG);
            }
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });


        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityStartActivity.this);
            supportDialog.showDialog();
        });


        configFooter();
    }

    private void assignCtrlVars() {
        tvLot = findViewById(R.id.tvLotNumber);
        tvPlant = findViewById(R.id.tvPlant);
        tvCage = findViewById(R.id.tvCage);
        tvFarm = findViewById(R.id.tvFarm);
        tvSpecies = findViewById(R.id.tvSpecies);
        tvFishDate = findViewById(R.id.tvFishDate);
        etStart = findViewById(R.id.etStartTime);
        swSealed = findViewById(R.id.swIsSealed);
        etArrival = findViewById(R.id.etArrival);
        btnScanBin = findViewById(R.id.btnScanBin);
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        ivDeleteBin = findViewById(R.id.ivDeleteBin);
        ivSupport = findViewById(R.id.ivSupport);

        tvFishDate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etArrival.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        etStart.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
//
//        // Add TextWatcher to auto-format input
//        tvFishDate.addTextChangedListener(new TextWatcher() {
//            private String current = "";
//            private String ddmmyyyy = "DDMMYYYY";
//            private Calendar cal = Calendar.getInstance();
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) { }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (!s.toString().equals(current)) {
//                    String clean = s.toString().replaceAll("[^\\d]", "");
//                    String cleanC = current.replaceAll("[^\\d]", "");
//
//                    int cl = clean.length();
//                    int sel = cl;
//                    for (int i = 2; i <= cl && i < 6; i += 2) {
//                        sel++;
//                    }
//                    // Fix for delete and backspace
//                    if (clean.equals(cleanC)) sel--;
//
//                    if (clean.length() < 8) {
//                        clean = clean + ddmmyyyy.substring(clean.length());
//                    } else {
//                        // Ensure the input date is a valid date
//                        int day = Integer.parseInt(clean.substring(0, 2));
//                        int mon = Integer.parseInt(clean.substring(2, 4));
//                        int year = Integer.parseInt(clean.substring(4, 8));
//
//                        mon = Math.max(1, Math.min(12, mon));
//                        cal.set(Calendar.MONTH, mon - 1);
//                        year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
//                        cal.set(Calendar.YEAR, year);
//                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
//                        clean = String.format("%02d%02d%02d", day, mon, year);
//                    }
//
//                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
//                            clean.substring(2, 4),
//                            clean.substring(4, 8));
//
//                    sel = Math.max(sel, 0);
//                    current = clean;
//                    tvFishDate.setText(current);
//                    tvFishDate.setSelection(Math.min(sel, current.length()));
//                }
//            }
//        });
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivNext);
        ivNext.setOnClickListener(view -> {
            stopScanner();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReceiptQualityFreshCheckActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            stopScanner();
            if (!Strings.isEmptyOrWhitespace(recQualityReceipt.lot)) {
                FragmentManager fm = getSupportFragmentManager();
                confirmSaveDataDialog.setMessage(getString(R.string.save_lot_quality, recQualityReceipt.lot));
                confirmSaveDataDialog.showNow(fm, getString(R.string.confirm_selection));
            } else {
                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
                startActivity(i);
            }

        });
    }

    private void initControlsFromState() {
        ReceiptQualityRecord qualityRecord = recQualityReceipt;

        if (recQualityReceipt.lot != null) {
            tvLot.setText(recQualityReceipt.lot);
        }

        if (recQualityReceipt.cage != null) {
            tvCage.setText(recQualityReceipt.cage);
        }

        if (recQualityReceipt.farm != null) {
            tvFarm.setText(recQualityReceipt.farm);
        }

        if (recQualityReceipt.arrivalTime != null) {
            etArrival.setText(recQualityReceipt.arrivalTime.toString());
        }

        if (recQualityReceipt.startTime != null) {
            etStart.setText(recQualityReceipt.startTime.toString());
        }

        if (recQualityReceipt.fishingDate != null) {
            tvFishDate.setText(recQualityReceipt.fishingDate.toString());
        }

        if (recQualityReceipt.plant != null) {
            tvPlant.setText(recQualityReceipt.plant);
        }

        if (recQualityReceipt.fishSpecies != null) {
            tvSpecies.setText(recQualityReceipt.fishSpecies);
        }

        if (recQualityReceipt.binSeal != null) {
            swSealed.setChecked(recQualityReceipt.binSeal);
        }

    }

    private void updateState() {
        recQualityReceipt.startTime = etStart.getText().toString();
        recQualityReceipt.arrivalTime = etArrival.getText().toString();
        recQualityReceipt.binSeal = swSealed.isChecked();
        recQualityReceipt.lot = tvLot.getText().toString();
        recQualityReceipt.plant = tvPlant.getText().toString();
        recQualityReceipt.cage = tvCage.getText().toString();
        recQualityReceipt.fishSpecies = tvSpecies.getText().toString();
        recQualityReceipt.farm = tvFarm.getText().toString();
        recQualityReceipt.fishingDate = tvFishDate.getText().toString();

        //GlobalState.commitQuality(db, Boolean.FALSE);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recQualityReceipt.lot == null || recQualityReceipt.lot.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'LOT'"));
            }
        }
        return sb.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        ContextCompat.registerReceiver(this, keyReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
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


    public void loadLotInfo(String epc) {
        try {
            if (!Strings.isEmptyOrWhitespace(epc)) {
                BinInfo binInfo = db.binInfoDAO().getByRFId(epc);
                if (binInfo == null || binInfo.lot == null || binInfo.lot.isEmpty()) {
                    CToast(getAppContext(), String.format(getResources().getString(R.string.no_lot_for_bin), epcShort), Toast.LENGTH_LONG);
                    return;
                } else {
                    ReceiptQualityTransaction recTrans = db.receiptQualityTransactionDAO().getByLot(binInfo.lot);
                    if (recTrans == null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                        recQualityReceipt.lot = binInfo.lot;
                        recQualityReceipt.cage = binInfo.cage;
                        recQualityReceipt.fishSpecies = binInfo.species;
                        recQualityReceipt.farm = binInfo.farm;
                        recQualityReceipt.plant = binInfo.plant;
                        if (binInfo.pickedAt != null){
                            LocalDate date = Instant.ofEpochMilli(binInfo.pickedAt)
                                    .atZone(ZoneId.systemDefault()) // You can specify your time zone if needed
                                    .toLocalDate();
                            recQualityReceipt.fishingDate = date.format(formatter);
                        }
                        if (binInfo.deliveredAt != null) {
                            recQualityReceipt.arrivalTime = Instant.ofEpochMilli(binInfo.deliveredAt)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalTime()
                                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                        }
                        recQualityReceipt.startTime = null;
                    } else if (recTrans.isSynced) {
                        CToast(getAppContext(),String.format(getResources().getString(R.string.lot_receipt_control_done), binInfo.lot), Toast.LENGTH_LONG );
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        recQualityReceipt.lot = recTrans.lot;
                        recQualityReceipt.cage = recTrans.cage;
                        recQualityReceipt.fishSpecies = recTrans.species;
                        recQualityReceipt.farm = recTrans.farm;
                        recQualityReceipt.plant = recTrans.plant;
                        if (recTrans.fishingDate != null) {
                            LocalDate date = recTrans.fishingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            recQualityReceipt.fishingDate = date.format(formatter);
                        }
                        if (recTrans.arrivalTime != null) {
                            recQualityReceipt.arrivalTime = recTrans.arrivalTime.toString();
                        }
                        if (recTrans.startTime != null) {
                            recQualityReceipt.startTime = recTrans.startTime.toString();
                        }
                        recQualityReceipt.binSeal = recTrans.sealed;
                        recQualityReceipt.eyeRating = recTrans.eyeRating;
                        recQualityReceipt.gillRating = recTrans.gillRating;
                        recQualityReceipt.fleshRating = recTrans.fleshRating;
                        recQualityReceipt.skinRating = recTrans.skinRating;
                        recQualityReceipt.disEyes = recTrans.disEyes;
                        recQualityReceipt.disTail = recTrans.disTail;
                        recQualityReceipt.disSkeletal = recTrans.disSkeletal;
                        recQualityReceipt.disBlood = recTrans.disBlood;
                        recQualityReceipt.disMouth = recTrans.disMouth;
                        recQualityReceipt.disOper = recTrans.disOper;
                        recQualityReceipt.comments = recTrans.comments;

                    }

                    initControlsFromState();
                    return;
                }
            } else {
                CToast(getApplicationContext(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<ReceiptQualityStartActivity> mActivity;

        public ScanHandler(ReceiptQualityStartActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    epcStr = msg.getData().getString("epc");
                    if (epcStr == null) return;
                    String rssi = msg.getData().getString("rssi");
                    epcShort = epcStr.substring(epcStr.length() - 6);
                    this.removeCallbacks(scanner_runnable);
                    if (recQualityReceipt.lot == null || recQualityReceipt.lot.isEmpty()) {
                        loadLotInfo(epcStr);
                        return;
                    } else {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            BinInfo binInfo = db.binInfoDAO().getByRFId(epcStr);
                            if (binInfo == null || binInfo.lot == null || binInfo.lot.isEmpty()) {
                                CToast(getAppContext(), String.format(getResources().getString(R.string.no_lot_for_bin), epcShort), Toast.LENGTH_LONG);
                                return;
                            } else if (binInfo.lot.equals(recQualityReceipt.lot)) {
                                CToast(getAppContext(), String.format(getResources().getString(R.string.lot_already_loaded), binInfo.lot), Toast.LENGTH_LONG);
                                return;
                            }
                            FragmentManager fm = getSupportFragmentManager();
                            confirmNewLotDialog.setMessage(getString(R.string.bin_on_another_lot, epcShort, recQualityReceipt.lot));
                            confirmNewLotDialog.showNow(fm, getString(R.string.confirm_selection));
                            return;
                        }
                    }


                case 1980:
                    this.removeCallbacks(scanner_runnable);
                    break;
            }

        }
    }
}