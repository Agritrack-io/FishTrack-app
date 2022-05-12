package io.agritrack.fish.ui.wh.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrelationPlatformActivity extends LocationAwareActivity {

    protected BroadcastReceiver keyReceiver;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final CorrelationPlatformActivity.ScanHandler mScanHandler = new CorrelationPlatformActivity.ScanHandler(this);
    private MobileDB db;
    private Button btnScanAssetTag, btnCorrelate;
    private TextView tvCorrPlatformBarcode;
    private EditText etPlatformBarcode;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_platform);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPlatformCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

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
        progressDialog = new ProgressDialog(CorrelationPlatformActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        btnCorrelate.setOnClickListener(view -> {
            recWHCorrelation.type = Constants.ftPlatform;
            recWHCorrelation.code = etPlatformBarcode.getText() != null ? etPlatformBarcode.getText().toString() : null;
            recWHCorrelation.rfid = tvCorrPlatformBarcode.getText() != null ? tvCorrPlatformBarcode.getText().toString() : null;

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            if (mLastLocation != null) {
                recWHCorrelation.longitude = mLastLocation.getLongitude();
                recWHCorrelation.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
            //correlate();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationPlatformActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen(){
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = correlate();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), CorrelationPlatformActivity.class);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onPause();
    }

    protected void configFooter() {
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        etPlatformBarcode = findViewById(R.id.etPlatformBarcode);
        tvCorrPlatformBarcode = findViewById(R.id.tvCorrPlatformBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        btnCorrelate = findViewById(R.id.btnCorrelate);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private boolean correlate() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);

            // sync WH Correlation Tx
            ArrayList<CorrelationTxDTO> dtos = new ArrayList<>();
            dtos.add(CorrelationTxDTO.convert(tx));
            Call<String> syncTxAsyncCall = updService.syncAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new CorrelationPlatformActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);

            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.code)) {
                sb.append(String.format("\n%s is missing", "'Platform code'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Platform RFID'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_PLATFORM);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    public class SyncTxCallBack implements Callback<String> {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            String rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
                tvCorrPlatformBarcode.setText("");
                etPlatformBarcode.setText("");
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<String> call, Throwable error) {
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

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<CorrelationPlatformActivity> mActivity;

        public ScanHandler(CorrelationPlatformActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");

                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            GlobalState.recWHCorrelation.assetRFID = epcStr;
                            tvCorrPlatformBarcode.setText(epcStr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render(String.format("No item of type %s was found!", selectedAssetType)), Toast.LENGTH_LONG);
                    }
                    break;
            }
        }
    }
}