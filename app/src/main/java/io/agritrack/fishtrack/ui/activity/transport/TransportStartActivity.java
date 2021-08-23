package io.agritrack.fishtrack.ui.activity.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.TransportationRecord;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class TransportStartActivity extends AppCompatActivity {

    private MobileDB db;
    private SwitchCompat swRefrigeratedTruck, swParallelTransport;
    private EditText etDriverName, etLicensePlate, etSecurityClip;
    private Spinner spPackagingSite, spCompany;

    private String[] company = {"nireas","andromeda","selonda"};

    Spinner companySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // get  references of the controls
        spPackagingSite = findViewById(R.id.spPackagingSite);
        spCompany = findViewById(R.id.spCompany);
        etDriverName = findViewById(R.id.etDriverName);
        etLicensePlate = findViewById(R.id.etLicensePlate);
        swRefrigeratedTruck = findViewById(R.id.swRefrigeratedTruck);
        swParallelTransport = findViewById(R.id.swParallelTransport);
        etSecurityClip = findViewById(R.id.etSecurityClip);


        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> packagingSites = db.siteDAO().getAll();
        if(packagingSites!=null && !packagingSites.isEmpty()) {
            String[] packagingSite = packagingSites.stream().map(x->x.name).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, packagingSite);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spPackagingSite.setAdapter(hrAdapter);
        }

        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, company);
        cAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spCompany.setAdapter(cAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();

        if(db!=null){
            if(db.isOpen()) {
                db.close();
            }
            db=null;
        }
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToTransportBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {

        TransportationRecord trns = GlobalState.recTransport;

        if(trns.sitePos>-1) {
            Spinner siteSpinner = findViewById(R.id.spPackagingSite);
            siteSpinner.setSelection(trns.sitePos);
        }

        if(trns.companyPos>-1) {
            Spinner companySpinner = findViewById(R.id.spCompany);
            companySpinner.setSelection(trns.companyPos);
        }

        if(!Strings.isEmptyOrWhitespace(trns.driverName)) {
            EditText etDrNm = findViewById(R.id.etDriverName);
            etDrNm.setText(trns.driverName);
        }

        if(!Strings.isEmptyOrWhitespace(trns.licensePlate)) {
            EditText etDrNm = findViewById(R.id.etDriverName);
            etDrNm.setText(trns.driverName);
        }
    }

    private TransportationRecord updateState() {
        TransportationRecord transportationRecord = GlobalState.initTransportationTx();

        transportationRecord.packagingSite = spPackagingSite.getSelectedItem().toString();
        transportationRecord.sitePos = spPackagingSite.getSelectedItemPosition();
        transportationRecord.destinationCompany = spCompany.getSelectedItem().toString();
        transportationRecord.companyPos = spCompany.getSelectedItemPosition();
        transportationRecord.driverName = etDriverName.getText().toString();
        transportationRecord.licensePlate = etLicensePlate.getText().toString();
        transportationRecord.clipNumber = etSecurityClip.getText().toString();
        transportationRecord.refrigeratedTruck = swRefrigeratedTruck.isChecked();
        transportationRecord.parallelTransport = swParallelTransport.isChecked();


        return transportationRecord;
    }
}