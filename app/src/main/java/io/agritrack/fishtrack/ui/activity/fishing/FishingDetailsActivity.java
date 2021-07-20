package io.agritrack.fishtrack.ui.activity.fishing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_details);

        getSupportActionBar().hide();

        configFooter();
    }


    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToFillBins);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Cage!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fill Bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
            startActivity(i);
        });
    }
}