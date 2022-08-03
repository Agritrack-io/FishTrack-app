package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.PhotoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.ui.service.LocalPreferences;

public class ReceiptQualityInfoActivity extends AppCompatActivity {

    private MobileDB db;
    private static final int pic_id = 123;
    private final MutableLiveData<Bitmap> photoResult = new MutableLiveData<>();
    private EditText mtvRemarks, etPlot, etMinFishTemp, etMeanFishTemp, etMaxFishTemp;
    private TextView tvMeanTempBin, tvMinTempBin, tvMaxTempBin;
    private PhotoDialog photoDialog;
    private ImageView ivTakenPhoto;
    private String binEpc;
    private final NumberFormat format = new DecimalFormat("0.#");

    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private final InputFilter filter = new InputFilter() {
        final int maxDigitsBeforeDecimalPoint = 2;
        final int maxDigitsAfterDecimalPoint = 2;

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder(dest);
            builder.replace(dstart, dend, source.subSequence(start, end).toString());
            if (!builder.toString().matches("(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?")) {
                if (source.length() == 0)
                    return dest.subSequence(dstart, dend);
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_info);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityInfo);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

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
            supportDialog = new SupportDialog(ReceiptQualityInfoActivity.this);
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
                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    // Set the image in imageview for display
                    photoResult.setValue(photo);

                    photoDialog = new PhotoDialog(ReceiptQualityInfoActivity.this, photoResult, binEpc, R.string.photo_taken);
                    photoDialog.showDialog();

                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackageQualityMoreInfo);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfoActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityTempProfiles);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityTemperatureProfilesActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etPlot = findViewById(R.id.etPlot);
        etMinFishTemp = findViewById(R.id.etMinFishTemp);
        etMinFishTemp.setFilters(new InputFilter[]{filter});
        etMeanFishTemp = findViewById(R.id.etMeanFishTemp);
        etMeanFishTemp.setFilters(new InputFilter[]{filter});
        etMaxFishTemp = findViewById(R.id.etMaxFishTemp);
        etMaxFishTemp.setFilters(new InputFilter[]{filter});

        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        ivSupport = findViewById(R.id.ivSupport);

        tvMinTempBin = findViewById(R.id.tvMinTempBin);
        tvMeanTempBin = findViewById(R.id.tvMeanTempBin);
        tvMaxTempBin = findViewById(R.id.tvMaxTempBin);

        mtvRemarks = findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    private void initControlsFromState() {
        QualityRecord qltTx = GlobalState.recQuality;

        if (qltTx.qualityBins != null && !qltTx.qualityBins.isEmpty()) {
            binEpc = qltTx.qualityBins.get(0);
        }

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

        if (!Strings.isEmptyOrWhitespace(qltTx.pLot)) {
            etPlot.setText(qltTx.pLot);
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

        if (etPlot.getText() != null) {
            qualityRecord.pLot = etPlot.getText().toString();
        }

        if (etMinFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMinFishTemp.getText().toString())) {
            qualityRecord.minFishTemp = Double.valueOf(etMinFishTemp.getText().toString());
        }

        if (etMeanFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMeanFishTemp.getText().toString())) {
            qualityRecord.meanFishTemp = Double.valueOf(etMeanFishTemp.getText().toString());
        }

        if (etMaxFishTemp.getText() != null && !Strings.isEmptyOrWhitespace(etMaxFishTemp.getText().toString())) {
            qualityRecord.maxFishTemp = Double.valueOf(etMaxFishTemp.getText().toString());
        }
        if (tvMinTempBin.getText() != null && !Strings.isEmptyOrWhitespace(tvMinTempBin.getText().toString())) {
            qualityRecord.minBinTemp = Double.valueOf(tvMinTempBin.getText().toString().replace(',', '.'));
        }

        if (tvMeanTempBin.getText() != null && !Strings.isEmptyOrWhitespace(tvMeanTempBin.getText().toString())) {
            qualityRecord.meanBinTemp = Double.valueOf(tvMeanTempBin.getText().toString().replace(',', '.'));
        }

        if (tvMaxTempBin.getText() != null && !Strings.isEmptyOrWhitespace(tvMaxTempBin.getText().toString())) {
            qualityRecord.maxBinTemp = Double.valueOf(tvMaxTempBin.getText().toString().replace(',', '.'));
        }

        if (mtvRemarks.getText() != null) {
            qualityRecord.remarks = mtvRemarks.getText().toString();
        }

        GlobalState.commitQuality(db, Boolean.FALSE);

        return qualityRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recQuality.pLot)) {
                sb.append(String.format("\n%s is missing", "'LOT'"));
            }

            if (GlobalState.recQuality.minFishTemp == null) {
                sb.append(String.format("\n%s is missing", "'Fish min temperature'"));
            }

            if (GlobalState.recQuality.meanFishTemp == null) {
                sb.append(String.format("\n%s is missing", "'Fish average temperature'"));
            }

            if (GlobalState.recQuality.maxFishTemp == null) {
                sb.append(String.format("\n%s is missing", "'Fish max temperature'"));
            }
        }

        return sb.toString();
    }
}