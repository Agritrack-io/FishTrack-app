package io.agritrack.fishtrack.ui.process;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.ProcessingRecord;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.common.LargeString.render;

public class ProcessStartActivity extends AppCompatActivity {

    private TextView etDispatchNote, etSecurityClip, etPlot;
    private Spinner spFishCondition;
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
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), ProcessBinsActivity.class);
                startActivity(i);
            }
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
        etPlot = findViewById(R.id.etPlot);
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

        if (!Strings.isEmptyOrWhitespace(prcTx.pLot)) {
            etPlot.setText(prcTx.pLot);
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
        ProcessingRecord processingRecord = GlobalState.initProcessingRecord();

        if (etDispatchNote.getText() != null) {
            processingRecord.dispatchNote = etDispatchNote.getText().toString();
        }
        if (etSecurityClip.getText() != null) {
            processingRecord.securityClip = etSecurityClip.getText().toString();
        }
        if (etPlot.getText() != null) {
            processingRecord.pLot = etPlot.getText().toString();
        }
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

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recProcessing.dispatchNote)){
            sb.append(String.format("\n%s is missing", "'Dispatch note'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recProcessing.pLot)){
            sb.append(String.format("\n%s is missing", "'LOT'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recProcessing.securityClip)){
            sb.append(String.format("\n%s is missing", "'Security clip number'"));
        }

        /*if(Strings.isEmptyOrWhitespace(GlobalState.recProcessing.fishCondition)){
            sb.append(String.format("\n%s is missing", "'Fish condition'"));
        }*/

        return sb.toString();
    }
}