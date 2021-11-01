package io.agritrack.fish.ui.transport;

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
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.TransportTxDTO;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class TransportSupervisorConfirmActivity extends AppCompatActivity implements LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;

    private LocationManager locationManager;
    private TimeOutProgressDlg syncProgressDialog;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private TextView tvSitePackaging, tvNumberOfBinsCount, tvDriverName, tvLicensePlate, tvSecurityClipNumber;
    private TextView tvUsername;
    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_supervisor_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportSupervisorConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(TransportSupervisorConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        syncProgressDialog = new TimeOutProgressDlg(200l, 500l, this) {
            @Override
            public void doTasks() {
                locationManager.removeUpdates(TransportSupervisorConfirmActivity.this);

                // Update state and proceed to next
                Boolean proceed = updateState();
                toggleProgress(false, R.string.app_name);

                if (proceed) {
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(i);
                }
            }
        };
        syncProgressDialog.setMessage(R.string.acquire_coordinates);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TransportSupervisorConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(TransportSupervisorConfirmActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TransportSupervisorConfirmActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, TransportSupervisorConfirmActivity.this);
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToDriverConfirm);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvSitePackaging = findViewById(R.id.tvSitePackaging);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (!Strings.isEmptyOrWhitespace(trns.packagingSite)) {
            tvSitePackaging.setText(trns.packagingSite);
        }

        if (trns.availBins != null) {
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

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        EditText etPIN = findViewById(R.id.etPasswordTransport);
        if (!TextUtils.isEmpty(etPIN.getText().toString())) {
            String login = LocalPreferences.getLoggedInUser("").trim();
            String pin = etPIN.getText().toString().trim();

            // use typed-in PIN to compare credentials with those stored in the Local DB.
            AuthenticationService authSvc = new AuthenticationService();
            boolean authentication = authSvc.authenticateUser(this.db, login, pin);

            // credentials do NOT match
            if (!authentication) {
                runOnUiThread(() -> CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG));
                return false;
            } else {
                try {
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(render("Synchronizing data..."));
                    progressDialog.show();

                    String token = LocalPreferences.getToken();
                    //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

                    // persist Transportation Record data to local DB.
                    TransportTransaction tx = GlobalState.commitTransport(db);

                    // sync fish species
                    Call<TransportTxDTO> syncTxAsyncCall = updService.syncTransportTx(TransportTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new SyncTxCallBack());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
                    return false;
                } finally {
                    progressDialog.dismiss();
                }
            }
        } else {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG));
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<TransportTxDTO> {
        @Override
        public void onResponse(Call<TransportTxDTO> call, Response<TransportTxDTO> response) {
            TransportTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Transport TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_transport_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<TransportTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_timeout), Toast.LENGTH_LONG));
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render("Network Error :: " + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }

    // GPS Location-Related functionality
    @Override
    public void onLocationChanged(@NonNull Location location) {
        recTransport.longitude = location.getLongitude();
        recTransport.latitude = location.getLatitude();
        locationManager.removeUpdates(this);
        toggleProgress(false, R.string.app_name);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            runOnUiThread(() -> {
                syncProgressDialog.show();
            });
        } else {
            runOnUiThread(() -> {
                syncProgressDialog.hide();
            });
        }
    }
}