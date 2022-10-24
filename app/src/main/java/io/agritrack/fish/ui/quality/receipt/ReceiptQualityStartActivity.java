package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.IsOnline;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.fish.state.GlobalState.recQuality;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.BinInfo;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.fish.ui.fishing.FishingTeamActivity;
import io.agritrack.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.BinWeightCageAdapter;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;

public class ReceiptQualityStartActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private boolean scanAllBins = false;
    private int attemptsToGetEpcList = 0;
    private int attemptsToScanBinOutOfLot = 0;
    private List<String> binList;
    private SingleShotScanner scanner_runnable;
    private MobileDB db;
    private RecyclerView rvBinsForTransport;
    private TextView tvExpectedBinsCount, tvCheckedBinsCount, tvNumberExpectedBins, tvNumberCheckedBins, tvSelectBins;
    private BinWeightCageAdapter adapterBins;
    private ImageButton ivDeleteBin;
    private List<String> scannedBinEPCs;
    private String loggerEPC, binEPC, productLot;
    private ImageView ivSupport;
    private Button btnScanBin;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_start);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityStartActivity);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        tvNumberExpectedBins.setVisibility(View.INVISIBLE);
        tvNumberCheckedBins.setVisibility(View.INVISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        rvBinsForTransport.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterBins = new BinWeightCageAdapter(this, new ArrayList<BinWeightCageAdapter.BinDetails>());
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        // instantiate a set to hold scanned EPCS.it will be passed to adapter shich feeds the ListView.
        scannedBinEPCs = new ArrayList<String>();

        // instantiate a set to hold expected EPCS.it will be passed to adapter shich feeds the ListView.
        binList = new ArrayList<String>();

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
                        // User can retrieve data from the deleted bin
                        scannedBinEPCs.remove(barcode);
                        adapterBins.notifyDataSetChanged();
                        tvCheckedBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                        adapterBins.clearSelectedValue();
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
            supportDialog = new SupportDialog(ReceiptQualityStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvSelectBins = findViewById(R.id.tvSelectBins);
        btnScanBin = findViewById(R.id.btnScanBin);
        tvExpectedBinsCount = findViewById(R.id.tvExpectedBinsCount);
        tvCheckedBinsCount = findViewById(R.id.tvCheckedBinsCount);
        tvNumberExpectedBins = findViewById(R.id.tvNumberExpectedBins);
        tvNumberCheckedBins = findViewById(R.id.tvNumberCheckedBins);
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
                Intent i = new Intent(getApplicationContext(), ReceiptQualityTemperatureProfilesActivity.class);
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
        QualityRecord qualityRecord = recQuality;

        if (qualityRecord.expectedBins != null && !(qualityRecord.expectedBins.size() == 0) && qualityRecord.qualityBins != null && !(qualityRecord.qualityBins.size() == 0)) {
            recQuality.expectedBins.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
            adapterBins.notifyDataSetChanged();
            scannedBinEPCs = qualityRecord.scannedBins;
            adapterBins.markReceived(convertBinDetailsToEPCs(qualityRecord.qualityBins));
            tvSelectBins.setText(R.string.qualified_bins);
            tvNumberExpectedBins.setVisibility(View.VISIBLE);
            tvNumberCheckedBins.setVisibility(View.VISIBLE);
            tvExpectedBinsCount.setText(String.valueOf(qualityRecord.expectedBins.size()));
            btnScanBin.setText(R.string.scan_one_to_one_bins);
            binList = qualityRecord.expectedBins;
            scanAllBins = true;

            //Get reference of binsCount textView
            tvCheckedBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
        } else if (IsOnline) {
            tvSelectBins.setText(null);
            btnScanBin.setText(R.string.scan_one_bin);
        } else {
            scanAllBins = true;
            btnScanBin.setText(R.string.scan_one_to_one_bins);
        }
    }

    private void updateState() {
        recQuality.qualityBinsCnt = adapterBins.getItemCount();
        recQuality.retrievedAt = System.currentTimeMillis();
        recQuality.logger_rfid = loggerEPC;

        GlobalState.commitQuality(db, Boolean.FALSE);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recQuality.qualityBins == null || recQuality.qualityBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
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
        if (adapterBins.getItemCount() < 1 && IsOnline && !scanAllBins) {
            scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_LOGGER);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        }
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private BinWeightCageAdapter.BinDetails loadBinInfo(String epc) {
        //Add code to retrieve bin info from local DB
        BinInfo tmpBin = db.binInfoDAO().getByRFId(epc);
        if (tmpBin != null) {
            return new BinWeightCageAdapter.BinDetails(epc, tmpBin.totalWeight, tmpBin.cage);
        } else {
            return new BinWeightCageAdapter.BinDetails(epc);
        }
    }

    private List<BinWeightCageAdapter.BinDetails> convertEPCsToBinDetails(List<String> epcs) {
        List<BinWeightCageAdapter.BinDetails> result = new ArrayList<>();
        for (String epc : epcs) {
            result.add(new BinWeightCageAdapter.BinDetails(epc));
        }
        return result;
    }

    private List<String> convertBinDetailsToEPCs(List<BinWeightCageAdapter.BinDetails> epcs) {
        List<String> result = new ArrayList<>();
        for (BinWeightCageAdapter.BinDetails epc : epcs) {
            result.add(epc.epc);
        }
        return result;
    }

    private class ScanHandler extends Handler {
        private final WeakReference<ReceiptQualityStartActivity> mActivity;

        public ScanHandler(ReceiptQualityStartActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    if (scanAllBins) {
                        tvSelectBins.setText(R.string.qualified_bins);
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                loggerEPC = epcStr;

                                // after bin is identified, initialize the temperatures logger.
                                Asset bin = db.assetDAO().getByLoggerEPC(loggerEPC);
                                if (bin != null) {
                                    binEPC = bin.rfid;
                                    if (scannedBinEPCs.contains(binEPC)) {
                                        CToast(getApplicationContext(), render(R.string.already_scannned_bin), Toast.LENGTH_LONG);
                                        return;
                                    }
                                    if (!binList.contains(binEPC)) {
                                        while (attemptsToScanBinOutOfLot < 1) {
                                            attemptsToScanBinOutOfLot++;
                                            CToast(getApplicationContext(), render(R.string.scanned_bin_out_of_lot), Toast.LENGTH_LONG);
                                            return;
                                        }
                                        confirmScanBinOutOfLotDialog();
                                        adapterBins.markReceived(Collections.singletonList(binEPC));
                                        attemptsToScanBinOutOfLot = 0;
                                        return;
                                    }
                                    triggerDataLoggerDialog();
                                } else if (!IsDemo) {
                                    CToast(getApplicationContext(), render(R.string.no_logger_found_linked_to_bin), Toast.LENGTH_SHORT);
                                }
                            } else {
                                CToast(getApplicationContext(), render(R.string.no_tag_detected), Toast.LENGTH_SHORT);
                            }
                            this.removeCallbacks(scanner_runnable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                List<BinInfo> binInfoList = db.binInfoDAO().getEPCListByRFId(epcStr);
                                for (BinInfo bin : binInfoList) {
                                    binList.add(bin.rfid);
                                    recQuality.pLot = bin.lot;
                                }
                                if (binList == null || binList.isEmpty()) {
                                    while (attemptsToGetEpcList < 3) {
                                        attemptsToGetEpcList++;
                                        CToast(getApplicationContext(), render(getString(R.string.no_epc_list_returned)), Toast.LENGTH_LONG);
                                        return;
                                    }
                                    attemptsToGetEpcList = 0;
                                    CToast(getApplicationContext(), render(getString(R.string.scan_all_bins)), Toast.LENGTH_LONG);
                                    btnScanBin.setText(R.string.scan_one_to_one_bins);
                                    scanAllBins = true;
                                    return;
                                }
                                binList.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
                                adapterBins.notifyDataSetChanged();
                                recQuality.expectedBins = binList;
                                GlobalState.commitQuality(db, Boolean.FALSE);
                                tvSelectBins.setText(R.string.expected_bins);
                                tvNumberExpectedBins.setVisibility(View.VISIBLE);
                                tvExpectedBinsCount.setText(String.valueOf(binList.size()));
                                btnScanBin.setText(R.string.scan_one_to_one_bins);
                                scanAllBins = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case 1980:
                    this.removeCallbacks(scanner_runnable);
                    break;
            }

        }
    }

    private void triggerDataLoggerDialog(){
        if (!scannedBinEPCs.contains(binEPC)) {
            scannedBinEPCs.add(binEPC);
        }
        recQuality.scannedBins = scannedBinEPCs;
        tvNumberCheckedBins.setVisibility(View.VISIBLE);
        tvCheckedBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
        adapterBins.addUniqueItem(loadBinInfo(binEPC));
        adapterBins.markReceived(scannedBinEPCs);
        recQuality.qualityBins = convertEPCsToBinDetails(scannedBinEPCs);
        GlobalState.commitQuality(db, Boolean.FALSE);
        adapterBins.notifyDataSetChanged();

        if (!Strings.isEmptyOrWhitespace(loggerEPC)) {
            BinInfo tmpBin = db.binInfoDAO().getByRFId(binEPC);
            FragmentManager fm = getSupportFragmentManager();
            LoggerInitDialogFragment loggerDlg;
            if (tmpBin != null && tmpBin.initedAt != null) {
                loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, tmpBin.initedAt, true, true, true);
            } else {
                loggerDlg = LoggerInitDialogFragment.newInstance(loggerEPC, binEPC, true, true, true);
            }
            loggerDlg.show(fm, LoggerInitDialogFragment.TAG);
        }
    }

    private void confirmScanBinOutOfLotDialog() {
        // Get custom login form view.
        final View confirmFormView = this.getLayoutInflater().inflate(R.layout.confirm_scan_bin_out_of_lot_dlg, null);

        final EditText pin = confirmFormView.findViewById(R.id.etPin);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText(Html.fromHtml("<b>"+ getAppContext().getResources().getString(R.string.confirm_scanned_bin_out_of_lot) +"</b>" + "<br>" + getAppContext().getResources().getString(R.string.confirm_with_pin), HtmlCompat.FROM_HTML_MODE_LEGACY));
        title.setBackgroundColor(Color.WHITE);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(confirmFormView)
                .setCustomTitle(title)
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String insertedPin = pin.getText().toString().trim();
                        String login = LocalPreferences.getLoggedInUser("").trim();

                        if (Strings.isEmptyOrWhitespace(insertedPin)) {
                            CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG);
                            return;
                        }

                        // use typed-in PIN to compare credentials with those stored in the Local DB.
                        AuthenticationService authSvc = new AuthenticationService();
                        boolean authentication = authSvc.authenticateUser(db, login, insertedPin);
                        if (authentication){
                            triggerDataLoggerDialog();
                            dialog.dismiss();
                        } else {
                            CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG);
                            return;
                        }
                    }
                });
            }
        });
        dialog.show();
    }
}