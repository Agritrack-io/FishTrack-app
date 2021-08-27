package io.agritrack.fishtrack.ui.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.TransportationRecord;
import io.agritrack.fishtrack.ui.custom.CaptureSignatureView;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class TransportDriverConfirmActivity extends AppCompatActivity {

    private TextView tvSitePackaging, tvCompany, tvNumberOfBinsCount, tvDriverName, tvLicensePlate, tvSecurityClipNumber;
    private CaptureSignatureView signatureView;

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

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToSupervisorconfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), TransportSupervisorConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToTransportBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportBinsActivity.class);
            startActivity(i);
        });

    }

    private void assignCtrlVars() {
        tvSitePackaging = findViewById(R.id.tvSitePackaging);
        tvCompany = findViewById(R.id.tvCompany);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        signatureView = findViewById(R.id.signatureView);
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (!Strings.isEmptyOrWhitespace(trns.packagingSite)) {
            tvSitePackaging.setText(trns.packagingSite);
        }

        if (!Strings.isEmptyOrWhitespace(trns.destinationCompany)) {
            tvCompany.setText(trns.destinationCompany);
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

        if (trns.signature!=null) {
            signatureView.ClearCanvas();
        }

//        swRefrigeratedTruck.setChecked(trns.refrigeratedTruck);
//        swParallelTransport.setChecked(trns.parallelTransport);
    }

    private void updateState() {
        GlobalState.recTransport.signature = signatureView.getBitmap();
        GlobalState.recTransport.signatureBytes = signatureView.getBytes();
    }
}