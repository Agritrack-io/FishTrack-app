package io.agritrack.fishtrack.ui.process;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.ProcessingRecord;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class ProcessStartActivity extends AppCompatActivity {

    private TextView etDispatchNote, etSecurityClip;
    private Spinner spLot, spFishCondition;
    private SwitchCompat swCleanTruck, swSmell;
    private EditText mtvRemarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToReceiveBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), ProcessBinsActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etDispatchNote = findViewById(R.id.etDispatchNote);
        etSecurityClip = findViewById(R.id.etSecurityClipNum);
        spLot = findViewById(R.id.spLOT);
        spFishCondition = findViewById(R.id.spFishCondition);
        swCleanTruck = findViewById(R.id.swCleanTruck);
        swSmell = findViewById(R.id.swSmell);
        mtvRemarks = findViewById(R.id.mtvRemarks);
    }

    private void initControlsFromState() {
        ProcessingRecord prcTx = GlobalState.recProcessing;

        if (!Strings.isEmptyOrWhitespace(prcTx.dispatchNote)) {
            etDispatchNote.setText(prcTx.dispatchNote);
        }

        if (!Strings.isEmptyOrWhitespace(prcTx.securityClip)) {
            etSecurityClip.setText(prcTx.securityClip);
        }

        if (prcTx.packagingSitePos > -1) {
            spLot.setSelection(prcTx.packagingSitePos);
        }

        if (prcTx.fishConditionPos > -1) {
            spFishCondition.setSelection(prcTx.fishConditionPos);
        }

        if (!Strings.isEmptyOrWhitespace(prcTx.remarks)) {
            mtvRemarks.setText(prcTx.remarks);
        }

        swCleanTruck.setChecked(prcTx.cleanTruck);
        swSmell.setChecked(prcTx.smellyTruck);
    }

    private ProcessingRecord updateState() {
        ProcessingRecord processingRecord = GlobalState.initProcessingTx();

        if (etDispatchNote.getText() != null) {
            processingRecord.dispatchNote = etDispatchNote.getText().toString();
        }
        if (etSecurityClip.getText() != null) {
            processingRecord.securityClip = etSecurityClip.getText().toString();
        }
        if (spLot.getSelectedItem() != null) {
            processingRecord.packagingSite = spLot.getSelectedItem().toString();
        }
        processingRecord.packagingSitePos = spLot.getSelectedItemPosition();

        if (spFishCondition.getSelectedItem() != null) {
            processingRecord.fishCondition = spFishCondition.getSelectedItem().toString();
        }
        processingRecord.fishConditionPos = spFishCondition.getSelectedItemPosition();

        if (mtvRemarks.getText() != null) {
            processingRecord.remarks = mtvRemarks.getText().toString();
        }

        processingRecord.cleanTruck = swCleanTruck.isChecked();
        processingRecord.smellyTruck = swSmell.isChecked();

        return processingRecord;
    }
}