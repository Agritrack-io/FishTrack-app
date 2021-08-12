package io.agritrack.fishtrack.ui.activity.wh.outgoing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class OutgoingProcessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_process);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartOutgoing);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });
    }
}