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

public class PackageQualityMoreInfo2Activity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgSmellCondition;
    private String selectedSmellCondition;
    private EditText etShiny, etBlurred, etHealed, etBlind, etCoherent, etSoft, etSwollen;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_more_info2);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityMoreInfo2);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityMoreInfo2Activity.this);
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
                Intent i = new Intent(getApplicationContext(), PackageQualityMoreInfo3Activity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackageQualityMoreInfoActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSmellCondition = findViewById(R.id.tgSmellCondition);
        tgSmellCondition.setOnCheckedChangeListener(this);
        etShiny = findViewById(R.id.etShiny);
        etBlurred = findViewById(R.id.etBlurred);
        etHealed = findViewById(R.id.etHealed);
        etBlind = findViewById(R.id.etBlind);
        etCoherent = findViewById(R.id.etCoherent);
        etSoft = findViewById(R.id.etSoft);
        etSwollen = findViewById(R.id.etSwollen);
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

        if (etShiny.getText() != null && !Strings.isEmptyOrWhitespace(etShiny.getText().toString())) {
            qualityRecord.shiny = Double.valueOf(etShiny.getText().toString());
        }

        if (etBlurred.getText() != null && !Strings.isEmptyOrWhitespace(etBlurred.getText().toString())) {
            qualityRecord.blurred = Double.valueOf(etBlurred.getText().toString());
        }

        if (etHealed.getText() != null && !Strings.isEmptyOrWhitespace(etHealed.getText().toString())) {
            qualityRecord.healed = Double.valueOf(etHealed.getText().toString());
        }

        if (etBlind.getText() != null && !Strings.isEmptyOrWhitespace(etBlind.getText().toString())) {
            qualityRecord.blindEyes = Double.valueOf(etBlind.getText().toString());
        }

        if (etCoherent.getText() != null && !Strings.isEmptyOrWhitespace(etCoherent.getText().toString())) {
            qualityRecord.coherent = Double.valueOf(etCoherent.getText().toString());
        }

        if (etSoft.getText() != null && !Strings.isEmptyOrWhitespace(etSoft.getText().toString())) {
            qualityRecord.soft = Double.valueOf(etSoft.getText().toString());
        }

        if (etSwollen.getText() != null && !Strings.isEmptyOrWhitespace(etSwollen.getText().toString())) {
            qualityRecord.swollen = Double.valueOf(etSwollen.getText().toString());
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