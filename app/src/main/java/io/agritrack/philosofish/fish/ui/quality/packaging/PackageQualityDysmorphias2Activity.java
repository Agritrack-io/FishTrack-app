package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityPackage;
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
import io.agritrack.philosofish.fish.state.PackageQualityRecord;
import io.agritrack.philosofish.fish.state.QualityRecord;
import io.agritrack.philosofish.fish.state.ReceiptQualityRecord;
import io.agritrack.philosofish.fish.ui.quality.receipt.ReceiptQualityConfirmActivity;
import io.agritrack.philosofish.fish.ui.quality.receipt.ReceiptQualityStartActivity;
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class PackageQualityDysmorphias2Activity extends AppCompatActivity {
    private MobileDB db;
    private EditText etHemslight, etHemSpots, etHemDiffuse, etHemWounds, etEyeBLurred, etEyeCured, etEyeBlind,
        etEyeBleed, etGillMucus, etGillBloody, etGillBrown, etGillDiscolored;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_package_dysmorphias_second);

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
            supportDialog = new SupportDialog(PackageQualityDysmorphias2Activity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityMoreInfo3);
        ivNext.setOnClickListener(view -> {
            updateState();

            Intent i = new Intent(getApplicationContext(), PackageQualityConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMoreInfo);
        ivBack.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), PackageQualityDysmorphiasActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etHemslight = findViewById(R.id.etSlightBlood);
        etHemslight.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHemslight.setHint("0");

        etHemWounds = findViewById(R.id.etHemWounds);
        etHemWounds.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHemWounds.setHint("0");

        etHemSpots = findViewById(R.id.etSpots);
        etHemSpots.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHemSpots.setHint("0");

        etEyeBLurred = findViewById(R.id.etBlur);
        etEyeBLurred.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEyeBLurred.setHint("0");

        etEyeCured = findViewById(R.id.etCured);
        etEyeCured.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEyeCured.setHint("0");

        etHemDiffuse = findViewById(R.id.etHemDiffuse);
        etHemDiffuse.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etHemDiffuse.setHint("0");

        etEyeBlind = findViewById(R.id.etBlind);
        etEyeBlind.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEyeBlind.setHint("0");

        etEyeBleed = findViewById(R.id.etBleeding);
        etEyeBleed.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etEyeBleed.setHint("0");

        etGillBloody = findViewById(R.id.etBloodyMucus);
        etGillBloody.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etGillBloody.setHint("0");

        etGillMucus = findViewById(R.id.etMucus);
        etGillMucus.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etGillMucus.setHint("0");

        etGillBrown = findViewById(R.id.etBrown);
        etGillBrown.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etGillBrown.setHint("0");

        etGillDiscolored = findViewById(R.id.etDiscolored);
        etGillDiscolored.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        etGillDiscolored.setHint("0");


        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PackageQualityRecord recQualityRecord = recQualityPackage;

        if (recQualityRecord.hemSlight != null) {
            etHemslight.setText(String.valueOf(recQualityRecord.hemSlight));
        }
        if (recQualityRecord.hemDiffuse != null) {
            etHemDiffuse.setText(String.valueOf(recQualityRecord.hemDiffuse));
        }
        if (recQualityRecord.hemSpots != null) {
            etHemSpots.setText(String.valueOf(recQualityRecord.hemSpots));
        }
        if (recQualityRecord.hemWounds != null) {
            etHemWounds.setText(String.valueOf(recQualityRecord.hemWounds));
        }
        if (recQualityRecord.eyeCured != null) {
            etEyeCured.setText(String.valueOf(recQualityRecord.eyeCured));
        }
        if (recQualityRecord.eyeBlurred != null) {
            etEyeBLurred.setText(String.valueOf(recQualityRecord.eyeBlurred));
        }
        if (recQualityRecord.eyeBlind != null) {
            etEyeBlind.setText(String.valueOf(recQualityRecord.eyeBlind));
        }
        if (recQualityRecord.gillMucus != null) {
            etGillMucus.setText(String.valueOf(recQualityRecord.gillMucus));
        }
        if (recQualityRecord.gillBloody != null) {
            etGillBloody.setText(String.valueOf(recQualityRecord.gillBloody));
        }
        if (recQualityRecord.eyeBleed != null) {
            etEyeBleed.setText(String.valueOf(recQualityRecord.eyeBleed));
        }
        if (recQualityRecord.gillBrown != null) {
            etGillBrown.setText(String.valueOf(recQualityRecord.gillBrown));
        }
        if (recQualityRecord.gillDiscolor != null) {
            etGillDiscolored.setText(String.valueOf(recQualityRecord.gillDiscolor));
        }

    }

    private PackageQualityRecord updateState() {
        PackageQualityRecord packQualityRecord = recQualityPackage;

        if (etHemslight.getText() != null && !Strings.isEmptyOrWhitespace(etHemslight.getText().toString())) {
            recQualityPackage.hemSlight = Integer.valueOf(etHemslight.getText().toString());
        }

        if (etHemWounds.getText() != null && !Strings.isEmptyOrWhitespace(etHemWounds.getText().toString())) {
            recQualityPackage.hemWounds = Integer.valueOf(etHemWounds.getText().toString());
        }

        if (etHemDiffuse.getText() != null && !Strings.isEmptyOrWhitespace(etHemDiffuse.getText().toString())) {
            recQualityPackage.hemDiffuse = Integer.valueOf(etHemDiffuse.getText().toString());
        }

        if (etEyeCured.getText() != null && !Strings.isEmptyOrWhitespace(etEyeCured.getText().toString())) {
            recQualityPackage.eyeCured = Integer.valueOf(etEyeCured.getText().toString());
        }

        if (etHemSpots.getText() != null && !Strings.isEmptyOrWhitespace(etHemSpots.getText().toString())) {
            recQualityPackage.hemSpots = Integer.valueOf(etHemSpots.getText().toString());
        }

        if (etEyeBLurred.getText() != null && !Strings.isEmptyOrWhitespace(etEyeBLurred.getText().toString())) {
            recQualityPackage.eyeBlurred = Integer.valueOf(etEyeBLurred.getText().toString());
        }

        if (etEyeBlind.getText() != null && !Strings.isEmptyOrWhitespace(etEyeBlind.getText().toString())) {
            recQualityPackage.eyeBlind = Integer.valueOf(etEyeBlind.getText().toString());
        }

        if (etEyeBleed.getText() != null && !Strings.isEmptyOrWhitespace(etEyeBleed.getText().toString())) {
            recQualityPackage.eyeBleed = Integer.valueOf(etEyeBleed.getText().toString());
        }

        if (etGillDiscolored.getText() != null && !Strings.isEmptyOrWhitespace(etGillDiscolored.getText().toString())) {
            recQualityPackage.gillDiscolor = Integer.valueOf(etGillDiscolored.getText().toString());
        }

        if (etGillBrown.getText() != null && !Strings.isEmptyOrWhitespace(etGillBrown.getText().toString())) {
            recQualityPackage.gillBrown = Integer.valueOf(etGillBrown.getText().toString());
        }

        if (etGillBloody.getText() != null && !Strings.isEmptyOrWhitespace(etGillBloody.getText().toString())) {
            recQualityPackage.gillBloody = Integer.valueOf(etGillBloody.getText().toString());
        }

        if (etGillMucus.getText() != null && !Strings.isEmptyOrWhitespace(etGillMucus.getText().toString())) {
            recQualityPackage.gillMucus = Integer.valueOf(etGillMucus.getText().toString());
        }


        return packQualityRecord;
    }



}