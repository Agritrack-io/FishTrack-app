package io.agritrack.philosofish.fish.ui.quality.receipt;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.common.InputFilterMinMax;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.QualityRecord;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class ReceiptQualityMoreInfo3Activity extends AppCompatActivity {

    private final InputFilter filter = new InputFilter() {
        final int maxDigitsBeforeDecimalPoint = 3;
        final int maxDigitsAfterDecimalPoint = 2;

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder(dest);
            builder.replace(dstart, dend, source
                    .subSequence(start, end).toString());
            if (!builder.toString().matches(
                    "(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

            )) {
                if (source.length() == 0)
                    return dest.subSequence(dstart, dend);
                return "";
            }

            return null;

        }
    };
    private MobileDB db;
    private EditText etNoHematoma, etLightHematoma, etHeavyHematoma, etPink, etDark, etWhite, etUncolored, etHematomas, etMucus, etProblematicFish;
    private String evaluation;
    private RadioGroup rgTotalEvaluation;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_more_info3);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityMoreInfo3);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        etNoHematoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etNoHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etNoHematoma.getText().toString().trim());
                int number2 = etLightHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etLightHematoma.getText().toString().trim());
                int number3 = etHeavyHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etHeavyHematoma.getText().toString().trim());
                if (number1 + number2 + number3 != 100 && !Strings.isEmptyOrWhitespace(etLightHematoma.getText().toString()) && !Strings.isEmptyOrWhitespace(etHeavyHematoma.getText().toString())) {
                    etNoHematoma.setBackgroundColor(Color.RED);
                    etLightHematoma.setBackgroundColor(Color.RED);
                    etHeavyHematoma.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etNoHematoma.setBackgroundColor(Color.WHITE);
                    etLightHematoma.setBackgroundColor(Color.WHITE);
                    etHeavyHematoma.setBackgroundColor(Color.WHITE);
                }
                etLightHematoma.requestFocus();
            }
        });

        etLightHematoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etNoHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etNoHematoma.getText().toString().trim());
                int number2 = etLightHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etLightHematoma.getText().toString().trim());
                int number3 = etHeavyHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etHeavyHematoma.getText().toString().trim());
                if (number1 + number2 + number3 != 100 && !Strings.isEmptyOrWhitespace(etNoHematoma.getText().toString()) && !Strings.isEmptyOrWhitespace(etHeavyHematoma.getText().toString())) {
                    etNoHematoma.setBackgroundColor(Color.RED);
                    etLightHematoma.setBackgroundColor(Color.RED);
                    etHeavyHematoma.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etNoHematoma.setBackgroundColor(Color.WHITE);
                    etLightHematoma.setBackgroundColor(Color.WHITE);
                    etHeavyHematoma.setBackgroundColor(Color.WHITE);
                }
                etHeavyHematoma.requestFocus();
            }
        });

        etHeavyHematoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etNoHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etNoHematoma.getText().toString().trim());
                int number2 = etLightHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etLightHematoma.getText().toString().trim());
                int number3 = etHeavyHematoma.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etHeavyHematoma.getText().toString().trim());
                if (number1 + number2 + number3 != 100 && !Strings.isEmptyOrWhitespace(etNoHematoma.getText().toString()) && !Strings.isEmptyOrWhitespace(etLightHematoma.getText().toString())) {
                    etNoHematoma.setBackgroundColor(Color.RED);
                    etLightHematoma.setBackgroundColor(Color.RED);
                    etHeavyHematoma.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etNoHematoma.setBackgroundColor(Color.WHITE);
                    etLightHematoma.setBackgroundColor(Color.WHITE);
                    etHeavyHematoma.setBackgroundColor(Color.WHITE);
                }
                etPink.requestFocus();
            }
        });

        etPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etPink.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etPink.getText().toString().trim());
                int number2 = etDark.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etDark.getText().toString().trim());
                int number3 = etWhite.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etWhite.getText().toString().trim());
                int number4 = etUncolored.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etUncolored.getText().toString().trim());
                if (number1 + number2 + number3 + number4 != 100 && !Strings.isEmptyOrWhitespace(etDark.getText().toString()) && !Strings.isEmptyOrWhitespace(etWhite.getText().toString()) && !Strings.isEmptyOrWhitespace(etUncolored.getText().toString())) {
                    etPink.setBackgroundColor(Color.RED);
                    etDark.setBackgroundColor(Color.RED);
                    etWhite.setBackgroundColor(Color.RED);
                    etUncolored.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etPink.setBackgroundColor(Color.WHITE);
                    etDark.setBackgroundColor(Color.WHITE);
                    etWhite.setBackgroundColor(Color.WHITE);
                    etUncolored.setBackgroundColor(Color.WHITE);
                }
                etDark.requestFocus();
            }
        });

        etDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etPink.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etPink.getText().toString().trim());
                int number2 = etDark.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etDark.getText().toString().trim());
                int number3 = etWhite.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etWhite.getText().toString().trim());
                int number4 = etUncolored.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etUncolored.getText().toString().trim());
                if (number1 + number2 + number3 + number4 != 100 && !Strings.isEmptyOrWhitespace(etPink.getText().toString()) && !Strings.isEmptyOrWhitespace(etWhite.getText().toString()) && !Strings.isEmptyOrWhitespace(etUncolored.getText().toString())) {
                    etPink.setBackgroundColor(Color.RED);
                    etDark.setBackgroundColor(Color.RED);
                    etWhite.setBackgroundColor(Color.RED);
                    etUncolored.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etPink.setBackgroundColor(Color.WHITE);
                    etDark.setBackgroundColor(Color.WHITE);
                    etWhite.setBackgroundColor(Color.WHITE);
                    etUncolored.setBackgroundColor(Color.WHITE);
                }
                etWhite.requestFocus();
            }
        });

        etWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etPink.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etPink.getText().toString().trim());
                int number2 = etDark.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etDark.getText().toString().trim());
                int number3 = etWhite.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etWhite.getText().toString().trim());
                int number4 = etUncolored.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etUncolored.getText().toString().trim());
                if (number1 + number2 + number3 + number4 != 100 && !Strings.isEmptyOrWhitespace(etPink.getText().toString()) && !Strings.isEmptyOrWhitespace(etDark.getText().toString()) && !Strings.isEmptyOrWhitespace(etUncolored.getText().toString())) {
                    etPink.setBackgroundColor(Color.RED);
                    etDark.setBackgroundColor(Color.RED);
                    etWhite.setBackgroundColor(Color.RED);
                    etUncolored.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etPink.setBackgroundColor(Color.WHITE);
                    etDark.setBackgroundColor(Color.WHITE);
                    etWhite.setBackgroundColor(Color.WHITE);
                    etUncolored.setBackgroundColor(Color.WHITE);
                }
                etUncolored.requestFocus();
            }
        });

        etUncolored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etPink.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etPink.getText().toString().trim());
                int number2 = etDark.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etDark.getText().toString().trim());
                int number3 = etWhite.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etWhite.getText().toString().trim());
                int number4 = etUncolored.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etUncolored.getText().toString().trim());
                if (number1 + number2 + number3 + number4 != 100 && !Strings.isEmptyOrWhitespace(etPink.getText().toString()) && !Strings.isEmptyOrWhitespace(etDark.getText().toString()) && !Strings.isEmptyOrWhitespace(etWhite.getText().toString())) {
                    etPink.setBackgroundColor(Color.RED);
                    etDark.setBackgroundColor(Color.RED);
                    etWhite.setBackgroundColor(Color.RED);
                    etUncolored.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo3Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etPink.setBackgroundColor(Color.WHITE);
                    etDark.setBackgroundColor(Color.WHITE);
                    etWhite.setBackgroundColor(Color.WHITE);
                    etUncolored.setBackgroundColor(Color.WHITE);
                }
                etHematomas.requestFocus();
            }
        });

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
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
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

    public void oneRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        int radioButtonID = rgTotalEvaluation.getCheckedRadioButtonId();
        View radioButton = rgTotalEvaluation.findViewById(radioButtonID);
        int idx = rgTotalEvaluation.indexOfChild(radioButton);

        switch (view.getId()) {
            case R.id.simpleRadioButton1:
                if (checked)
                    evaluation = String.valueOf(idx + 1);
                break;
            case R.id.simpleRadioButton2:
                if (checked)
                    evaluation = String.valueOf(idx + 1);
                break;
            case R.id.simpleRadioButton3:
                if (checked)
                    evaluation = String.valueOf(idx + 1);
                break;
            case R.id.simpleRadioButton4:
                if (checked)
                    evaluation = String.valueOf(idx + 1);
                break;
            case R.id.simpleRadioButton5:
                if (checked)
                    evaluation = String.valueOf(idx + 1);
        }
    }

    private void assignCtrlVars() {
        etNoHematoma = findViewById(R.id.etNoHematoma);
        etNoHematoma.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etNoHematoma.setText("0");
        etLightHematoma = findViewById(R.id.etLightHematoma);
        etLightHematoma.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etLightHematoma.setText("0");
        etHeavyHematoma = findViewById(R.id.etHeavyHematoma);
        etHeavyHematoma.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHeavyHematoma.setText("0");
        etPink = findViewById(R.id.etPink);
        etPink.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etPink.setText("0");
        etDark = findViewById(R.id.etDark);
        etDark.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etDark.setText("0");
        etWhite = findViewById(R.id.etWhite);
        etWhite.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etWhite.setText("0");
        etUncolored = findViewById(R.id.etUncolored);
        etUncolored.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etUncolored.setText("0");
        etHematomas = findViewById(R.id.etHematomas);
        etHematomas.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHematomas.setText("0");
        etMucus = findViewById(R.id.etMucus);
        etMucus.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etMucus.setText("0");
        etProblematicFish = findViewById(R.id.etProblematicFish);
        etProblematicFish.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etProblematicFish.setText("0");
        rgTotalEvaluation = findViewById(R.id.rgTotalEvaluation);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (qualityRecord.noHematoma != null) {
            etNoHematoma.setText(String.valueOf(qualityRecord.noHematoma));
        }
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
        if (qualityRecord.selectedRgId > -1) {
            rgTotalEvaluation.check(qualityRecord.selectedRgId);
        }
    }

    @SuppressLint("ResourceType")
    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etNoHematoma.getText() != null && !Strings.isEmptyOrWhitespace(etNoHematoma.getText().toString())) {
            qualityRecord.noHematoma = Integer.valueOf(etNoHematoma.getText().toString());
        }

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

        if (rgTotalEvaluation.getCheckedRadioButtonId() > 0) {
            int radioButtonID = rgTotalEvaluation.getCheckedRadioButtonId();
            View radioButton = rgTotalEvaluation.findViewById(radioButtonID);
            int idx = rgTotalEvaluation.indexOfChild(radioButton);
            qualityRecord.evaluation = String.valueOf(idx + 1);
            qualityRecord.selectedRgId = rgTotalEvaluation.getCheckedRadioButtonId();
        }

        GlobalState.commitQuality(db, Boolean.FALSE);

        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recQuality.noHematoma == null || GlobalState.recQuality.lightHematoma == null || GlobalState.recQuality.heavyHematoma == null || GlobalState.recQuality.pink == null || GlobalState.recQuality.dark == null || GlobalState.recQuality.white == null || GlobalState.recQuality.uncolored == null || GlobalState.recQuality.hematomas == null || GlobalState.recQuality.mucus == null || GlobalState.recQuality.problematicFish == null) {
                sb.append(String.format("\n%s is missing", "'Some percentages fields'"));
            }
            if (Strings.isEmptyOrWhitespace(GlobalState.recQuality.evaluation)) {
                sb.append(String.format("\n%s is missing", "'Overall evaluation'"));
            }
        }

        return sb.toString();
    }
}