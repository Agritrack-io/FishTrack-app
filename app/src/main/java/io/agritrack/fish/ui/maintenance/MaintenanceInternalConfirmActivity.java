package io.agritrack.fish.ui.maintenance;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.tx.RepairTransaction;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recInternalRepair;

public class MaintenanceInternalConfirmActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;

    private LocationManager locationManager;
    private TimeOutProgressDlg syncProgressDialog;
    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvMaintenanceTeam, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordFishing;
    private final String dtFormat = "dd/MM/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_confirm);

        // get  references of the controls
        assignCtrlVars();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        syncProgressDialog = new TimeOutProgressDlg(10000l, 500l, this) {
            @Override
            public void doTasks() {
                locationManager.removeUpdates(MaintenanceInternalConfirmActivity.this);

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
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceInternalTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceInternalTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(MaintenanceInternalConfirmActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MaintenanceInternalConfirmActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MaintenanceInternalConfirmActivity.this);
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });
    }

    private void assignCtrlVars() {
        tvSite = findViewById(R.id.tvSite);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        tvMaintenanceType = findViewById(R.id.tvMaintenanceType);
        tvMaintenanceTeam = findViewById(R.id.tvMaintenanceTeam);
        tvNextDateMaintenance = findViewById(R.id.tvNextDateMaintenance);

        tvUsername = findViewById(R.id.tvUsername);
        etPasswordFishing = findViewById(R.id.etPasswordFishing);
    }

    private void initControlsFromState() {
        tvSite.setText(!Strings.isEmptyOrWhitespace(recInternalRepair.site) ? recInternalRepair.site : "N/A");
        tvAssetBarcode.setText(!Strings.isEmptyOrWhitespace(recInternalRepair.assetBC) ? recInternalRepair.assetBC : "N/A");
        tvMaintenanceType.setText(!Strings.isEmptyOrWhitespace(recInternalRepair.maintenanceType) ? recInternalRepair.maintenanceType : "N/A");
        tvMaintenanceTeam.setText(String.valueOf(recInternalRepair.repairTeam)); //!Strings.isEmptyOrWhitespace(  ? recInternalRepair.repairTeam : "N/A"
        String string_date = recInternalRepair.nextDateMaintenance.toString();
        try {
            Date d = sdf.parse(string_date);
            tvNextDateMaintenance.setText(d.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvUsername.setText(LocalPreferences.getLoggedInUser("N/A"));
    }

        private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // persist Internal Repair Record data to local DB.
        RepairTransaction tx = GlobalState.commitInternalRepair(db);

            return true;
    }

    // GPS Location-Related functionality
    @Override
    public void onLocationChanged(@NonNull Location location) {
        recInternalRepair.longitude = location.getLongitude();
        recInternalRepair.latitude = location.getLatitude();
        locationManager.removeUpdates(this);
        toggleProgress(false, R.string.app_name);
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