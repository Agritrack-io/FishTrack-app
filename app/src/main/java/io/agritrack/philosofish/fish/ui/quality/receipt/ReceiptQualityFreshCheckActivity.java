package io.agritrack.philosofish.fish.ui.quality.receipt;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityReceipt;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
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
import io.agritrack.philosofish.fish.state.ReceiptQualityRecord;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class ReceiptQualityFreshCheckActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;
    private ToggleGroup tgSmellCondition , tgEyeCondition, tgGillCondition, tgColorCondition, tgFleshCondition;
    private Integer selectedEyeRating, selectedGillRating, selectedSkinRating, selectedFleshRating;
    private EditText etEyes, etBlood, etMouth, etTail, etSkelet, etOper, etComments;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_receipt_freshness_check);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityFresh);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityFreshCheckActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToReceiptConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReceiptQualityConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBacktoReceiptStart);
        ivBack.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), ReceiptQualityStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {

        tgEyeCondition = findViewById(R.id.tgEyeCondition);
        tgEyeCondition.setOnCheckedChangeListener(this);

        tgGillCondition = findViewById(R.id.tgGillCondition);
        tgGillCondition.setOnCheckedChangeListener(this);

        tgFleshCondition = findViewById(R.id.tgElasticityCondition);
        tgFleshCondition.setOnCheckedChangeListener(this);

        tgColorCondition = findViewById(R.id.tgColorCondition);
        tgColorCondition.setOnCheckedChangeListener(this);

        etEyes = findViewById(R.id.disEyes);
        etEyes.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEyes.setHint("0");

        etTail = findViewById(R.id.disTail);
        etTail.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etTail.setHint("0");

        etBlood = findViewById(R.id.disBlood);
        etBlood.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etBlood.setHint("0");

        etSkelet = findViewById(R.id.disSkeletal);
        etSkelet.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etSkelet.setHint("0");

        etOper = findViewById(R.id.disOper);
        etOper.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etOper.setHint("0");

        etMouth = findViewById(R.id.disMouth);
        etMouth.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etMouth.setHint("0");

        etComments = findViewById(R.id.etComments);
        etComments.setHint("Παρατηρήσεις");

        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        ReceiptQualityRecord recQualityRecord = recQualityReceipt;

        if (recQualityRecord.eyeRating != null) {
            switch (recQualityRecord.eyeRating) {
                case 1:
                    tgEyeCondition.check(R.id.tbFailEyes);
                    break;
                case 2:
                    tgEyeCondition.check(R.id.tbBEyes);
                    break;
                case 3:
                    tgEyeCondition.check(R.id.tbAEyes);
                    break;
                case 4:
                    tgEyeCondition.check(R.id.tbExtraEyes);
                    break;
                default:
                    break;
            }
        }

        if (recQualityRecord.gillRating != null) {

            switch (recQualityRecord.gillRating) {
                case 1:
                    tgGillCondition.check(R.id.tbFailGill);
                    break;
                case 2:
                    tgGillCondition.check(R.id.tbBGill);
                    break;
                case 3:
                    tgGillCondition.check(R.id.tbAGill);
                    break;
                case 4:
                    tgGillCondition.check(R.id.tbExtraGill);
                    break;
                default:
                    break;
            }
        }


        if (recQualityRecord.fleshRating != null) {
            switch (recQualityRecord.fleshRating) {
                case 1:
                    tgFleshCondition.check(R.id.tbFailElasticity);
                    break;
                case 2:
                    tgFleshCondition.check(R.id.tbBElasticity);
                    break;
                case 3:
                    tgFleshCondition.check(R.id.tbAElasticity);
                    break;
                case 4:
                    tgFleshCondition.check(R.id.tbExtraElasticity);
                    break;
                default:
                    break;
            }
        }

        if (recQualityRecord.skinRating != null) {
            switch (recQualityRecord.skinRating) {
                case 1:
                    tgColorCondition.check(R.id.tbFailColor);
                    break;
                case 2:
                    tgColorCondition.check(R.id.tbBColor);
                    break;
                case 3:
                    tgColorCondition.check(R.id.tbAColor);
                    break;
                case 4:
                    tgColorCondition.check(R.id.tbExtraColor);
                    break;
                default:
                    break;
            }
        }

        if (recQualityRecord.disEyes != null) {
            etEyes.setText(String.valueOf(recQualityRecord.disEyes));
        }
        if (recQualityRecord.disMouth != null) {
            etMouth.setText(String.valueOf(recQualityRecord.disMouth));
        }
        if (recQualityRecord.disBlood != null) {
            etBlood.setText(String.valueOf(recQualityRecord.disBlood));
        }
        if (recQualityRecord.disTail != null) {
            etTail.setText(String.valueOf(recQualityRecord.disTail));
        }
        if (recQualityRecord.disOper != null) {
            etOper.setText(String.valueOf(recQualityRecord.disOper));
        }
        if (recQualityRecord.disSkeletal != null) {
            etSkelet.setText(String.valueOf(recQualityRecord.disSkeletal));
        }

        if (!Strings.isEmptyOrWhitespace(recQualityRecord.comments)) {
            etComments.setText(recQualityRecord.comments);
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etEyes.getText() != null && !Strings.isEmptyOrWhitespace(etEyes.getText().toString())) {
            recQualityReceipt.disEyes = Integer.valueOf(etEyes.getText().toString());
        }

        if (etTail.getText() != null && !Strings.isEmptyOrWhitespace(etTail.getText().toString())) {
            recQualityReceipt.disTail = Integer.valueOf(etTail.getText().toString());
        }

        if (etMouth.getText() != null && !Strings.isEmptyOrWhitespace(etMouth.getText().toString())) {
            recQualityReceipt.disMouth = Integer.valueOf(etMouth.getText().toString());
        }

        if (etOper.getText() != null && !Strings.isEmptyOrWhitespace(etOper.getText().toString())) {
            recQualityReceipt.disOper = Integer.valueOf(etOper.getText().toString());
        }

        if (etBlood.getText() != null && !Strings.isEmptyOrWhitespace(etBlood.getText().toString())) {
            recQualityReceipt.disBlood = Integer.valueOf(etBlood.getText().toString());
        }

        if (etSkelet.getText() != null && !Strings.isEmptyOrWhitespace(etSkelet.getText().toString())) {
            recQualityReceipt.disSkeletal = Integer.valueOf(etSkelet.getText().toString());
        }
        recQualityReceipt.eyeRating = selectedEyeRating;
        recQualityReceipt.gillRating = selectedGillRating;
        recQualityReceipt.skinRating = selectedSkinRating;
        recQualityReceipt.fleshRating = selectedFleshRating;
        if (etComments.getText() != null && !Strings.isEmptyOrWhitespace(etComments.getText().toString())) {
            recQualityReceipt.comments = etComments.getText().toString();
        }

        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recQualityReceipt.eyeRating == null || recQualityReceipt.eyeRating < 1 || recQualityReceipt.eyeRating > 4) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.eye_evaluation)));

            }
            if (recQualityReceipt.skinRating == null || recQualityReceipt.skinRating < 1 || recQualityReceipt.skinRating > 4) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.skin_condition)));

            }
            if (recQualityReceipt.fleshRating == null || recQualityReceipt.fleshRating < 1 || recQualityReceipt.fleshRating > 4) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.elasticity)));

            }
            if (recQualityReceipt.gillRating == null || recQualityReceipt.gillRating < 1 || recQualityReceipt.gillRating > 4) {
                sb.append(String.format(getString(R.string.field) +"\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.gill_condition)));

            }

        }

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbExtraEyes) {
            selectedEyeRating = 4;
        } else if (checkedId == R.id.tbAEyes) {
            selectedEyeRating = 3;
        } else if (checkedId == R.id.tbBEyes) {
            selectedEyeRating = 2;
        } else if (checkedId == R.id.tbFailEyes) {
            selectedEyeRating = 1;
        }else if (checkedId == R.id.tbExtraGill) {
            selectedGillRating = 4;
        } else if (checkedId == R.id.tbAGill) {
            selectedGillRating = 3;
        } else if (checkedId == R.id.tbBGill) {
            selectedGillRating = 2;
        } else if (checkedId == R.id.tbFailGill) {
            selectedGillRating = 1;
        }else if (checkedId == R.id.tbExtraElasticity) {
            selectedFleshRating = 4;
        } else if (checkedId == R.id.tbAElasticity) {
            selectedFleshRating = 3;
        } else if (checkedId == R.id.tbBElasticity) {
            selectedFleshRating = 2;
        } else if (checkedId == R.id.tbFailElasticity) {
            selectedFleshRating = 1;
        }else if (checkedId == R.id.tbExtraColor) {
            selectedSkinRating = 4;
        } else if (checkedId == R.id.tbAColor) {
            selectedSkinRating = 3;
        } else if (checkedId == R.id.tbBColor) {
            selectedSkinRating = 2;
        } else if (checkedId == R.id.tbFailColor) {
            selectedSkinRating = 1;
        }
    }
}
