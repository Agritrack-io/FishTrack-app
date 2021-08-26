package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.CageDetails;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingCageActivity extends AppCompatActivity {
    private MobileDB db;
    private UhfReader uhfReader;
    private ScanInventoryThread cageScanningThread = new ScanInventoryThread();
    private ScanInventoryThread netScanningThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TextView tvCageRFID, tvNetRFID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_cage);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

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
                cageScanningThread = new ScanInventoryThread();
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
                cageScanningThread = new ScanInventoryThread();
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
        ImageView ivNext = findViewById(R.id.ivToDetails);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        tvCageRFID.setText(hvst.cageRFID);
        tvNetRFID.setText(hvst.netRFID);
    }

    private void updateState() {
        CharSequence cageRFID = tvCageRFID.getText();

        if(cageRFID != null) {
            CageDetails cage = db.cageDetailsDAO().getByRFId(cageRFID.toString());
            if(cage!=null) {
                GlobalState.recFishing.speciesName = cage.fishType;
                GlobalState.recFishing.pathologist = cage.ichthyopathologist;
                GlobalState.recFishing.lastFed = cage.lastFed;
                GlobalState.recFishing.cageRFID = cageRFID.toString();
            } else {
                // TODO:: add alert, no cage corresponding to RFID found in local DB!!
            }
        }

        GlobalState.recFishing.netRFID = tvNetRFID.getText().toString();
    }

    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }
}