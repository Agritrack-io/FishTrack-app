package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class FishingFillBinsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        //tvAvailableBinsCount

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }
}