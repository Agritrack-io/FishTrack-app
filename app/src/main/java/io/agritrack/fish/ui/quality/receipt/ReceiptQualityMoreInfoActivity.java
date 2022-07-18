package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.common.InputFilterMinMax;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class ReceiptQualityMoreInfoActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;
    private ToggleGroup tgBinCondition;
    private String selectedBinCondition;
    private ToggleGroup tgIceCondition;
    private String selectedIceCondition;
    private EditText etRigorMortis, etEliminationFood, etEliminationSperm, etParasites, etPeeling;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_more_info);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityMoreInfo);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityMoreInfoActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityMoreInfo2);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfo2Activity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityInfo);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityInfoActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgBinCondition = findViewById(R.id.tgBinCondition);
        tgBinCondition.setOnCheckedChangeListener(this);
        tgIceCondition = findViewById(R.id.tgIceCondition);
        tgIceCondition.setOnCheckedChangeListener(this);
        etRigorMortis = findViewById(R.id.etRigorMortis);
        etRigorMortis.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEliminationFood = findViewById(R.id.etEliminationFood);
        etEliminationFood.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEliminationSperm = findViewById(R.id.etEliminationSperm);
        etEliminationSperm.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etParasites = findViewById(R.id.etParasites);
        etParasites.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etPeeling = findViewById(R.id.etPeeling);
        etPeeling.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if ("GOOD".equalsIgnoreCase(qualityRecord.binCondition)) {
            tgBinCondition.check(R.id.tbGoodBin);
        } else if ("ACCEPTABLE".equalsIgnoreCase(qualityRecord.binCondition)) {
            tgBinCondition.check(R.id.tbMediumBin);
        } else if ("NOT ACCEPTABLE".equalsIgnoreCase(qualityRecord.binCondition)) {
            tgBinCondition.check(R.id.tbBadBin);
        }

        if ("GOOD".equalsIgnoreCase(qualityRecord.iceCondition)) {
            tgIceCondition.check(R.id.tbGoodIce);
        } else if ("ACCEPTABLE".equalsIgnoreCase(qualityRecord.iceCondition)) {
            tgIceCondition.check(R.id.tbMediumIce);
        } else if ("NOT ACCEPTABLE".equalsIgnoreCase(qualityRecord.iceCondition)) {
            tgIceCondition.check(R.id.tbBadIce);
        }
        if (qualityRecord.rigorMortis != null) {
            etRigorMortis.setText(String.valueOf(qualityRecord.rigorMortis));
        }
        if (qualityRecord.eliminationFood != null) {
            etEliminationFood.setText(String.valueOf(qualityRecord.eliminationFood));
        }
        if (qualityRecord.eliminationSperm != null) {
            etEliminationSperm.setText(String.valueOf(qualityRecord.eliminationSperm));
        }
        if (qualityRecord.parasites != null) {
            etParasites.setText(String.valueOf(qualityRecord.parasites));
        }
        if (qualityRecord.peeling != null) {
            etPeeling.setText(String.valueOf(qualityRecord.peeling));
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etRigorMortis.getText() != null && !Strings.isEmptyOrWhitespace(etRigorMortis.getText().toString())) {
            qualityRecord.rigorMortis = Integer.valueOf(etRigorMortis.getText().toString());
        }

        if (etEliminationFood.getText() != null && !Strings.isEmptyOrWhitespace(etEliminationFood.getText().toString())) {
            qualityRecord.eliminationFood = Integer.valueOf(etEliminationFood.getText().toString());
        }

        if (etEliminationSperm.getText() != null && !Strings.isEmptyOrWhitespace(etEliminationSperm.getText().toString())) {
            qualityRecord.eliminationSperm = Integer.valueOf(etEliminationSperm.getText().toString());
        }

        if (etParasites.getText() != null && !Strings.isEmptyOrWhitespace(etParasites.getText().toString())) {
            qualityRecord.parasites = Integer.valueOf(etParasites.getText().toString());
        }

        if (etPeeling.getText() != null && !Strings.isEmptyOrWhitespace(etPeeling.getText().toString())) {
            qualityRecord.peeling = Integer.valueOf(etPeeling.getText().toString());
        }

        qualityRecord.binCondition = selectedBinCondition;
        qualityRecord.iceCondition = selectedIceCondition;

        GlobalState.commitQuality(db, Boolean.FALSE);

        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recQuality.binCondition)) {
                sb.append(String.format("\n%s is missing", "'Bin condition'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recQuality.iceCondition)) {
                sb.append(String.format("\n%s is missing", "'Ice condition'"));
            }

            if (GlobalState.recQuality.rigorMortis == null || GlobalState.recQuality.eliminationFood == null || GlobalState.recQuality.eliminationSperm == null || GlobalState.recQuality.parasites == null || GlobalState.recQuality.peeling == null) {
                sb.append(String.format("\n%s is missing", "'Some percentages fields'"));
            }
        }

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbGoodBin) {
            selectedBinCondition = "GOOD";
        } else if (checkedId == R.id.tbMediumBin) {
            selectedBinCondition = "ACCEPTABLE";
        } else if (checkedId == R.id.tbBadBin) {
            selectedBinCondition = "NOT ACCEPTABLE";
        }

        if (checkedId == R.id.tbGoodIce) {
            selectedIceCondition = "GOOD";
        } else if (checkedId == R.id.tbMediumIce) {
            selectedIceCondition = "ACCEPTABLE";
        } else if (checkedId == R.id.tbBadIce) {
            selectedIceCondition = "NOT ACCEPTABLE";
        }
    }
}