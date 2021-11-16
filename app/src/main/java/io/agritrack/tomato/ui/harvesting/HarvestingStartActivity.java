package io.agritrack.tomato.ui.harvesting;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.tomato.ui.FruitHomeActivity;
import io.agritrack.tomato.ui.seeding.SeedingConfirmActivity;
import io.agritrack.tomato.ui.seeding.SeedingStartActivity;
import io.agritrack.ui.service.LocalPreferences;

public class HarvestingStartActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private Spinner spTomatoType;
    private TextView tvPoleName, tvHarvestLot;
    private Button btnScanPole;

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(view -> {
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
                            tvPoleName.setText(epcStr);
                        }
                    });
                    //tvCageName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestingStartActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToScanTotes);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HarvestingTotesActivity.class);
            startActivity(i);
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
    }

    private void initControlsFromState() {

    }

    private void updateState() {

    }
}