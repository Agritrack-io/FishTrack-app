package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.CageDetails;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.service.LocalPreferences;

public class FishingCageActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private Button scanPlatformButton, scanCageButton;
    private TextView tvPlatformRFID, tvCageRFID;
    private YesNoDialogFragment confirmCageSelectionDlg;
    private String cageCode = "", scannedCage;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_cage);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingCage);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get references of the controls
        assignCtrlVars();

        confirmCageSelectionDlg = YesNoDialogFragment.instance();
        confirmCageSelectionDlg.onConfirm(bundle -> {
            showAddDialog();
        });
        confirmCageSelectionDlg.onReject(bundle -> {
            tvCageRFID.setText(null);
            recFishing.cageRFID = null;
        });

        // =================================
        // RFID scanning functionality
        scanPlatformButton.setOnClickListener(this::onClick);

        scanCageButton.setOnClickListener(this::onClick);
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
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void onClick(View view) {

        MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
        scanner_runnable.LowEnergy();
        if(view!=null){
            if(view.getId() == scanCageButton.getId()){
                scanner_runnable.setFilter(new String[]{Filters.RFID_CAGE});
            } else if (view.getId() == scanPlatformButton.getId()) {
                scanner_runnable.setFilter(new String[]{Filters.RFID_PLATFORM});
            }
        }
        //scanner_runnable.setFilter(new String[]{Filters.RFID_PLATFORM, Filters.RFID_CAGE});
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

    @SuppressLint("StringFormatMatches")
    private void showAddDialog() {
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle(getString(R.string.confirm_cage, scannedCage))
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean wantToCloseDialog;
                        cageCode = input.getText().toString();
                        if (cageCode.equalsIgnoreCase(scannedCage)) {
                            recFishing.typedCageCode = cageCode.toUpperCase(Locale.ROOT);
                            recFishing.cageCode = recFishing.typedCageCode;
                            input.getShowSoftInputOnFocus();
                            wantToCloseDialog = true;
                        } else {
                            dialog.setTitle(getString(R.string.wrong_typing_cage, scannedCage));
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            dialog.dismiss();

                        input.setText("");

                        /*//Dismiss once everything is OK.
                        dialog.dismiss();*/
                    }
                });
            }
        });
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);


        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm_cage, scannedCage));
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set up the buttons
        AlertDialog dialog = builder.setTitle(getString(R.string.confirm_cage, scannedCage))
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean wantToCloseDialog;
                        cageCode = input.getText().toString();
                        if (cageCode.equalsIgnoreCase(scannedCage)) {
                            recFishing.typedCageCode = cageCode.toUpperCase(Locale.ROOT);
                            recFishing.cageCode = recFishing.typedCageCode;
                            input.getShowSoftInputOnFocus();
                            wantToCloseDialog = true;
                        } else {
                            builder.setMessage("Wrong typing");
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog)
                            dialog.dismiss();
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);*/
    }

    private void initControlsFromState() {
        FishingRecord hvst = recFishing;

        tvPlatformRFID.setText(hvst.platformRFID != null ? hvst.platformRFID.substring(14) : null);
        tvCageRFID.setText(hvst.cageRFID != null ? hvst.cageRFID.substring(14) : null);
    }

    private void updateState() {
        if (recFishing.cageRFID != null) {
            CageDetails cage = db.cageDetailsDAO().getByRFId(recFishing.cageRFID);
            if (cage != null) {
                recFishing.speciesName = cage.species; //TODO: compare with Requested Species
                recFishing.pathologist = cage.ichthyopathologist;
                recFishing.lastFed = cage.lastFed;
                recFishing.hlot = cage.hlot;
            } else {
                // TODO:: add alert, no cage corresponding to RFID found in local DB!!
            }
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recFishing.platformRFID)) {
                sb.append(String.format("\n%s is missing", "'Platform tag'"));
            }

            if (Strings.isEmptyOrWhitespace(recFishing.cageRFID)) {
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
    private void stopScanner() {
//        if(singleShot_runnable !=null) {
//            mScanHandler.removeCallbacks(singleShot_runnable);
//            singleShot_runnable.stopReading();
//        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<FishingCageActivity> mActivity;

        public ScanHandler(FishingCageActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!epcList.isEmpty()) {
                            for (CharSequence epcCharSeq : epcList) {
                                String epc = epcCharSeq.toString();
                                String tag = epc.substring(11);
                                String label = tag.substring(3);
                                if (tag.startsWith(Filters.RFID_PLATFORM)) {
                                    tvPlatformRFID.setText(label);
                                    recFishing.platformRFID = epc;
                                } else if (tag.startsWith(Filters.RFID_CAGE)) {
                                    tvCageRFID.setText(label);
                                    recFishing.cageRFID = epc;
                                    CageDetails cage = db.cageDetailsDAO().getByRFId(epc);
                                    if (cage != null) {
                                        if (!recFishing.cageCode.equals(cage.cageCode)) {
                                            scannedCage = cage.cageCode;
                                            FragmentManager fm = getSupportFragmentManager();
                                            confirmCageSelectionDlg.setMessage(getString(R.string.proceed_without_cage, scannedCage));
                                            confirmCageSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                                        }
                                    }
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