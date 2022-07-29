package io.agritrack.fish.ui.quality.postpackage;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recQuality;
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
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.PostPackageQualityTxDTO;
import io.agritrack.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.data.model.tx.QualityTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostPackagingQualityConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ProgressDialog progressDialog;
    private TextView tvLot, tvBox, tvFishTemp, tvUsername;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_packaging_quality_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderAfterPackagingQualityConfirm);
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
        progressDialog = new ProgressDialog(PostPackagingQualityConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PostPackagingQualityConfirmActivity.this);
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
                    CToast(PostPackagingQualityConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(PostPackagingQualityConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recQuality.longitude = mLastLocation.getLongitude();
                    recQuality.latitude = mLastLocation.getLatitude();
                    proceedWithoutLocation = true;
                    moveToNextScreen();
                } else if (!proceedWithoutLocation) {
                    FragmentManager fm = getSupportFragmentManager();
                    confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                }
            }
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PostPackagingQualityActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvLot = findViewById(R.id.tvLot);
        tvBox = findViewById(R.id.tvBox);
        tvFishTemp = findViewById(R.id.tvFishTemp);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToAfterPackagingQuality);
        etPIN = findViewById(R.id.etPasswordProcessing);
    }

    private void initControlsFromState() {
        QualityRecord qltRecord = GlobalState.recQuality;

        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        if (recQuality.etT1!=null && recQuality.etT2!=null && recQuality.etT3!=null) {
            tvFishTemp.setText(df.format((recQuality.etT1 + recQuality.etT2 + recQuality.etT3)/3));
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.pLot)) {
            tvLot.setText(qltRecord.pLot);
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.boxSn)) {
            tvBox.setText(qltRecord.boxSn);
        }

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

            // persist Processing Record data to local DB.
            PostPackageQualityTransaction tx = GlobalState.commitPostPackageQuality(db);

            // sync Processing records
            Call<PostPackageQualityTxDTO> syncTxAsyncCall = updService.syncPostPackageQualityTx(PostPackageQualityTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new PostPackagingQualityConfirmActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private boolean deletePostQualityTx(){
        try {
            System.out.println("About to delete quality tx");
            PostPackageQualityTransaction delObj = new PostPackageQualityTransaction();
            delObj.id = recQuality.txKey;
            db.postPackageQualityTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<PostPackageQualityTxDTO> {
        @Override
        public void onResponse(Call<PostPackageQualityTxDTO> call, Response<PostPackageQualityTxDTO> response) {
            PostPackageQualityTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                deletePostQualityTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_postquality_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<PostPackageQualityTxDTO> call, Throwable error) {
            error.printStackTrace();
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