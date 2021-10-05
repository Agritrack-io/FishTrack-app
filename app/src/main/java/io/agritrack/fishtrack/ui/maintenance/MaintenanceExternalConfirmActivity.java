package io.agritrack.fishtrack.ui.maintenance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;
import io.agritrack.fishtrack.dialog.TimeOutProgressDlg;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.state.GlobalState.recExternalRepair;
import static io.agritrack.fishtrack.state.GlobalState.recFishing;

public class MaintenanceExternalConfirmActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;

    private LocationManager locationManager;
    private TimeOutProgressDlg syncProgressDialog;
    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvSupplier, tvMaintenanceManager,tvMaintenanceCost, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordExtMaintenance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_confirm);

        // get  references of the controls
        assignCtrlVars();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        
        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        syncProgressDialog = new TimeOutProgressDlg(10000l, 500l, this) {
            @Override
            protected void doTasks() {
                locationManager.removeUpdates(MaintenanceExternalConfirmActivity.this);

                // Update state and proceed to next
                Boolean proceed = updateState();
                toggleProgress(false, R.string.app_name);

                if (proceed) {
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(i);
                }
            }
        };
        syncProgressDialog.setMessage(R.string.acquire_coordinates);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceExternalSupplier);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceExternalSupplierActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(MaintenanceExternalConfirmActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MaintenanceExternalConfirmActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MaintenanceExternalConfirmActivity.this);
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });
    }

    private void assignCtrlVars() {
        tvSite = findViewById(R.id.tvSite);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        tvMaintenanceType = findViewById(R.id.tvMaintenanceType);
        tvSupplier = findViewById(R.id.tvSupplier);
        tvMaintenanceManager = findViewById(R.id.tvMaintenanceManager);
        tvMaintenanceCost = findViewById(R.id.tvMaintenanceCost);
        tvNextDateMaintenance = findViewById(R.id.tvNextDateMaintenance);

        tvUsername = findViewById(R.id.tvUsername);
        etPasswordExtMaintenance = findViewById(R.id.etPasswordExtMaintenance);
    }

    private void initControlsFromState() {
        tvSite.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.site) ? recExternalRepair.site : "N/A");
        tvAssetBarcode.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.assetBC) ? recExternalRepair.assetBC : "N/A");
        tvMaintenanceType.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.maintenanceType) ? recExternalRepair.maintenanceType : "N/A");
        tvSupplier.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.supplier) ? recExternalRepair.supplier : "N/A");
        tvMaintenanceManager.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.manager) ? recExternalRepair.manager : "N/A");
        tvMaintenanceCost.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.cost) ? recExternalRepair.cost : "N/A");
        tvNextDateMaintenance.setText(!Strings.isEmptyOrWhitespace(recExternalRepair.teamSize) ? recExternalRepair.teamSize : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("N/A"));
    }

    private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // persist Transportation Record data to local DB.
        RepairTransaction tx = GlobalState.commitExternalRepair(db);

        return true;
    }

    // GPS Location-Related functionality
    @Override
    public void onLocationChanged(@NonNull Location location) {
        recExternalRepair.longitude = location.getLongitude();
        recExternalRepair.latitude = location.getLatitude();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            runOnUiThread(() -> {
                syncProgressDialog.show();
            });
        } else {
            runOnUiThread(() -> {
                syncProgressDialog.hide();
            });
        }
    }
}