package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.data.model.common.FishSpecies;
import io.agritrack.fishtrack.rfid.ScanThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingStartActivity extends AppCompatActivity {
    private MobileDB db;
    private UhfReader uhfReader;
    private ScanThread inventoryThread = new ScanThread();
    private boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load users with Harvest role and fill in the spHarvest Spinner.
        List<AppUser> harvestRequestUsers = db.userDAO().getByRole("ROLE_HARVEST");
        if(harvestRequestUsers!=null && !harvestRequestUsers.isEmpty()) {
            String[] harvestRequester = harvestRequestUsers.stream().map(x->x.email).toArray(String[]::new);
            Spinner harvestSpinner = findViewById(R.id.spHarvest);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestRequester);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            harvestSpinner.setAdapter(hrAdapter);
        }

        // load fish species and fill in the spFishType Spinner.
        List<FishSpecies> fishSpecies = db.speciesDAO().getAll();
        if(fishSpecies!=null && !fishSpecies.isEmpty()) {
            String[] species = fishSpecies.stream().map(x->x.localName).toArray(String[]::new);
            Spinner speciesSpinner = (Spinner) findViewById(R.id.spFishType);
            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, species);
            spAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            speciesSpinner.setAdapter(spAdapter);
        }

        // fill in Quantities spinner
        String[] harvestQuantities = new String[]{"500","750","1000","1250","1500","1750","2000"};
        {
            Spinner qtySpinner = (Spinner) findViewById(R.id.spRequestedQuantity);
            ArrayAdapter<String> qtAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestQuantities);
            qtAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            qtySpinner.setAdapter(qtAdapter);
        }

        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);
        final Button scanButton = findViewById(R.id.btnScanPlatform);
        scanButton.setOnClickListener(view -> {
            TextView tvPlatformName = findViewById(R.id.tvPlatformName);
            scanning = !scanning;

            // Follwing check is required to instantiate a ScanningThread that was stopped previously.
            if (inventoryThread.getState() == Thread.State.TERMINATED)
            {
                inventoryThread = new ScanThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            inventoryThread.setScanInProgress(scanning);
            inventoryThread.setUhfReader(uhfReader);
            inventoryThread.setRfidTag(tvPlatformName);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (inventoryThread.getState() == Thread.State.NEW)
                {
                    inventoryThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_platfom);
                try {
                    inventoryThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }


    private void initControlsFromState() {

        if(GlobalState.recHarvest == null) {
            return;
        }
        HarvestRecord hvst = GlobalState.recHarvest;

        if(hvst.requesterPos>-1) {
            Spinner harvestSpinner = findViewById(R.id.spHarvest);
            harvestSpinner.setSelection(hvst.requesterPos);
        }

        if(hvst.speciesPos>-1) {
            Spinner speciesSpinner = findViewById(R.id.spFishType);
            speciesSpinner.setSelection(hvst.speciesPos);
        }

        if(!Strings.isEmptyOrWhitespace(hvst.reqWeight)) {
            Spinner qtySpinner = findViewById(R.id.spRequestedQuantity);
            qtySpinner.setSelection(1);
        }

        if(!Strings.isEmptyOrWhitespace(hvst.platformBC)) {
            TextView tvPlatformRFID = findViewById(R.id.tvPlatformName);
            tvPlatformRFID.setText(hvst.platformBC);
        }
        //harvestSpinner.setSelection(arrayAdapter.getPosition("Category 2"));
    }


    private HarvestRecord updateState() {
        HarvestRecord harvestRecord = GlobalState.initHarvest();

        Spinner harvestSpinner = findViewById(R.id.spHarvest);
        Spinner speciesSpinner = findViewById(R.id.spFishType);
        Spinner qtySpinner = findViewById(R.id.spRequestedQuantity);
        TextView tvPlatformRFID = findViewById(R.id.tvPlatformName);

        harvestRecord.requesterName = harvestSpinner.getSelectedItem().toString();
        harvestRecord.requesterPos = harvestSpinner.getSelectedItemPosition();
        harvestRecord.speciesName = speciesSpinner.getSelectedItem().toString();
        harvestRecord.speciesPos = speciesSpinner.getSelectedItemPosition();
        harvestRecord.reqWeight = qtySpinner.getSelectedItem().toString();
        harvestRecord.platformBC = tvPlatformRFID.getText().toString();

        return harvestRecord;
    }


    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
    }
}