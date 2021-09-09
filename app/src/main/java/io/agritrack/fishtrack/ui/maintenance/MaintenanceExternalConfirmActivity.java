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

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.state.GlobalState.recExternalRepair;

public class MaintenanceExternalConfirmActivity extends AppCompatActivity {
    private MobileDB db;
    private TextView tvSite, tvAssetBarcode, tvMaintenanceType, tvSupplier, tvMaintenanceManager,tvMaintenanceCost, tvNextDateMaintenance, tvUsername;
    private EditText etPasswordExtMaintenance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_confirm);

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceExternalSupplier);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceExternalSupplierActivity.class);
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

    private void updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // persist Transportation Record data to local DB.
        RepairTransaction tx = GlobalState.commitExternalRepair(db);
    }
}