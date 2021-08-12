package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.rfid.ScanThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class FishingCageActivity extends AppCompatActivity {
    private MobileDB db;
    private UhfReader uhfReader;
    private ScanThread cageScanningThread = new ScanThread();
    private ScanThread netScanningThread = new ScanThread();
    private boolean scanning = false;

    private TextView tvCageRFID, tvNetRFID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_cage);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingCage);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        tvNetRFID = findViewById(R.id.tvNetName);
        tvCageRFID = findViewById(R.id.tvCageName);

        // initialize scanning threads
        prepareScanCageButton();
        prepareScanNetButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // create Footer
        configFooter();
    }

    private void prepareScanCageButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanCageButton = findViewById(R.id.btnScanCage);

        scanCageButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (cageScanningThread.getState() == Thread.State.TERMINATED)
            {
                cageScanningThread = new ScanThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            cageScanningThread.setScanInProgress(scanning);
            cageScanningThread.setUhfReader(uhfReader);
            cageScanningThread.setRfidTag(tvCageRFID);

            if (scanning) {
                scanCageButton.setText(R.string.stop_scan);
                if (cageScanningThread.getState() == Thread.State.NEW)
                {
                    cageScanningThread.start();
                }
            } else {
                scanCageButton.setText(R.string.scan_cage);
                try {
                    cageScanningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void prepareScanNetButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanNetButton = findViewById(R.id.btnScanNet);

        scanNetButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (cageScanningThread.getState() == Thread.State.TERMINATED)
            {
                cageScanningThread = new ScanThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            cageScanningThread.setScanInProgress(scanning);
            cageScanningThread.setUhfReader(uhfReader);
            cageScanningThread.setRfidTag(tvNetRFID);

            if (scanning) {
                scanNetButton.setText(R.string.stop_scan);
                if (cageScanningThread.getState() == Thread.State.NEW)
                {
                    cageScanningThread.start();
                }
            } else {
                scanNetButton.setText(R.string.scan_net);
                try {
                    cageScanningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToDetails);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }


    private void initControlsFromState() {
        if (GlobalState.getInstance().recHarvest == null) {
            return;
        }
        HarvestRecord hvst = GlobalState.getInstance().recHarvest;

        tvCageRFID.setText(hvst.cageRFID);
        tvNetRFID.setText(hvst.netRFID);
    }

    private void updateState() {
        GlobalState.getInstance().recHarvest.cageRFID = tvCageRFID.getText().toString();
        GlobalState.getInstance().recHarvest.netRFID = tvNetRFID.getText().toString();
    }
}