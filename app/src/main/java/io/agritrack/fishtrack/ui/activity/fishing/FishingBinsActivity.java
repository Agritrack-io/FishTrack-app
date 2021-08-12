package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.rfid.ScanThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class FishingBinsActivity extends AppCompatActivity {

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanThread inventoryThread = new ScanThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterBins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());


        RecyclerView rvBins = (RecyclerView) findViewById(R.id.rvBins);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBins.setLayoutManager(layoutManager);
        rvBins.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>());
        rvBins.setAdapter(adapterBins);
        rvBins.setNestedScrollingEnabled(false);

        //Get reference of binsCount textView
        TextView tvBinsCount = findViewById(R.id.tvBinsCount);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvBinsCount.setText(String.valueOf(response.size()));
            adapterBins.setValues(new ArrayList<>(response));
            adapterBins.notifyDataSetChanged();
        });


        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);
        final Button scanButton = findViewById(R.id.btnScanBin);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (inventoryThread.getState() == Thread.State.TERMINATED) {
                inventoryThread = new ScanThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            inventoryThread.setScanInProgress(scanning);
            inventoryThread.setUhfReader(uhfReader);
            inventoryThread.setAdapter(adapterBins);
            inventoryThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (inventoryThread.getState() == Thread.State.NEW) {
                    inventoryThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_bin);
                try {
                    inventoryThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //Get reference of delete bin item button
        ImageView ivDeleteBin = findViewById(R.id.ivDeleteBin);
        ivDeleteBin.setOnClickListener(view -> {
            //rvBins.getAdapter().
        });


        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        HarvestRecord hvst = GlobalState.recHarvest;

        if (hvst.availBins != null) {
            adapterBins.setValues((ArrayList<String>) hvst.availBins);
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(hvst.availBins.size()));
        }
    }

    private void updateState() {
        GlobalState.recHarvest.availBins = adapterBins.getValues();
    }

    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }
}