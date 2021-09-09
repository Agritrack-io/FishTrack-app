package io.agritrack.fishtrack.ui.fishing;

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

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.CageDetails;
import io.agritrack.fishtrack.rfid.SingleShotScanner;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class FishingCageActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MobileDB db;
    private Button scanCageButton, scanNetButton;
    private TextView tvCageRFID, tvNetRFID;

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
        scanCageButton.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            scanner.setUhfReader(UhfReader.getInstance());
            scanner.setFilter(Filters.RFID_CAGE);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
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

        scanNetButton.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            scanner.setUhfReader(UhfReader.getInstance());
            scanner.setFilter(Filters.RFID_NET);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvNetRFID.setText(epcStr);
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

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToDetails);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
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
        scanCageButton = findViewById(R.id.btnScanCage);
        scanNetButton = findViewById(R.id.btnScanNet);
        tvNetRFID = findViewById(R.id.tvNetName);
        tvCageRFID = findViewById(R.id.tvCageName);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        tvCageRFID.setText(hvst.cageRFID);
        tvNetRFID.setText(hvst.netRFID);
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

        GlobalState.recFishing.netRFID = tvNetRFID.getText().toString();
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.cageRFID)) {
            sb.append(String.format("\n%s is missing", "'Cage tag'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.netRFID)) {
            sb.append(String.format("\n%s is missing", "'Net tag'"));
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