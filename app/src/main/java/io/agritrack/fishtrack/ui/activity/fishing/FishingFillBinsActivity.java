package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingFillBinsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        configFooter();
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Details!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Confirm!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
            startActivity(i);
        });

    }
}