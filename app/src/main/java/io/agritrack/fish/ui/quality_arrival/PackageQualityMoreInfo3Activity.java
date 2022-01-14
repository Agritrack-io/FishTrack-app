package io.agritrack.fish.ui.quality_arrival;

import static io.agritrack.FishTrackApplication.IsDemo;
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
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.service.LocalPreferences;

public class PackageQualityMoreInfo3Activity extends AppCompatActivity {

    private EditText etLightHematoma, etHeavyHematoma, etPink, etDark, etWhite, etUncolored, etHematomas, etMucus, etProblematicFish;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_more_info3);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityMoreInfo3);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityMoreInfo3Activity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackageQualityConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo2);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackageQualityMoreInfo2Activity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etLightHematoma = findViewById(R.id.etLightHematoma);
        etHeavyHematoma = findViewById(R.id.etHeavyHematoma);
        etPink = findViewById(R.id.etPink);
        etDark = findViewById(R.id.etDark);
        etWhite = findViewById(R.id.etWhite);
        etUncolored = findViewById(R.id.etUncolored);
        etHematomas = findViewById(R.id.etHematomas);
        etMucus = findViewById(R.id.etMucus);
        etProblematicFish = findViewById(R.id.etProblematicFish);
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

        if (etLightHematoma.getText() != null && !Strings.isEmptyOrWhitespace(etLightHematoma.getText().toString())) {
            qualityRecord.lightHematoma = Double.valueOf(etLightHematoma.getText().toString());
        }

        if (etHeavyHematoma.getText() != null && !Strings.isEmptyOrWhitespace(etHeavyHematoma.getText().toString())) {
            qualityRecord.heavyHematoma = Double.valueOf(etHeavyHematoma.getText().toString());
        }

        if (etPink.getText() != null && !Strings.isEmptyOrWhitespace(etPink.getText().toString())) {
            qualityRecord.pink = Double.valueOf(etPink.getText().toString());
        }

        if (etDark.getText() != null && !Strings.isEmptyOrWhitespace(etDark.getText().toString())) {
            qualityRecord.dark = Double.valueOf(etDark.getText().toString());
        }

        if (etWhite.getText() != null && !Strings.isEmptyOrWhitespace(etWhite.getText().toString())) {
            qualityRecord.white = Double.valueOf(etWhite.getText().toString());
        }

        if (etUncolored.getText() != null && !Strings.isEmptyOrWhitespace(etUncolored.getText().toString())) {
            qualityRecord.uncolored = Double.valueOf(etUncolored.getText().toString());
        }

        if (etHematomas.getText() != null && !Strings.isEmptyOrWhitespace(etHematomas.getText().toString())) {
            qualityRecord.hematomas = Double.valueOf(etHematomas.getText().toString());
        }

        if (etMucus.getText() != null && !Strings.isEmptyOrWhitespace(etMucus.getText().toString())) {
            qualityRecord.mucus = Double.valueOf(etMucus.getText().toString());
        }

        if (etProblematicFish.getText() != null && !Strings.isEmptyOrWhitespace(etProblematicFish.getText().toString())) {
            qualityRecord.problematicFish = Double.valueOf(etProblematicFish.getText().toString());
        }

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
}