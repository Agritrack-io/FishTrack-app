package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingMainActivity extends AppCompatActivity {
    String[] harvestRequestors = {"Nikos", "George", "Vlasis"};

    Spinner harvestSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_main);

        getSupportActionBar().hide();


        Spinner harvestSpinner = (Spinner) findViewById(R.id.spHarvest);
        ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestRequestors);
        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        harvestSpinner.setAdapter(hrAdapter);

        configFooter();

    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToBins);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fishing bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });
    }
}