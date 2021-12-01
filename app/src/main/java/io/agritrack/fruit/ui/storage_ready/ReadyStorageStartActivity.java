package io.agritrack.fruit.ui.storage_ready;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recStorage;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
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
import io.agritrack.ui.adapter.BarcodeRecyclerAdapter;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class ReadyStorageStartActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private TextView tvPoleName;
    private Button btnScanPole;
    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
                adapterIfco.addItem(barcode);
                adapterIfco.notifyDataSetChanged();
                tvIfcoCount.setText("# " + adapterIfco.getItemCount());
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
            TextView tvRecyclerItem = view.findViewById(R.id.tvItemDescription);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIfcoForStorage.setLayoutManager(layoutManager);
        rvIfcoForStorage.setItemAnimator(new DefaultItemAnimator());
        adapterIfco = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvIfcoForStorage.setAdapter(adapterIfco);
        rvIfcoForStorage.setNestedScrollingEnabled(false);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilter(Filters.RFID_POLE);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvPoleName.setText(epcStr);
                            Asset pole = db.assetDAO().getAssetByEpc(epcStr);
                            Site tempSite = db.siteDAO().getBySiteNameAndCode(LocalPreferences.getCurrentSiteLevel3(), pole.siteCode);
                            if (tempSite !=null) {
                                warehouse = tempSite.name;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

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

       /* ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

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

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
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
            storageRecord.totalIfcoCnt = Short.valueOf(tvIfcoCount.getText().toString());
        }

        recStorage.poleRFID = tvPoleName.getText().toString();

        if (!Strings.isEmptyOrWhitespace(this.warehouse)) {
            recStorage.warehouse = this.warehouse;
        }

        return storageRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (FruitGlobalState.recStorage.receivedTotes == null || FruitGlobalState.recStorage.receivedTotes.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received totes'"));
            }
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
        super.onResume();
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}