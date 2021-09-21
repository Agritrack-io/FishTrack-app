package io.agritrack.fishtrack.ui.fishing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.FishingTxDTO;
import io.agritrack.fishtrack.data.model.HarvestRequest;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.custom.CustomToast;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.AuthenticationService;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.state.GlobalState.recFishing;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class FishingConfirmActivity extends AppCompatActivity implements LocationListener {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private LocationManager locationManager;
    private volatile Location location;
    private ProgressDialog progressDialog;
    private final int REQUEST_FINE_LOCATION = 1234;
    private TextView tvTotalQuantityCount, tvReqQuantityCount, tvNumberOfBinsCount, tvNameCage, tvTypeOfFishConfirm, tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(FishingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Boolean proceed = updateState();
            if (proceed) {
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFillBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingFillBinsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvTotalQuantityCount = findViewById(R.id.tvTotalQuantityCount);
        tvReqQuantityCount = findViewById(R.id.tvReqQuantityCount);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvNameCage = findViewById(R.id.tvNameCage);
        tvTypeOfFishConfirm = findViewById(R.id.tvTypeOfFishConfirm);
        tvUsername = findViewById(R.id.tvUsername);
    }

    private void initControlsFromState() {
        tvUsername.setText(LocalPreferences.getLoggedInUser(""));

        tvTotalQuantityCount.setText(recFishing.totalFishWeight != null ? recFishing.totalFishWeight.toString() : "N/A");
        tvReqQuantityCount.setText(recFishing.reqWeight != null ? recFishing.reqWeight : "N/A");
        tvNumberOfBinsCount.setText(recFishing.totalBinsUsed != null ? recFishing.totalBinsUsed.toString() : "N/A");
        tvNameCage.setText(recFishing.cageRFID != null ? recFishing.cageRFID : "N/A");
        tvTypeOfFishConfirm.setText(recFishing.speciesName != null ? recFishing.speciesName : "N/A");
    }

    private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        EditText etPIN = findViewById(R.id.etPasswordFishing);
        if (etPIN.getText() != null) {
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

                    // persist Fishing Record data to local DB.
                    FishingTransaction tx = GlobalState.commitFishing(db, Boolean.TRUE);

                    // sync fish species
                    Call<FishingTxDTO> syncTxAsyncCall = updService.syncFishingTx(FishingTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new SyncTxCallBack());
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
                    return false;
                } finally {
                    progressDialog.dismiss();
                }
//                RouteService routeService = new RouteService();
//                NewRouteState newRouteState = NewRouteState.getInstance();
//                Route savedRoute = routeService.saveNewRoute(db, usernameText.getText().toString(), newRouteState);
//                RouteSyncService routeSyncService = new RouteSyncService();
//                routeSyncService.uploadRoute(db, token, savedRoute);
//                routeSyncService.syncRoute(db, token);
//                runOnUiThread(() -> loadingText.setText(R.string.getting_location));
//                while (true) {
//                    if (location != null || (System.currentTimeMillis() - startLocationSearchTime > 60000)) {
//                        break;
//                    }
//                }
//                runOnUiThread(() -> loadingText.setText(R.string.saving_transaction));
//                TransactionService transactionService = new TransactionService();
//                transactionService.saveNewRouteTransaction(db, location);
//
//                TransactionSyncService transactionSyncService = new TransactionSyncService();
//                transactionSyncService.syncUpTransactions(db, token);
//                mCountDown.cancel();
//                Intent i = new Intent(getApplicationContext(), SuccessfulTransactionActivity.class);
//                startActivity(i);
//                return true;
            }
        } else {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG));
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<FishingTxDTO> {
        @Override
        public void onResponse(Call<FishingTxDTO> call, Response<FishingTxDTO> response) {
            FishingTxDTO rs = response.body();

            if (rs != null) {
                if (recFishing.harvestRqPkId != null) {
                    HarvestRequest delObj = new HarvestRequest();
                    delObj.id = recFishing.harvestRqPkId;
                    db.harvestRequestsDAO().delete(delObj);
                }
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_fishing_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<FishingTxDTO> call, Throwable error) {
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
        this.location = location;
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}