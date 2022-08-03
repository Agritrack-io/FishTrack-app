package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.bo.BinLoadsMap;
import io.agritrack.fish.ui.bo.BinWeightRecord;
import io.agritrack.fish.ui.testBinTemperature.TestBinTempActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;


public class FishingFillBinsActivity extends AppCompatActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    //
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("Activity result", "OK");
                    // There are no request codes
                    Intent data = result.getData();
                }
            });
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    private Button btnCurrentBinScan, btnAddCatch, btnDeleteCatch, btnFillBin;
    private TextView tvCurrentBin, tvBinWeight, tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;
    private ImageView ivBT;
    private RecyclerView rvWeightBatchesBin;
    private TemplateRecyclerAdapter adapterCatches;
    private boolean isClickable;
    private boolean intentForFillBinActivity = false;
    private String mCatchWeight = "";
    private String currentBin;
    private Integer weightOfBin;
    private BinLoadsMap loadsMap;
    private long epochFrom;
    private ImageView ivSupport, ivInfo, ivCheckLastTemp;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private boolean isClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            intentForFillBinActivity = bundle != null ? bundle.getBoolean("FillBinActivity") : intentForFillBinActivity;
            if (intentForFillBinActivity) {
                isClicked = true;
            }
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        loadsMap = new BinLoadsMap();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        rvWeightBatchesBin.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterCatches = new TemplateRecyclerAdapter(this, new ArrayList<>(), false);
        isClickable = adapterCatches.isClickable;
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

        // initially only scan button is active.
        btnCurrentBinScan.setEnabled(true);
        btnAddCatch.setEnabled(false);
        btnAddCatch.setTextColor(Color.DKGRAY);
        btnDeleteCatch.setEnabled(false);
        btnDeleteCatch.setTextColor(Color.DKGRAY);
        btnFillBin.setEnabled(false);
        btnFillBin.setTextColor(Color.DKGRAY);

        // =================================
        // RFID scanning functionality
        btnCurrentBinScan.setOnClickListener(this::onClick);

        // =================================
        // Adding fish catch functionality
        btnAddCatch.setOnClickListener(view -> {
            btnCurrentBinScan.setEnabled(false);
            btnCurrentBinScan.setTextColor(Color.DKGRAY);
            btnFillBin.setEnabled(true);
            btnFillBin.setTextColor(getColor(R.color.aqua));
            btnDeleteCatch.setEnabled(true);
            btnDeleteCatch.setTextColor(getColor(R.color.aqua));

            //show Message box
            showCatchDialog();
        });

        // =================================
        // Adding bin load completion functionality
        btnFillBin.setOnClickListener(view -> {
            isClicked = true;
            if (weightOfBin != null) {
                BinWeightRecord.BinRecord currRec = recFishing.binWeightRecord.getRecordForEPC(currentBin);
                if (currRec.from != null) {
                    GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, currRec.init, currRec.from, System.currentTimeMillis() / 1000l);
                } else {
                    GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, currRec.init, epochFrom, System.currentTimeMillis() / 1000l);
                }
                weightOfBin = null;
            }
            isClickable = false;
            btnCurrentBinScan.setEnabled(true);
            btnCurrentBinScan.setTextColor(getColor(R.color.aqua));
            btnAddCatch.setEnabled(false);
            btnAddCatch.setTextColor(Color.DKGRAY);
            btnDeleteCatch.setEnabled(false);
            btnDeleteCatch.setTextColor(Color.DKGRAY);
            view.setEnabled(false);
            ((Button) view).setTextColor(Color.DKGRAY);
            epochFrom = 0;
        });

        btnDeleteCatch.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(adapterCatches.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedCatch", adapterCatches.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterCatches.getSelectedValue() + " kg");

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String aCatch = bundle.getString("selectedCatch");
                    if (aCatch != null) {
                        adapterCatches.removeItem(aCatch);
                        adapterCatches.notifyDataSetChanged();

                        tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                        tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));

                        adapterCatches.clearSelectedValue();
                        btnDeleteCatch.setEnabled(false);
                        btnDeleteCatch.setTextColor(Color.DKGRAY);
                    }
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterCatches.clearSelectedValue();
                    btnDeleteCatch.setEnabled(false);
                    btnDeleteCatch.setTextColor(Color.DKGRAY);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Catch first.
                CToast(getApplicationContext(), render("Plz select a Catch to delete!!"), Toast.LENGTH_LONG);
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivCheckLastTemp.setOnClickListener(view -> {
            this.stopScanner();
            updateState();
            Intent i = new Intent(getApplicationContext(), TestBinTempActivity.class);
            i.putExtra("FillBinActivity", true);
            startActivity(i);
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingFillBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingFillBinsActivity.this);
            infoDialog.showDialog();
        });

        // ============
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
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void onClick(View view) {
        isClicked = false;
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
        isClickable = true;
    }

    private void assignCtrlVars() {
        btnCurrentBinScan = findViewById(R.id.btnScanCurrentBin);
        btnAddCatch = findViewById(R.id.btnAddCatch);
        btnDeleteCatch = findViewById(R.id.btnDeleteCatch);
        btnFillBin = findViewById(R.id.btnEndBin);
        tvCurrentBin = findViewById(R.id.tvBinName);
        tvBinWeight = findViewById(R.id.tvBinWeight);
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        ivBT = findViewById(R.id.ivBT);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (recFishing.totalFishWeight != null) {
            tvTotalWeightCount.setText(recFishing.totalFishWeight.toString());
        }

        if (recFishing.totalBinsUsed != null) {
            tvUsedBinsCount.setText(recFishing.totalBinsUsed.toString());
        }

        if (!loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBins();
            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
        }

        tvAvailableBinsCount.setText(String.valueOf(hvst.availBins.size()));

        if (!recFishing.binWeightRecord.isEmpty()) {
            for (BinWeightRecord.BinRecord bin : recFishing.binWeightRecord.getBins()) {
                loadsMap.addLoad(bin.binEPC, bin.weight + "");
            }
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
            isClicked = true;
        }
        //txFishing.harvestBinsData = brecFishing.binWeightRecord.getBins();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            if (!isClicked) {
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            if (!isClicked && loadsMap.hasLoads()) {
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }

    private FishingRecord updateState() {
        // get an instance of local DB
        MobileDB db = MobileDB.getInstance(getAppContext());

        if (tvTotalWeightCount.getText() != null) {
            recFishing.totalFishWeight = loadsMap.totalWeight();
        }

        if (tvUsedBinsCount.getText() != null && !Strings.isEmptyOrWhitespace(tvUsedBinsCount.getText().toString())) {
            recFishing.totalBinsUsed = Short.valueOf(tvUsedBinsCount.getText().toString());
        }
        GlobalState.commitFishing(db, Boolean.FALSE);

        return recFishing;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recFishing.totalBinsUsed == null) {
                sb.append(String.format("\n%s is missing", "'Harvest bins'"));
            }
        }
        return sb.toString();
    }

    private void showCatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.fish_catch_weight);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) FishingFillBinsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));
        input.requestFocus();
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mCatchWeight = input.getText().toString();
            if (Strings.isEmptyOrWhitespace(mCatchWeight)) {
                CToast(getApplicationContext(), render(R.string.type_weight), Toast.LENGTH_LONG);
                return;
            }
            adapterCatches.addItem(mCatchWeight);
            adapterCatches.notifyDataSetChanged();

            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
            weightOfBin = loadsMap.weightOf(currentBin);
            tvUsedBinsCount.setText(loadsMap.loadsCnt());
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<FishingFillBinsActivity> mActivity;

        public ScanHandler(FishingFillBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
//            FishingFillBinsActivity activity = mActivity.get();
//            if (activity != null) {
//            }
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    if (!Strings.isEmptyOrWhitespace(epcStr)) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            String epc = epcStr.substring(14);
                            tvCurrentBin.setText(epc);
                            currentBin = epcStr;

                            BinWeightRecord.BinRecord currRec = recFishing.binWeightRecord.getRecordForEPC(currentBin);
                            if (currRec == null || currRec.init == null) {
                                CToast(getApplicationContext(), render(R.string.data_logger_not_initialized), Toast.LENGTH_LONG);
                                GlobalState.recFishing.binWeightRecord.addRecord(currentBin, weightOfBin, null, epochFrom, null);
                                loadsMap.addLoad(currentBin, "0");
                            }
                            List<String> loadForBin = loadsMap.getLoads(currentBin);
                            if (loadForBin == null) {
                                loadForBin = new ArrayList<>();
                            }
                            adapterCatches.setValues(loadForBin);
                            if (loadsMap.getLoads(currentBin) != null) {
                                isClicked = true;
                            }
                            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                            adapterCatches.notifyDataSetChanged();
                            tvUsedBinsCount.setText(loadsMap.loadsCnt());
                            epochFrom = System.currentTimeMillis() / 1000l;
                        });

                        btnAddCatch.setEnabled(true);
                        btnAddCatch.setTextColor(getColor(R.color.aqua));
                        if (adapterCatches.getItemCount() != 0) {
                            btnFillBin.setEnabled(true);
                            btnFillBin.setTextColor(getColor(R.color.aqua));
                        }
                    }

                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No BIN was found!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}