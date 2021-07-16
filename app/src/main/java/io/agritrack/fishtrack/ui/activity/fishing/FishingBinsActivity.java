package io.agritrack.fishtrack.ui.activity.fishing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingBinsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_bins);

        getSupportActionBar().hide();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fishing team!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToFishing);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fishing!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingMainActivity.class);
            startActivity(i);
        });
    }
}