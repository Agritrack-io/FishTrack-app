package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingCageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_cage);

        getSupportActionBar().hide();

        configFooter();


    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToTeam);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Team!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToDetails);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Details!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }
}