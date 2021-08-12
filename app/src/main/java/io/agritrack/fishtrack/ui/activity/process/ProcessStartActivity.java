package io.agritrack.fishtrack.ui.activity.process;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportBinsActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ProcessStartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

       /* Spinner siteSpinner = (Spinner) findViewById(R.id.spPackagingSite);
        ArrayAdapter<String> psAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, sites);
        psAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        siteSpinner.setAdapter(psAdapter);

        Spinner companySpinner = (Spinner) findViewById(R.id.spCompany);
        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, company);
        cAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        companySpinner.setAdapter(cAdapter);
*/


        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToReceiveBins);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ProcessBinsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }
}