package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class ReceiptQualityMoreInfoActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

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
        etEliminationFood = findViewById(R.id.etEliminationFood);
        etEliminationSperm = findViewById(R.id.etEliminationSperm);
        etParasites = findViewById(R.id.etParasites);
        etPeeling = findViewById(R.id.etPeeling);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        /*if (!Strings.isEmptyOrWhitespace(prcTx.remarks)) {
            mtvRemarks.setText(prcTx.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(prcTx.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }*/
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etRigorMortis.getText() != null && !Strings.isEmptyOrWhitespace(etRigorMortis.getText().toString())) {
            qualityRecord.rigorMortis = Double.valueOf(etRigorMortis.getText().toString());
        }

        if (etEliminationFood.getText() != null && !Strings.isEmptyOrWhitespace(etEliminationFood.getText().toString())) {
            qualityRecord.eliminationFood = Double.valueOf(etEliminationFood.getText().toString());
        }

        if (etEliminationSperm.getText() != null && !Strings.isEmptyOrWhitespace(etEliminationSperm.getText().toString())) {
            qualityRecord.eliminationSperm = Double.valueOf(etEliminationSperm.getText().toString());
        }

        if (etParasites.getText() != null && !Strings.isEmptyOrWhitespace(etParasites.getText().toString())) {
            qualityRecord.parasites = Double.valueOf(etParasites.getText().toString());
        }

        if (etPeeling.getText() != null && !Strings.isEmptyOrWhitespace(etPeeling.getText().toString())) {
            qualityRecord.peeling = Double.valueOf(etPeeling.getText().toString());
        }

        qualityRecord.binCondition = selectedBinCondition;
        qualityRecord.iceCondition = selectedIceCondition;

        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        /*if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.pLot)) {
                sb.append(String.format("\n%s is missing", "'LOT'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.fishCondition)) {
                sb.append(String.format("\n%s is missing", "'Fish condition'"));
            }
        }*/

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