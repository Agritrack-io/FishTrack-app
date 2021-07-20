package io.agritrack.fishtrack.ui.activity.fishing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingFillBinsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        getSupportActionBar().hide();

        configFooter();
    }

    protected void configFooter() {

        Button ivBack = (Button) findViewById(R.id.btnBackToDetails);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Details!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });

        Button ivNext = (Button) findViewById(R.id.btnEndFishing);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Confirm!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
            startActivity(i);
        });

    }
}