package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;
import static io.agritrack.fishtrack.state.GlobalState.recHarvest;

public class FishingConfirmActivity extends AppCompatActivity {
    private MobileDB db;
    private TextView tvTotalQuantityCount, tvReqQuantityCount, tvNumberOfBinsCount, tvNameCage, tvTypeOfFishConfirm, tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // get reference to Login
        tvUsername = findViewById(R.id.tvUsername);

        // get references to local TextViews
        tvTotalQuantityCount = findViewById(R.id.tvTotalQuantityCount);
        tvReqQuantityCount = findViewById(R.id.tvReqQuantityCount);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvNameCage = findViewById(R.id.tvNameCage);
        tvTypeOfFishConfirm = findViewById(R.id.tvTypeOfFishConfirm);


        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToFillBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
            startActivity(i);
        });
    }


    private void initControlsFromState() {
        tvUsername.setText(LocalPreferences.getLoggedInUser(""));

        tvTotalQuantityCount.setText(recHarvest.totalFishWeight != null ? recHarvest.totalFishWeight.toString() : "N/A");
        tvReqQuantityCount.setText(recHarvest.reqWeight != null ? recHarvest.reqWeight : "N/A");
        tvNumberOfBinsCount.setText(recHarvest.totalBinsUsed != null ? recHarvest.totalBinsUsed.toString() : "N/A");
        tvNameCage.setText(recHarvest.cageRFID != null ? recHarvest.cageRFID : "N/A");
        tvTypeOfFishConfirm.setText(recHarvest.speciesName != null ? recHarvest.speciesName : "N/A");
    }

    private void updateState() {
        TextView tvUsername = findViewById(R.id.tvUsername);
        EditText etPasswordFishing = findViewById(R.id.etPasswordFishing);



        ImageView ivBack = findViewById(R.id.ivBackToFillBins);

        db.harvestTransactionDAO();


    }
}