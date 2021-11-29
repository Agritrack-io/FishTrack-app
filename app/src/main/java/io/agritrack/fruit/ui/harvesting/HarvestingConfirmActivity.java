package io.agritrack.fruit.ui.harvesting;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recHarvest;
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
import io.agritrack.data.dto.tx.CollectTxDTO;
import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.HarvestRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HarvestingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;

    private ProgressDialog progressDialog;
    private TextView tvGreenHouse, tvPole, tvHarvestLot, tvNumberTotes, tvUsername;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvesting_confirm);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HarvestingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestingConfirmActivity.this);
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
                    recHarvest.longitude = mLastLocation.getLongitude();
                    recHarvest.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(HarvestingConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
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

        ImageView ivBack = findViewById(R.id.ivBackToHarvestingTotes);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HarvestingTotesActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvGreenHouse = findViewById(R.id.tvGreenHouse);
        tvPole = findViewById(R.id.tvPole);
        tvHarvestLot = findViewById(R.id.tvHarvestLot);
        tvNumberTotes = findViewById(R.id.tvNumberTotes);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        HarvestRecord recHarvest = FruitGlobalState.recHarvest;

        tvGreenHouse.setText(recHarvest.greenhouse != null ? recHarvest.greenhouse : "N/A");
        tvPole.setText(recHarvest.poleRFID != null ? recHarvest.poleRFID : "N/A");
        tvHarvestLot.setText(recHarvest.harvestLot != null ? recHarvest.harvestLot : "N/A");
        tvNumberTotes.setText(recHarvest.totalTotesUsed != null ? recHarvest.totalTotesUsed.toString() : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean updateState(){
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

                    // persist Planting Record data to local DB.
                    CollectTransaction tx = FruitGlobalState.commitCollecting(db);

                    // sync fish species
                    Call<CollectTxDTO> syncTxAsyncCall = updService.syncCollectingTx(CollectTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new HarvestingConfirmActivity.SyncTxCallBack());

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

    public class SyncTxCallBack implements Callback<CollectTxDTO> {
        @Override
        public void onResponse(Call<CollectTxDTO> call, Response<CollectTxDTO> response) {
            CollectTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_harvest_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<CollectTxDTO> call, Throwable error) {
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