package io.agritrack.fishtrack.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.rfid.SingleShotScanner;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.RepairRecord;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class MaintenanceInternalStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private Spinner spAssetType;
    private Button btnScanAsset;
    private TextView tvAssetBarcode;
    private ToggleGroup tgInMtRepairTypes;
    private EditText etIMtNextMaintenance, etIMtEstWithdrawal;

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String selectedOperation;
    private String activeFilter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalStart);
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

        // =================================
        // RFID scanning functionality
        btnScanAsset.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            scanner.setUhfReader(UhfReader.getInstance());

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvAssetBarcode.setText(epcStr);
                        }
                    });
                    //tvCageName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });
        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        etIMtNextMaintenance = findViewById(R.id.etIMtNextMaintenance);
        etIMtEstWithdrawal = findViewById(R.id.etIMtEstWithdrawal);

        btnScanAsset = findViewById(R.id.btnScanAsset);

        tgInMtRepairTypes = findViewById(R.id.tgInMtRepairTypes);
        tgInMtRepairTypes.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToMaintenanceTeam);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), MaintenanceInternalTeamActivity.class);
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
        RepairRecord indoorsRepairRecord = GlobalState.initInternalRepairTx();

        indoorsRepairRecord.site = LocalPreferences.getCurrentSiteName();

        if (spAssetType.getSelectedItem() != null) {
            indoorsRepairRecord.assetType = AssetType.valueOf(spAssetType.getSelectedItem().toString());
        }
        indoorsRepairRecord.assetTypePos = spAssetType.getSelectedItemPosition();

        if (tvAssetBarcode.getText() != null) {
            indoorsRepairRecord.assetBC = tvAssetBarcode.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(selectedOperation)) {
            indoorsRepairRecord.maintenanceType = selectedOperation;
        }
        return indoorsRepairRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.assetBC)){
            sb.append(String.format("\n%s is missing", "'Scan barcode'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.maintenanceType)){
            sb.append(String.format("\n%s is missing", "'Maintenance type'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.maintenanceType)){
            sb.append(String.format("\n%s is missing", "'Next maintenance date'"));
        }

        return sb.toString();
    }

    private void initControlsFromState() {
        if (GlobalState.recInternalRepair.assetTypePos > -1) {
            spAssetType.setSelection(GlobalState.recInternalRepair.assetTypePos);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.assetBC)) {
            tvAssetBarcode.setText(GlobalState.recInternalRepair.assetBC);
        }

        if (Constants.ftCleaning.equalsIgnoreCase(GlobalState.recInternalRepair.maintenanceType)) {
            tgInMtRepairTypes.check(R.id.tbCleaning);
        } else if (Constants.ftRepair.equalsIgnoreCase(GlobalState.recInternalRepair.maintenanceType)) {
            tgInMtRepairTypes.check(R.id.tbRepair);
        }
    }

    @Override
    protected void onDestroy() {
        if (executor != null)
            executor.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (executor != null)
            executor.shutdown();
    }
}