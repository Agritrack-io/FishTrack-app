package io.agritrack.fishtrack.ui.activity.wh.incoming;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class IncomingStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToIncomingProcess);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), IncomingProcessActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}