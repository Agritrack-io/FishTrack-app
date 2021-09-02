package io.agritrack.fishtrack.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.Arrays;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.RepairRecord;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class MaintenanceExternalStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private Spinner spAssetType;
    private Button btnScanAsset;
    private TextView tvAssetBarcode;
    private ToggleGroup tgOutMtRepairTypes;
    private EditText etOMtNextMaintenance, etOMtEstWithdrawal;

    private UhfReader uhfReader;
    private ScanInventoryThread assetScanningThread = new ScanInventoryThread();
    private boolean scanning = false;

    private String selectedOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // load all Asset Types and fill in the spAssetType Spinner.
        AssetType[] assetTypes = AssetType.values();
        if (assetTypes != null) {
            String[] assetTypeArray = Arrays.stream(assetTypes).map(x -> x.name()).toArray(String[]::new);
            ArrayAdapter<String> atAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, assetTypeArray);
            atAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spAssetType.setAdapter(atAdapter);
        }

        // initialize scanning threads
        prepareScanAssetButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();
        configFooter();
    }

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        etOMtNextMaintenance = findViewById(R.id.etOMtNextMaintenance);
        etOMtEstWithdrawal = findViewById(R.id.etOMtEstWithdrawal);

        btnScanAsset = findViewById(R.id.btnScanAsset);

        tgOutMtRepairTypes = findViewById(R.id.tgOutMtRepairTypes);
        tgOutMtRepairTypes.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToMaintenanceSupplier);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), MaintenanceExternalSupplierActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbCleaning) {
            selectedOperation = Constants.ftCleaning;
        } else if (checkedId == R.id.tbRepair) {
            selectedOperation = Constants.ftRepair;
        }
    }

    private RepairRecord updateState() {
        RepairRecord externalRepairRecord = GlobalState.initExternalRepairTx();

        externalRepairRecord.site = LocalPreferences.getCurrentSiteName();

        if (spAssetType.getSelectedItem() != null) {
            externalRepairRecord.assetType = AssetType.valueOf(spAssetType.getSelectedItem().toString());
        }
        externalRepairRecord.assetTypePos = spAssetType.getSelectedItemPosition();

        if (tvAssetBarcode.getText() != null) {
            externalRepairRecord.assetBC = tvAssetBarcode.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(selectedOperation)) {
            externalRepairRecord.maintenanceType = selectedOperation;
        }
        return externalRepairRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.assetBC)){
            sb.append(String.format("\n%s is missing", "'Scan barcode'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.maintenanceType)){
            sb.append(String.format("\n%s is missing", "'Maintenance type'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.maintenanceType)){
            sb.append(String.format("\n%s is missing", "'Next maintenance date'"));
        }

        return sb.toString();
    }


    private void initControlsFromState() {
        if (GlobalState.recExternalRepair.assetTypePos > -1) {
            spAssetType.setSelection(GlobalState.recExternalRepair.assetTypePos);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.assetBC)) {
            tvAssetBarcode.setText(GlobalState.recExternalRepair.assetBC);
        }

        if (Constants.ftCleaning.equalsIgnoreCase(GlobalState.recExternalRepair.maintenanceType)) {
            tgOutMtRepairTypes.check(R.id.tbCleaning);
        } else if (Constants.ftRepair.equalsIgnoreCase(GlobalState.recExternalRepair.maintenanceType)) {
            tgOutMtRepairTypes.check(R.id.tbRepair);
        }
    }

    private void prepareScanAssetButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        if(uhfReader!=null)
        uhfReader.setOutputPower(33);

        btnScanAsset.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (assetScanningThread.getState() == Thread.State.TERMINATED) {
                assetScanningThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            assetScanningThread.setScanInProgress(scanning);
            assetScanningThread.setUhfReader(uhfReader);
            assetScanningThread.setRfidTag(tvAssetBarcode);

            if (scanning) {
                btnScanAsset.setText(R.string.stop_scan);
                if (assetScanningThread.getState() == Thread.State.NEW) {
                    assetScanningThread.start();
                }
            } else {
                btnScanAsset.setText(R.string.scan_assets);
                try {
                    assetScanningThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}