package io.agritrack.fishtrack.ui.activity.maintenance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.fishing.FishingFillBinsActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class MaintenanceInternalConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_confirm);

        configFooter();
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceInternalTeam);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fill Bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), MaintenanceInternalTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Congratulations!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
        });
    }
}