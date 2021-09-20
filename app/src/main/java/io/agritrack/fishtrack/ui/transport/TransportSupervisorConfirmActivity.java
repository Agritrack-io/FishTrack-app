package io.agritrack.fishtrack.ui.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.TransportTxDTO;
import io.agritrack.fishtrack.data.model.tx.TransportTransaction;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.TransportationRecord;
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
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class TransportSupervisorConfirmActivity extends AppCompatActivity {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private TextView tvSitePackaging, tvCompany, tvNumberOfBinsCount, tvDriverName, tvLicensePlate, tvSecurityClipNumber;
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_supervisor_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportSupervisorConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToDriverConfirm);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
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
        tvUsername = findViewById(R.id.tvUsername);
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (!Strings.isEmptyOrWhitespace(trns.packagingSite)) {
            tvSitePackaging.setText(trns.packagingSite);
        }

        if (!Strings.isEmptyOrWhitespace(trns.destinationCompany)) {
            tvCompany.setText(trns.destinationCompany);
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

    private void updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        EditText etPIN = findViewById(R.id.etPasswordTransport);
        if (etPIN.getText() != null) {
            String login = LocalPreferences.getLoggedInUser("").trim();
            String pin = etPIN.getText().toString().trim();

            // use typed-in PIN to compare credentials with those stored in the Local DB.
            AuthenticationService authSvc = new AuthenticationService();
            boolean authentication = authSvc.authenticateUser(this.db, login, pin);

            // credentials do NOT match
            if (!authentication) {
                runOnUiThread(() -> CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG));
            } else {
                try {
                    String token = LocalPreferences.getToken();
                    //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

                    // persist Transportation Record data to local DB.
                    TransportTransaction tx = GlobalState.commitTransport(db);

                    // sync fish species
                    Call<TransportTxDTO> syncTxAsyncCall = updService.syncTransportTx(TransportTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new SyncTxCallBack());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // hideSyncProgress();
                }
            }
        } else {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG));
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

}