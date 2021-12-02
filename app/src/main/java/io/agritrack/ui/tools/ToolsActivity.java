package io.agritrack.ui.tools;

import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.R;
import io.agritrack.caen.api.CAENCommander;
import io.agritrack.common.Filters;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.rfid.SingleShotScanner;

public class ToolsActivity extends AppCompatActivity {

    private UhfReader uhfReader;

    private TextView tvFWRevision, tvHWRevision, tvTimeBIN, tvDateTime, tvInterval, tvLastSampleValue, tvCurrentEPC, tvMemory, tvBattery;
    private Button btnRead, btnReset, btnInit, btnSamplesCnt, btnControlReg;

    private CAENCommander cmd;
    private short valuesCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        // get  references of the controls
        assignCtrlVars();

        this.valuesCnt = 0;

        uhfReader = UhfReader.getInstance();
        uhfReader.setWorkArea(3);
        uhfReader.setOutputPower(24);

        btnRead.setOnClickListener(view -> {
            scanClosestEPC();
        });

        btnReset.setOnClickListener(view -> {

            if (this.cmd != null) {
                try {
                    tvFWRevision.setText("");
                    tvHWRevision.setText("");
                    tvTimeBIN.setText("");
                    tvDateTime.setText("");
                    tvInterval.setText("");
                    tvLastSampleValue.setText("");
                    tvCurrentEPC.setText("");
                    btnControlReg.setText("");
                    tvMemory.setText("");
                    tvBattery.setText("");
                    btnSamplesCnt.setText("");

                    this.cmd.RESET();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
            }
        });

        btnInit.setOnClickListener(view -> {

            if (this.cmd != null) {
                try {
                    this.cmd.INIT();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
            }
        });

        btnControlReg.setOnClickListener(view -> {
            if (this.cmd != null) {
                try {
                    this.cmd.HighSensitivity();
                    String controlReg = cmd.READ_CONTROL_REGISTER();
                    btnControlReg.setText(controlReg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
            }
        });

        // create Footer
        configFooter();
    }

    private void scanClosestEPC() {
        SingleShotScanner scanner = new SingleShotScanner();
        scanner.setUhfReader(uhfReader);
        scanner.setFilter(Filters.RFID_LOGGER);
        scanner.trimEPC(Boolean.FALSE);

        try {
            String epcStr = scanner.call();
            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                tvCurrentEPC.setText(epcStr);
                btnReset.setEnabled(true);
                btnInit.setEnabled(true);
                ReadLogger(epcStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ReadLogger(String selectedEPC) {
        try {
            this.cmd = new CAENCommander(uhfReader, selectedEPC);
            String revFW = cmd.READ_FW_REVISION();
            tvFWRevision.setText(revFW);

            String revHW = cmd.READ_HW_REVISION();
            tvHWRevision.setText(revHW);

            String controlReg = cmd.READ_CONTROL_REGISTER();
            btnControlReg.setText(controlReg);

            String statusReg = cmd.READ_STATUS_REGISTER();
            tvBattery.setText(statusReg.substring(statusReg.length() - 2));
            tvMemory.setText(statusReg.charAt(2) + "");

            short timeBin = cmd.READ_TIME_BIN();
            tvTimeBIN.setText("Bin " + timeBin);

            short cnt = cmd.READ_SAMPLES_COUNT();
            this.valuesCnt = cnt;
            btnSamplesCnt.setOnClickListener(view -> {
                if (this.valuesCnt > 0) {
                    try {
                        List<String[]> values = this.cmd.READ_SAMPLES(this.valuesCnt);
                        displayMeasurementsDialog(values);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            btnSamplesCnt.setText(cnt + " measurements.");

            short interval = cmd.READ_INTERVAL();
            tvInterval.setText(interval + " seconds.");

            String epoch = cmd.READ_INIT_DATETIME();
            tvDateTime.setText(epoch);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignCtrlVars() {
        tvCurrentEPC = findViewById(R.id.tvCurrentEPC);
        btnRead = findViewById(R.id.btnRead);
        btnReset = findViewById(R.id.btnReset);
        btnInit = findViewById(R.id.btnInit);
        tvFWRevision = findViewById(R.id.tvFWRevision);
        tvHWRevision = findViewById(R.id.tvHWRevision);
        tvTimeBIN = findViewById(R.id.tvTimeBIN);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvInterval = findViewById(R.id.tvInterval);
        btnSamplesCnt = findViewById(R.id.btnSamplesCnt);
        tvLastSampleValue = findViewById(R.id.tvLastSampleValue);
        btnControlReg = findViewById(R.id.btnControlReg);
        tvMemory = findViewById(R.id.tvMemory);
        tvBattery = findViewById(R.id.tvBattery);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }


    private void displayMeasurementsDialog(List<String[]> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ToolsActivity.this);
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ToolsActivity.this, R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%3d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }
}