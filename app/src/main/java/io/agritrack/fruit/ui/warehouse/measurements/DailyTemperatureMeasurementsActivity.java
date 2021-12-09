package io.agritrack.fruit.ui.warehouse.measurements;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.CaenLoggerDialogFragment;
import io.agritrack.ui.tools.CaenLoggerFruitDialogFragment;

public class DailyTemperatureMeasurementsActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private TextView tvPoleName;
    private Button btnScanPole;
    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_temperature_measurements);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderDailyMeasurements);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(DailyTemperatureMeasurementsActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToFruitWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {

    }

    private String scanCloserEPC(UhfReader uhfReader) {
        SingleShotScanner scanner = new SingleShotScanner();
        scanner.setUhfReader(uhfReader);
        scanner.setFilter(Filters.RFID_POLE);

        try {
            String epcStr = scanner.call();
            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                // after bin is identified, initialize the temperatures logger.
                IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
                if (logger != null) {
                    return logger.rfid;
                } else if (!IsDemo) {
                    CToast(getApplicationContext(), render("No IOT Logger was found linked to this POLE!!"), Toast.LENGTH_LONG);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateState() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        btnScanPole = findViewById(R.id.btnScanPole);
        tvPoleName = findViewById(R.id.tvPoleName);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void onClick(View view) {
        //update scanning, uhfReader, tvPlatformName values in thread
        UhfReader _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);
        _uhfReader.setOutputPower(24);

        String strEPC = scanCloserEPC(_uhfReader);

        if (!Strings.isEmptyOrWhitespace(strEPC)) {
            FragmentManager fm = getSupportFragmentManager();
            CaenLoggerFruitDialogFragment loggerDlg = CaenLoggerFruitDialogFragment.newInstance(strEPC);
            loggerDlg.show(fm, CaenLoggerFruitDialogFragment.TAG);
        } else {
            CToast(getApplicationContext(), "No Logger Found. Please scan again!!", Toast.LENGTH_LONG);
        }

        return;
/*
        if (!Strings.isEmptyOrWhitespace(strEPC)) {
            if (IsDemo) {

//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
                try {
                    CAENCommander cmd = new CAENCommander(_uhfReader, strEPC);
                    cmd.HighSensitivity();
                    Thread.sleep(1000);
                    CAENCommander.Response rs = resetLogger(cmd);
                    Thread.sleep(1000);
                    //String temp = initializeLogger(cmd);
                    //Thread.sleep(1000);
                    cmd.LowSensitivity();

                    CToast(getApplicationContext(), "Logger resetted!"*//*temp*//*, Toast.LENGTH_LONG);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                }
//            });
//            t.start();
            } else {
                tempLoggerDialog = new TempLoggerDialog(FishingBinsActivity.this, R.string.init_temp_logger);
                if (!Strings.isEmptyOrWhitespace(strEPC)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            currentBin = strEPC.substring(11);
                            adapterBins.addUniqueItem(currentBin);
                            adapterBins.notifyDataSetChanged();
                            tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));

                            // after bin is identified, initialize the temperatures logger.
                            IotLogger logger = db.iotLoggerDAO().getByAssetRFID(strEPC);
                            if (logger != null) {
                                tempLoggerDialog.showDialog(logger.rfid);
                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_LONG);
                            }
                        }
                    });
                }
            }
        } else {
            CToast(getApplicationContext(), "No Logger Found. Please scan again!!", Toast.LENGTH_LONG);
        }*/
    }


/*
    public void prepareLogger(UhfReader uhfReader, CaenLoggerDialogFragment loggerDlg, String loggerEPC) {
        //---------------------------------------------------
        CAENCommander cmd = new CAENCommander(uhfReader, loggerEPC);
        //---------------------------------------------------

        loggerDlg.setMessage("Preparing Logger...");
        cmd.HighSensitivity();

        loggerDlg.setMessage("Resetting Logger...");
        loggerDlg.setButtonText(R.id.btnReset, "Resetting...");
        CAENCommander.Response rs = resetLogger(cmd);
        if(rs.succeeded()) {
            loggerDlg.setButtonText(R.id.btnReset, "Success");
        } else {
            loggerDlg.setButtonText(R.id.btnReset, "Failed");
        }

        loggerDlg.setMessage("Setting Logger up...");
        loggerDlg.setButtonText(R.id.btnSetup, "Setting Up...");
        CAENCommander.Response res = setupLogger(cmd);
        if(res.succeeded()) {
            loggerDlg.setButtonText(R.id.btnSetup, "Success");
        } else {
            loggerDlg.setButtonText(R.id.btnSetup, "Failed");
        }

        loggerDlg.setMessage("Start Logging...");
        loggerDlg.setButtonText(R.id.btnInit, "Start Logger...");
        String temp = enableLogger(cmd);
        if(!Strings.isEmptyOrWhitespace(temp)) {
            loggerDlg.setButtonText(R.id.btnInit, "Success");
        } else {
            loggerDlg.setButtonText(R.id.btnInit, "Failed");
        }

        cmd.LowSensitivity();

        loggerDlg.setMessage("First value: " + temp);
        loggerDlg.dismiss();
    }
*/
}