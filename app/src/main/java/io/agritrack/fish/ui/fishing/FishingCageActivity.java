package io.agritrack.fish.ui.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.CageDetails;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class FishingCageActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MobileDB db;
    private Button scanPlatformButton, scanCageButton;
    private TextView tvPlatformRFID, tvCageRFID;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_cage);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingCage);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // =================================
        // RFID scanning functionality
        scanPlatformButton.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilter(Filters.RFID_PLATFORM);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvPlatformRFID.setText(epcStr);
                        }
                    });
                    //tvCageName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        scanCageButton.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilter(Filters.RFID_CAGE);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvCageRFID.setText(epcStr);
                        }
                    });
                    //tvCageName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        // =================================

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingCageActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingCageActivity.this);
            infoDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToDetails);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToTeam);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        scanPlatformButton = findViewById(R.id.btnScanPlatform);
        scanCageButton = findViewById(R.id.btnScanCage);
        tvCageRFID = findViewById(R.id.tvCageName);
        tvPlatformRFID = findViewById(R.id.tvPlatformName);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        tvPlatformRFID.setText(hvst.platformRFID);
        tvCageRFID.setText(hvst.cageRFID);
    }

    private void updateState() {
        CharSequence cageRFID = tvCageRFID.getText();

        if (cageRFID != null) {
            GlobalState.recFishing.cageRFID = cageRFID.toString();
            CageDetails cage = db.cageDetailsDAO().getByRFId(GlobalState.recFishing.cageRFID);
            if (cage != null) {
                GlobalState.recFishing.speciesName = cage.fishType; //TODO: compare with Requested Species
                GlobalState.recFishing.pathologist = cage.ichthyopathologist;
                GlobalState.recFishing.lastFed = cage.lastFed;
            } else {
                // TODO:: add alert, no cage corresponding to RFID found in local DB!!
            }
        }

        GlobalState.recFishing.platformRFID = tvPlatformRFID.getText().toString();
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if(!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.platformRFID)) {
                sb.append(String.format("\n%s is missing", "'Platform tag'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.cageRFID)) {
                sb.append(String.format("\n%s is missing", "'Cage tag'"));
            }
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        if (executor != null)
            executor.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (executor != null)
            executor.shutdown();
    }
}