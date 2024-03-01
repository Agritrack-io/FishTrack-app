package io.agritrack.kefalonia.fish.ui.process;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.IsOnline;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recFishing;
import static io.agritrack.kefalonia.fish.state.GlobalState.recProcessing;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.kefalonia.fish.ui.FishHomeActivity;
import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.tx.TransactionApi;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.tx.ProcessingTxDTO;
import io.agritrack.kefalonia.data.model.tx.ProcessingTransaction;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.state.ProcessingRecord;
import io.agritrack.kefalonia.ui.LocationAwareActivity;
import io.agritrack.kefalonia.ui.service.AuthenticationService;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ProgressDialog progressDialog;
    private TextView tvNumberOfBinsCount, tvDispatchNote, tvPackagingLot, tvUsername;
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

        etPIN.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable editable) {
                // get credential string values
                final String username = LocalPreferences.getLoggedInUser("").trim();
                final String pin = editable.toString().trim();

                if (pin.isEmpty()) {
                    CToast(ProcessConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                } else if (editable != null && editable.length() == 4) {
                    // invoke login
                    boolean userIsValid = isAuthenticated(username, pin);
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
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
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
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToReceiveBins);
        etPIN = findViewById(R.id.etPasswordProcessing);
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = recProcessing;

        if (!Strings.isEmptyOrWhitespace(prcRecord.dispatchNote)) {
            tvDispatchNote.setText(prcRecord.dispatchNote);
        }

        if (prcRecord.availBins != null) {
            tvNumberOfBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }

        //tvNumberOfBinsCount.setText(prcRecord.totalBinsUsed != null ? prcRecord.totalBinsUsed.toString() : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean isAuthenticated(String login, String pin) {
//        String login = LocalPreferences.getLoggedInUser("").trim();
//        String pin = etPIN.getText().toString().trim();

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

            // persist Transportation Record data to local DB.
            ProcessingTransaction tx = GlobalState.commitProcessing(db);

            if (IsOnline) {
                // sync Processing records
                Call<ProcessingTxDTO> syncTxAsyncCall = updService.syncProcessingTx(ProcessingTxDTO.convert(tx), "Bearer " + token);
                syncTxAsyncCall.enqueue(new SyncTxCallBack());
            } else {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private boolean deleteProcessTx() {
        try {
            System.out.println("About to delete process tx");
            ProcessingTransaction delObj = new ProcessingTransaction();
            delObj.id = recProcessing.txKey;
            db.processingTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<ProcessingTxDTO> {
        @Override
        public void onResponse(Call<ProcessingTxDTO> call, Response<ProcessingTxDTO> response) {
            ProcessingTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                deleteProcessTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_processing_tx_update_failure), Toast.LENGTH_SHORT));
            }
        }

        @Override
        public void onFailure(Call<ProcessingTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_SHORT));
            } else if (error instanceof IOException) {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_SHORT));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + error.getLocalizedMessage()), Toast.LENGTH_SHORT));
                }
            }
        }
    }
}