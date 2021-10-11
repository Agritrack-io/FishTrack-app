package io.agritrack.fish.ui.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class TransportStartActivity extends AppCompatActivity {

    private MobileDB db;
    private SwitchCompat swRefrigeratedTruck, swParallelTransport;
    private AutoCompleteTextView etDriverName, etLicensePlate, etDriverPhone;
    private EditText etSecurityClip;
    private Spinner spPackagingSite, spCompany;

    private final String[] company = {"Nireas", "Andromeda", "Selonda"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> packagingSites = db.siteDAO().getAllProcessingPlants();
        if (packagingSites != null && !packagingSites.isEmpty()) {
            String[] packagingSite = packagingSites.stream().map(x -> x.name).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, packagingSite);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spPackagingSite.setAdapter(hrAdapter);
        }

        ArrayAdapter<String> cAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, company);
        cAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spCompany.setAdapter(cAdapter);

        // AutoCompleteTextView driverNames, driverPhones, licensePlates

        Set<String> driverNames = LocalPreferences.getDriverNames();
        ArrayAdapter<String> driverNamesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, driverNames.toArray(new String[driverNames.size()]));
        etDriverName.setThreshold(3);
        etDriverName.setAdapter(driverNamesAdapter);

        Set<String> driverPhones = LocalPreferences.getDriverPhones();
        ArrayAdapter<String> driverPhonesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, driverPhones.toArray(new String[driverPhones.size()]));
        etDriverPhone.setThreshold(3);
        etDriverPhone.setAdapter(driverPhonesAdapter);

        Set<String> licensePlates = LocalPreferences.getLicensePlates();
        ArrayAdapter<String> licensePlatesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, licensePlates.toArray(new String[licensePlates.size()]));
        etLicensePlate.setThreshold(3);
        etLicensePlate.setAdapter(licensePlatesAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    private void assignCtrlVars() {
        spPackagingSite = findViewById(R.id.spPackagingSite);
        spCompany = findViewById(R.id.spCompany);
        etDriverName = (AutoCompleteTextView) findViewById(R.id.etDriverName);
        etDriverPhone = (AutoCompleteTextView) findViewById(R.id.etDriverPhone);
        etLicensePlate = (AutoCompleteTextView) findViewById(R.id.etLicensePlate);
        swRefrigeratedTruck = findViewById(R.id.swRefrigeratedTruck);
        swParallelTransport = findViewById(R.id.swParallelTransport);
        etSecurityClip = findViewById(R.id.etSecurityClip);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTransportBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (trns.sitePos > -1) {
            spPackagingSite.setSelection(trns.sitePos);
        }

        if (trns.companyPos > -1) {
            spCompany.setSelection(trns.companyPos);
        }

        if (!Strings.isEmptyOrWhitespace(trns.driverName)) {
            etDriverName.setText(trns.driverName);
        }

        if (!Strings.isEmptyOrWhitespace(trns.driverPhone)) {
            etDriverPhone.setText(trns.driverPhone);
        }

        if (!Strings.isEmptyOrWhitespace(trns.licensePlate)) {
            etLicensePlate.setText(trns.licensePlate);
        }

        if (!Strings.isEmptyOrWhitespace(trns.clipNumber)) {
            etSecurityClip.setText(trns.clipNumber);
        }

        swRefrigeratedTruck.setChecked(trns.refrigeratedTruck);
        swParallelTransport.setChecked(trns.parallelTransport);
    }

    private TransportationRecord updateState() {
        TransportationRecord transportationRecord = GlobalState.initTransportationRecord();

        if (spPackagingSite.getSelectedItem() != null) {
            transportationRecord.packagingSite = spPackagingSite.getSelectedItem().toString();
        }
        transportationRecord.sitePos = spPackagingSite.getSelectedItemPosition();
        if (spCompany.getSelectedItem() != null) {
            transportationRecord.destinationCompany = spCompany.getSelectedItem().toString();
        }
        transportationRecord.companyPos = spCompany.getSelectedItemPosition();
        if (etDriverName.getText() != null) {
            transportationRecord.driverName = etDriverName.getText().toString();
            LocalPreferences.addDriverName(transportationRecord.driverName);
        }
        if (etDriverPhone.getText() != null) {
            transportationRecord.driverPhone = etDriverPhone.getText().toString();
            LocalPreferences.addDriverPhone(transportationRecord.driverPhone);
        }
        if (etLicensePlate.getText() != null) {
            transportationRecord.licensePlate = etLicensePlate.getText().toString();
            LocalPreferences.addLicensePlate(transportationRecord.licensePlate);
        }
        if (etSecurityClip.getText() != null) {
            transportationRecord.clipNumber = etSecurityClip.getText().toString();
        }
        transportationRecord.refrigeratedTruck = swRefrigeratedTruck.isChecked();
        transportationRecord.parallelTransport = swParallelTransport.isChecked();

        return transportationRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.packagingSite)) {
            sb.append(String.format("\n%s is missing", "'Packaging site'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.destinationCompany)) {
            sb.append(String.format("\n%s is missing", "'Company'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.driverName)) {
            sb.append(String.format("\n%s is missing", "'Driver name'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.driverPhone)) {
            sb.append(String.format("\n%s is missing", "'Driver phone'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.licensePlate)) {
            sb.append(String.format("\n%s is missing", "'License plate'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recTransport.clipNumber)) {
            sb.append(String.format("\n%s is missing", "'Security clip number'"));
        }

        return sb.toString();
    }
}