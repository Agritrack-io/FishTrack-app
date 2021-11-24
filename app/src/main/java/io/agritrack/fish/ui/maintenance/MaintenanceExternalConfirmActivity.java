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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.tx.RepairTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.transport.TransportSupervisorConfirmActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recExternalRepair;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class MaintenanceExternalConfirmActivity extends LocationAwareActivity {

    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvSupplier, tvMaintenanceManager,tvMaintenanceCost, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordExtMaintenance;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_confirm);

        // activate GPS location update feature.
        super.findLocation();

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(MaintenanceExternalConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceExternalSupplier);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceExternalSupplierActivity.class);
            startActivity(i);
        });

        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recExternalRepair.longitude = mLastLocation.getLongitude();
                    recExternalRepair.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(MaintenanceExternalConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // stop GPS location updates.
                    stopListener();

                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                    startActivity(i);
                }
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
        ivSupport = findViewById(R.id.ivSupport);
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
}