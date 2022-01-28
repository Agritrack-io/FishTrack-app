package io.agritrack.fruit.ui.planting;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.Constants.Greek_Locale;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recPlant;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.common.Species;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.PlantRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.service.LocalPreferences;

public class PlantingStartActivity extends AppCompatActivity {
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private Spinner spTomatoType;
    private TextView tvPoleName;
    private Button btnScanPole;
    private String greenhouse, plantLot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planting_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSeedingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // API < 26
        int weekOfYearId = 0, dayOfWeekId = 0;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Calendar cal = new GregorianCalendar(Greek_Locale);
            Date today = new Date();
            cal.setTime(today);
            weekOfYearId = cal.get(Calendar.WEEK_OF_YEAR);
            dayOfWeekId = cal.get(Calendar.DAY_OF_WEEK);
        } else {
            LocalDate date = LocalDate.now();
            TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
            weekOfYearId = date.get(woy);
            dayOfWeekId = date.getDayOfWeek().ordinal()+1;
        }
        plantLot = (String.format("%02d%s",weekOfYearId, dayOfWeekId));

        // load fish species and fill in the spFishType Spinner.
        List<Species> tomatoSpecies = db.speciesDAO().getAll();
        if (tomatoSpecies != null && !tomatoSpecies.isEmpty()) {
            String[] species = tomatoSpecies.stream().map(x -> x.localName).toArray(String[]::new);
            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, species);
            spAdapter.setDropDownViewResource(R.layout.simple_spinner_item);

            if (!Strings.isEmptyOrWhitespace(recPlant.speciesName)) {
                //recPlant.speciesPos = Arrays.asList(species).indexOf(recPlant.speciesName);
                spTomatoType.setAdapter(spAdapter);
                spTomatoType.setSelection(recPlant.speciesPos);
            } else {
                spTomatoType.setAdapter(spAdapter);
            }
        }

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PlantingStartActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onPause();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PlantingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
        tvPoleName = findViewById(R.id.tvPoleName);
        btnScanPole = findViewById(R.id.btnScanPole);
        spTomatoType = findViewById(R.id.spTomatoType);
    }

    private void initControlsFromState() {
        PlantRecord trns = FruitGlobalState.recPlant;

        if (!Strings.isEmptyOrWhitespace(trns.poleRFID)) {
            tvPoleName.setText(trns.poleRFID);
        }

        if (trns.speciesPos > -1) {
            spTomatoType.setSelection(trns.speciesPos);
        }
    }

    private PlantRecord updateState() {
        PlantRecord plantRecord = recPlant;

        plantRecord.poleRFID = tvPoleName.getText().toString();

        if (spTomatoType.getSelectedItem() != null) {
            plantRecord.speciesName = spTomatoType.getSelectedItem().toString();
        }
        plantRecord.speciesPos = spTomatoType.getSelectedItemPosition();

        if (!Strings.isEmptyOrWhitespace(this.greenhouse)) {
            plantRecord.greenhouse = this.greenhouse;
        }

        if (!Strings.isEmptyOrWhitespace(this.plantLot)) {
            plantRecord.plantLot = this.plantLot;
        }

        return plantRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recPlant.poleRFID)) {
                sb.append(String.format("\n%s is missing", "'Pole tag'"));
            }

            if (Strings.isEmptyOrWhitespace(recPlant.speciesName)) {
                sb.append(String.format("\n%s is missing", "'Tomato type'"));
            }
        }

        return sb.toString();
    }

    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_POLE);
        scanner_runnable.startReading(); //TODO: check if reading has started (startReading should return a boolean..)
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<PlantingStartActivity> mActivity;

        public ScanHandler(PlantingStartActivity activity) {
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
                            tvPoleName.setText(epcStr);
                            Asset pole = db.assetDAO().getAssetByEpc(epcStr);
                            Site tempSite = db.siteDAO().getBySiteNameAndCode(LocalPreferences.getCurrentSiteLevel3(), pole.siteCode);
                            if (tempSite != null) {
                                greenhouse = tempSite.name;
                            }
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