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
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class TransportInfoActivity extends AppCompatActivity {

    private MobileDB db;
    private SwitchCompat swRefrigeratedTruck, swParallelTransport;
    private AutoCompleteTextView etDriverName, etLicensePlate, etDriverPhone;
    private EditText etSecurityClip;
    private Spinner spPackagingSite;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_info);

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
            String[] packagingSite = packagingSites.stream().map(x -> x.name).sorted().toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, packagingSite);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spPackagingSite.setAdapter(hrAdapter);
            spPackagingSite.setSelection(hrAdapter.getPosition("PLANT"));
        }

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

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TransportInfoActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        spPackagingSite = findViewById(R.id.spPackagingSite);
        etDriverName = (AutoCompleteTextView) findViewById(R.id.etDriverName);
        etDriverPhone = (AutoCompleteTextView) findViewById(R.id.etDriverPhone);
        etLicensePlate = (AutoCompleteTextView) findViewById(R.id.etLicensePlate);
        swRefrigeratedTruck = findViewById(R.id.swRefrigeratedTruck);
        swParallelTransport = findViewById(R.id.swParallelTransport);
        etSecurityClip = findViewById(R.id.etSecurityClip);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTransportBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        TransportationRecord trns = recTransport;

        if (trns.sitePos > -1) {
            spPackagingSite.setSelection(trns.sitePos);
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

    private  void updateState() {
        if (spPackagingSite.getSelectedItem() != null) {
            recTransport.packagingSite = spPackagingSite.getSelectedItem().toString();
        }
        recTransport.sitePos = spPackagingSite.getSelectedItemPosition();

        if (etDriverName.getText() != null) {
            recTransport.driverName = etDriverName.getText().toString();
            LocalPreferences.addDriverName(recTransport.driverName);
        }
        if (etDriverPhone.getText() != null) {
            recTransport.driverPhone = etDriverPhone.getText().toString();
            LocalPreferences.addDriverPhone(recTransport.driverPhone);
        }
        if (etLicensePlate.getText() != null) {
            recTransport.licensePlate = etLicensePlate.getText().toString();
            LocalPreferences.addLicensePlate(recTransport.licensePlate);
        }
        if (etSecurityClip.getText() != null) {
            recTransport.clipNumber = etSecurityClip.getText().toString();
        }
        recTransport.refrigeratedTruck = swRefrigeratedTruck.isChecked();
        recTransport.parallelTransport = swParallelTransport.isChecked();
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recTransport.packagingSite)) {
                sb.append(String.format("\n%s is missing", "'Packaging site'"));
            }

            if (Strings.isEmptyOrWhitespace(recTransport.driverName)) {
                sb.append(String.format("\n%s is missing", "'Driver name'"));
            }

            if (Strings.isEmptyOrWhitespace(recTransport.driverPhone)) {
                sb.append(String.format("\n%s is missing", "'Driver phone'"));
            }

            if (Strings.isEmptyOrWhitespace(recTransport.licensePlate)) {
                sb.append(String.format("\n%s is missing", "'License plate'"));
            }

            if (Strings.isEmptyOrWhitespace(recTransport.clipNumber)) {
                sb.append(String.format("\n%s is missing", "'Security clip number'"));
            }
        }

        return sb.toString();
    }
}