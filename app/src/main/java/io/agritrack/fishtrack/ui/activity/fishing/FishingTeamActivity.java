package io.agritrack.fishtrack.ui.activity.fishing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingTeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_team);

        getSupportActionBar().hide();

        configFooter();
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });
    }
}