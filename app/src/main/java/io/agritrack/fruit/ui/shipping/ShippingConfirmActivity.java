package io.agritrack.fruit.ui.shipping;

import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.fruit.state.FruitGlobalState.recHarvest;
import static io.agritrack.fruit.state.FruitGlobalState.recShipping;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.fruit.ui.harvesting.HarvestingConfirmActivity;
import io.agritrack.fruit.ui.storage_ready.ReadyStorageStartActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;

public class ShippingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;

    private ProgressDialog progressDialog;
    private TextView tvCustomer, tvNumberIfco, tvDriverName, tvLicensePlate;

    private ImageView ivSupport;
    private SupportDialog supportDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_confirm);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderShippingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(ShippingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ShippingConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recShipping.longitude = mLastLocation.getLongitude();
                    recShipping.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(ShippingConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToShippingDetails);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ShippingDetailsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvNumberIfco = findViewById(R.id.tvNumberIfco);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        /*tvUsername.setText(LocalPreferences.getLoggedInUser(""));

        tvTotalQuantityCount.setText(recFishing.totalFishWeight != null ? recFishing.totalFishWeight.toString() : "N/A");
        tvReqQuantityCount.setText(recFishing.reqWeight != null ? recFishing.reqWeight : "N/A");
        tvNumberOfBinsCount.setText(recFishing.totalBinsUsed != null ? recFishing.totalBinsUsed.toString() : "N/A");
        tvNameCage.setText(recFishing.cageRFID != null ? recFishing.cageRFID : "N/A");
        tvTypeOfFishConfirm.setText(recFishing.speciesName != null ? recFishing.speciesName : "N/A");*/
    }

    private boolean updateState(){
        return true;
    }
}