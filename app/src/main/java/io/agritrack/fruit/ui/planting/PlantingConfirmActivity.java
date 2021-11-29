package io.agritrack.fruit.ui.planting;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.fruit.state.FruitGlobalState.recPlant;
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
import io.agritrack.data.dto.tx.PlantTxDTO;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.fishing.FishingConfirmActivity;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.PlantRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlantingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;

    private ProgressDialog progressDialog;
    private TextView tvTomatoType, tvPole, tvGreenHouse, tvUsername;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planting_confirm);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSeedingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(PlantingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PlantingConfirmActivity.this);
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
                    recPlant.longitude = mLastLocation.getLongitude();
                    recPlant.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(PlantingConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
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

        ImageView ivBack = findViewById(R.id.ivBackToStartSeeding);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PlantingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvGreenHouse = findViewById(R.id.tvGreenHouse);
        tvPole = findViewById(R.id.tvPole);
        tvTomatoType = findViewById(R.id.tvTomatoType);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PlantRecord recPlant = FruitGlobalState.recPlant;

        tvGreenHouse.setText(recPlant.greenhouse != null ? recPlant.greenhouse : "N/A");
        tvPole.setText(recPlant.poleRFID != null ? recPlant.poleRFID : "N/A");
        tvTomatoType.setText(recPlant.speciesName != null ? recPlant.speciesName : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
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

                    // persist Planting Record data to local DB.
                    PlantTransaction tx = FruitGlobalState.commitPlanting(db);

                    // sync fish species
                    Call<PlantTxDTO> syncTxAsyncCall = updService.syncPlantTx(PlantTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new PlantingConfirmActivity.SyncTxCallBack());

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

    public class SyncTxCallBack implements Callback<PlantTxDTO> {
        @Override
        public void onResponse(Call<PlantTxDTO> call, Response<PlantTxDTO> response) {
            PlantTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_plant_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<PlantTxDTO> call, Throwable error) {
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