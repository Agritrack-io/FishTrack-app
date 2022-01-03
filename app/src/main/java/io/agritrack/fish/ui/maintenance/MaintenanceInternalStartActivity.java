package io.agritrack.fish.ui.maintenance;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.FishTrackUtils.detectAssetType;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.RepairRecord;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class MaintenanceInternalStartActivity extends TriggerKeyAwareActivity implements ToggleGroup.OnCheckedChangeListener {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private final String dtFormat = "dd/MM/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);

    private final Calendar calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener withdrawalDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateEstWithdrawalDate();
    };
    private Button btnScanAsset;
    private TextView tvAssetBarcode, tvAssetType;
    private ToggleGroup tgInMtRepairTypes;
    private EditText etIMtNextMaintenance, etIMtEstWithdrawal;
    DatePickerDialog.OnDateSetListener nextDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateNextMaintenanceDate();
    };
    private String selectedOperation;
    private final String activeFilter = null;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_internal_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceInternalStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        setUpNextMaintenanceDate();
        setUpEstWithdrawalDate();

        // =================================
        // RFID scanning functionality
        btnScanAsset.setOnClickListener(this::onClick);
        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(MaintenanceInternalStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvAssetType = findViewById(R.id.tvAssetType);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        etIMtNextMaintenance = findViewById(R.id.etIMtNextMaintenance);
        etIMtEstWithdrawal = findViewById(R.id.etIMtEstWithdrawal);
        btnScanAsset = findViewById(R.id.btnScanAsset);
        tgInMtRepairTypes = findViewById(R.id.tgInMtRepairTypes);
        tgInMtRepairTypes.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToMaintenanceMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
        });

        ImageView ivNext = findViewById(R.id.ivToMaintenanceTeam);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
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
        RepairRecord indoorsRepairRecord = GlobalState.initInternalRepairRecord();

        indoorsRepairRecord.site = LocalPreferences.getCurrentSiteName();

        if (tvAssetBarcode.getText() != null) {
            indoorsRepairRecord.assetBC = tvAssetBarcode.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(selectedOperation)) {
            indoorsRepairRecord.maintenanceType = selectedOperation;
        }

        Editable txtNextMaintenance = etIMtNextMaintenance.getText();
        if (txtNextMaintenance != null && !Strings.isEmptyOrWhitespace(txtNextMaintenance.toString())) {
            String string_date = txtNextMaintenance.toString();
            try {
                Date d = sdf.parse(string_date);
                indoorsRepairRecord.nextDateMaintenance = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Editable txtEstWithdrawal = etIMtEstWithdrawal.getText();
        if (txtEstWithdrawal != null && !Strings.isEmptyOrWhitespace(txtEstWithdrawal.toString())) {
            String string_date = txtEstWithdrawal.toString();
            try {
                Date d = sdf.parse(string_date);
                indoorsRepairRecord.estimatedDateWithdrawal = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return indoorsRepairRecord;
    }

    private void setUpNextMaintenanceDate() {
        etIMtNextMaintenance.setOnClickListener(view -> new DatePickerDialog(MaintenanceInternalStartActivity.this, nextDate, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 10,
                calendar.get(Calendar.DAY_OF_MONTH) + 25).show());
    }

    private void setUpEstWithdrawalDate() {
        etIMtEstWithdrawal.setOnClickListener(view -> new DatePickerDialog(MaintenanceInternalStartActivity.this, withdrawalDate, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateNextMaintenanceDate() {
        etIMtNextMaintenance.setText(sdf.format(calendar.getTime()));
    }

    private void updateEstWithdrawalDate() {
        etIMtEstWithdrawal.setText(sdf.format(calendar.getTime()));
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.assetBC)) {
                sb.append(String.format("\n%s is missing", "'Scan barcode'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.maintenanceType)) {
                sb.append(String.format("\n%s is missing", "'Maintenance type'"));
            }

            if (GlobalState.recInternalRepair.nextDateMaintenance == null) {
                sb.append(String.format("\n%s is missing", "'Next maintenance date'"));
            }
        }

        return sb.toString();
    }

    private void initControlsFromState() {
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
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_BIN);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<MaintenanceInternalStartActivity> mActivity;

        public ScanHandler(MaintenanceInternalStartActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            tvAssetBarcode.setText(epcStr);
                            tvAssetType.setText(detectAssetType(epcStr));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        CToast(getApplicationContext(), render("No Asset was scanned!!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}