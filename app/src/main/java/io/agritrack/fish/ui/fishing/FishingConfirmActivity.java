package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.FishingTxDTO;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FishingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;

    private ProgressDialog progressDialog;
    private TextView tvTotalQuantityCount, tvReqQuantityCount, tvNumberOfBinsCount, tvNameCage, tvTypeOfFishConfirm, tvUsername;

    private ImageView ivSupport, ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_confirm);

        // activate GPS location update feature.
        //super.findLocation();   //TODO:: remove comment

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

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingConfirmActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingConfirmActivity.this);
            infoDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recFishing.longitude = mLastLocation.getLongitude();
                    recFishing.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(FishingConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                    startActivity(i);
                }
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
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
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
                    String token = LocalPreferences.getToken();

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
                }
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

}