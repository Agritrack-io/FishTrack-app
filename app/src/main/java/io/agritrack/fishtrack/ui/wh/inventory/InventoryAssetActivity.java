package io.agritrack.fishtrack.ui.wh.inventory;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.common.FishTrackUtils;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class InventoryAssetActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private  ToggleGroup tgChooseAssetType;

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private RecyclerView rvInventoryItems;
    private TextView tvInventoryItemsCount;
    private InventoryWHRecord whInventoryRecord;
    private UhfReader uhfReader;
    private ScanInventoryThread transportationBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterInventoryItems;
    private String selectedAssetType;
    private String activeFilter = null;
    private  int selectedToggleButton = -1;
    private ImageButton ivAddItem, ivDeleteItem;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_asset);

        // instantiate an inventory Record
        whInventoryRecord = GlobalState.recWHInventory;

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initiate RFID scanner behaviour
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryItems.setLayoutManager(layoutManager);
        rvInventoryItems.setItemAnimator(new DefaultItemAnimator());
        adapterInventoryItems = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvInventoryItems.setAdapter(adapterInventoryItems);
        rvInventoryItems.setNestedScrollingEnabled(false);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvInventoryItemsCount.setText(String.valueOf(response.size()));
            adapterInventoryItems.setValues(new ArrayList<>(response));
            adapterInventoryItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

            if(selectedBarcode != null){
                adapterInventoryItems.removeItem(selectedBarcode);
                adapterInventoryItems.notifyDataSetChanged();
                tvInventoryItemsCount.setText(String.valueOf(adapterInventoryItems.getItemCount()));
            }
        });

        ivAddItem.setOnClickListener(view -> {

        });

        configFooter();
    }

    private void clearSelectedItem(){
        if(selectedItem!=null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        tgChooseAssetType = findViewById(R.id.tgChooseAssetType);
        rvInventoryItems = findViewById(R.id.rvInventoryItems);
        tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
        ivDeleteItem = (ImageButton) findViewById(R.id.ivDeleteItem);
        ivAddItem = (ImageButton) findViewById(R.id.ivAddItem);

        tgChooseAssetType.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            transportationBinsThread.setScanInProgress(scanning);

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            transportationBinsThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), InventoryStartActivity.class);
            startActivity(i);
        });
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanAsset);
        scanButton.setOnClickListener(view -> {
            clearSelectedItem();
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (transportationBinsThread.getState() == Thread.State.TERMINATED) {
                transportationBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            transportationBinsThread.setScanInProgress(scanning);
            transportationBinsThread.setUhfReader(uhfReader);
            transportationBinsThread.setScanResult(scanResult);
            transportationBinsThread.setFilter(activeFilter);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (transportationBinsThread.getState() == Thread.State.NEW) {
                    transportationBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    transportationBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if( selectedToggleButton == checkedId){
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch(checkedId){
            case R.id.tbCage:
                selectedAssetType = Constants.ftCage;
                activeFilter = Filters.RFID_CAGE;
                break;
            case R.id.tbNet:
                selectedAssetType = Constants.ftNet;
                activeFilter = Filters.RFID_NET;
                break;
            case R.id.tbBin:
                selectedAssetType = Constants.ftBin;
                activeFilter = Filters.RFID_BIN;
                break;
            case R.id.tbPlatform:
                selectedAssetType = Constants.ftPlatform;
                activeFilter = Filters.RFID_PLATFORM;
                break;
            default:
                selectedAssetType = null;
                activeFilter = null;
                selectedToggleButton = -1;
                break;
        }
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

     /*If(GlobalState.recWHInventory.items==null || GlobalState.recWHIncoming.items.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Incoming items'"));
        }*/

        return sb.toString();
    }
}