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
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.service.LocalPreferences;

public class ReceiptQualityMoreInfo3Activity extends AppCompatActivity {

    private EditText etLightHematoma, etHeavyHematoma, etPink, etDark, etWhite, etUncolored, etHematomas, etMucus, etProblematicFish;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_more_info3);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityMoreInfo3);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityMoreInfo3Activity.this);
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
                Intent i = new Intent(getApplicationContext(), ReceiptQualityConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo2);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfo2Activity.class);
            startActivity(i);
        });
    }

    private InputFilter filter = new InputFilter() {
        final int maxDigitsBeforeDecimalPoint=3;
        final int maxDigitsAfterDecimalPoint=2;

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder(dest);
            builder.replace(dstart, dend, source
                    .subSequence(start, end).toString());
            if (!builder.toString().matches(
                    "(([1-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"

            )) {
                if(source.length()==0)
                    return dest.subSequence(dstart, dend);
                return "";
            }

            return null;

        }
    };

    private void assignCtrlVars() {
        etLightHematoma = findViewById(R.id.etLightHematoma);
        etLightHematoma.setFilters(new InputFilter[] { filter });
        etHeavyHematoma = findViewById(R.id.etHeavyHematoma);
        etHeavyHematoma.setFilters(new InputFilter[] { filter });
        etPink = findViewById(R.id.etPink);
        etPink.setFilters(new InputFilter[] { filter });
        etDark = findViewById(R.id.etDark);
        etDark.setFilters(new InputFilter[] { filter });
        etWhite = findViewById(R.id.etWhite);
        etWhite.setFilters(new InputFilter[] { filter });
        etUncolored = findViewById(R.id.etUncolored);
        etUncolored.setFilters(new InputFilter[] { filter });
        etHematomas = findViewById(R.id.etHematomas);
        etHematomas.setFilters(new InputFilter[] { filter });
        etMucus = findViewById(R.id.etMucus);
        etMucus.setFilters(new InputFilter[] { filter });
        etProblematicFish = findViewById(R.id.etProblematicFish);
        etProblematicFish.setFilters(new InputFilter[] { filter });
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (qualityRecord.lightHematoma != null) {
            etLightHematoma.setText(String.valueOf(qualityRecord.lightHematoma));
        }
        if (qualityRecord.heavyHematoma != null) {
            etHeavyHematoma.setText(String.valueOf(qualityRecord.heavyHematoma));
        }
        if (qualityRecord.pink != null) {
            etPink.setText(String.valueOf(qualityRecord.pink));
        }
        if (qualityRecord.dark != null) {
            etDark.setText(String.valueOf(qualityRecord.dark));
        }
        if (qualityRecord.white != null) {
            etWhite.setText(String.valueOf(qualityRecord.white));
        }
        if (qualityRecord.uncolored != null) {
            etUncolored.setText(String.valueOf(qualityRecord.uncolored));
        }
        if (qualityRecord.hematomas != null) {
            etHematomas.setText(String.valueOf(qualityRecord.hematomas));
        }
        if (qualityRecord.mucus != null) {
            etMucus.setText(String.valueOf(qualityRecord.mucus));
        }
        if (qualityRecord.problematicFish != null) {
            etProblematicFish.setText(String.valueOf(qualityRecord.problematicFish));
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etLightHematoma.getText() != null && !Strings.isEmptyOrWhitespace(etLightHematoma.getText().toString())) {
            qualityRecord.lightHematoma = Integer.valueOf(etLightHematoma.getText().toString());
        }

        if (etHeavyHematoma.getText() != null && !Strings.isEmptyOrWhitespace(etHeavyHematoma.getText().toString())) {
            qualityRecord.heavyHematoma = Integer.valueOf(etHeavyHematoma.getText().toString());
        }

        if (etPink.getText() != null && !Strings.isEmptyOrWhitespace(etPink.getText().toString())) {
            qualityRecord.pink = Integer.valueOf(etPink.getText().toString());
        }

        if (etDark.getText() != null && !Strings.isEmptyOrWhitespace(etDark.getText().toString())) {
            qualityRecord.dark = Integer.valueOf(etDark.getText().toString());
        }

        if (etWhite.getText() != null && !Strings.isEmptyOrWhitespace(etWhite.getText().toString())) {
            qualityRecord.white = Integer.valueOf(etWhite.getText().toString());
        }

        if (etUncolored.getText() != null && !Strings.isEmptyOrWhitespace(etUncolored.getText().toString())) {
            qualityRecord.uncolored = Integer.valueOf(etUncolored.getText().toString());
        }

        if (etHematomas.getText() != null && !Strings.isEmptyOrWhitespace(etHematomas.getText().toString())) {
            qualityRecord.hematomas = Integer.valueOf(etHematomas.getText().toString());
        }

        if (etMucus.getText() != null && !Strings.isEmptyOrWhitespace(etMucus.getText().toString())) {
            qualityRecord.mucus = Integer.valueOf(etMucus.getText().toString());
        }

        if (etProblematicFish.getText() != null && !Strings.isEmptyOrWhitespace(etProblematicFish.getText().toString())) {
            qualityRecord.problematicFish = Integer.valueOf(etProblematicFish.getText().toString());
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