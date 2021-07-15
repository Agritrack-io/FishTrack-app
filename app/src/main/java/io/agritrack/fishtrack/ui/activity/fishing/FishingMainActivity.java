package io.agritrack.fishtrack.ui.activity.fishing;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

public class FishingMainActivity extends AppCompatActivity {
    String[] harvestRequestors = {"Nikos", "George", "Vlasis"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_main);

        getSupportActionBar().hide();

        Spinner harvestSpinner = (Spinner) findViewById(R.id.spHarvest);
        ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestRequestors);
        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        harvestSpinner.setAdapter(hrAdapter);

    }
}