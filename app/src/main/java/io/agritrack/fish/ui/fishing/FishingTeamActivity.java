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
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.CageDetails;
import io.agritrack.data.model.common.Employee;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.bo.GenericListModel;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.service.LocalPreferences;

public class FishingTeamActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private ListView lvFishingTeam;
    private List<GenericListModel> candidates;
    private ArrayAdapter<GenericListModel> candidatesAdapter;
    private ImageButton ivAddEmployee;
    private String memberName;
    private Button scanCageButton;
    private TextView tvCageRFID;
    private String cageCode = "", scannedCage;

    private YesNoDialogFragment confirmDeleteFishingDlg;
    private boolean proceed = false;
    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private YesNoDialogFragment confirmCageSelectionDlg;

    private String reasonOutOfSystemFishing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_team);

        if (getIntent() != null && reasonOutOfSystemFishing == null) {
            Bundle bundle = getIntent().getExtras();
            reasonOutOfSystemFishing = bundle != null ? bundle.getString("reason") : reasonOutOfSystemFishing;
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        assignCtrlVars();

        confirmDeleteFishingDlg = YesNoDialogFragment.instance();
        confirmDeleteFishingDlg.setMessage(getText(R.string.delete_fishing_tx));
        confirmDeleteFishingDlg.onConfirm(bundle -> {
            FishingTransaction openTx = db.fishingTransactionDAO().getMostRecentOpenTx(LocalPreferences.getLoggedInUser(""));
            db.fishingTransactionDAO().delete(openTx);
            proceed = true;
            moveToNextScreen();
        });
        confirmDeleteFishingDlg.onReject(bundle -> {
            proceed = true;
            moveToNextScreen();
        });

        confirmCageSelectionDlg = YesNoDialogFragment.instance();
        confirmCageSelectionDlg.onConfirm(bundle -> {
            showAddCageDialog();
        });
        confirmCageSelectionDlg.onReject(bundle -> {
            tvCageRFID.setText(null);
            recFishing.cageRFID = null;
        });

        // =================================
        // RFID scanning functionality
        scanCageButton.setOnClickListener(this::onClick);
        // =================================

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingTeam);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get main controls references
        this.lvFishingTeam = findViewById(R.id.lvFishingTeam);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvFishingTeam.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Employee> teamCandidates = db.employeeDAO().getBySite(LocalPreferences.getCurrentSiteId());
        if (teamCandidates != null && !teamCandidates.isEmpty()) {
            this.candidates = teamCandidates.stream().map(x -> new GenericListModel(x.id, x.fullName())).collect(Collectors.toList());
            candidatesAdapter = new ArrayAdapter<GenericListModel>(this, R.layout.simple_list_checked_item_1, candidates) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvFishingTeam.setAdapter(candidatesAdapter);
            this.lvFishingTeam.setOnItemClickListener(this);
        }

        ivAddEmployee.setOnClickListener(view -> {
            showAddDialog();
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        confirmDeleteFishingDlg = YesNoDialogFragment.instance();
        confirmDeleteFishingDlg.setMessage(getText(R.string.delete_fishing_tx));
        confirmDeleteFishingDlg.onConfirm(bundle -> {
            FishingTransaction openTx = db.fishingTransactionDAO().getMostRecentOpenTx(LocalPreferences.getLoggedInUser(""));
            db.fishingTransactionDAO().delete(openTx);
            proceed = true;
            moveToNextScreen();
        });
        confirmDeleteFishingDlg.onReject(bundle -> {
            proceed = true;
            moveToNextScreen();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingTeamActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingTeamActivity.this);
            infoDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    private void moveToNextScreen() {
        if (proceed) {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        }
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
        this.stopScanner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCage);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            if (recFishing.outOfSystemFishing || !proceed) {
                FragmentManager fm = getSupportFragmentManager();
                confirmDeleteFishingDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                startActivity(i);
            }
            if (IsDemo) {
                db.fishingTransactionDAO().deleteAll();
            }
        });
    }

    private void assignCtrlVars() {
        ivAddEmployee = (ImageButton) findViewById(R.id.ivAddEmployee);
        scanCageButton = findViewById(R.id.btnScanCage);
        tvCageRFID = findViewById(R.id.tvCageName);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {

        if (recFishing.fishingTeam != null && !recFishing.fishingTeam.isEmpty()) {
            int[] matchingIndices = IntStream.range(0, this.candidates.size())
                    .filter(i -> recFishing.fishingTeam.contains(this.candidates.get(i).toString()))
                    .toArray();

            int sz = recFishing.fishingTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i : matchingIndices) {
                this.lvFishingTeam.setItemChecked(i, Boolean.TRUE);
            }

            //Get reference of selected Team Count textView
            TextView tvEmployeesCount = findViewById(R.id.tvEmployeesCount);
            tvEmployeesCount.setText(String.valueOf(sz));
        }

        if (!Strings.isEmptyOrWhitespace(recFishing.reasonOutOfSystemFishing)) {
            reasonOutOfSystemFishing = recFishing.reasonOutOfSystemFishing;
        }

        tvCageRFID.setText(recFishing.cageRFID != null ? recFishing.cageRFID.substring(14) : null);
    }

    private void updateState() {
        // reset the list of selected Indexes.
        recFishing.fishingTeam = new ArrayList<>();
        SparseBooleanArray sp = this.lvFishingTeam.getCheckedItemPositions();
        for (int idx = 0; idx < sp.size(); idx++) {
            if (sp.valueAt(idx)) {
                recFishing.fishingTeam.add(((GenericListModel) this.lvFishingTeam.getAdapter().getItem(sp.keyAt(idx))).toString());
            }
        }

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

        recFishing.reasonOutOfSystemFishing = reasonOutOfSystemFishing;

        GlobalState.commitFishing(db, Boolean.FALSE);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recFishing.fishingTeam == null || recFishing.fishingTeam.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Team members'"));
            }

            if (Strings.isEmptyOrWhitespace(recFishing.cageRFID)) {
                sb.append(String.format("\n%s is missing", "'Cage tag'"));
            }
        }

        return sb.toString();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type member's name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                memberName = input.getText().toString();
                candidatesAdapter.add(new GenericListModel(memberName, Boolean.TRUE));
                candidatesAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @SuppressLint("StringFormatMatches")
    private void showAddCageDialog() {
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvFishingTeam.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        //Get reference of selected Team Count textView
        TextView tvEmployeesCount = findViewById(R.id.tvEmployeesCount);
        tvEmployeesCount.setText(String.valueOf(this.lvFishingTeam.getCheckedItemCount()));
    }

    protected void onClick(View view) {
//        scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.LowEnergy();
        scanner_runnable.setFilter(Filters.RFID_CAGE);
        // NOTE: if the following lines are moved outside the If{view!=null} statement,
        // a NullPointerException will be thrown when trigger is pressed. The App crashes!!!
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (scanner_runnable != null) {
            mScanHandler.removeCallbacks(scanner_runnable);
            scanner_runnable.stopReading();
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<FishingTeamActivity> mActivity;

        public ScanHandler(FishingTeamActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr) && epcStr != null) {
                            String tag = epcStr.substring(11);
                            String label = tag.substring(3);
                            tvCageRFID.setText(label);
                            recFishing.cageRFID = epcStr;
                            CageDetails cage = db.cageDetailsDAO().getByRFId(epcStr);
                            if (cage != null) {
                                if (!recFishing.cageCode.equals(cage.cageCode)) {
                                    scannedCage = cage.cageCode;
                                    FragmentManager fm = getSupportFragmentManager();
                                    confirmCageSelectionDlg.setMessage(getString(R.string.proceed_without_cage, scannedCage));
                                    confirmCageSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
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