package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingDetailsActivity extends AppCompatActivity {
    private MobileDB db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_details);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingDetails);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load users with Harvest role and fill in the spHarvest Spinner.
        List<AppUser> ichthyopathologists = db.userDAO().getByRole("ROLE_PATHOLOGIST");
        if(ichthyopathologists!=null && !ichthyopathologists.isEmpty()) {
            String[] harvestRequester = ichthyopathologists.stream().map(x->x.email).toArray(String[]::new);
            Spinner harvestSpinner = findViewById(R.id.spHarvest);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, harvestRequester);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            harvestSpinner.setAdapter(hrAdapter);
        }










        configFooter();
    }


    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToCage);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
        ImageView ivNext = (ImageView) findViewById(R.id.ivToFillBins);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
            startActivity(i);
        });
    }
}