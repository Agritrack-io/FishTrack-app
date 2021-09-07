package io.agritrack.fishtrack.ui.fishing;

import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingBinsActivity extends AppCompatActivity {
    private MobileDB db;

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread inventoryThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterBins;

    private RecyclerView rvBins;
    private TextView tvBinsCount;

    private ImageButton ivAddBin, ivDeleteBin;
    private  String selectedBarcode;
    private ConstraintLayout selectedItem;

    // Instantiate a clickListener to be passed to adapterBins.
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
        setContentView(R.layout.activity_fishing_bins);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBins.setLayoutManager(layoutManager);
        rvBins.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvBins.setAdapter(adapterBins);
        rvBins.setNestedScrollingEnabled(false);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvBinsCount.setText(String.valueOf(response.size()));
            adapterBins.setValues(new ArrayList<>(response));
            adapterBins.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteBin.setOnClickListener(view -> {
            if(selectedBarcode != null){
                adapterBins.removeItem(selectedBarcode);
                adapterBins.notifyDataSetChanged();
                tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
            }
        });

        ivAddBin.setOnClickListener(view -> {

        });

        // create Footer
        configFooter();
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanBin);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (inventoryThread.getState() == Thread.State.TERMINATED) {
                inventoryThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            inventoryThread.setScanInProgress(scanning);
            inventoryThread.setUhfReader(uhfReader);
            inventoryThread.setScanResult(scanResult);
            inventoryThread.setFilter(Filters.RFID_BIN);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                        }
                    });
                if (inventoryThread.getState() == Thread.State.NEW) {
                    inventoryThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_bin);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    inventoryThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            inventoryThread.setScanInProgress(scanning);

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            inventoryThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        rvBins = findViewById(R.id.rvBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = (ImageButton) findViewById(R.id.ivDeleteBin1);
        ivAddBin = (ImageButton) findViewById(R.id.ivAddBin);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (hvst.availBins != null) {
            adapterBins.setValues(new LinkedList<String>(hvst.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvBinsCount.setText(String.valueOf(hvst.availBins.size()));
        }
    }

    private void updateState() {
        GlobalState.recFishing.availBins = new LinkedList<>(adapterBins.getValues());

        GlobalState.commitFishing(db, Boolean.FALSE);
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(GlobalState.recFishing.availBins==null || GlobalState.recFishing.availBins.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Bins for usage'"));
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }
}