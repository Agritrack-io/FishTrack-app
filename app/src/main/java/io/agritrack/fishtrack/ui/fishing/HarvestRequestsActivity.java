package io.agritrack.fishtrack.ui.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class HarvestRequestsActivity extends AppCompatActivity {
    private MobileDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_requests);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestReq);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // create Footer
        configFooter();
    }


    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToStartFishing);
        ivNext.setOnClickListener(view -> {
            //updateState();
            String v = null; //validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToHomeMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }
}