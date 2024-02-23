package io.agritrack.kefalonia.fish.ui.process;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.common.util.Strings;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.state.ProcessingRecord;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class ProcessInfoActivity extends AppCompatActivity {


    private TextView etDispatchNote;
    private SwitchCompat swCleanTruck, swSmell;


    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_info);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProcessInfoActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToReceiveBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ProcessConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ProcessBinsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etDispatchNote = findViewById(R.id.etDispatchNote);
        swCleanTruck = findViewById(R.id.swCleanTruck);
        swSmell = findViewById(R.id.swSmell);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        ProcessingRecord prcTx = GlobalState.recProcessing;

        if (!Strings.isEmptyOrWhitespace(prcTx.dispatchNote)) {
            etDispatchNote.setText(prcTx.dispatchNote);
        }

        swCleanTruck.setChecked(prcTx.cleanTruck);
        swSmell.setChecked(prcTx.smellyTruck);
    }

    private ProcessingRecord updateState() {
        ProcessingRecord processingRecord = GlobalState.recProcessing;

        if (etDispatchNote.getText() != null) {
            processingRecord.dispatchNote = etDispatchNote.getText().toString();
        }

        processingRecord.cleanTruck = swCleanTruck.isChecked();
        processingRecord.smellyTruck = swSmell.isChecked();

        return processingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.dispatchNote)) {
                sb.append(String.format(R.string.field +"\n%s" + R.string.is_missing, R.string.dispatch_note_error));


            }
        }

        return sb.toString();
    }
}