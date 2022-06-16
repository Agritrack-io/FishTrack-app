package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class FishingStartActivity extends AppCompatActivity {

    private MobileDB db;
    private Spinner tvHarvestSpinner, speciesSpinner;
    private EditText etQty;
    private TextView tvCageName, tvAverageWeight, tvNotes, tvHarvest, tvFishType, tvRequestedQuantity;
    private YesNoDialogFragment confirmDeleteFishingDlg;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private boolean proceed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingStartActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingStartActivity.this);
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

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            if (!proceed){
                FragmentManager fm = getSupportFragmentManager();
                confirmDeleteFishingDlg.showNow(fm, getString(R.string.confirm_selection));
            }
            if (IsDemo) {
                db.fishingTransactionDAO().deleteAll();
            }
        });
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
        tvCageName = findViewById(R.id.tvCageName);
        tvAverageWeight = findViewById(R.id.tvAverageWeight);
        tvNotes = findViewById(R.id.tvNotes);
        tvHarvest = findViewById(R.id.tvHarvest);
        tvFishType = findViewById(R.id.tvFishType);
        tvRequestedQuantity = findViewById(R.id.tvRequestedQuantity);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {

        FishingRecord hvst = recFishing;

        if (!Strings.isEmptyOrWhitespace(hvst.requesterName)) {
            tvHarvest.setText(hvst.requesterName);
        }

        if (!Strings.isEmptyOrWhitespace(hvst.speciesName)) {
            tvFishType.setText(hvst.speciesName);
        }

        if (hvst.reqWeight!=null) {
            tvRequestedQuantity.setText(hvst.reqWeight.toString());
        }

        if (!Strings.isEmptyOrWhitespace(hvst.cageCode)) {
            tvCageName.setText(hvst.cageCode);
        }

        if (hvst.averageWeight!=null) {
            tvAverageWeight.setText(hvst.averageWeight.toString());
        }

        if (!Strings.isEmptyOrWhitespace(hvst.notes)) {
            tvNotes.setText(hvst.notes);
        }
        //harvestSpinner.setSelection(arrayAdapter.getPosition("Category 2"));
    }

    private FishingRecord updateState() {
        FishingRecord fishingRecord = recFishing;


        if (tvCageName.getText() != null) {
            fishingRecord.cageCode = tvCageName.getText().toString();
        }

        if (tvAverageWeight.getText() != null && !Strings.isEmptyOrWhitespace(tvAverageWeight.getText().toString())) {
            fishingRecord.averageWeight = Double.valueOf(tvAverageWeight.getText().toString());
        }
        //fishingRecord.reqWeight = etQty.getText() != null ? Double.valueOf(etQty.getText().toString()).intValue() + "" : "0";

        GlobalState.commitFishing(db, Boolean.FALSE);

        return fishingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        /*if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.requesterName)) {
                sb.append(String.format("\n%s is missing", "'Harvest initiator'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.speciesName)) {
                sb.append(String.format("\n%s is missing", "'Fish type'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.reqWeight)) {
                sb.append(String.format("\n%s is missing", "'Requested quantity'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.cageCode)) {
                sb.append(String.format("\n%s is missing", "'Cage code'"));
            }
        }*/
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}