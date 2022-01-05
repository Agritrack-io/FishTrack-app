package io.agritrack.fruit.ui.warehouse.measurements;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitFruitDialogFragment;

public class DailyTemperatureMeasurementsActivity extends TriggerKeyAwareActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private TextView tvPoleName;
    private Button btnScanPole;


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

     @Override
    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_POLE);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<DailyTemperatureMeasurementsActivity> mActivity;

        public ScanHandler(DailyTemperatureMeasurementsActivity activity) {
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
                            FragmentManager fm = getSupportFragmentManager();
                            LoggerInitFruitDialogFragment loggerDlg = LoggerInitFruitDialogFragment.newInstance(epcStr);
                            loggerDlg.show(fm, LoggerInitFruitDialogFragment.TAG);
                        } else {
                            CToast(getApplicationContext(), "No Logger Found. Please scan again!!", Toast.LENGTH_LONG);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No Pole Tag was detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}
