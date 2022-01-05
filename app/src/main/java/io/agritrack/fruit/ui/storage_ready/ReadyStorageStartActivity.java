package io.agritrack.fruit.ui.storage_ready;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recStorage;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import io.agritrack.api.sync.IfcoBatchByIfcoBarcode;
import io.agritrack.barcode.BarcodeScanService;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.StorageRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class ReadyStorageStartActivity extends TriggerKeyAwareActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<List<String>> enquiryResult = new MutableLiveData<>();
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private TextView tvPoleName;
    private Button btnScanPole;
    private RecyclerView rvIfcoForStorage;
    private TextView tvIfcoCount;
    private String warehouse;
    private boolean scanning = false;
    private BarcodeScanService scanService;
    private TemplateRecyclerAdapter adapterIfco;

    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                invokeEnquiryIfcoBatch(barcode);
                tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
                scanning = false;
            }
        }
    };
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsOnClickListener = new View.OnClickListener() {
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

    private String ifcoBarcode;
    private ImageButton ivAddIfco, ivDeleteIfco;
    private Button btnScanIfco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_storage_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderStorageReadyStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIfcoForStorage.setLayoutManager(layoutManager);
        rvIfcoForStorage.setItemAnimator(new DefaultItemAnimator());
        adapterIfco = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvIfcoForStorage.setAdapter(adapterIfco);
        rvIfcoForStorage.setNestedScrollingEnabled(false);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        // RFID scanning functionality
        btnScanPole.setOnClickListener(this::onClick);

        ivDeleteIfco.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterIfco.removeItem(barcode);
                        adapterIfco.notifyDataSetChanged();
                        tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                clearSelectedItem();
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddIfco.setOnClickListener(view -> {
            showAddDialog();
        });

        btnScanIfco.setOnClickListener(view -> {
            clearSelectedItem();
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReadyStorageStartActivity.this);
            supportDialog.showDialog();
        });

        enquiryResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No ifco batch returned for this ifco"), Toast.LENGTH_LONG);
                return;
            }
            adapterIfco.setValues(response);
            adapterIfco.notifyDataSetChanged();
            tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
        });

        // create Footer
        configFooter();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
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

    private void invokeEnquiryIfcoBatch(String ifcoBarcode) {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync collection lot for current Site
            Call<List<String>> enquiryIfcoBatchByIfcoBarcodeAsyncCall = enquiryService.getIfcoBatch(ifcoBarcode, "Bearer " + token);
            enquiryIfcoBatchByIfcoBarcodeAsyncCall.enqueue(new IfcoBatchByIfcoBarcode(this.enquiryResult));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            scanService.stopScan();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReadyStorageConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        StorageRecord trns = FruitGlobalState.recStorage;

        if (trns.packagedIfco != null) {
            adapterIfco.setValues(new LinkedList<>(trns.packagedIfco));
            adapterIfco.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvIfcoCount.setText(String.valueOf(trns.packagedIfco.size()));
        }

        if (!Strings.isEmptyOrWhitespace(trns.poleRFID)) {
            tvPoleName.setText(trns.poleRFID);
        }
    }

    private StorageRecord updateState() {
        StorageRecord storageRecord = FruitGlobalState.initStorageRecord();

        storageRecord.packagedIfco = new LinkedList<>(adapterIfco.getValues());

        if (tvIfcoCount.getText() != null && !Strings.isEmptyOrWhitespace(tvIfcoCount.getText().toString())) {
            storageRecord.totalIfcoCnt = Integer.valueOf(tvIfcoCount.getText().toString());
        }

        recStorage.poleRFID = tvPoleName.getText().toString();

        if (!Strings.isEmptyOrWhitespace(this.warehouse)) {
            recStorage.warehouse = this.warehouse;
        }

        return storageRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recStorage.poleRFID)) {
                sb.append(String.format("\n%s is missing", "'Scan tag'"));
            }

            /*if (FruitGlobalState.recStorage.packagedIfco == null || FruitGlobalState.recStorage.packagedIfco.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received IFCO'"));
            }*/
        }
        return sb.toString();
    }

    private void assignCtrlVars() {
        btnScanPole = findViewById(R.id.btnScanPole);
        tvPoleName = findViewById(R.id.tvPoleName);
        rvIfcoForStorage = findViewById(R.id.rvIfcoForStorage);
        tvIfcoCount = findViewById(R.id.tvIfcoCount);
        ivDeleteIfco = findViewById(R.id.ivDeleteIfco);
        ivAddIfco = findViewById(R.id.ivAddIfco);
        btnScanIfco = findViewById(R.id.btnScanIfco);
        ivSupport = findViewById(R.id.ivSupport);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type item BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ifcoBarcode = input.getText().toString();
                adapterIfco.addUniqueItem(ifcoBarcode);
                adapterIfco.notifyDataSetChanged();
                tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
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

    @Override
    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_POLE);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<ReadyStorageStartActivity> mActivity;

        public ScanHandler(ReadyStorageStartActivity activity) {
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
                            tvPoleName.setText(epcStr);
                            Asset pole = db.assetDAO().getAssetByEpc(epcStr);
                            Site tempSite = db.siteDAO().getBySiteNameAndCode(LocalPreferences.getCurrentSiteLevel3(), pole.siteCode);
                            if (tempSite != null) {
                                warehouse = tempSite.name;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No Pole Tag was detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}