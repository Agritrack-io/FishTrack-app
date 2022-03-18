package io.agritrack.fruit.ui.shipping;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recShipping;
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
import io.agritrack.data.dto.tx.ShippingTxDTO;
import io.agritrack.data.model.tx.items.ShippingTxWithItems;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.ShippingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippingConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;

    private ProgressDialog progressDialog;
    private TextView tvCustomer, tvNumberIfco, tvDriverName, tvLicensePlate, tvUsername;
    private EditText etPIN;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderShippingConfirm);
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
        progressDialog = new ProgressDialog(ShippingConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ShippingConfirmActivity.this);
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
                    CToast(ShippingConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(ShippingConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recShipping.longitude = mLastLocation.getLongitude();
                    recShipping.latitude = mLastLocation.getLatitude();
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
            Intent i = new Intent(getApplicationContext(), ShippingDetailsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvNumberIfco = findViewById(R.id.tvNumberIfco);
        ivSupport = findViewById(R.id.ivSupport);
        tvUsername = findViewById(R.id.tvUsername);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToShippingDetails);
        etPIN = findViewById(R.id.etPasswordFishing);
    }

    private void initControlsFromState() {
        ShippingRecord recShipping = FruitGlobalState.recShipping;

        tvCustomer.setText(recShipping.customer != null ? recShipping.customer : "N/A");
        tvNumberIfco.setText(recShipping.totalIfcoCnt != null ? recShipping.totalIfcoCnt.toString() : "N/A");
        tvDriverName.setText(recShipping.driverName != null ? recShipping.driverName : "N/A");
        tvLicensePlate.setText(recShipping.licensePlate != null ? recShipping.licensePlate : "N/A");

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
        recShipping.state = WarehouseTxState.Outgoing;
        try {
            String token = LocalPreferences.getToken();
            //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

            // persist Transportation Record data to local DB.
            ShippingTxWithItems tx = FruitGlobalState.commitShipping(db);

            // sync fish species
            Call<ShippingTxDTO> syncTxAsyncCall = updService.syncShippingTx(ShippingTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new ShippingConfirmActivity.SyncTxCallBack());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<ShippingTxDTO> {
        @Override
        public void onResponse(Call<ShippingTxDTO> call, Response<ShippingTxDTO> response) {
            ShippingTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Transport TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_shipping_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ShippingTxDTO> call, Throwable error) {
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