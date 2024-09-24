package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityPackage;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.common.InputFilterMinMax;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.state.PackageQualityRecord;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityDysmorphiasActivity extends AppCompatActivity {
    private MobileDB db;
    private EditText etCrookedMouth, etHeadDeform, etLowerJaw, etJawOver, etOper, etLordosis, etShort,
            etSkelet, etTailDeform, etTailDeformity, etFinDeform, etScratched;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_package_dysmorphias);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityMoreInfo2);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityDysmorphiasActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityMoreInfo3);
        ivNext.setOnClickListener(view -> {
            updateState();

            Intent i = new Intent(getApplicationContext(), PackageQualityDysmorphias2Activity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo);
        ivBack.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), PackageQualityFreshnessActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etCrookedMouth = findViewById(R.id.etCrooked);
        etCrookedMouth.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etCrookedMouth.setHint("0");

        etJawOver = findViewById(R.id.etParrot);
        etJawOver.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etJawOver.setHint("0");

        etHeadDeform = findViewById(R.id.etHead);
        etHeadDeform.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHeadDeform.setHint("0");

        etOper = findViewById(R.id.etOper);
        etOper.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etOper.setHint("0");

        etLordosis = findViewById(R.id.etLordosis);
        etLordosis.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etLordosis.setHint("0");

        etLowerJaw = findViewById(R.id.etJaw);
        etLowerJaw.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etLowerJaw.setHint("0");

        etShort = findViewById(R.id.etShort);
        etShort.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etShort.setHint("0");

        etSkelet = findViewById(R.id.etSkelet);
        etSkelet.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etSkelet.setHint("0");

        etTailDeformity = findViewById(R.id.etTail);
        etTailDeformity.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etTailDeformity.setHint("0");

        etTailDeform = findViewById(R.id.etTailDef);
        etTailDeform.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etTailDeform.setHint("0");

        etFinDeform = findViewById(R.id.etFin);
        etFinDeform.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etFinDeform.setHint("0");

        etScratched = findViewById(R.id.etScratched);
        etScratched.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etScratched.setHint("0");


        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PackageQualityRecord recQualityRecord = recQualityPackage;

        if (recQualityRecord.crookedMouth != null) {
            etCrookedMouth.setText(String.valueOf(recQualityRecord.crookedMouth));
        }
        if (recQualityRecord.lowerJaw != null) {
            etLowerJaw.setText(String.valueOf(recQualityRecord.lowerJaw));
        }
        if (recQualityRecord.headDeform != null) {
            etHeadDeform.setText(String.valueOf(recQualityRecord.headDeform));
        }
        if (recQualityRecord.jawOver != null) {
            etJawOver.setText(String.valueOf(recQualityRecord.jawOver));
        }
        if (recQualityRecord.lordosis != null) {
            etLordosis.setText(String.valueOf(recQualityRecord.lordosis));
        }
        if (recQualityRecord.operculum != null) {
            etOper.setText(String.valueOf(recQualityRecord.operculum));
        }
        if (recQualityRecord.shortening != null) {
            etShort.setText(String.valueOf(recQualityRecord.shortening));
        }
        if (recQualityRecord.tailDeform != null) {
            etTailDeform.setText(String.valueOf(recQualityRecord.tailDeform));
        }
        if (recQualityRecord.tailDeformity != null) {
            etTailDeformity.setText(String.valueOf(recQualityRecord.tailDeformity));
        }
        if (recQualityRecord.skeletical != null) {
            etSkelet.setText(String.valueOf(recQualityRecord.skeletical));
        }
        if (recQualityRecord.finDeform != null) {
            etFinDeform.setText(String.valueOf(recQualityRecord.finDeform));
        }
        if (recQualityRecord.woundsDeform != null) {
            etScratched.setText(String.valueOf(recQualityRecord.woundsDeform));
        }

    }

    private PackageQualityRecord updateState() {
        PackageQualityRecord packQualityRecord = recQualityPackage;

        if (etCrookedMouth.getText() != null && !Strings.isEmptyOrWhitespace(etCrookedMouth.getText().toString())) {
            recQualityPackage.crookedMouth = Integer.valueOf(etCrookedMouth.getText().toString());
        }

        if (etJawOver.getText() != null && !Strings.isEmptyOrWhitespace(etJawOver.getText().toString())) {
            recQualityPackage.jawOver = Integer.valueOf(etJawOver.getText().toString());
        }

        if (etLowerJaw.getText() != null && !Strings.isEmptyOrWhitespace(etLowerJaw.getText().toString())) {
            recQualityPackage.lowerJaw = Integer.valueOf(etLowerJaw.getText().toString());
        }

        if (etLordosis.getText() != null && !Strings.isEmptyOrWhitespace(etLordosis.getText().toString())) {
            recQualityPackage.lordosis = Integer.valueOf(etLordosis.getText().toString());
        }

        if (etHeadDeform.getText() != null && !Strings.isEmptyOrWhitespace(etHeadDeform.getText().toString())) {
            recQualityPackage.headDeform = Integer.valueOf(etHeadDeform.getText().toString());
        }

        if (etOper.getText() != null && !Strings.isEmptyOrWhitespace(etOper.getText().toString())) {
            recQualityPackage.operculum = Integer.valueOf(etOper.getText().toString());
        }

        if (etShort.getText() != null && !Strings.isEmptyOrWhitespace(etShort.getText().toString())) {
            recQualityPackage.shortening = Integer.valueOf(etShort.getText().toString());
        }

        if (etSkelet.getText() != null && !Strings.isEmptyOrWhitespace(etSkelet.getText().toString())) {
            recQualityPackage.skeletical = Integer.valueOf(etSkelet.getText().toString());
        }

        if (etScratched.getText() != null && !Strings.isEmptyOrWhitespace(etScratched.getText().toString())) {
            recQualityPackage.woundsDeform = Integer.valueOf(etScratched.getText().toString());
        }

        if (etFinDeform.getText() != null && !Strings.isEmptyOrWhitespace(etFinDeform.getText().toString())) {
            recQualityPackage.finDeform = Integer.valueOf(etFinDeform.getText().toString());
        }

        if (etTailDeformity.getText() != null && !Strings.isEmptyOrWhitespace(etTailDeformity.getText().toString())) {
            recQualityPackage.tailDeformity = Integer.valueOf(etTailDeformity.getText().toString());
        }

        if (etTailDeform.getText() != null && !Strings.isEmptyOrWhitespace(etTailDeform.getText().toString())) {
            recQualityPackage.tailDeform = Integer.valueOf(etTailDeform.getText().toString());
        }


        return packQualityRecord;
    }


}
