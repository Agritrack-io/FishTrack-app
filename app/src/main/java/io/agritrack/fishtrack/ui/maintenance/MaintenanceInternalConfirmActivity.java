package io.agritrack.fishtrack.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;
import static io.agritrack.fishtrack.state.GlobalState.recInternalRepair;

public class MaintenanceInternalConfirmActivity extends AppCompatActivity {
    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvMaintenanceTeam, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordFishing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_confirm);

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceInternalTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceInternalTeamActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
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
        tvMaintenanceTeam.setText(!Strings.isEmptyOrWhitespace(recInternalRepair.teamSize) ? recInternalRepair.teamSize : "N/A");
        tvNextDateMaintenance.setText(!Strings.isEmptyOrWhitespace(recInternalRepair.teamSize) ? recInternalRepair.teamSize : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("N/A"));
    }

    private void updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getContext());

        // persist Internal Repair Record data to local DB.
        RepairTransaction tx = GlobalState.commitInternalRepair(db);
    }
}