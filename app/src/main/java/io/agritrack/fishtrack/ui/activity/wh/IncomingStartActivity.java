package io.agritrack.fishtrack.ui.activity.wh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.fishing.FishingTeamActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class IncomingStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_start);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToIncomingProcess);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Incoming process!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), IncomingProcessActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "WH Menu!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}