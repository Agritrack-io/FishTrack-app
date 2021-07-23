package io.agritrack.fishtrack.ui.activity.transport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class TransportSupervisorConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_supervisor_confirm);

        getSupportActionBar().hide();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToDriverConfirm);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Driver confirms!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
            startActivity(i);
        });
    }
}