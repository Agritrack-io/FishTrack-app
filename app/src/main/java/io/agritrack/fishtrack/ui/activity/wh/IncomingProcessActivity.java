package io.agritrack.fishtrack.ui.activity.wh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.fishing.FishingTeamActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class IncomingProcessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_process);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Congratulations!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartIncoming);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Start incoming!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), IncomingStartActivity.class);
            startActivity(i);
        });
    }
}