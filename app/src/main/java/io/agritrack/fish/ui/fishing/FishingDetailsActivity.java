package io.agritrack.fish.ui.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Arrays;
import java.util.List;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class FishingDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final List<Double> temperatures = Arrays.asList(15.0d, 16.0d, 17.0d, 18.0d, 19.0d, 20.0d, 21.0d, 22.0d, 23.0d, 24.0d, 25.0d, 26.0d);
    private SwitchCompat bIceAdequacy;
    private EditText etIceSupplier;
    private Spinner spSeaTemp;
    private TextView tvPathologist, tvLastFed, tvSpecies;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_details);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingDetails);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        tvPathologist.setText(GlobalState.recFishing.pathologist);

        tvLastFed.setText(GlobalState.recFishing.lastFed != null ? GlobalState.recFishing.lastFed.toString() : "");

        tvSpecies.setText(GlobalState.recFishing.speciesName);

        // fill the Temperatures spinner with data
        ArrayAdapter<Double> temperaturesAdapter = new ArrayAdapter<Double>(this, R.layout.simple_spinner_item, this.temperatures);
        spSeaTemp.setAdapter(temperaturesAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingDetailsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        GlobalState.recFishing.seaTemperature = this.temperatures.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToFillBins);
        ivNext.setOnClickListener(view -> {
            updateState();
                Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
                startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToCage);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        spSeaTemp = findViewById(R.id.spSeaTemp);
        tvPathologist = findViewById(R.id.tvNameOfIchthyopathologist);
        tvLastFed = findViewById(R.id.tvDateOfLastNutrition);
        tvSpecies = findViewById(R.id.tvTypeOfFish);
        bIceAdequacy = findViewById(R.id.switchIceAdequacy);
        etIceSupplier = findViewById(R.id.etIceSupplier);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;
        if (hvst != null) {
            spSeaTemp.setSelection(this.temperatures.indexOf(GlobalState.recFishing.seaTemperature));
            etIceSupplier.setText(hvst.iceSupplier);
            bIceAdequacy.setChecked(hvst.adequateIce);
        }
    }

    private void updateState() {
        GlobalState.recFishing.adequateIce = bIceAdequacy.isChecked();
        GlobalState.recFishing.iceSupplier = etIceSupplier.getText().toString();
        GlobalState.recFishing.seaTemperature = Double.valueOf(spSeaTemp.getSelectedItem().toString());
    }
}