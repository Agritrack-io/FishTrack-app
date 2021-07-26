package io.agritrack.fishtrack.ui.activity.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.fishtrack.R;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class TransportStartActivity extends AppCompatActivity {

    private String[] sites = {"sagiada","anixi","agios stefanos","penteli","ilioupoli"};

    private String[] company = {"nireas","andromeda","selonda"};

    Spinner siteSpinner;

    Spinner companySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_start);

        Spinner siteSpinner = (Spinner) findViewById(R.id.spPackagingSite);
        ArrayAdapter<String> psAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, sites);
        psAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        siteSpinner.setAdapter(psAdapter);

        Spinner companySpinner = (Spinner) findViewById(R.id.spCompany);
        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, company);
        cAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        companySpinner.setAdapter(cAdapter);



        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToTransportBins);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Load bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });
    }
}