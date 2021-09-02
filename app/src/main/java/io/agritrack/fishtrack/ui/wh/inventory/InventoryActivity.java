package io.agritrack.fishtrack.ui.wh.inventory;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import io.agritrack.fishtrack.common.FishTrackUtils;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class InventoryActivity extends AppCompatActivity {

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private Spinner spSite, spAssetType;
    private RecyclerView rvInventoryItems;
    private TextView tvInventoryItemsCount;
    private InventoryWHRecord whInventoryRecord;
    private UhfReader uhfReader;
    private ScanInventoryThread transportationBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterInventoryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

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
        adapterInventoryItems = new TemplateRecyclerAdapter(this, new ArrayList<>());
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

        // Get reference of widgets from XML layout
        final Spinner spSite = (Spinner) findViewById(R.id.spSite);

        // Initializing a String Array
        String[] sites = new String[]{
                "Select site...",
                "Warehouse",
                "platform",
                "else",
                "else"
        };

        final List<String> siteList = new ArrayList<>(Arrays.asList(sites));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spSiteArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.simple_spinner_item, siteList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spSiteArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spSite.setAdapter(spSiteArrayAdapter);

        spSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Handle Assets spinner behaviour.
        // ---------------------------------
        // load all Asset Types and fill in the spAssetType Spinner.
        String[] assetTypeArray = FishTrackUtils.assetTypes("Select asset type...");

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spAssetTypeArrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, assetTypeArray) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spAssetTypeArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spAssetType.setAdapter(spAssetTypeArrayAdapter);

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        configFooter();
    }

    private void assignCtrlVars() {
        spSite = findViewById(R.id.spSite);
        spAssetType = findViewById(R.id.spAssetType);
        rvInventoryItems = findViewById(R.id.rvInventoryItems);
        tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
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
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanAsset);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (transportationBinsThread.getState() == Thread.State.TERMINATED) {
                transportationBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            transportationBinsThread.setScanInProgress(scanning);
            transportationBinsThread.setUhfReader(uhfReader);
            transportationBinsThread.setAdapter(adapterInventoryItems);
            transportationBinsThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (transportationBinsThread.getState() == Thread.State.NEW) {
                    transportationBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                try {
                    transportationBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

     /*If(GlobalState.recWHInventory.items==null || GlobalState.recWHIncoming.items.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Incoming items'"));
        }*/

        return sb.toString();
    }
}
