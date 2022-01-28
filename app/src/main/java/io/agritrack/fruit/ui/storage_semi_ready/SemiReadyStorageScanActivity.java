package io.agritrack.fruit.ui.storage_semi_ready;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recStorage;
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
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.CollectionLotEnquiryCallBack;
import io.agritrack.api.sync.RfidBatchByRfidBarcode;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.LotDTO;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.StorageRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class SemiReadyStorageScanActivity extends AppCompatActivity {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    private final MutableLiveData<List<String>> enquiryResult = new MutableLiveData<>();
    private final MutableLiveData<LotDTO> enquiryLotResult = new MutableLiveData<>();
    private ScanHandler mScanHandler;
    private String rfidBarcode;

    private TemplateRecyclerAdapter adapterTotes;

    private RecyclerView rvUsedTotesHarvest;
    private TextView tvTotesCount;

    private ImageButton ivAddTote, ivDeleteTote;
    private Button scanButton;
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
    private String toteBarcode, firstToteRfid, collectionLot;
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semi_ready_storage_scan);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSemiReadyStorageScan);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvUsedTotesHarvest.setLayoutManager(layoutManager);
        rvUsedTotesHarvest.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvUsedTotesHarvest.setAdapter(adapterTotes);
        rvUsedTotesHarvest.setNestedScrollingEnabled(false);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteTote.setOnClickListener(view -> {
            clearSelectedItem();

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterTotes.removeItem(barcode);
                        adapterTotes.notifyDataSetChanged();
                        tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Tote to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddTote.setOnClickListener(view -> {
            showAddDialog();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(SemiReadyStorageScanActivity.this);
            supportDialog.showDialog();
        });

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        enquiryResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No RFID batch returned for this RFID"), Toast.LENGTH_LONG);
                return;
            }
            adapterTotes.setValues(response);
            adapterTotes.notifyDataSetChanged();
            tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
        });

        enquiryLotResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No harvest LOT returned for these totes"), Toast.LENGTH_LONG);
                return;
            }
            collectionLot = response.lot;
        });

        // create Footer
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onPause();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToSemiReadyStorageWeight);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), SemiReadyStorageWeightActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitHome);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void invokeEnquiryRfidBatch(String rfidBarcode) {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync RFID batch for this rfidBarcode
            Call<List<String>> enquiryRFIDBatchByRFIDBarcodeAsyncCall = enquiryService.getRFIDBatch(rfidBarcode, "Bearer " + token);
            enquiryRFIDBatchByRFIDBarcodeAsyncCall.enqueue(new RfidBatchByRfidBarcode(this.enquiryResult));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void invokeEnquiryLot() {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync collection lot for current Site
            Call<LotDTO> enquiryCollectionLotAsyncCall = enquiryService.getCollectionLotByToteRfid(firstToteRfid, "Bearer " + token);
            enquiryCollectionLotAsyncCall.enqueue(new CollectionLotEnquiryCallBack(this.enquiryLotResult));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
        rvUsedTotesHarvest = findViewById(R.id.rvUsedTotesHarvest);
        tvTotesCount = findViewById(R.id.tvTotesCount);
        ivDeleteTote = findViewById(R.id.ivDeleteTote);
        ivAddTote = findViewById(R.id.ivAddTote);
        scanButton = findViewById(R.id.btnScanTotes);
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void initControlsFromState() {
        StorageRecord trns = recStorage;

        if (trns.receivedTotes != null) {
            adapterTotes.setValues(new LinkedList<>(trns.receivedTotes));
            adapterTotes.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvTotesCount.setText(String.valueOf(trns.receivedTotes.size()));
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type tote BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toteBarcode = input.getText().toString();
                adapterTotes.addUniqueItem(toteBarcode);
                tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
                adapterTotes.notifyDataSetChanged();
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

    private StorageRecord updateState() {
        StorageRecord storageRecord = recStorage;

        storageRecord.receivedTotes = new LinkedList<>(adapterTotes.getValues());

        if (tvTotesCount.getText() != null && !Strings.isEmptyOrWhitespace(tvTotesCount.getText().toString())) {
            storageRecord.totalTotesReceived = Integer.valueOf(tvTotesCount.getText().toString());
        }

        if (!Strings.isEmptyOrWhitespace(this.collectionLot)) {
            storageRecord.collectionLot = this.collectionLot;
        }

        return storageRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recStorage.receivedTotes == null || recStorage.receivedTotes.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received totes'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_TOTE);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<SemiReadyStorageScanActivity> mActivity;

        public ScanHandler(SemiReadyStorageScanActivity activity) {
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
                            rfidBarcode = epcStr;
                            invokeEnquiryRfidBatch(rfidBarcode);
                        }
                        if (!IsDemo && adapterTotes.getValues() != null) {
                                firstToteRfid = rfidBarcode;
                                invokeEnquiryLot();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:

                    break;
            }
        }
    }
}