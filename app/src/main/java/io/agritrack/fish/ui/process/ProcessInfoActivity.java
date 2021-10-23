package io.agritrack.fish.ui.process;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.dialog.ExpandableListDialog;
import io.agritrack.dialog.PhotoDialog;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingStartActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class ProcessInfoActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private static final int pic_id = 123;
    private final MutableLiveData<Bitmap> photoResult = new MutableLiveData<>();
    private TextView etDispatchNote, etSecurityClip, etPlot;
    private ToggleGroup tgChooseFishCondition;
    private SwitchCompat swCleanTruck, swSmell;
    private ImageView ivTakenPhoto;
    private EditText mtvRemarks;
    private PhotoDialog photoDialog;
    private String selectedFishCondition;

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

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        ImageButton ivCamera = findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create the camera_intent ACTION_IMAGE_CAPTURE
                // it will open the camera for capture the image
                Intent camera_intent
                        = new Intent(MediaStore
                        .ACTION_IMAGE_CAPTURE);

                // Start the activity with camera_intent,
                // and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });

        photoResult.observe(this, response -> {
            if (response != null) {
                GlobalState.recProcessing.photoPath = System.currentTimeMillis() + "";
                ivTakenPhoto.setVisibility(View.VISIBLE);
            } else {
                ivTakenPhoto.setVisibility(View.GONE);
                GlobalState.recProcessing.photoPath = null;
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProcessInfoActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {

            // BitMap is data structure of image file
            // which stor the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Set the image in imageview for display
            photoResult.setValue(photo);

            photoDialog = new PhotoDialog(ProcessInfoActivity.this, photoResult, R.string.photo_taken);
            photoDialog.showDialog();
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToReceiveBins);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
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
        etSecurityClip = findViewById(R.id.etSecurityClipNum);
        etPlot = findViewById(R.id.etPlot);
        swCleanTruck = findViewById(R.id.swCleanTruck);
        swSmell = findViewById(R.id.swSmell);
        tgChooseFishCondition = findViewById(R.id.tgChooseFishCondition);
        tgChooseFishCondition.setOnCheckedChangeListener(this);
        mtvRemarks = findViewById(R.id.mtvRemarks);
        mtvRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        ivSupport = findViewById(R.id.ivSupport);
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

        if (!Strings.isEmptyOrWhitespace(prcTx.remarks)) {
            mtvRemarks.setText(prcTx.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(prcTx.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }

        swCleanTruck.setChecked(prcTx.cleanTruck);
        swSmell.setChecked(prcTx.smellyTruck);
    }

    private ProcessingRecord updateState() {
        ProcessingRecord processingRecord = GlobalState.recProcessing;

        if (etDispatchNote.getText() != null) {
            processingRecord.dispatchNote = etDispatchNote.getText().toString();
        }
        if (etSecurityClip.getText() != null) {
            processingRecord.securityClip = etSecurityClip.getText().toString();
        }
        if (etPlot.getText() != null) {
            processingRecord.pLot = etPlot.getText().toString();
        }
        if (!Strings.isEmptyOrWhitespace(selectedFishCondition)) {
            processingRecord.fishCondition = selectedFishCondition;
        }

        if (mtvRemarks.getText() != null) {
            processingRecord.remarks = mtvRemarks.getText().toString();
        }

        processingRecord.cleanTruck = swCleanTruck.isChecked();
        processingRecord.smellyTruck = swSmell.isChecked();

        return processingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        /*if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.dispatchNote)) {
            sb.append(String.format("\n%s is missing", "'Dispatch note'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.pLot)) {
            sb.append(String.format("\n%s is missing", "'LOT'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recProcessing.securityClip)) {
            sb.append(String.format("\n%s is missing", "'Security clip number'"));
        }*/

        /*if(Strings.isEmptyOrWhitespace(GlobalState.recProcessing.fishCondition)){
            sb.append(String.format("\n%s is missing", "'Fish condition'"));
        }*/

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbGood) {
            selectedFishCondition = "GOOD";
        } else if (checkedId == R.id.tbAcceptable) {
            selectedFishCondition = "ACCEPTABLE";
        } else if (checkedId == R.id.tbNotAcceptable) {
            selectedFishCondition = "NOT ACCEPTABLE";
        }
    }
}