package io.agritrack.fruit.ui.planting;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recPlant;
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

import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.PlantTxDTO;
import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
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
    private EditText etPIN;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planting_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSeedingConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        confirmGPSSelectionDlg = YesNoDialogFragment.instance();
        confirmGPSSelectionDlg.setMessage(getText(R.string.procced_without_location));
        confirmGPSSelectionDlg.onConfirm(bundle -> {
            proceedWithoutLocation = true;
            moveToNextScreen();
        });
        confirmGPSSelectionDlg.onReject(bundle -> {
            mLastLocation = findLocation();
            proceedWithoutLocation = false;
        });

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

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = updateState();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
                startActivity(i);
            }
        }
    }

    protected void configFooter() {
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etPIN.getText().toString())) {
                    CToast(PlantingConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(PlantingConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recPlant.longitude = mLastLocation.getLongitude();
                    recPlant.latitude = mLastLocation.getLatitude();
                    proceedWithoutLocation = true;
                    moveToNextScreen();
                } else if (!proceedWithoutLocation) {
                    FragmentManager fm = getSupportFragmentManager();
                    confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                } else {
                    //CToast(HarvestingConfirmActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }
            }
        });

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
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToStartSeeding);
        etPIN = findViewById(R.id.etPasswordFishing);
    }

    private void initControlsFromState() {
        PlantRecord recPlant = FruitGlobalState.recPlant;

        tvGreenHouse.setText(recPlant.greenhouse != null ? recPlant.greenhouse : "N/A");
        tvPole.setText(recPlant.poleRFID != null ? recPlant.poleRFID : "N/A");
        tvTomatoType.setText(recPlant.speciesName != null ? recPlant.speciesName : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean isAuthenticated() {
        String login = LocalPreferences.getLoggedInUser("").trim();
        String pin = etPIN.getText().toString().trim();

        // use typed-in PIN to compare credentials with those stored in the Local DB.
        AuthenticationService authSvc = new AuthenticationService();
        boolean authentication = authSvc.authenticateUser(this.db, login, pin);

        return authentication;
    }

    private boolean updateState() {
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

    public class SyncTxCallBack implements Callback<PlantTxDTO> {
        @Override
        public void onResponse(Call<PlantTxDTO> call, Response<PlantTxDTO> response) {
            PlantTxDTO rs = response.body();

            if (rs != null || IsDemo) {
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