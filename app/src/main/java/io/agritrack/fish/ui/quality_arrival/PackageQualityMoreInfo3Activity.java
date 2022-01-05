package io.agritrack.fish.ui.quality_arrival;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.ui.service.LocalPreferences;

public class PackageQualityMoreInfo3Activity extends AppCompatActivity {

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
        //etPlot = findViewById(R.id.etPlot);
        /*mtvRemarks = findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);*/
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        ProcessingRecord prcTx = GlobalState.recProcessing;

        /*if (!Strings.isEmptyOrWhitespace(prcTx.remarks)) {
            mtvRemarks.setText(prcTx.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(prcTx.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }*/
    }

    private ProcessingRecord updateState() {
        ProcessingRecord processingRecord = GlobalState.recProcessing;

        /*if (etPlot.getText() != null) {
            processingRecord.pLot = etPlot.getText().toString();
        }
        if (!Strings.isEmptyOrWhitespace(selectedFishCondition)) {
            processingRecord.fishCondition = selectedFishCondition;
        }
        if (mtvRemarks.getText() != null) {
            processingRecord.remarks = mtvRemarks.getText().toString();
        }*/
        return processingRecord;
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