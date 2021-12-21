package io.agritrack.ui.tools;

import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.util.List;
import java.util.concurrent.Callable;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.ui.login.LoginActivity;

public class CAENLoggerActivity extends AppCompatActivity {
    private TextView tvFWRevision, tvHWRevision, tvTimeBIN, tvDateTime, tvLastSampleValue, tvCurrentEPC, tvMemory, tvBattery;
    private EditText etInterval;
    private Button btnRead, btnReset, btnInit, btnSamplesCnt, btnControlReg;

    private ICAEN_API cmd;
    Callable<String> readFWRevisionTask = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return cmd.ReadFWRevision();
        }
    };
    Callable<String> readHWRevisionTask = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return cmd.ReadHWRevision();
        }
    };
    Callable<String> readCTRLRegisterTask = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return cmd.ReadControlRegister();
        }
    };
    Callable<String> readSTATUSRegisterTask = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return cmd.ReadStatusRegister();
        }
    };
    Callable<Short> readTimeBINTask = new Callable<Short>() {
        @Override
        public Short call() throws Exception {
            return cmd.ReadTimeBIN();
        }
    };
    Callable<Short> readSampleCNTTask = new Callable<Short>() {
        @Override
        public Short call() throws Exception {
            return cmd.ReadSamplesCount();
        }
    };
    Callable<Short> readIntervalTask = new Callable<Short>() {
        @Override
        public Short call() throws Exception {
            return cmd.ReadInterval();
        }
    };
    Callable<String> readInitTSTask = new Callable<String>() {
        @Override
        public String call() throws Exception {
            return cmd.ReadInitDatetime();
        }
    };
    Callable<Double> readLastTemperatureTask = new Callable<Double>() {
        @Override
        public Double call() throws Exception {
            return cmd.ReadLastSample();
        }
    };
    Callable<Reader.READER_ERR> resetTask = new Callable<Reader.READER_ERR>() {
        @Override
        public Reader.READER_ERR call() throws Exception {
            return cmd.Reset();
        }
    };
    Callable<Double> readInitTask = new Callable<Double>() {
        @Override
        public Double call() throws Exception {
            Double lastTemperature = Double.valueOf("-99");
            if (etInterval.getText() != null && !Strings.isEmptyOrWhitespace(etInterval.getText().toString())) {
                lastTemperature = cmd.Init(Short.valueOf(etInterval.getText().toString()));
            } else {
                lastTemperature = cmd.Init();
            }

            return lastTemperature;
        }
    };

    private short valuesCnt;
    private TaskRunner taskRunner;
    private String loggerEpc;

    protected final View.OnClickListener btnReadListener = v -> {
        loggerEpc = scanClosestEPC();

        if(Strings.isEmptyOrWhitespace(loggerEpc)) {
            CToast(getApplicationContext(), "No EPC found, plz scan again!!", Toast.LENGTH_LONG);
            return;
        }

        // detects which RFID module is installed on device.
        this.cmd.setFilterEPC(loggerEpc);

        tvCurrentEPC.setText(loggerEpc);
        btnReset.setEnabled(true);
        btnInit.setEnabled(true);

        taskRunner.executeAsync(readFWRevisionTask, (rs) -> {
            if (!Strings.isEmptyOrWhitespace(rs)) {
                tvFWRevision.setText(rs);
            } else {
                tvFWRevision.setText("ERR");
            }
        });

        taskRunner.executeAsync(readHWRevisionTask, (rs) -> {
            if (!Strings.isEmptyOrWhitespace(rs)) {
                tvHWRevision.setText(rs);
            } else {
                tvHWRevision.setText("ERR");
            }
        });

        taskRunner.executeAsync(readCTRLRegisterTask, (rs) -> {
            if (!Strings.isEmptyOrWhitespace(rs)) {
                btnControlReg.setText(rs);
            } else {
                btnControlReg.setText("ERR");
            }
        });

        taskRunner.executeAsync(readSTATUSRegisterTask, (rs) -> {
            if (!Strings.isEmptyOrWhitespace(rs)) {
                tvBattery.setText(rs.substring(rs.length() - 2));
                tvMemory.setText(rs.charAt(2) + "");
            } else {
                tvBattery.setText("ERR");
                tvMemory.setText("ERR");
            }
        });

        taskRunner.executeAsync(readTimeBINTask, (rs) -> {
            if (rs != null && rs >= 0) {
                tvTimeBIN.setText(String.valueOf(rs));
            } else {
                tvTimeBIN.setText("ERR");
            }
        });

        taskRunner.executeAsync(readSampleCNTTask, (rs) -> {
            if (rs != null && rs >= 0) {
                this.valuesCnt = rs;
                btnSamplesCnt.setOnClickListener(view -> {
                    if (this.valuesCnt > 0) {
                        try {
                            List<String[]> values = this.cmd.ReadSamples(this.valuesCnt);
                            displayMeasurementsDialog(values);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                btnSamplesCnt.setText(rs + " measurements.");
            } else {
                btnSamplesCnt.setText("ERR");
            }
        });

        taskRunner.executeAsync(readIntervalTask, (rs) -> {
            if (rs != null && rs >= 0) {
                etInterval.setText(String.valueOf(rs));
            } else {
                etInterval.setText("ERR");
            }
        });

        taskRunner.executeAsync(readLastTemperatureTask, (rs) -> {
            if (rs != null && rs >= 0) {
                tvLastSampleValue.setText(String.format("%.2f\u2103", rs));
            } else {
                tvLastSampleValue.setText("ERR");
            }
        });
    };

    protected final View.OnClickListener btnResetListener = v -> {
        if (this.cmd != null) {
            try {
                tvFWRevision.setText("");
                tvHWRevision.setText("");
                tvTimeBIN.setText("");
                tvDateTime.setText("");
                etInterval.setText("");
                tvLastSampleValue.setText("");
                tvCurrentEPC.setText("");
                btnControlReg.setText("");
                tvMemory.setText("");
                tvBattery.setText("");
                btnSamplesCnt.setText("");

                taskRunner.executeAsync(resetTask, (rs) -> {
                    if (!Reader.READER_ERR.MT_OK_ERR.equals(rs)) {
                        CToast(getApplicationContext(), "Reset Failed!\n", Toast.LENGTH_SHORT);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
        }
    };

    protected final View.OnClickListener btnInitListener = v -> {
        if (this.cmd != null) {
            try {
                taskRunner.executeAsync(readInitTask, (rs) -> {
                    if (rs==-99) {
                        tvLastSampleValue.setText("ERR");
                    } else {
                        tvLastSampleValue.setText(String.format("%.2f\u2103", rs));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caen_logger);

        taskRunner = new TaskRunner();

        // get  references of the controls
        assignCtrlVars();

        this.valuesCnt = 0;

        btnRead.setOnClickListener(btnReadListener);

        btnReset.setOnClickListener(btnResetListener);

        btnInit.setOnClickListener(btnInitListener);

        btnControlReg.setOnClickListener(view -> {
            if (this.cmd != null) {
                try {
                    CharSequence regCtrl = btnControlReg.getText();
                    String regCtrlStr = (regCtrl != null) ? regCtrl.toString() : null;
                    if (regCtrlStr != null && regCtrlStr.length() == 5 && regCtrlStr.charAt(0) == '1') {
                        this.cmd.LowSensitivity();
                    } else if (regCtrlStr != null && regCtrlStr.length() == 3 && regCtrlStr.charAt(0) == '1') {
                        this.cmd.HighSensitivity();
                    }

                    String controlReg = cmd.ReadControlRegister();
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

    private String scanClosestEPC() {
//        SingleShotScanner scanner = new SingleShotScanner();
//        scanner.setUhfReader(uhfReader);
//        scanner.setFilter(Filters.RFID_LOGGER);
//
//        try {
//            String epcStr = scanner.call();
//            if (!Strings.isEmptyOrWhitespace(epcStr)) {
//                return epcStr;
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return "300EFE2F94D01C02540BE8BB"; //null;
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
        etInterval = findViewById(R.id.etInterval);
        btnSamplesCnt = findViewById(R.id.btnSamplesCnt);
        tvLastSampleValue = findViewById(R.id.tvLastSampleValue);
        btnControlReg = findViewById(R.id.btnControlReg);
        tvMemory = findViewById(R.id.tvMemory);
        tvBattery = findViewById(R.id.tvBattery);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void displayMeasurementsDialog(List<String[]> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(CAENLoggerActivity.this);
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CAENLoggerActivity.this, R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%3d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cmd.CloseReader();
    }
}