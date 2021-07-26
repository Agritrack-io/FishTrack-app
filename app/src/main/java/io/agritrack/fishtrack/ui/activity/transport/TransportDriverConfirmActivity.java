package io.agritrack.fishtrack.ui.activity.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class TransportDriverConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_driver_confirm);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToTransportBins);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Load bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToSupervisorconfirm);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Supervisor confirms!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportSupervisorConfirmActivity.class);
            startActivity(i);
        });
    }
}