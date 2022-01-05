package io.agritrack.fish.ui.fishing;

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

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.CageDetails;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.service.LocalPreferences;

public class FishingCageActivity extends TriggerKeyAwareActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
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

        // get references of the controls
        assignCtrlVars();

        // =================================
        // RFID scanning functionality
        scanPlatformButton.setOnClickListener(view -> {
            MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(new String[]{Filters.RFID_PLATFORM});
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        });

        scanCageButton.setOnClickListener(view -> {
            MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(new String[]{Filters.RFID_CAGE});
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
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

    @Override
    protected void onClick(View view) {
        MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(new String[]{Filters.RFID_PLATFORM, Filters.RFID_CAGE});
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
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
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
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
                GlobalState.recFishing.speciesName = cage.species; //TODO: compare with Requested Species
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
        if (!IsDemo) {
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
    protected void onResume() {
        super.onResume();
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<FishingCageActivity> mActivity;

        public ScanHandler(FishingCageActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
//            FishingCageActivity activity = mActivity.get();
//            if (activity != null) {
//            }
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!epcList.isEmpty()) {
                            for (CharSequence epcCharSeq : epcList) {
                                String epc = epcCharSeq.toString();
                                if (epc.startsWith(Filters.RFID_PLATFORM)) {
                                    tvPlatformRFID.setText(epc);
                                } else if (epc.startsWith(Filters.RFID_CAGE)) {
                                    tvCageRFID.setText(epc);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Neither Platform nor Cage were detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

}