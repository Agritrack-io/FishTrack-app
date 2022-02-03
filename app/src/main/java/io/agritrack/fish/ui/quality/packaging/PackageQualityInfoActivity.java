package io.agritrack.fish.ui.quality.packaging;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.dialog.PhotoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.fish.ui.quality.receipt.ReceiptQualityMoreInfoActivity;
import io.agritrack.ui.service.LocalPreferences;

public class PackageQualityInfoActivity extends AppCompatActivity {

    private static final int pic_id = 123;
    private final MutableLiveData<Bitmap> photoResult = new MutableLiveData<>();
    private EditText mtvRemarks, etFishTemp;
    private PhotoDialog photoDialog;
    private ImageView ivTakenPhoto;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_quality_info);
        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualityInfo);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        ImageButton ivCamera = findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create the camera_intent ACTION_IMAGE_CAPTURE
                // it will open the camera for capture the image
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Start the activity with camera_intent,
                // and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });

        photoResult.observe(this, response -> {
            if (response != null) {
                GlobalState.recQuality.photoPath = System.currentTimeMillis() + "";
                ivTakenPhoto.setVisibility(View.VISIBLE);
            } else {
                ivTakenPhoto.setVisibility(View.GONE);
                GlobalState.recQuality.photoPath = null;
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityInfoActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityConfirm);
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

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityTempProfiles);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackageQualityTemperatureProfilesActivity.class);
            startActivity(i);
        });
    }

    private InputFilter filter = new InputFilter() {
        final int maxDigitsBeforeDecimalPoint=2;
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
        etFishTemp = findViewById(R.id.etFishTemp);
        etFishTemp.setFilters(new InputFilter[] { filter });
        mtvRemarks = findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        QualityRecord qltTx = GlobalState.recQuality;

        if (!Strings.isEmptyOrWhitespace(qltTx.remarks)) {
            mtvRemarks.setText(qltTx.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(qltTx.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }

        if (qltTx.fishTemp != null) {
            etFishTemp.setText(String.valueOf(qltTx.fishTemp));
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etFishTemp.getText().toString())) {
            qualityRecord.fishTemp = Double.valueOf(etFishTemp.getText().toString());
        }

        if (mtvRemarks.getText() != null) {
            qualityRecord.remarks = mtvRemarks.getText().toString();
        }
        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}