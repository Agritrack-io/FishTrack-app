package io.agritrack.fishtrack.ui.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.data.model.common.FishSpecies;
import io.agritrack.fishtrack.rfid.SingleShotScanner;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;
import static io.agritrack.fishtrack.state.GlobalState.recFishing;

public class FishingStartActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private MobileDB db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView tvPlatformName;
    private Spinner harvestSpinner, speciesSpinner;
    private EditText etQty;

    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // load users with Harvest role and fill in the spHarvest Spinner.
        List<AppUser> harvestRequestUsers = db.userDAO().getByRole("ROLE_HARVEST");
        if (harvestRequestUsers != null && !harvestRequestUsers.isEmpty()) {
            String[] harvestRequester = harvestRequestUsers.stream().map(x -> x.email).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestRequester);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            harvestSpinner.setAdapter(hrAdapter);
        }

        // load fish species and fill in the spFishType Spinner.
        List<FishSpecies> fishSpecies = db.speciesDAO().getAll();
        if (fishSpecies != null && !fishSpecies.isEmpty()) {
            String[] species = fishSpecies.stream().map(x -> x.localName).toArray(String[]::new);
            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, species);
            spAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            speciesSpinner.setAdapter(spAdapter);

            if (!Strings.isEmptyOrWhitespace(recFishing.speciesName)) {
                recFishing.speciesPos = Arrays.asList(species).indexOf(recFishing.speciesName);
            }
        }

        // =================================
        // RFID scanning functionality
        scanButton.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            scanner.setUhfReader(UhfReader.getInstance());

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvPlatformName.setText(epcStr);
                        }
                    });
                    //tvPlatformName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });
        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        scanButton = findViewById(R.id.btnScanPlatform);
        harvestSpinner = findViewById(R.id.spHarvest);
        speciesSpinner = findViewById(R.id.spFishType);
        tvPlatformName = findViewById(R.id.tvPlatformName);
        etQty = findViewById(R.id.etRequestedQuantity);
    }

    private void initControlsFromState() {

        FishingRecord hvst = recFishing;

        if (hvst.requesterPos > -1) {
            harvestSpinner.setSelection(hvst.requesterPos);
        }

        if (hvst.speciesPos > -1) {
            speciesSpinner.setSelection(hvst.speciesPos);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.reqWeight)) {
            etQty.setText(hvst.reqWeight);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.platformRFID)) {
            tvPlatformName.setText(hvst.platformRFID);
        }
        //harvestSpinner.setSelection(arrayAdapter.getPosition("Category 2"));
    }

    private FishingRecord updateState() {
        FishingRecord fishingRecord = recFishing;

        if(harvestSpinner.getSelectedItem()!=null) {
            fishingRecord.requesterName = harvestSpinner.getSelectedItem().toString();
        }
        fishingRecord.requesterPos = harvestSpinner.getSelectedItemPosition();
        if(speciesSpinner.getSelectedItem()!=null) {
            fishingRecord.speciesName = speciesSpinner.getSelectedItem().toString();
        }
        fishingRecord.speciesPos = speciesSpinner.getSelectedItemPosition();
        fishingRecord.reqWeight = etQty.getText().toString();
        if(tvPlatformName.getText()!=null) {
            fishingRecord.platformRFID = tvPlatformName.getText().toString();
        }

        GlobalState.commitFishing(db, Boolean.FALSE);

        return fishingRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recFishing.requesterName)){
            sb.append(String.format("\n%s is missing", "'Harvest initiator'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recFishing.speciesName)){
            sb.append(String.format("\n%s is missing", "'Fish type'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recFishing.reqWeight)){
            sb.append(String.format("\n%s is missing", "'Requested quantity'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recFishing.platformRFID)){
            sb.append(String.format("\n%s is missing", "'Platform tag'"));
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        if (executor != null)
            executor.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (executor != null)
            executor.shutdown();
    }
}