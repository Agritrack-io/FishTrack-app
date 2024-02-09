package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdDisableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdEnableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadCTRLReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadFWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadHWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadLastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSTATUSReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamples;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadTimeBIN;
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
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;
import com.kusu.loadingbutton.LoadingButton;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.agritrack.kefalonia.R;
import io.agritrack.caen.api.CAENLoggerService;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.common.Filters;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.tools.caen.ILoggerDialog;
import io.agritrack.ui.tools.caen.InitLoggerDialogDecorator;
import io.agritrack.ui.tools.caen.LoggerDialogFragment;
import io.agritrack.ui.tools.caen.ReadLoggerDialogDecorator;

public class CAENLoggerActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final CAENCommandsHandler mScanHandler = new CAENCommandsHandler(this);

    private TextView tvFWRevision, tvHWRevision, tvTimeBIN, tvDateTime, tvLastSampleValue, tvCurrentEPC, tvMemory, tvBattery;
    private EditText etInterval;
    private ProgressBar progressBar;
    private Button btnSamplesCnt, btnControlReg, btnScanEPC;
    private LoadingButton btnStopLogging, btnReset, btnInit, btnRead, btnDialog;
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
            runOnUiThread(() -> btnInit.showLoading());

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
            runOnUiThread(() -> btnInit.hideLoading());
        }).start();
    };

    protected final View.OnClickListener btnResetListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> btnReset.showLoading());

            // -------------------------------------
            clearControls();

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                cmd.setFilterEPC(loggerEpc);
                loggerSvc.doResetLogger();
            }

            // -------------------------------------
            // Hide ProgressBar
            runOnUiThread(() -> btnReset.hideLoading());
        }).start();
    };

    protected final View.OnClickListener btnReadListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> btnRead.showLoading());

            // -------------------------------------
            clearControls();

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                loggerSvc.setEPCFilter(loggerEpc);
                loggerSvc.doReadFullLoggerState();
            }

            // -------------------------------------
            // Hide ProgressBar
            runOnUiThread(() -> btnRead.hideLoading());
        }).start();
    };

    protected final View.OnClickListener btnStopLoggingListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressBar
            runOnUiThread(() -> btnStopLogging.showLoading());

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                loggerSvc.setEPCFilter(loggerEpc);
                this.loggerSvc.doStopLogging();
            }

            // -------------------------------------
            // Hide ProgressBar
            runOnUiThread(() -> btnStopLogging.hideLoading());
        }).start();
    };

    protected final View.OnClickListener btnDialogListener = v -> {
        new Thread(() -> {
            // -------------------------------------
            // Show ProgressButton
            runOnUiThread(() -> btnDialog.showLoading());

            // -------------------------------------
            if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                FragmentManager fm = getSupportFragmentManager();
                ILoggerDialog loggerDlg = LoggerDialogFragment.newInstance(loggerEpc, null);
                ReadLoggerDialogDecorator readLoggerDecorator = new ReadLoggerDialogDecorator(loggerDlg);
                readLoggerDecorator.show(fm);
//                InitLoggerDialogDecorator initLoggerDecorator = new InitLoggerDialogDecorator(loggerDlg);
//                initLoggerDecorator.show(fm);
            }
            // -------------------------------------

            // Hide ProgressButton
            runOnUiThread(() -> btnDialog.hideLoading());
        }).start();
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caen_logger);

        // assign control references to respective global member variables.
        assignCtrlVars();

        this.valuesCnt = 0;

        btnScanEPC.setOnClickListener(btnScanEPCListener);

        btnRead.setOnClickListener(btnReadListener);

        btnReset.setOnClickListener(btnResetListener);

        btnInit.setOnClickListener(btnInitListener);

        btnStopLogging.setOnClickListener(btnStopLoggingListener);

        btnDialog.setOnClickListener(btnDialogListener);

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

    private void assignCtrlVars() {
        progressBar = findViewById(R.id.progressBar);
        tvCurrentEPC = findViewById(R.id.tvCurrentEPC);
        btnRead = (LoadingButton) findViewById(R.id.btnRead);
        btnReset = (LoadingButton) findViewById(R.id.btnReset);
        btnInit = (LoadingButton) findViewById(R.id.btnInit);
        btnDialog = (LoadingButton) findViewById(R.id.btnDialog);
        btnScanEPC = findViewById(R.id.btnScanEPC);
        btnStopLogging = (LoadingButton) findViewById(R.id.btnStopLogging);
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

        if(values==null || !(values instanceof List)) {
            CToast(getApplicationContext(), "Invalid data read!!!", Toast.LENGTH_LONG);
            return;
        }

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(CAENLoggerActivity.this);
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CAENLoggerActivity.this, R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%04d. [%s] -->%3$10s \u00B0C", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
        this.loggerSvc = new CAENLoggerService(this.cmd, this.mScanHandler, Boolean.TRUE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cmd != null) {
            cmd.CloseReader();
        }
        if (loggerSvc != null) {
            loggerSvc.shutdownExecutorService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // ###################################################
    private class CAENCommandsHandler extends Handler {
        private final WeakReference<CAENLoggerActivity> mActivity;

        public CAENCommandsHandler(CAENLoggerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String value = null;
            Object obj = null;
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
                                btnDialog.setEnabled(true);
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
                case ReadTimeBIN:
                    Short timeBIN = msg.getData().getShort("body");
                    tvTimeBIN.setText(String.valueOf(timeBIN));
                    break;
                case ReadSamplesCnt:
                    Short valuesCnt = msg.getData().getShort("body");
                    if (valuesCnt >= 0) {
                        btnSamplesCnt.setText(String.format("%s measurements.", valuesCnt));
                        btnSamplesCnt.setOnClickListener(view -> {
                            if (valuesCnt > 0) {
                                new Thread(() -> {
                                    // Show ProgressBar
                                    runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

                                    try {
                                        int intervalVal = DefaultInterval;
                                        long initTSmSec = System.currentTimeMillis();

                                        // get existing interval value, if set...
                                        if (etInterval.getText() != null) {
                                            try {
                                                intervalVal = Integer.valueOf(etInterval.getText().toString());
                                            } catch (Exception e) {
                                            }
                                        }

                                        // get existing init timestamp value, if set...
                                        if (tvDateTime.getText() != null) {
                                            try {
                                                initTSmSec = dtParser.parse(tvDateTime.getText().toString()).getTime();
                                            } catch (Exception e) {
                                            }
                                        }
                                        //samplesCnt, intervalSeconds, startTSmSec
                                        //loggerSvc.ReadSamples(valuesCnt, intervalVal, initTSmSec);
                                        loggerSvc.doReadSamples(valuesCnt.intValue());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                        });
                    } else {
                        btnSamplesCnt.setText("ERR");
                    }
                    break;
                case ReadSamples:
                    // Hide ProgressBar
                    runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
                    Object samplesObj = msg.getData().get("body");
                    if(samplesObj instanceof List) {
                        List samples = (List) msg.getData().get("body");
                        displayMeasurementsDialog(samples);
                    }
                    break;
                case ReadInterval:
                    obj = extractData(Short.class, msg.getData());
                    if(obj != null) {
                        short interval = (short) obj;
                        etInterval.setText(String.valueOf(interval));
                    } else {
                        etInterval.setText("ERR");
                    }
                    break;
                case ReadLastSample:
                    obj = extractData(String.class, msg.getData());
                    if(obj != null) {
                        String lastSample = (String) obj;
                        tvLastSampleValue.setText(lastSample);
                    } else {
                        tvLastSampleValue.setText("ERR");
                    }
                    break;
                case ReadInitTimeStamp:
                    value = msg.getData().getString("body");
                    tvDateTime.setText(value);
                    break;
                case CmdRESET:
                    obj = extractData(Integer.class, msg.getData());
                    if(obj != null) {
                        Integer resReset = (Integer) obj;
                        if (resReset != 1) {
                            CToast(getApplicationContext(), "Reset Failed!\n", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case CmdDisableLogging:
                    obj = extractData(Integer.class, msg.getData());
                    if(obj != null) {
                        int resDisableLogging = (int) obj;
                        if (resDisableLogging == 1) {
                            CToast(getApplicationContext(), "Failed to stop Logging!\n", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
                case CmdEnableLogging:
                    obj = extractData(Integer.class, msg.getData());
                    if(obj != null) {
                        int resEnableLogging = (int) obj;
                        if (resEnableLogging == 0) {
                            CToast(getApplicationContext(), "Failed to start Logging!\n", Toast.LENGTH_SHORT);
                        }
                    }
                    break;
            }
        }
    }

    private Object extractData(Class clazz, Bundle data) {
        Class dataClazz = data.get("body").getClass();

        switch (clazz.getSimpleName()) {
            case "String":
                return data.getString("body");
            case "Short":
                return data.getShort("body");
            case "Integer":
                return data.getInt("body");
            case "Boolean":
                return data.getBoolean("body");
            default:
                return data.get("body");
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