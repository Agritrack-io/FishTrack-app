package io.agritrack.fish.ui.process;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recProcessing;
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

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.ProcessingTxDTO;
import io.agritrack.data.model.tx.ProcessingTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ProgressDialog progressDialog;
    private TextView tvNumberOfBinsCount, tvDispatchNote, tvPackagingLot, tvSecurityClipNumber, tvUsername;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessConfirm);
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
        progressDialog = new ProgressDialog(ProcessConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProcessConfirmActivity.this);
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
                Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                startActivity(i);
            }
        }
    }

    protected void configFooter() {
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etPIN.getText().toString())) {
                    CToast(ProcessConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(ProcessConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recProcessing.longitude = mLastLocation.getLongitude();
                    recProcessing.latitude = mLastLocation.getLatitude();
                    proceedWithoutLocation = true;
                    moveToNextScreen();
                } else if (!proceedWithoutLocation) {
                    FragmentManager fm = getSupportFragmentManager();
                    confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                }
            }
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ProcessInfoActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDispatchNote = findViewById(R.id.tvDispatchNote);
        tvPackagingLot = findViewById(R.id.tvPackagingLot);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToReceiveBins);
        etPIN = findViewById(R.id.etPasswordProcessing);
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        if (!Strings.isEmptyOrWhitespace(prcRecord.dispatchNote)) {
            tvDispatchNote.setText(prcRecord.dispatchNote);
        }

        if (prcRecord.availBins != null) {
            tvNumberOfBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }

        if (!Strings.isEmptyOrWhitespace(prcRecord.securityClip)) {
            tvSecurityClipNumber.setText(prcRecord.securityClip);
        }

        //tvNumberOfBinsCount.setText(prcRecord.totalBinsUsed != null ? prcRecord.totalBinsUsed.toString() : "N/A");

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
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();
            //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

            // persist Transportation Record data to local DB.
            ProcessingTransaction tx = GlobalState.commitProcessing(db);

            // sync Processing records
            Call<ProcessingTxDTO> syncTxAsyncCall = updService.syncProcessingTx(ProcessingTxDTO.convert(tx), "Bearer " + token);
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

    public class SyncTxCallBack implements Callback<ProcessingTxDTO> {
        @Override
        public void onResponse(Call<ProcessingTxDTO> call, Response<ProcessingTxDTO> response) {
            ProcessingTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_processing_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ProcessingTxDTO> call, Throwable error) {
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