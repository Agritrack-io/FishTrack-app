package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import io.agritrack.fish.ui.quality.packaging.PackageQualityConfirmActivity;
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

        etCoherent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etCoherent.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etCoherent.getText().toString().trim());
                int number2 = etSoft.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSoft.getText().toString().trim());
                int number3 = etSwollen.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSwollen.getText().toString().trim());
                if (number1+number2+number3!=100 && !Strings.isEmptyOrWhitespace(etSoft.getText().toString()) && !Strings.isEmptyOrWhitespace(etSwollen.getText().toString())){
                    etCoherent.setBackgroundColor(Color.RED);
                    etSoft.setBackgroundColor(Color.RED);
                    etSwollen.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo2Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etCoherent.setBackgroundColor(Color.WHITE);
                    etSoft.setBackgroundColor(Color.WHITE);
                    etSwollen.setBackgroundColor(Color.WHITE);
                }
                etSoft.requestFocus();
            }
        });

        etSoft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etCoherent.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etCoherent.getText().toString().trim());
                int number2 = etSoft.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSoft.getText().toString().trim());
                int number3 = etSwollen.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSwollen.getText().toString().trim());
                if (number1+number2+number3!=100 && !Strings.isEmptyOrWhitespace(etCoherent.getText().toString()) && !Strings.isEmptyOrWhitespace(etSwollen.getText().toString())){
                    etCoherent.setBackgroundColor(Color.RED);
                    etSoft.setBackgroundColor(Color.RED);
                    etSwollen.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo2Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etCoherent.setBackgroundColor(Color.WHITE);
                    etSoft.setBackgroundColor(Color.WHITE);
                    etSwollen.setBackgroundColor(Color.WHITE);
                }
                etSwollen.requestFocus();
            }
        });

        etSwollen.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int number1 = etCoherent.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etCoherent.getText().toString().trim());
                int number2 = etSoft.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSoft.getText().toString().trim());
                int number3 = etSwollen.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etSwollen.getText().toString().trim());
                if (number1+number2+number3!=100 && !Strings.isEmptyOrWhitespace(etCoherent.getText().toString()) && !Strings.isEmptyOrWhitespace(etSoft.getText().toString())){
                    etCoherent.setBackgroundColor(Color.RED);
                    etSoft.setBackgroundColor(Color.RED);
                    etSwollen.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo2Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etCoherent.setBackgroundColor(Color.WHITE);
                    etSoft.setBackgroundColor(Color.WHITE);
                    etSwollen.setBackgroundColor(Color.WHITE);
                }
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    //Clear focus here from edittext
                    etSwollen.clearFocus();
                }
                return false;
            }
        });

        etShiny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etShiny.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etShiny.getText().toString().trim());
                int number2 = etBlurred.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etBlurred.getText().toString().trim());
                if (number1+number2!=100 && !Strings.isEmptyOrWhitespace(etShiny.getText().toString()) && !Strings.isEmptyOrWhitespace(etBlurred.getText().toString())){
                    etShiny.setBackgroundColor(Color.RED);
                    etBlurred.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo2Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etShiny.setBackgroundColor(Color.WHITE);
                    etBlurred.setBackgroundColor(Color.WHITE);
                }
                etBlurred.requestFocus();
            }
        });

        etBlurred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int number1 = etShiny.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etShiny.getText().toString().trim());
                int number2 = etBlurred.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(etBlurred.getText().toString().trim());
                if (number1+number2!=100 && !Strings.isEmptyOrWhitespace(etShiny.getText().toString()) && !Strings.isEmptyOrWhitespace(etBlurred.getText().toString())){
                    etShiny.setBackgroundColor(Color.RED);
                    etBlurred.setBackgroundColor(Color.RED);
                    CToast(ReceiptQualityMoreInfo2Activity.this, "Το άθροισμα των ποσοστών είναι διαφορετικό από 100%", Toast.LENGTH_SHORT);
                } else {
                    etShiny.setBackgroundColor(Color.WHITE);
                    etBlurred.setBackgroundColor(Color.WHITE);
                }
                etHealed.requestFocus();
            }
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
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recQuality.smellCondition)) {
                sb.append(String.format("\n%s is missing", "'Smell condition'"));
            }

            if (GlobalState.recQuality.shiny == null || GlobalState.recQuality.blurred == null || GlobalState.recQuality.healed == null || GlobalState.recQuality.blindEyes == null || GlobalState.recQuality.coherent == null || GlobalState.recQuality.soft == null || GlobalState.recQuality.swollen == null) {
                sb.append(String.format("\n%s is missing", "'Some percentages fields'"));
            }
        }

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