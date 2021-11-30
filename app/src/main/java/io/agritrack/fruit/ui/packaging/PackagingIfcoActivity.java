package io.agritrack.fruit.ui.packaging;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recPackaging;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;

import io.agritrack.R;
import io.agritrack.barcode.BarcodeScanService;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.fruit.state.StorageRecord;
import io.agritrack.ui.adapter.BarcodeRecyclerAdapter;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class PackagingIfcoActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private RecyclerView rvInventoryIfco;
    private TextView tvIfcoCount;
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
        setContentView(R.layout.activity_packaging_ifco);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackagingIfco);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryIfco.setLayoutManager(layoutManager);
        rvInventoryIfco.setItemAnimator(new DefaultItemAnimator());
        adapterIfco = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvInventoryIfco.setAdapter(adapterIfco);
        rvInventoryIfco.setNestedScrollingEnabled(false);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

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
            supportDialog = new SupportDialog(PackagingIfcoActivity.this);
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
            Intent i = new Intent(getApplicationContext(), PackagingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackagingLot);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackagingLotActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        PackagingRecord trns = recPackaging;

        if (trns.packagedIfco != null) {
            adapterIfco.setValues(new LinkedList<>(trns.packagedIfco));
            adapterIfco.notifyDataSetChanged();
            //Get reference of binsCount textView
            //TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvIfcoCount.setText(String.valueOf(trns.totesForPackaging.size()));
        }
    }

    private void updateState(){
        PackagingRecord packagingRecord = FruitGlobalState.recPackaging;

        packagingRecord.packagedIfco = new LinkedList<>(adapterIfco.getValues());

        if (tvIfcoCount.getText() != null && !Strings.isEmptyOrWhitespace(tvIfcoCount.getText().toString())) {
            packagingRecord.totalPackagedIfco = Short.valueOf(tvIfcoCount.getText().toString());
        }
    }

    private void assignCtrlVars() {
        rvInventoryIfco = findViewById(R.id.rvInventoryIfco);
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