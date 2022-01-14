package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CTRLReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdENABLE;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.FWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.HWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.HideProgressBar;
import static io.agritrack.caen.api.CAEN_CONSTANTS.InitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.LastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadTimeBIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.STATUSReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.SamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ShowProgressBar;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeStamp;
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
import com.uhf.api.cls.Reader;

import java.lang.ref.WeakReference;
import java.util.List;

import io.agritrack.R;
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
    private Button btnRead, btnReset, btnInit, btnSamplesCnt, btnControlReg;
    private ProgressBar progressBar;

    private ICAEN_API cmd;

    private short valuesCnt;
    private String loggerEpc;

    final Runnable readFWRevisionThread = new Runnable() {
        @Override
        public void run() {
            String response = cmd.ReadFWRevision();
            mScanHandler.sendMessage(createMessage(FWRevision, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readHWRevisionThread = new Runnable() {
        @Override
        public void run() {
            String response = cmd.ReadHWRevision();
            mScanHandler.sendMessage(createMessage(HWRevision, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readCTRLRegisterThread = new Runnable() {
        @Override
        public void run() {
            String response = cmd.ReadControlRegister();
            mScanHandler.sendMessage(createMessage(CTRLReg, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readSTATUSRegisterThread = new Runnable() {
        @Override
        public void run() {
            String response = cmd.ReadStatusRegister();
            mScanHandler.sendMessage(createMessage(STATUSReg, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readTimeBINThread = new Runnable() {
        @Override
        public void run() {
            Short response = cmd.ReadTimeBIN();
            mScanHandler.sendMessage(createMessage(ReadTimeBIN, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readSampleCNTThread = new Runnable() {
        @Override
        public void run() {
            Short response = cmd.ReadSamplesCount();
            mScanHandler.sendMessage(createMessage(SamplesCnt, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readIntervalThread = new Runnable() {
        @Override
        public void run() {
            Short response = cmd.ReadInterval();
            mScanHandler.sendMessage(createMessage(ReadInterval, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readInitTSThread = new Runnable() {
        @Override
        public void run() {
            String response = cmd.ReadInitDatetime();
            mScanHandler.sendMessage(createMessage(InitTimeStamp, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readLastTemperatureThread = new Runnable() {
        @Override
        public void run() {
            Double response = cmd.ReadLastSample();
            mScanHandler.sendMessage(createMessage(LastSample, String.valueOf(response)));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable readThread = new Runnable() {
        @Override
        public void run() {

            // Show ProgressBar
            Message msgShowPB = new Message();
            msgShowPB.what = ShowProgressBar;
            mScanHandler.sendMessage(msgShowPB);

            // TimeBIN
            Short value = cmd.ReadTimeBIN();
            mScanHandler.sendMessage(createMessage(ReadTimeBIN, value));
            delay(100l);

            // SampleCNT
            value = cmd.ReadSamplesCount();
            mScanHandler.sendMessage(createMessage(SamplesCnt, value));
            delay(100l);

            // Interval
            value = cmd.ReadInterval();
            mScanHandler.sendMessage(createMessage(ReadInterval, value));
            delay(100l);

            // FWRevision
            String response = cmd.ReadFWRevision();
            mScanHandler.sendMessage(createMessage(FWRevision, response));
            delay(100l);

            // HWRevision
            response = cmd.ReadHWRevision();
            mScanHandler.sendMessage(createMessage(HWRevision, response));
            delay(100l);

            // CTRLRegister
            response = cmd.ReadControlRegister();
            mScanHandler.sendMessage(createMessage(CTRLReg, response));
            delay(100l);

            // STATUSRegister
            response = cmd.ReadStatusRegister();
            mScanHandler.sendMessage(createMessage(STATUSReg, response));
            delay(100l);

            // InitTS
            response = cmd.ReadInitDatetime();
            mScanHandler.sendMessage(createMessage(InitTimeStamp, response));
            delay(100l);

            // LastTemperature
            Double temp = cmd.ReadLastSample();
            mScanHandler.sendMessage(createMessage(LastSample, String.valueOf(temp)));
            delay(100l);

            // Hide ProgressBar
            Message msgHidePB = new Message();
            msgHidePB.what = HideProgressBar;
            mScanHandler.sendMessage(msgHidePB);
        }
    };

    final Runnable resetThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.Reset();
            mScanHandler.sendMessage(createMessage(CmdRESET, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable showProgressThread = new Runnable() {
        @Override
        public void run() {
            Message msgShowPB = new Message();
            msgShowPB.what = ShowProgressBar;
            mScanHandler.sendMessage(msgShowPB);
        }
    };

    final Runnable hideProgressThread = new Runnable() {
        @Override
        public void run() {
            Message msgShowPB = new Message();
            msgShowPB.what = HideProgressBar;
            mScanHandler.sendMessage(msgShowPB);
        }
    };


    final Runnable writeTimeBinOneThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.WriteTimeBinONE();
            mScanHandler.sendMessage(createMessage(WriteTimeBIN, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable writeIntervalThread = new Runnable() {
        @Override
        public void run() {
            short samplingInterval = DefaultInterval;
            if (etInterval.getText() != null && !Strings.isEmptyOrWhitespace(etInterval.getText().toString())) {
                samplingInterval = Short.valueOf(etInterval.getText().toString());
            }

            Reader.READER_ERR response = cmd.WriteInterval(samplingInterval);
            mScanHandler.sendMessage(createMessage(WriteInterval, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable writeCurrentDateTimeThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.WriteCurrentDatetime();
            mScanHandler.sendMessage(createMessage(WriteTimeStamp, response));
            mScanHandler.removeCallbacks(this);
        }
    };

    final Runnable enableLoggingThread = new Runnable() {
        @Override
        public void run() {
            Reader.READER_ERR response = cmd.EnableLogging();
            mScanHandler.sendMessage(createMessage(CmdENABLE, response));
            mScanHandler.removeCallbacks(this);
        }
    };


    protected final View.OnClickListener btnResetListener = v -> {
        // Show ProgressBar
        mScanHandler.post(showProgressThread);
        // -------------------------------------
        if (this.cmd != null) {
            try {
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
                // reset command
                mScanHandler.postDelayed(resetThread, 50l);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            CToast(getApplicationContext(), "Plz Read Tag First!!", Toast.LENGTH_SHORT);
        }
        // -------------------------------------
        // Hide ProgressBar
        mScanHandler.postDelayed(hideProgressThread, 800l);
    };

    protected final View.OnClickListener btnInitListener = v -> {

        cmd.setFilterEPC(loggerEpc);

        // Show ProgressBar
        mScanHandler.post(showProgressThread);
        // -------------------------------------
        mScanHandler.postDelayed(writeTimeBinOneThread, 50l);
        mScanHandler.postDelayed(writeIntervalThread, 100l);
        mScanHandler.postDelayed(writeCurrentDateTimeThread, 150l);
        mScanHandler.postDelayed(enableLoggingThread, 200l);
        mScanHandler.postDelayed(readLastTemperatureThread, 300l);

        // -------------------------------------
        // Hide ProgressBar
        mScanHandler.postDelayed(hideProgressThread, 800l);
    };

    protected final View.OnClickListener btnReadListener = v -> {
        // Show ProgressBar
        mScanHandler.post(showProgressThread);
        // -------------------------------------
        clearControls();
        // -------------------------------------
        SingleShotScanner singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_LOGGER);
        singleShot_runnable.startReading();
        mScanHandler.post(singleShot_runnable);
        // -------------------------------------
        btnReset.setEnabled(true);
        btnInit.setEnabled(true);
        // -------------------------------------
        if(!Strings.isEmptyOrWhitespace(loggerEpc)) {
            cmd.setFilterEPC(loggerEpc);
            mScanHandler.postDelayed(readThread, 100l);
        }
        // -------------------------------------

//        mScanHandler.postDelayed(readFWRevisionThread, 50l);
//        mScanHandler.postDelayed(readHWRevisionThread, 100l);
//        mScanHandler.postDelayed(readCTRLRegisterThread, 150l);
//        mScanHandler.postDelayed(readSTATUSRegisterThread, 200l);
//        mScanHandler.postDelayed(readTimeBINThread, 250l);
//        mScanHandler.postDelayed(readInitTSThread, 300l);
//        mScanHandler.postDelayed(readSampleCNTThread, 350l);
//        mScanHandler.postDelayed(readIntervalThread, 400l);
//        mScanHandler.postDelayed(readLastTemperatureThread, 450l);
        // -------------------------------------
        // Hide ProgressBar
        mScanHandler.postDelayed(hideProgressThread, 800l);
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

        progressBar = findViewById(R.id.progressBar);

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
            // remove all callbacks and messages.
            mScanHandler.removeCallbacks(null);

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
    protected void onDestroy() {
        super.onDestroy();
        cmd.CloseReader();
    }

    //--------------------------------------------
    private Message createMessage(int what, Object value) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();

        if (value!=null && value instanceof String)
            b.putString("body", (String)value);
        else if (value!=null && value instanceof Short)
            b.putShort("body", (short)value);
        else if (value!=null && value instanceof Reader.READER_ERR)
            b.putString("body", ((Reader.READER_ERR) value).name());

        msg.setData(b);

        return msg;
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
            switch (msg.what) {
                case 1:
                    loggerEpc = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(loggerEpc)) {
                            // sets Logger filter to closest EPC.
                            cmd.setFilterEPC(loggerEpc);
                            tvCurrentEPC.setText(loggerEpc);
                        } else if (!IsDemo) {
                            CToast(getApplicationContext(), "No Logger EPC found, plz scan again!!", Toast.LENGTH_LONG);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case FWRevision:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvFWRevision.setText(value);
                    } else {
                        tvFWRevision.setText("ERR");
                    }
                    break;
                case HWRevision:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvHWRevision.setText(value);
                    } else {
                        tvHWRevision.setText("ERR");
                    }
                    break;
                case CTRLReg:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        btnControlReg.setText(value);
                    } else {
                        btnControlReg.setText("ERR");
                    }
                    break;
                case STATUSReg:
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
                    short timeBin = msg.getData().getShort("body");
                    tvTimeBIN.setText(String.valueOf(timeBin));
                    break;
                case SamplesCnt:
                    valuesCnt = msg.getData().getShort("body");
                    if (valuesCnt >= 0) {
                        btnSamplesCnt.setText(String.format("%s measurements.", valuesCnt));
                        btnSamplesCnt.setOnClickListener(view -> {
                            if (valuesCnt > 0) {
                                try {
                                    List<String[]> values = cmd.ReadSamples(valuesCnt);
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
                case LastSample:
                    value = msg.getData().getString("body");
                    if (!Strings.isEmptyOrWhitespace(value)) {
                        tvLastSampleValue.setText(value);
                    } else {
                        tvLastSampleValue.setText("ERR");
                    }
                    break;
                case InitTimeStamp:
                    value = msg.getData().getString("body");
                    tvDateTime.setText(value);
                    break;
                case ShowProgressBar:
                    runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
                    break;
                case HideProgressBar:
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    break;

                case CmdRESET:
                    String result = msg.getData().getString("body");
                    if(!"MT_OK_ERR".equalsIgnoreCase(result)) {
                        CToast(getApplicationContext(), "Reset Failed!\n", Toast.LENGTH_SHORT);
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


    private void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {}
    }
}