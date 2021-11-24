package io.agritrack.fish.ui.maintenance;

import android.Manifest;
import android.app.ProgressDialog;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import static io.agritrack.fish.state.GlobalState.recInternalRepair;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class MaintenanceInternalConfirmActivity extends LocationAwareActivity {

    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvMaintenanceTeam, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordFishing;
    private final String dtFormat = "dd/MM/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);
    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_confirm);

        // activate GPS location update feature.
        super.findLocation();

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(MaintenanceInternalConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceInternalTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceInternalTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recInternalRepair.longitude = mLastLocation.getLongitude();
                    recInternalRepair.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(MaintenanceInternalConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
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
        tvMaintenanceTeam = findViewById(R.id.tvMaintenanceTeam);
        tvNextDateMaintenance = findViewById(R.id.tvNextDateMaintenance);
        tvUsername = findViewById(R.id.tvUsername);
        etPasswordFishing = findViewById(R.id.etPasswordFishing);
        ivSupport = findViewById(R.id.ivSupport);
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
}