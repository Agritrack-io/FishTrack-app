package io.agritrack.kefalonia.fish.ui.quality.packaging;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recLoggerData;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.dialog.PhotoDialog;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.state.QualityRecord;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class PackageQualityInfoActivity extends AppCompatActivity {

    private static final int pic_id = 123;
    private final MutableLiveData<Bitmap> photoResult = new MutableLiveData<>();
    private final InputFilter filter = new InputFilter() {
        final int maxDigitsBeforeDecimalPoint = 2;
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
    private EditText mtvRemarks, etMinFishTemp, etMeanFishTemp, etMaxFishTemp;
    private TextView tvMeanTempBin, tvMinTempBin, tvMaxTempBin;
    private PhotoDialog photoDialog;
    private ImageView ivTakenPhoto;
    private String binEpc;
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

            @RequiresApi(api = Build.VERSION_CODES.FROYO)
            @Override
            public void onClick(View v) {
                // Create the camera_intent ACTION_IMAGE_CAPTURE
                // it will open the camera for capture the image
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Date currentDate = new Date();
                String compactTSFormat = "yyyyMMddHHmmss";
                SimpleDateFormat sdf = new SimpleDateFormat(compactTSFormat);
                // create the local jpeg file name
                String fileName = String.format("Photo_%s_%s.png", binEpc, sdf.format(currentDate));

                String photoPath = String.valueOf(PackageQualityInfoActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));

                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(photoPath + "/" + fileName));

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

        etMinFishTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Strings.isEmptyOrWhitespace(etMinFishTemp.getText().toString()) && Double.parseDouble(etMinFishTemp.getText().toString()) > 7) {
                    etMinFishTemp.setBackgroundColor(Color.RED);
                } else if (!Strings.isEmptyOrWhitespace(etMinFishTemp.getText().toString()) && Double.parseDouble(etMinFishTemp.getText().toString()) <= 7) {
                    etMinFishTemp.setBackgroundColor(Color.WHITE);
                }
                etMeanFishTemp.requestFocus();
            }
        });

        etMeanFishTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Strings.isEmptyOrWhitespace(etMeanFishTemp.getText().toString()) && Double.parseDouble(etMeanFishTemp.getText().toString()) > 7) {
                    etMeanFishTemp.setBackgroundColor(Color.RED);
                } else if (!Strings.isEmptyOrWhitespace(etMeanFishTemp.getText().toString()) && Double.parseDouble(etMeanFishTemp.getText().toString()) <= 7) {
                    etMeanFishTemp.setBackgroundColor(Color.WHITE);
                }
                etMaxFishTemp.requestFocus();
            }
        });

        etMaxFishTemp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!Strings.isEmptyOrWhitespace(etMaxFishTemp.getText().toString()) && Double.parseDouble(etMaxFishTemp.getText().toString()) > 7) {
                    etMaxFishTemp.setBackgroundColor(Color.RED);
                } else if (!Strings.isEmptyOrWhitespace(etMaxFishTemp.getText().toString()) && Double.parseDouble(etMaxFishTemp.getText().toString()) <= 7) {
                    etMaxFishTemp.setBackgroundColor(Color.WHITE);
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    etMaxFishTemp.clearFocus();
                }
                return false;
            }
        });

        configFooter();
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Uri photoUri = data.getData();


                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    // Set the image in imageview for display
                    photoResult.setValue(photo);

                    photoDialog = new PhotoDialog(PackageQualityInfoActivity.this, photoResult, binEpc, R.string.photo_taken);
                    photoDialog.showDialog();

                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
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

    private void assignCtrlVars() {
        etMinFishTemp = findViewById(R.id.etMinFishTemp);
        etMinFishTemp.setFilters(new InputFilter[]{filter});
        etMeanFishTemp = findViewById(R.id.etMeanFishTemp);
        etMeanFishTemp.setFilters(new InputFilter[]{filter});
        etMaxFishTemp = findViewById(R.id.etMaxFishTemp);
        etMaxFishTemp.setFilters(new InputFilter[]{filter});
        mtvRemarks = findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        ivSupport = findViewById(R.id.ivSupport);
        tvMinTempBin = findViewById(R.id.tvMinTempBin);
        tvMeanTempBin = findViewById(R.id.tvMeanTempBin);
        tvMaxTempBin = findViewById(R.id.tvMaxTempBin);
    }

    private void initControlsFromState() {
        QualityRecord qltTx = GlobalState.recQuality;

        /*if (qltTx.qualityBins != null && !qltTx.qualityBins.isEmpty()) {
            binEpc = qltTx.qualityBins.get(0);
        }*/

        if (recLoggerData.lowT != null) {
            tvMinTempBin.setText(String.format("%.1f", recLoggerData.lowT));
        }

        if (recLoggerData.avgT != null) {
            tvMeanTempBin.setText(String.format("%.1f", recLoggerData.avgT));
        }

        if (recLoggerData.highT != null) {
            tvMaxTempBin.setText(String.format("%.1f", recLoggerData.highT));
        }

        if (!Strings.isEmptyOrWhitespace(qltTx.remarks)) {
            mtvRemarks.setText(qltTx.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(qltTx.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }

        if (qltTx.minFishTemp != null) {
            etMinFishTemp.setText(String.valueOf(qltTx.minFishTemp));
        }

        if (qltTx.meanFishTemp != null) {
            etMeanFishTemp.setText(String.valueOf(qltTx.meanFishTemp));
        }

        if (qltTx.maxFishTemp != null) {
            etMaxFishTemp.setText(String.valueOf(qltTx.maxFishTemp));
        }
    }

    private QualityRecord updateState() {
        QualityRecord qualityRecord = GlobalState.recQuality;

        if (etMinFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMinFishTemp.getText().toString())) {
            qualityRecord.minFishTemp = Double.valueOf(etMinFishTemp.getText().toString());
        }

        if (etMeanFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMeanFishTemp.getText().toString())) {
            qualityRecord.meanFishTemp = Double.valueOf(etMeanFishTemp.getText().toString());
        }

        if (etMaxFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMaxFishTemp.getText().toString())) {
            qualityRecord.maxFishTemp = Double.valueOf(etMaxFishTemp.getText().toString());
        }

        if (mtvRemarks.getText() != null) {
            qualityRecord.remarks = mtvRemarks.getText().toString();
        }
        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recQuality.minFishTemp == null) {
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.fish_min_temp));

            }

            if (GlobalState.recQuality.meanFishTemp == null) {
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.fish_avg_temp));
            }

            if (GlobalState.recQuality.maxFishTemp == null) {
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.fish_max_temp));
            }
        }

        return sb.toString();
    }
}