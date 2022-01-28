package io.agritrack.fruit.ui.harvesting;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.Constants.Greek_Locale;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recHarvest;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.PlantLotEnquiryCallBack;
import io.agritrack.api.sync.SpeciesByPoleRfidEnquiryCallBack;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.LotDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.HarvestRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class HarvestingStartActivity extends AppCompatActivity {
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<SpeciesDTO> enquiryResult = new MutableLiveData<>();
    private final MutableLiveData<LotDTO> enquiryLotResult = new MutableLiveData<>();
    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private TextView tvPoleName, tvHarvestLot, tvSpeciesNameLabel, tvSpeciesName;
    private Button btnScanPole;
    private String greenhouse, poleRFID, speciesName, plantLot;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvesting_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        tvSpeciesNameLabel.setVisibility(View.INVISIBLE);
        tvSpeciesName.setVisibility(View.INVISIBLE);

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(this::onClick);
        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        // API < 26
        int weekOfYearId = 0, dayOfWeekId = 0;
        String formatTime = ""; //String.format("%02d%02d%02d",now.getHour(), now.getMinute(), now.getSecond());
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Calendar cal = new GregorianCalendar(Greek_Locale);
            cal.setTime(new Date());
            weekOfYearId = cal.get(Calendar.WEEK_OF_YEAR);
            dayOfWeekId = cal.get(Calendar.DAY_OF_WEEK)+1;
            formatTime = String.format("%02d%02d%02d",cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        } else {
            LocalDateTime now = LocalDateTime.now();
            TemporalField woy = WeekFields.of(Greek_Locale).weekOfWeekBasedYear();
            weekOfYearId = now.get(woy);
            dayOfWeekId = now.getDayOfWeek().ordinal()+1;
            formatTime = String.format("%02d%02d%02d",now.getHour(), now.getMinute(), now.getSecond());
        }

        tvHarvestLot.setText(String.format("%02d%s", weekOfYearId, dayOfWeekId));
        recHarvest.harvestLot = String.format("%s%s", tvHarvestLot.getText().toString(), formatTime);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestingStartActivity.this);
            supportDialog.showDialog();
        });

        enquiryResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No planting returned for this pole"), Toast.LENGTH_LONG);
                return;
            }
            tvSpeciesNameLabel.setVisibility(View.VISIBLE);
            tvSpeciesName.setVisibility(View.VISIBLE);
            tvSpeciesName.setText(response.local_name);
            speciesName = response.local_name;
        });

        enquiryLotResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No plant LOT returned for this greenhouse"), Toast.LENGTH_LONG);
                return;
            }
            plantLot = response.lot;
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
        ImageView ivNext = findViewById(R.id.ivToScanTotes);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), HarvestingTotesActivity.class);
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
        tvHarvestLot = findViewById(R.id.tvHarvestLot);
        tvSpeciesNameLabel = findViewById(R.id.tvSpeciesNameLabel);
        tvSpeciesName = findViewById(R.id.tvSpeciesName);
    }

    private void initControlsFromState() {
        HarvestRecord trns = FruitGlobalState.recHarvest;

        if (!Strings.isEmptyOrWhitespace(trns.poleRFID)) {
            tvPoleName.setText(trns.poleRFID);
        }

        if (!Strings.isEmptyOrWhitespace(trns.speciesName)) {
            tvSpeciesNameLabel.setVisibility(View.VISIBLE);
            tvSpeciesName.setVisibility(View.VISIBLE);
            tvSpeciesName.setText(trns.speciesName);
        }
    }

    private void invokeEnquirySpecies() {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // get species by pole rfid
            Call<SpeciesDTO> enquirySpeciesByPoleAsyncCall = enquiryService.getSpeciesByPoleRfid(poleRFID, "Bearer " + token);
            enquirySpeciesByPoleAsyncCall.enqueue(new SpeciesByPoleRfidEnquiryCallBack(this.enquiryResult));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void invokeEnquiryLot() {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync collection lot for current Site
            Call<LotDTO> enquiryPlantLotAsyncCall = enquiryService.getPlantLotByPoleRfid(poleRFID, "Bearer " + token);
            enquiryPlantLotAsyncCall.enqueue(new PlantLotEnquiryCallBack(this.enquiryLotResult));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private HarvestRecord updateState() {
        HarvestRecord harvestRecord = recHarvest;

        harvestRecord.poleRFID = tvPoleName.getText().toString();
        harvestRecord.harvestLotForCustomer = tvHarvestLot.getText().toString();
        if (!Strings.isEmptyOrWhitespace(this.speciesName)) {
            harvestRecord.speciesName = tvSpeciesName.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(this.greenhouse)) {
            harvestRecord.greenhouse = this.greenhouse;
        }

        if (!Strings.isEmptyOrWhitespace(this.plantLot)) {
            harvestRecord.plantLot= this.plantLot;
        }
        return harvestRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recHarvest.poleRFID)) {
                sb.append(String.format("\n%s is missing", "'Pole tag'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_POLE);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<HarvestingStartActivity> mActivity;

        public ScanHandler(HarvestingStartActivity activity) {
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
                            poleRFID = epcStr;
                            invokeEnquirySpecies();
                            invokeEnquiryLot();
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
                        CToast(getApplicationContext(), render("No Asset was scanned!!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}