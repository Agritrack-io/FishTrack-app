package io.agritrack.fish.ui.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.ui.custom.CaptureSignatureView;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class TransportDriverConfirmActivity extends AppCompatActivity {

    private TextView tvSitePackaging, tvNumberOfBinsCount, tvDriverName, tvLicensePlate, tvSecurityClipNumber;
    private CaptureSignatureView signatureView;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_driver_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportDriverConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TransportDriverConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToSupervisorconfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), TransportSupervisorConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToTransportBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });

    }

    private void assignCtrlVars() {
        tvSitePackaging = findViewById(R.id.tvSitePackaging);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        signatureView = findViewById(R.id.signatureView);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (!Strings.isEmptyOrWhitespace(trns.packagingSite)) {
            tvSitePackaging.setText(trns.packagingSite);
        }

        if (trns.availBins!=null) {
            tvNumberOfBinsCount.setText(String.valueOf(trns.availBins.size()));
        }

        if (!Strings.isEmptyOrWhitespace(trns.driverName)) {
            tvDriverName.setText(trns.driverName);
        }

        if (!Strings.isEmptyOrWhitespace(trns.licensePlate)) {
            tvLicensePlate.setText(trns.licensePlate);
        }

        if (!Strings.isEmptyOrWhitespace(trns.clipNumber)) {
            tvSecurityClipNumber.setText(trns.clipNumber);
        }

        /*if (trns.signature!=null) {
            signatureView.ClearCanvas();
        }*/

//        swRefrigeratedTruck.setChecked(trns.refrigeratedTruck);
//        swParallelTransport.setChecked(trns.parallelTransport);
    }

    private void updateState() {
        GlobalState.recTransport.signature = signatureView.getBitmap();
        GlobalState.recTransport.signatureBytes = signatureView.getBytes();
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (!signatureView.isSigned()) {
                sb.append(String.format("\n%s is missing", "'Signature'"));
            }
        }

        return sb.toString();
    }
}