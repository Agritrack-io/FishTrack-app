package io.agritrack.philosofish.fish.ui.process;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recProcessing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.query.EnquiryApi;
import io.agritrack.philosofish.api.sync.RfidBatchByRfidBarcode;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.ProcessingRecord;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.rfid.ScanInventoryThread;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.BinWeightCageAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;

public class ProcessBinsActivity extends AppCompatActivity {

    private final MutableLiveData<List<String>> enquiryResult = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    // Local handler that receives the RFID scanner results.
    private ScanHandler mScanHandler = new ScanHandler(this);
    private SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    private ScanInventoryThread scanner_inv = new ScanInventoryThread(mScanHandler);

    private MobileDB db;
    private BinWeightCageAdapter adapterBins;

    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;
    private TextView tvSelectBins;

    private ImageButton ivAddBin, ivDeleteBin;

    private String binBarcode;
    private ImageView ivSupport;
    private Button scanButton;
    private int attemptsToGetEpcList = 0;
    private boolean scanAllBins = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_bins);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        rvBinsForTransport.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterBins = new BinWeightCageAdapter(this, new ArrayList<BinWeightCageAdapter.BinDetails>()); //, itemsClickListener
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvBinsCount.setText(String.valueOf(response.size()));
            adapterBins.setValues(convertEPCsToBinDetails(response));
            adapterBins.notifyDataSetChanged();
        });

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        ivDeleteBin.setOnClickListener(view -> {
            List<BinWeightCageAdapter.BinDetails> selectedBins = adapterBins.getValues().stream().filter(x -> x.isSelected()).collect(Collectors.toList());
            if (!selectedBins.isEmpty()) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_selected_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    for (BinWeightCageAdapter.BinDetails bin : selectedBins) {
                        adapterBins.removeItem(bin.epc);
                    }

                    adapterBins.notifyDataSetChanged();
                    tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                    adapterBins.clearSelectedValue();

                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterBins.clearSelectedValue();
                    adapterBins.notifyDataSetChanged();
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(getString(R.string.delete_item)), Toast.LENGTH_LONG);
            }
        });

        ivAddBin.setOnClickListener(view -> {
            showAddDialog();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProcessBinsActivity.this);
            supportDialog.showDialog();
        });

        enquiryResult.observe(this, response -> {
            if (response == null || response.isEmpty()) {
//                while (attemptsToGetEpcList++ < 3) {
//                    CToast(getApplicationContext(), render(getString(R.string.no_epc_list_returned)), Toast.LENGTH_LONG);
//                    return;
//                }
//                attemptsToGetEpcList = 0;

                CToast(getApplicationContext(), render(getString(R.string.no_epc_list_continue)), Toast.LENGTH_LONG);
                scanButton.setText(R.string.scan_all_bins);
                scanAllBins = true;
                return;
            }
            if (response.get(0).equalsIgnoreCase(getString(R.string.change_position_to_find_network_coverage_and_scan_again))) {
                while (attemptsToGetEpcList++ < 3) {
                    CToast(getApplicationContext(), render(response.get(0)), Toast.LENGTH_LONG);
                    return;
                }
                attemptsToGetEpcList = 0;
                CToast(getApplicationContext(), render(getString(R.string.scan_all_bins)), Toast.LENGTH_LONG);
                scanButton.setText(R.string.scan_all_bins);
                scanAllBins = true;
                return;
            }
            if (response.size() == 1) {
                if(response.get(0).equalsIgnoreCase("RECEIVED")) {
                    for (int i = 0; i < 2; i++) {
                        CToast(getApplicationContext(), getString(R.string.all_bins_received), Toast.LENGTH_LONG);
                    }
                    return;
                }
            }
            response.stream().forEach(x -> adapterBins.addExpectedItem(loadBinInfo(x)));
            adapterBins.notifyDataSetChanged();
            tvSelectBins.setText(R.string.expected_bins);
            tvBinsCount.setText(String.valueOf(adapterBins.getValues().size()));
            scanButton.setText(R.string.scan_all_bins);
        });

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
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    private void assignCtrlVars() {
        tvSelectBins = findViewById(R.id.tvSelectBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        ivDeleteBin = findViewById(R.id.ivDeleteBin);
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanBin);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToSupervisorConfirm);
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_inv != null) {
                scanner_inv.stopReading();
            }

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ProcessConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToStartProcess);
        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_inv != null) {
                scanner_inv.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = recProcessing;

        if (prcRecord.availBins != null) {
            adapterBins.setValues(prcRecord.availBins);
            adapterBins.notifyDataSetChanged();
            tvBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
            tvSelectBins.setText(R.string.expected_bins);
            scanButton.setText(R.string.scan_all_bins);
        } else if (IsOnline) {
            tvSelectBins.setText(null);
            scanButton.setText(R.string.scan_one_bin);
        } else {
            scanButton.setText(R.string.scan_all_bins);
        }
    }

    protected void onClick(View view) {
        if (adapterBins.getItemCount() < 1 && IsOnline && !scanAllBins) {
            scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            if (scanner_inv == null) {
                scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                scanner_inv = new ScanInventoryThread(mScanHandler);
                scanner_inv.setFilter(Filters.RFID_BIN);
                scanner_inv.startReading();
                scanButton.setText(R.string.stop_scan);
            } else if (!scanner_inv.isReading()) {
                scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                scanner_inv.setFilter(Filters.RFID_BIN);
                scanner_inv.startReading();
                scanButton.setText(R.string.stop_scan);
            } else {
                scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                scanner_inv.stopReading();
                scanButton.setText(R.string.scan_all_bins);
            }
            mScanHandler.postDelayed(scanner_inv, 0);
        }
    }

    private void invokeEnquiryRfidBatch(String epc) {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync RFID batch for this rfidBarcode
            Call<List<String>> enquiryEpcsByEpcAsyncCall = enquiryService.getFishingEpcsBatch(epc, "Bearer " + token);
            enquiryEpcsByEpcAsyncCall.enqueue(new RfidBatchByRfidBarcode(this.enquiryResult));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type bin BARCODE");

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
                adapterBins.addUniqueItem(new BinWeightCageAdapter.BinDetails(binBarcode));
                tvSelectBins.setText(R.string.received_bins_uppercase);
                tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                adapterBins.notifyDataSetChanged();
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
//        GlobalState.initProcessingRecord();

        recProcessing.availBins = adapterBins.getValues();
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recProcessing.availBins == null || recProcessing.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
            }
        }
        return sb.toString();
    }

    private BinWeightCageAdapter.BinDetails loadBinInfo(String rfid) {
        // Use 'rfid' to retrieve bin info from local DB
        BinInfo tmpBin = db.binInfoDAO().getByRFId(rfid);
        if (tmpBin != null) {
            return new BinWeightCageAdapter.BinDetails(rfid, tmpBin.totalWeight, tmpBin.cage);
        } else {
            return new BinWeightCageAdapter.BinDetails(rfid);
        }
    }

    private List<BinWeightCageAdapter.BinDetails> convertEPCsToBinDetails(Set<String> rfids) {
        List<BinWeightCageAdapter.BinDetails> result = new ArrayList<>();
        for (String rfid : rfids) {
            result.add(new BinWeightCageAdapter.BinDetails(rfid));
        }
        return result;
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<ProcessBinsActivity> mActivity;

        public ScanHandler(ProcessBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            invokeEnquiryRfidBatch(epcStr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 100:
                    ArrayList<String> epcList = msg.getData().getStringArrayList("epc");
                    if (epcList != null && !epcList.isEmpty() && IsOnline && !scanAllBins) {
                        adapterBins.markReceived(epcList);
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                    } else {
                        epcList.stream().forEach(x -> adapterBins.addUniqueItem(loadBinInfo(x)));
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        recProcessing.availBins = adapterBins.getValues();
                        adapterBins.notifyDataSetChanged();
                    }
                    tvSelectBins.setText(R.string.received_bins_uppercase);
                    break;
                case 1980:

                    break;
            }
        }
    }
}