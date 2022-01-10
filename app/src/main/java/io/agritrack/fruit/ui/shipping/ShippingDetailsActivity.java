package io.agritrack.fruit.ui.shipping;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

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
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Customer;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fish.ui.transport.TransportStartActivity;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.ShippingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class ShippingDetailsActivity extends AppCompatActivity {

    private MobileDB db;
    private AutoCompleteTextView etDriverName, etDriverPhone, etLicensePlate;
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
        List<Customer> customers = db.customerDAO().getAll();
        if (customers != null && !customers.isEmpty()) {
            List<String> customerList = customers.stream().map(s -> s.name).collect(Collectors.toList());
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, customerList);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spCustomer.setAdapter(hrAdapter);
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
            supportDialog = new SupportDialog(ShippingDetailsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ShippingConfirmActivity.class);
                startActivity(i);
            }
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
        etDriverPhone = (AutoCompleteTextView) findViewById(R.id.etDriverPhone);
        etLicensePlate = (AutoCompleteTextView) findViewById(R.id.etLicensePlate);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        ShippingRecord trns = FruitGlobalState.recShipping;
        if (trns.customerPos > -1) {
            spCustomer.setSelection(trns.customerPos);
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
    }

    private void updateState() {
        ShippingRecord shippingRecord = FruitGlobalState.recShipping;

        if (spCustomer.getSelectedItem() != null) {
            shippingRecord.customer = spCustomer.getSelectedItem().toString();
        }
        shippingRecord.customerPos = spCustomer.getSelectedItemPosition();

        if (etDriverName.getText() != null) {
            shippingRecord.driverName = etDriverName.getText().toString();
            LocalPreferences.addDriverName(shippingRecord.driverName);
        }
        if (etDriverPhone.getText() != null) {
            shippingRecord.driverPhone = etDriverPhone.getText().toString();
            LocalPreferences.addDriverPhone(shippingRecord.driverPhone);
        }
        if (etLicensePlate.getText() != null) {
            shippingRecord.licensePlate = etLicensePlate.getText().toString();
            LocalPreferences.addLicensePlate(shippingRecord.licensePlate);
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(FruitGlobalState.recShipping.customer)) {
                sb.append(String.format("\n%s is missing", "'Customer'"));
            }

            if (Strings.isEmptyOrWhitespace(FruitGlobalState.recShipping.driverName)) {
                sb.append(String.format("\n%s is missing", "'Driver name'"));
            }

            if (Strings.isEmptyOrWhitespace(FruitGlobalState.recShipping.licensePlate)) {
                sb.append(String.format("\n%s is missing", "'License plate'"));
            }
        }

        return sb.toString();
    }
}