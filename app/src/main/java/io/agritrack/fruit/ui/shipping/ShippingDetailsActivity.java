package io.agritrack.fruit.ui.shipping;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.transport.TransportStartActivity;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class ShippingDetailsActivity extends AppCompatActivity {

    private MobileDB db;
    private AutoCompleteTextView etDriverName, etLicensePlate;
    private Spinner spCustomer;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_details);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderShippingDetails);
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
            spCustomer.setAdapter(hrAdapter);
        }

        // AutoCompleteTextView driverNames, driverPhones, licensePlates

        Set<String> driverNames = LocalPreferences.getDriverNames();
        ArrayAdapter<String> driverNamesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, driverNames.toArray(new String[driverNames.size()]));
        etDriverName.setThreshold(3);
        etDriverName.setAdapter(driverNamesAdapter);

        Set<String> licensePlates = LocalPreferences.getLicensePlates();
        ArrayAdapter<String> licensePlatesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, licensePlates.toArray(new String[licensePlates.size()]));
        etLicensePlate.setThreshold(3);
        etLicensePlate.setAdapter(licensePlatesAdapter);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ShippingDetailsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ShippingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToShippingStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ShippingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        spCustomer = findViewById(R.id.spCustomer);
        etDriverName = (AutoCompleteTextView) findViewById(R.id.etDriverName);
        etLicensePlate = (AutoCompleteTextView) findViewById(R.id.etLicensePlate);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {

    }
}