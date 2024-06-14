package io.agritrack.philosofish.fish.ui.fishing;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.FileUtils.saveCrashInfo2File;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
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

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.FishingTxDTO;
import io.agritrack.philosofish.data.model.FishingRequest;
import io.agritrack.philosofish.data.model.tx.FishingTransaction;
import io.agritrack.philosofish.dialog.InfoDialog;
import io.agritrack.philosofish.dialog.SelectReasonOfFishingWeightDeviationDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.FishHomeActivity;
import io.agritrack.philosofish.ui.LocationAwareActivity;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FishingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private SelectReasonOfFishingWeightDeviationDialog selectReasonDialog;

    private ProgressDialog progressDialog;
    private TextView tvTotalQuantityCount, tvReqQuantityCount, tvNumberOfBinsCount, tvNameCage, tvTypeOfFishConfirm, tvUsername;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private ImageView ivInfo;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingConfirm);
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
        progressDialog = new ProgressDialog(FishingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        if (recFishing.reqWeight != null && recFishing.totalFishWeight != null) {
            if (recFishing.reqWeight - recFishing.totalFishWeight >= 250) {
                selectReasonDialog = new SelectReasonOfFishingWeightDeviationDialog(FishingConfirmActivity.this);
                selectReasonDialog.showDialog();
            } else if (recFishing.totalFishWeight - recFishing.reqWeight >= 250) {
                CToast(FishingConfirmActivity.this,
                        render(String.format(getString(R.string.weight_deviation), recFishing.totalFishWeight - recFishing.reqWeight)),
                        Toast.LENGTH_LONG);
            }
        }

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingConfirmActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingConfirmActivity.this);
            infoDialog.showDialog();
        });

        etPIN.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable editable) {
                // get credential string values
                final String username = LocalPreferences.getLoggedInUser("").trim();
                final String pin = editable.toString().trim();

                if (pin.isEmpty()) {
                    CToast(FishingConfirmActivity.this, render(getString(R.string.missing_pin)), Toast.LENGTH_LONG);
                } else if (editable != null && editable.length() == 4) {
                    // invoke login
                    boolean userIsValid = isAuthenticated(username, pin);
                    if (!userIsValid) {
                        CToast(FishingConfirmActivity.this, render(getString(R.string.invalid_password)), Toast.LENGTH_LONG);
                        return;
                    } else if (mLastLocation != null) {
                        recFishing.longitude = mLastLocation.getLongitude();
                        recFishing.latitude = mLastLocation.getLatitude();
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
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToFillBins);
        etPIN = findViewById(R.id.etPasswordFishing);
    }

    private boolean deleteTx() {
        try {
            System.out.println("About to delete fishing tx");
            FishingTransaction delObj = new FishingTransaction();
            delObj.id = recFishing.txKey;
            db.fishingTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    private void initControlsFromState() {
        tvUsername.setText(LocalPreferences.getLoggedInUser(""));

        tvTotalQuantityCount.setText(recFishing.totalFishWeight != null ? recFishing.totalFishWeight.toString() : "N/A");
        tvReqQuantityCount.setText(recFishing.reqWeight != null ? recFishing.reqWeight.toString() : "N/A");
        tvNumberOfBinsCount.setText(recFishing.totalBinsUsed != null ? recFishing.totalBinsUsed.toString() : "N/A");
        tvNameCage.setText(recFishing.cageCode != null ? recFishing.cageCode : "N/A");
        tvTypeOfFishConfirm.setText(recFishing.speciesName != null ? recFishing.speciesName : "N/A");
    }

    private boolean isAuthenticated(String login, String pin) {
        // use typed-in PIN to compare credentials with those stored in the Local DB.
        AuthenticationService authSvc = new AuthenticationService();
        boolean authentication = authSvc.authenticateUser(this.db, login, pin);

        return authentication;
    }

    private boolean updateState() {
        try {
            String token = LocalPreferences.getToken();

            // persist Fishing Record data to local DB.
            FishingTransaction tx = GlobalState.commitFishing(db, Boolean.TRUE);

            // Delete harvest request since it is executed
            if (recFishing.fishingRq != null) {
                FishingRequest hDelObj = new FishingRequest();
                hDelObj.requestId = recFishing.fishingRq;
                db.fishingRequestsDAO().delete(hDelObj);
            }

            if (IsOnline) {
                // sync fish tx
                Call<FishingTxDTO> syncTxAsyncCall = updService.syncFishingTx(FishingTxDTO.convert(tx), "Bearer " + token);
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
            saveCrashInfo2File(e);
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<FishingTxDTO> {
        @Override
        public void onResponse(Call<FishingTxDTO> call, Response<FishingTxDTO> response) {
            if (response.isSuccessful() || IsDemo) {
                deleteTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
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
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }

}