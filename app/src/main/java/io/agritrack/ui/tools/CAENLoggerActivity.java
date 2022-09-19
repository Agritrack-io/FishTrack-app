package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdDisableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadCTRLReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadFWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadHWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadLastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSTATUSReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamplesCnt;
import static io.agritrack.caen.api.ICAEN_API.DefaultInterval;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.agritrack.R;
import io.agritrack.caen.api.CAENLoggerService;
import io.agritrack.caen.api.CAEN_CONSTANTS;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.common.Filters;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.login.LoginActivity;

public class CAENLoggerActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final CAENCommandsHandler mScanHandler = new CAENCommandsHandler(this);

    private TextView tvFWRevision, tvHWRevision, tvTimeBIN, tvDateTime, tvLastSampleValue, tvCurrentEPC, tvMemory, tvBattery;
    private EditText etInterval;
    private Button btnRead, btnReset, btnInit, btnSamplesCnt, btnControlReg, btnScanEPC, btnStopLogging;
    private ProgressBar progressBar;
    private final SimpleDateFormat dtParser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    private ICAEN_API cmd;
    private CAENLoggerService loggerSvc;

    private short valuesCnt;
    private String loggerEpc;



    protected final View.OnClickListener btnScanEPCListener = v -> {
        // Show ProgressBar and clear EPC text view.
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            tvCurrentEPC.setText("");
        });

        // -------------------------------------
        SingleShotScanner singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_LOGGER);
        singleShot_runnable.startReading();
        mScanHandler.post(singleShot_runnable);
    };

    protected final View.OnClickListener btnInitListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // -------------------------------------
            clearControls();
            // -------------------------------------

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                cmd.setFilterEPC(loggerEpc);

                // fetch sampling interval from editTextView.
                short samplingInterval = DefaultInterval;
                if (etInterval.getText() != null && !Strings.isEmptyOrWhitespace(etInterval.getText().toString())) {
                    samplingInterval = Short.valueOf(etInterval.getText().toString());
                }
                // set config params and start logging.
                this.loggerSvc.doEnableLogger(samplingInterval);
            }

            // Hide ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }).start();
    };

    protected final View.OnClickListener btnResetListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // -------------------------------------
            clearControls();
            // -------------------------------------

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                cmd.setFilterEPC(loggerEpc);

                this.loggerSvc.doResetLogger();
                //this.loggerSvc.Reset();
            }

            // Hide ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }).start();
    };

    protected final View.OnClickListener btnReadListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // -------------------------------------
            clearControls();
            // -------------------------------------

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                cmd.setFilterEPC(loggerEpc);

                this.loggerSvc.ReadFWRevision();
                this.loggerSvc.ReadHWRevision();
                this.loggerSvc.ReadTimeBIN();
                this.loggerSvc.ReadInitDatetime();
                this.loggerSvc.ReadInterval();
                this.loggerSvc.ReadControlRegister();
                this.loggerSvc.ReadInterval();
                this.loggerSvc.ReadStatusRegister();
                this.loggerSvc.ReadSamplesCount();
                this.loggerSvc.ReadLastSample();
            }

            // Hide ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }).start();
    };

    protected final View.OnClickListener btnStopLoggingListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

            // -------------------------------------
            clearControls();
            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                cmd.setFilterEPC(loggerEpc);

                this.loggerSvc.StopLogging();
                this.loggerSvc.ReadControlRegister();
            }
            // -------------------------------------
            // Hide ProgressBar
            runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
        }).start();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caen_logger);

        progressBar = findViewById(R.id.progressBar);

        // get  references of the controls
        assignCtrlVars();

        this.valuesCnt = 0;

        btnScanEPC.setOnClickListener(btnScanEPCListener);

        btnRead.setOnClickListener(btnReadListener);

        btnReset.setOnClickListener(btnResetListener);

        btnInit.setOnClickListener(btnInitListener);

        btnStopLogging.setOnClickListener(btnStopLoggingListener);

        btnControlReg.setOnClickListener(view -> {
            if (this.cmd != null) {
                try {
                    CharSequence regCtrl = btnControlReg.getText();
                    String regCtrlStr = (regCtrl != null) ? regCtrl.toString() : null;
                    if (regCtrlStr != null && regCtrlStr.length() == 5 && regCtrlStr.charAt(0) == '1') {
                        //this.cmd.LowSensitivity();
                    } else if (regCtrlStr != null && regCtrlStr.length() == 3 && regCtrlStr.charAt(0) == '1') {
                        this.cmd.HighSensitivity(); // resets logger at the same time.
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

    @Override
    protected void onStart() {
        super.onStart();
        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
        this.loggerSvc = new CAENLoggerService(this.cmd, this.mScanHandler);
    }

    private void assignCtrlVars() {
        tvCurrentEPC = findViewById(R.id.tvCurrentEPC);
        btnRead = findViewById(R.id.btnRead);
        btnReset = findViewById(R.id.btnReset);
        btnInit = findViewById(R.id.btnInit);
        btnScanEPC = findViewById(R.id.btnScanEPC);
        btnStopLogging = findViewById(R.id.btnStopLogging);
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
            // remove all callbacks and messages.
            mScanHandler.removeCallbacks(null);
            cmd.CloseReader();
            cmd = null;

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
            arrayAdapter.add(String.format("%04d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //if (cmd != null) {
        //    cmd.CloseReader();
        //}
        super.onDestroy();
    }

    //--------------------------------------------
//    private Message createMessage(int what, Object value) {
//        Message msg = new Message();
//        msg.what = what;
//        Bundle b = new Bundle();
//
//        if (value != null && value instanceof String)
//            b.putString("body", (String) value);
//        else if (value != null && value instanceof Short)
//            b.putShort("body", (short) value);
//        else if (value != null && value instanceof Reader.READER_ERR)
//            b.putString("body", ((Reader.READER_ERR) value).name());
//
//        msg.setData(b);
//
//        return msg;
//    }


    // ###################################################
    private class CAENCommandsHandler extends Handler {
        private final WeakReference<CAENLoggerActivity> mActivity;

        public CAENCommandsHandler(CAENLoggerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String value = null;
            switch (msg.what) {
                case 1:
                    // Hide ProgressBar
                    runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));

                    loggerEpc = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                            // since EPC was found enable the Operation Buttons
                            runOnUiThread(() -> {
                                btnReset.setEnabled(true);
                                btnInit.setEnabled(true);
                                btnStopLogging.setEnabled(true);
                                // -------------------------------------
                                tvCurrentEPC.setText(loggerEpc);
                            });

                            // sets Logger filter to closest EPC.
                            cmd.setFilterEPC(loggerEpc);
                        } else if (!IsDemo) {
                            CToast(getApplicationContext(), "No Logger EPC found, plz scan again!!", Toast.LENGTH_LONG);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case ReadFWRevision:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvFWRevision.setText(value);
                    } else {
                        tvFWRevision.setText("ERR");
                    }
                    break;
                case ReadHWRevision:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvHWRevision.setText(value);
                    } else {
                        tvHWRevision.setText("ERR");
                    }
                    break;
                case ReadCTRLReg:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        btnControlReg.setText(value);
                    } else {
                        btnControlReg.setText("ERR");
                    }
                    break;
                case ReadSTATUSReg:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value) && !"N/A".equalsIgnoreCase(value)) {
                        tvBattery.setText(value.substring(value.length() - 2));
                        tvMemory.setText(value.charAt(2) + "");
                    } else {
                        tvBattery.setText("ERR");
                        tvMemory.setText("ERR");
                    }
                    break;
                case CAEN_CONSTANTS.ReadTimeBIN:
                    short timeBin = msg.getData().getShort("body");
                    tvTimeBIN.setText(String.valueOf(timeBin));
                    break;
                case ReadSamplesCnt:
                    valuesCnt = msg.getData().getShort("body");
                    if (valuesCnt >= 0) {
                        btnSamplesCnt.setText(String.format("%s measurements.", valuesCnt));
                        btnSamplesCnt.setOnClickListener(view -> {
                            if (valuesCnt > 0) {
                                try {
                                    int intervalVal = DefaultInterval;
                                    long initTSmSec = System.currentTimeMillis();

                                    // get existing init timestamp value, if set...
                                    if (tvDateTime.getText() != null) {
                                        try {
                                            initTSmSec = dtParser.parse(tvDateTime.getText().toString()).getTime();
                                        } catch (Exception e) {
                                        }
                                    }

                                    // get existing interval value, if set...
                                    if (etInterval.getText() != null) {
                                        try {
                                            intervalVal = Integer.valueOf(etInterval.getText().toString());
                                        } catch (Exception e) {
                                        }
                                    }

                                    List<String[]> values = cmd.ReadSamples(valuesCnt, intervalVal, initTSmSec);
                                    displayMeasurementsDialog(values);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        btnSamplesCnt.setText("ERR");
                    }
                    break;
                case ReadInterval:
                    short interval = msg.getData().getShort("body");
                    etInterval.setText(String.valueOf(interval));
                    break;
                case ReadLastSample:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvLastSampleValue.setText(value);
                    } else {
                        tvLastSampleValue.setText("ERR");
                    }
                    break;
                case ReadInitTimeStamp:
                    value = msg.getData().getString("body");
                    tvDateTime.setText(value);
                    break;
                case CmdRESET:
                    String result = msg.getData().getString("body");
                    if (!"MT_OK_ERR".equalsIgnoreCase(result)) {
                        CToast(getApplicationContext(), "Reset Failed!\n", Toast.LENGTH_SHORT);
                    }
                    break;
                case CmdDisableLogging:
                    String resDisableLogging = msg.getData().getString("body");
                    if (!"MT_OK_ERR".equalsIgnoreCase(resDisableLogging)) {
                        CToast(getApplicationContext(), "Failed to stop Logging!\n", Toast.LENGTH_SHORT);
                    }
                    break;

                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

    private void clearControls() {
        runOnUiThread(() -> {
            tvFWRevision.setText("");
            tvHWRevision.setText("");
            tvTimeBIN.setText("");
            tvDateTime.setText("");
            etInterval.setText("");
            tvLastSampleValue.setText("");
            btnControlReg.setText("");
            tvMemory.setText("");
            tvBattery.setText("");
            btnSamplesCnt.setText("");
        });
    }
}