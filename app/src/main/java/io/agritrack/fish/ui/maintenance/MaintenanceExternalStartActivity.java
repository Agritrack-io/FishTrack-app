package io.agritrack.fish.ui.maintenance;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.RepairRecord;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.FishTrackUtils.detectAssetType;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class MaintenanceExternalStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final String dtFormat = "dd/MM/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);
    private final Calendar calendar = Calendar.getInstance();
    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    DatePickerDialog.OnDateSetListener withdrawalDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateEstWithdrawalDate();
    };
    private Button btnScanAsset;
    private TextView tvAssetBarcode, tvAssetType;
    private ToggleGroup tgOutMtRepairTypes;
    private EditText etOMtNextMaintenance, etOMtEstWithdrawal;
    DatePickerDialog.OnDateSetListener nextDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateNextMaintenanceDate();
    };
    private String selectedOperation;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        setUpNextMaintenanceDate();
        setUpEstWithdrawalDate();

        // RFID scanning functionality
        btnScanAsset.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvAssetBarcode.setText(epcStr);
                            tvAssetType.setText(detectAssetType(epcStr));
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });
        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(MaintenanceExternalStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvAssetType = findViewById(R.id.tvAssetType);
        tvAssetBarcode = findViewById(R.id.tvAssetBarcode);
        etOMtNextMaintenance = findViewById(R.id.etOMtNextMaintenance);
        etOMtEstWithdrawal = findViewById(R.id.etOMtEstWithdrawal);
        btnScanAsset = findViewById(R.id.btnScanAsset);
        tgOutMtRepairTypes = findViewById(R.id.tgOutMtRepairTypes);
        tgOutMtRepairTypes.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToMaintenanceMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceMenuActivity.class);
            startActivity(i);
        });

        ImageView ivNext = findViewById(R.id.ivToMaintenanceSupplier);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
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
        RepairRecord externalRepairRecord = GlobalState.initExternalRepairRecord();

        externalRepairRecord.site = LocalPreferences.getCurrentSiteName();

        if (tvAssetBarcode.getText() != null) {
            externalRepairRecord.assetBC = tvAssetBarcode.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(selectedOperation)) {
            externalRepairRecord.maintenanceType = selectedOperation;
        }

        Editable txtNextMaintenance = etOMtNextMaintenance.getText();
        if (txtNextMaintenance != null && !Strings.isEmptyOrWhitespace(txtNextMaintenance.toString())) {
            String string_date = txtNextMaintenance.toString();
            try {
                Date d = sdf.parse(string_date);
                externalRepairRecord.nextDateMaintenance = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Editable txtEstWithdrawal = etOMtEstWithdrawal.getText();
        if (txtEstWithdrawal != null && !Strings.isEmptyOrWhitespace(txtEstWithdrawal.toString())) {
            String string_date = txtEstWithdrawal.toString();
            try {
                Date d = sdf.parse(string_date);
                externalRepairRecord.estimatedDateWithdrawal = d.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return externalRepairRecord;
    }

    private void setUpNextMaintenanceDate() {
        etOMtNextMaintenance.setOnClickListener(view -> new DatePickerDialog(MaintenanceExternalStartActivity.this, nextDate, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void setUpEstWithdrawalDate() {
        etOMtEstWithdrawal.setOnClickListener(view -> new DatePickerDialog(MaintenanceExternalStartActivity.this, withdrawalDate, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateNextMaintenanceDate() {
        etOMtNextMaintenance.setText(sdf.format(calendar.getTime()));
    }

    private void updateEstWithdrawalDate() {
        etOMtEstWithdrawal.setText(sdf.format(calendar.getTime()));
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.assetBC)) {
                sb.append(String.format("\n%s is missing", "'Scan barcode'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.maintenanceType)) {
                sb.append(String.format("\n%s is missing", "'Maintenance type'"));
            }

            if (GlobalState.recExternalRepair.nextDateMaintenance == null) {
                sb.append(String.format("\n%s is missing", "'Next maintenance date'"));
            }
        }
        return sb.toString();
    }


    private void initControlsFromState() {
        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.assetBC)) {
            tvAssetBarcode.setText(GlobalState.recExternalRepair.assetBC);
        }

        if (Constants.ftCleaning.equalsIgnoreCase(GlobalState.recExternalRepair.maintenanceType)) {
            tgOutMtRepairTypes.check(R.id.tbCleaning);
        } else if (Constants.ftRepair.equalsIgnoreCase(GlobalState.recExternalRepair.maintenanceType)) {
            tgOutMtRepairTypes.check(R.id.tbRepair);
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