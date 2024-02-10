package io.agritrack.kefalonia.fish.ui.testBinTemperature;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.caen.api.CAENLoggerService;
import io.agritrack.kefalonia.caen.api.ICAEN_API;
import io.agritrack.kefalonia.caen.api.RFIDModuleFactory;
import io.agritrack.kefalonia.caen.common.CAENState;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.fish.ui.fishing.FishingBinsActivity;
import io.agritrack.kefalonia.fish.ui.fishing.FishingFillBinsActivity;
import io.agritrack.kefalonia.fish.ui.initBins.InitBinsActivity;
import io.agritrack.kefalonia.rfid.SingleShotScanner;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.sound.SoundUtil;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class TestBinTempActivity extends AppCompatActivity {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ProgressBar progressBar;
    private TextView tvCurrentTemp, tvCurrentBin;
    private Button btnScanBin;
    private ICAEN_API cmd;
    private CAENLoggerService loggerSvc;
    private int readSamplesCountCnt = 0;
    final Runnable readLastSampleThread = new Runnable() {
        @Override
        public void run() {
            while (readSamplesCountCnt < 3) {
                readSamplesCountCnt++;
                if (cmd.ReadSamplesCount() < 1) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    if (!(readSamplesCountCnt < 3)) {
                        CToast(getApplicationContext(), render(R.string.error_reading_logger), Toast.LENGTH_LONG);
                        return;
                    }
                    CToast(getApplicationContext(), render(R.string.retry_last_temp), Toast.LENGTH_SHORT);
                    return;
                }
            }

            //cmd.LowPowerLevel();
            // read CONTROL register state
            CAENState state = loggerSvc.doReadLastTemperature();
            Double lastTemp = state != null ? state.getLastSample() : null;

            progressBar.setVisibility(ProgressBar.INVISIBLE);

            if (lastTemp != null && lastTemp >= -10 && lastTemp < 40 && lastTemp != 0.03 && lastTemp != -0.03) {
                readSamplesCountCnt = 0;
                Message msg = new Message();
                msg.what = 200;
                Bundle b = new Bundle();
                b.putDouble("value", lastTemp);

                msg.setData(b);
                mScanHandler.sendMessage(msg);
            } else {
                CToast(getApplicationContext(), render(R.string.retry_last_temp), Toast.LENGTH_LONG);
            }
        }
    };
    private boolean intentForBinActivity = false;
    private boolean intentForFillBinActivity = false;
    private boolean intentForInitBinActivity = false;
    private SingleShotScanner singleShot_runnable;
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bin_temp);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.getBoolean("BinActivity")) {
                intentForBinActivity = bundle.getBoolean("BinActivity");
            } else if (bundle.getBoolean("FillBinActivity")) {
                intentForFillBinActivity = bundle.getBoolean("FillBinActivity");
            } else if (bundle.getBoolean("BinInitActivity")) {
                intentForInitBinActivity = bundle.getBoolean("BinInitActivity");
            }
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTestTemp);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // =================================
        // RFID scanning functionality
        btnScanBin.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TestBinTempActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null) {
            unregisterReceiver(keyReceiver);
        }
        if (loggerSvc != null) {
            loggerSvc.shutdownExecutorService();
        }
    }

    @Override
    protected void onStart() {
        // instantiate Reader Module
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);

        // instantiate Reader Module
        this.cmd = RFIDModuleFactory.getInstance();
        this.loggerSvc = new CAENLoggerService(this.cmd, this.mScanHandler, Boolean.TRUE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stopScanner();
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToMainMenu);
        ivBack.setOnClickListener(view -> {
            this.stopScanner();
            Intent i = new Intent();
            if (intentForBinActivity) {
                i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                i.putExtra("BinActivity", true);
            } else if (intentForFillBinActivity) {
                i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
                i.putExtra("FillBinActivity", true);
            } else if (intentForInitBinActivity) {
                i = new Intent(getApplicationContext(), InitBinsActivity.class);
                i.putExtra("BinInitActivity", true);
            } else {
                i = new Intent(getApplicationContext(), FishHomeActivity.class);
            }
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnScanBin = findViewById(R.id.btnScanBin);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/digital-7.ttf");
        tvCurrentTemp.setTypeface(type);
        tvCurrentBin = findViewById(R.id.tvCurrentBin);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void onClick(View view) {
        tvCurrentTemp.setText("");
        tvCurrentBin.setText("");
        singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_LOGGER);
        singleShot_runnable.startReading();
        mScanHandler.postDelayed(singleShot_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.singleShot_runnable != null) {
            this.singleShot_runnable.stopReading();
            mScanHandler.removeCallbacks(this.singleShot_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<TestBinTempActivity> mActivity;

        public ScanHandler(TestBinTempActivity activity) {
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
                            // after bin is identified, initialize the temperatures logger.
                            Asset bin = db.assetDAO().getByLoggerEPC(epcStr);
                            if (bin != null) {
                                String binEPC = bin.rfid;
                                tvCurrentBin.setText(binEPC.substring(binEPC.length() - 10));
                                progressBar.setVisibility(ProgressBar.VISIBLE);
                                progressBar.setProgress(0);
                                cmd.setFilterEPC(epcStr);
                                mScanHandler.post(readLastSampleThread);
                            } else if (!IsDemo) {
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                CToast(getApplicationContext(), render(R.string.no_logger_found_linked_to_bin), Toast.LENGTH_SHORT);
                            }
                        } else {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            CToast(getApplicationContext(), render(R.string.scan_bin_again), Toast.LENGTH_SHORT);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        saveCrashInfo2File(e);
                    }
                    break;
                case 200:
                    Double lastTemp = msg.getData().getDouble("value");
                    tvCurrentTemp.setText(String.format(new DecimalFormat("##.##").format(lastTemp) + "°C"));
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}