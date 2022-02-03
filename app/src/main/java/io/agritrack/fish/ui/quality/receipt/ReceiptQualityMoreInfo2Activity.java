package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.common.InputFilterMinMax;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class ReceiptQualityMoreInfo2Activity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgSmellCondition;
    private String selectedSmellCondition;
    private EditText etShiny, etBlurred, etHealed, etBlind, etCoherent, etSoft, etSwollen;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_more_info2);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityMoreInfo2);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityMoreInfo2Activity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityMoreInfo3);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfo3Activity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfoActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSmellCondition = findViewById(R.id.tgSmellCondition);
        tgSmellCondition.setOnCheckedChangeListener(this);
        etShiny = findViewById(R.id.etShiny);
        etShiny.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etBlurred = findViewById(R.id.etBlurred);
        etBlurred.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHealed = findViewById(R.id.etHealed);
        etHealed.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etBlind = findViewById(R.id.etBlind);
        etBlind.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etCoherent = findViewById(R.id.etCoherent);
        etCoherent.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etSoft = findViewById(R.id.etSoft);
        etSoft.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etSwollen = findViewById(R.id.etSwollen);
        etSwollen.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if ("FRESH-METALLIC".equalsIgnoreCase(qualityRecord.smellCondition)) {
            tgSmellCondition.check(R.id.tbFreshSmell);
        } else if ("NO SMELL".equalsIgnoreCase(qualityRecord.smellCondition)) {
            tgSmellCondition.check(R.id.tbNoSmell);
        } else if ("LIGHT BAD".equalsIgnoreCase(qualityRecord.smellCondition)) {
            tgSmellCondition.check(R.id.tbLightBadSmell);
        } else if ("HEAVY BAD".equalsIgnoreCase(qualityRecord.smellCondition)) {
            tgSmellCondition.check(R.id.tbHeavyBadSmell);
        }
        if (qualityRecord.shiny != null) {
            etShiny.setText(String.valueOf(qualityRecord.shiny));
        }
        if (qualityRecord.blurred != null) {
            etBlurred.setText(String.valueOf(qualityRecord.blurred));
        }
        if (qualityRecord.healed != null) {
            etHealed.setText(String.valueOf(qualityRecord.healed));
        }
        if (qualityRecord.blindEyes != null) {
            etBlind.setText(String.valueOf(qualityRecord.blindEyes));
        }
        if (qualityRecord.coherent != null) {
            etCoherent.setText(String.valueOf(qualityRecord.coherent));
        }
        if (qualityRecord.soft != null) {
            etSoft.setText(String.valueOf(qualityRecord.soft));
        }
        if (qualityRecord.swollen != null) {
            etSwollen.setText(String.valueOf(qualityRecord.swollen));
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etShiny.getText() != null && !Strings.isEmptyOrWhitespace(etShiny.getText().toString())) {
            qualityRecord.shiny = Integer.valueOf(etShiny.getText().toString());
        }

        if (etBlurred.getText() != null && !Strings.isEmptyOrWhitespace(etBlurred.getText().toString())) {
            qualityRecord.blurred = Integer.valueOf(etBlurred.getText().toString());
        }

        if (etHealed.getText() != null && !Strings.isEmptyOrWhitespace(etHealed.getText().toString())) {
            qualityRecord.healed = Integer.valueOf(etHealed.getText().toString());
        }

        if (etBlind.getText() != null && !Strings.isEmptyOrWhitespace(etBlind.getText().toString())) {
            qualityRecord.blindEyes = Integer.valueOf(etBlind.getText().toString());
        }

        if (etCoherent.getText() != null && !Strings.isEmptyOrWhitespace(etCoherent.getText().toString())) {
            qualityRecord.coherent = Integer.valueOf(etCoherent.getText().toString());
        }

        if (etSoft.getText() != null && !Strings.isEmptyOrWhitespace(etSoft.getText().toString())) {
            qualityRecord.soft = Integer.valueOf(etSoft.getText().toString());
        }

        if (etSwollen.getText() != null && !Strings.isEmptyOrWhitespace(etSwollen.getText().toString())) {
            qualityRecord.swollen = Integer.valueOf(etSwollen.getText().toString());
        }

        qualityRecord.smellCondition = selectedSmellCondition;

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
        if (checkedId == R.id.tbFreshSmell) {
            selectedSmellCondition = "FRESH-METALLIC";
        } else if (checkedId == R.id.tbNoSmell) {
            selectedSmellCondition = "NO SMELL";
        } else if (checkedId == R.id.tbLightBadSmell) {
            selectedSmellCondition = "LIGHT BAD";
        } else if (checkedId == R.id.tbHeavyBadSmell) {
            selectedSmellCondition = "HEAVY BAD";
        }
    }
}