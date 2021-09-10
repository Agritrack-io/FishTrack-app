package io.agritrack.fishtrack.ui.process;

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
import io.agritrack.fishtrack.data.dto.tx.ProcessingTxDTO;
import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.ProcessingRecord;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.AuthenticationService;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;

public class ProcessConfirmActivity extends AppCompatActivity {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private TextView tvNumberOfBinsCount, tvDispatchNote, tvPackagingLot, tvSecurityClipNumber, tvFishCondition, tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessConfirm);
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

        ImageView ivBack = findViewById(R.id.ivBackToReceiveBins);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ProcessBinsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDispatchNote = findViewById(R.id.tvDispatchNote);
        tvPackagingLot = findViewById(R.id.tvPackagingLot);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        tvFishCondition = findViewById(R.id.tvFishCondition);
        tvUsername = findViewById(R.id.tvUsername);
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        if (!Strings.isEmptyOrWhitespace(prcRecord.dispatchNote)) {
            tvDispatchNote.setText(prcRecord.dispatchNote);
        }

        if (!Strings.isEmptyOrWhitespace(prcRecord.pLot)) {
            tvPackagingLot.setText(prcRecord.pLot);
        }

        if (prcRecord.availBins != null) {
            tvNumberOfBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }

        if (!Strings.isEmptyOrWhitespace(prcRecord.fishCondition)) {
            tvFishCondition.setText(prcRecord.fishCondition);
        }

        if (!Strings.isEmptyOrWhitespace(prcRecord.securityClip)) {
            tvSecurityClipNumber.setText(prcRecord.securityClip);
        }

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private void updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        EditText etPIN = findViewById(R.id.etPasswordProcessing);
        if (etPIN.getText() != null) {
            String login = LocalPreferences.getLoggedInUser("").trim();
            String pin = etPIN.getText().toString().trim();

            // use typed-in PIN to compare credentials with those stored in the Local DB.
            AuthenticationService authSvc = new AuthenticationService();
            boolean authentication = authSvc.authenticateUser(this.db, login, pin);

            // credentials do NOT match
            if (!authentication) {
                runOnUiThread(() -> Toast.makeText(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG).show());
            } else {
                try {
                    String token = LocalPreferences.getToken();
                    //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

                    // persist Transportation Record data to local DB.
                    ProcessingTransaction tx = GlobalState.commitProcessing(db);

                    // sync Processing records
                    Call<ProcessingTxDTO> syncTxAsyncCall = updService.syncProcessingTx(ProcessingTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new SyncTxCallBack());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // hideSyncProgress();
                }
            }
        } else {
            runOnUiThread(() -> Toast.makeText(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG).show());
        }
    }

    public class SyncTxCallBack implements Callback<ProcessingTxDTO> {
        @Override
        public void onResponse(Call<ProcessingTxDTO> call, Response<ProcessingTxDTO> response) {
            ProcessingTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG).show());
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), render(R.string.error_processing_tx_update_failure), Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<ProcessingTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG).show());
            } else if (error instanceof IOException) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), render(R.string.error_timeout), Toast.LENGTH_LONG).show());
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG).show());
                } else {
                    //Generic error handling
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), render("Network Error :: " + error.getLocalizedMessage()), Toast.LENGTH_LONG).show());
                }
            }
        }
    }
}