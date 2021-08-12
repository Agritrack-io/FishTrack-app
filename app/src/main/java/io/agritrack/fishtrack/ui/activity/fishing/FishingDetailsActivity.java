package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.HarvestRecord;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingDetailsActivity extends AppCompatActivity {

    private SwitchCompat bIceAdequacy;
    private EditText etIceSupplier;

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


        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
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
        if (hvst.fishingTeam != null) {
            etIceSupplier.setText(hvst.iceSupplier);
            bIceAdequacy.setChecked(hvst.adequateIce);
        }
    }

    private void updateState() {
        GlobalState.recHarvest.adequateIce = bIceAdequacy.isChecked();
        GlobalState.recHarvest.iceSupplier = etIceSupplier.getText().toString();
    }
}