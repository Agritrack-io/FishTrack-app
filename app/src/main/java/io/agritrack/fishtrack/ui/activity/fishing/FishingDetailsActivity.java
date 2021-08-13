package io.agritrack.fishtrack.ui.activity.fishing;

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

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class FishingDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private List<Double> temperatures = Arrays.asList(15.0d, 16.0d, 17.0d, 18.0d, 19.0d, 20.0d, 21.0d, 22.0d, 23.0d, 24.0d, 25.0d, 26.0d);
    private SwitchCompat bIceAdequacy;
    private EditText etIceSupplier;
    private Spinner spSeaTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_details);

        // get references to input controls
        bIceAdequacy = findViewById(R.id.switchIceAdequacy);
        etIceSupplier = findViewById(R.id.etIceSupplier);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingDetails);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        TextView tvPathologist = findViewById(R.id.tvNameOfIchthyopathologist);
        tvPathologist.setText(GlobalState.recHarvest.Pathologist);

        TextView tvLastFed = findViewById(R.id.tvDateOfLastNutrition);
        tvLastFed.setText(GlobalState.recHarvest.lastFed.toString());

        TextView tvSpecies = findViewById(R.id.tvTypeOfFish);
        tvSpecies.setText(GlobalState.recHarvest.speciesName);

        // fill the Temperatures spinner with data
        spSeaTemp = findViewById(R.id.spSeaTemp);
        ArrayAdapter<Double> temperaturesAdapter = new ArrayAdapter<Double>(this, R.layout.simple_spinner_item, this.temperatures);
        spSeaTemp.setAdapter(temperaturesAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        GlobalState.recHarvest.seaTemperature = this.temperatures.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

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

    private void initControlsFromState() {
        HarvestRecord hvst = GlobalState.recHarvest;
        if (hvst != null) {
            spSeaTemp.setSelection(this.temperatures.indexOf(GlobalState.recHarvest.seaTemperature));
            etIceSupplier.setText(hvst.iceSupplier);
            bIceAdequacy.setChecked(hvst.adequateIce);
        }
    }

    private void updateState() {
        GlobalState.recHarvest.adequateIce = bIceAdequacy.isChecked();
        GlobalState.recHarvest.iceSupplier = etIceSupplier.getText().toString();
        GlobalState.recHarvest.seaTemperature = Double.valueOf(spSeaTemp.getSelectedItem().toString());
    }
}