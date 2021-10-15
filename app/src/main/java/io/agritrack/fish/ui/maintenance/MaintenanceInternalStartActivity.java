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
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.RepairRecord;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.common.FishTrackUtils.detectAssetType;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class MaintenanceInternalStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final String dtFormat = "dd/MM/yyyy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);

    private Button btnScanAsset;
    private TextView tvAssetBarcode, tvAssetType;
    private ToggleGroup tgInMtRepairTypes;
    private EditText etIMtNextMaintenance, etIMtEstWithdrawal;
    private final Calendar calendar = Calendar.getInstance();

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String selectedOperation;
    private String activeFilter = null;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    DatePickerDialog.OnDateSetListener nextDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateNextMaintenanceDate();
    };

    DatePickerDialog.OnDateSetListener withdrawalDate = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateEstWithdrawalDate();
    };


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
        btnScanAsset.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvAssetBarcode.setText(epcStr);
                            tvAssetType.setText(detectAssetType(epcStr));
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

    private void setUpNextMaintenanceDate(){
        etIMtNextMaintenance.setOnClickListener(view -> new DatePickerDialog(MaintenanceInternalStartActivity.this, nextDate, calendar
                .get(Calendar.YEAR), calendar.get(Calendar.MONTH)+10,
                calendar.get(Calendar.DAY_OF_MONTH)+25).show());
    }

    private void setUpEstWithdrawalDate(){
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

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.assetBC)){
            sb.append(String.format("\n%s is missing", "'Scan barcode'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recInternalRepair.maintenanceType)){
            sb.append(String.format("\n%s is missing", "'Maintenance type'"));
        }

        if(GlobalState.recInternalRepair.nextDateMaintenance == null){
            sb.append(String.format("\n%s is missing", "'Next maintenance date'"));
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