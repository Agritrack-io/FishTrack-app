package io.agritrack.fish.ui.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.AppUser;
import io.agritrack.data.model.common.Species;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class FishingStartActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MobileDB db;
    private Spinner harvestSpinner, speciesSpinner;
    private EditText etQty;
    private TextView tvCageName, tvFishSize, tvNotes;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

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

            if (!Strings.isEmptyOrWhitespace(recFishing.requesterName)) {
                recFishing.requesterPos = Arrays.asList(harvestRequester).indexOf(recFishing.requesterName);
            }
        }

        // load fish species and fill in the spFishType Spinner.
        List<Species> fishSpecies = db.speciesDAO().getAll();
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingStartActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingStartActivity.this);
            infoDialog.showDialog();
        });


        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            if(IsDemo){
                db.fishingTransactionDAO().deleteAll();
            }

            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        harvestSpinner = findViewById(R.id.spHarvest);
        speciesSpinner = findViewById(R.id.spFishType);
        etQty = findViewById(R.id.etRequestedQuantity);
        ivSupport = findViewById(R.id.ivSupport);
        tvCageName = findViewById(R.id.tvCageName);
        tvFishSize = findViewById(R.id.tvFishSize);
        tvNotes = findViewById(R.id.tvNotes);
        ivInfo = findViewById(R.id.ivInfo);
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

        if (!Strings.isEmptyOrWhitespace(hvst.cageCode)){
            tvCageName.setText(hvst.cageCode);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.fishSize)){
            tvFishSize.setText(hvst.fishSize);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.notes)){
            tvNotes.setText(hvst.notes);
        }
        //harvestSpinner.setSelection(arrayAdapter.getPosition("Category 2"));
    }

    private FishingRecord updateState() {
        FishingRecord fishingRecord = recFishing;

        if (harvestSpinner.getSelectedItem() != null) {
            fishingRecord.requesterName = harvestSpinner.getSelectedItem().toString();
        }
        fishingRecord.requesterPos = harvestSpinner.getSelectedItemPosition();
        if (speciesSpinner.getSelectedItem() != null) {
            fishingRecord.speciesName = speciesSpinner.getSelectedItem().toString();
        }
        fishingRecord.speciesPos = speciesSpinner.getSelectedItemPosition();
        if (etQty.getText() != null) {
            fishingRecord.reqWeight = etQty.getText().toString();
        }

        if (tvCageName.getText() != null) {
            fishingRecord.cageCode = tvCageName.getText().toString();
        }

        if (tvFishSize.getText() != null) {
            fishingRecord.fishSize = tvFishSize.getText().toString();
        }
        //fishingRecord.reqWeight = etQty.getText() != null ? Double.valueOf(etQty.getText().toString()).intValue() + "" : "0";

        GlobalState.commitFishing(db, Boolean.FALSE);

        return fishingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if(!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.requesterName)) {
                sb.append(String.format("\n%s is missing", "'Harvest initiator'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.speciesName)) {
                sb.append(String.format("\n%s is missing", "'Fish type'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.reqWeight)) {
                sb.append(String.format("\n%s is missing", "'Requested quantity'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.cageCode)) {
                sb.append(String.format("\n%s is missing", "'Cage code'"));
            }
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