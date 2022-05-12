package io.agritrack.fish.ui.wh.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.ui.warehouse.correlation.FruitCorrelationActivity;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrelationBinActivity extends LocationAwareActivity {

    protected BroadcastReceiver keyReceiver;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final CorrelationBinActivity.ScanHandler mScanHandler = new CorrelationBinActivity.ScanHandler(this);
    private MobileDB db;
    private Button btnScanAssetTag, btnCorrelate;
    private TextView tvCorrBinBarcode, tvCorrTempLoggerBarcode;
    private EditText etAssetBarcode;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_bin);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinCorrelation);
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
        progressDialog = new ProgressDialog(CorrelationBinActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        btnCorrelate.setOnClickListener(view -> {
            GlobalState.recWHCorrelation.assetType = Constants.ftBin;
            GlobalState.recWHCorrelation.assetCode = etAssetBarcode.getText() != null ? etAssetBarcode.getText().toString() : null;
            GlobalState.recWHCorrelation.assetRFID = tvCorrBinBarcode.getText() != null ? tvCorrBinBarcode.getText().toString() : null;
            GlobalState.recWHCorrelation.type = Constants.ftDataLogger;
            GlobalState.recWHCorrelation.rfid = tvCorrTempLoggerBarcode.getText() != null ? tvCorrTempLoggerBarcode.getText().toString() : null;

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
            supportDialog = new SupportDialog(CorrelationBinActivity.this);
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
                Intent i = new Intent(getApplicationContext(), CorrelationBinActivity.class);
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
        etAssetBarcode = findViewById(R.id.etAssetBarcode);
        tvCorrBinBarcode = findViewById(R.id.tvCorrBinBarcode);
        tvCorrTempLoggerBarcode = findViewById(R.id.tvCorrTempLoggerBarcode);
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
            Call<CorrelationTxDTO> syncTxAsyncCall = updService.syncCorrelationTx(CorrelationTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new CorrelationBinActivity.SyncTxCallBack());

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
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetCode)) {
                sb.append(String.format("\n%s is missing", "'Bin code'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetRFID)) {
                sb.append(String.format("\n%s is missing", "'Bin RFID'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Logger RFID'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
        scanner_runnable.setFilters(Filters.RFID_BIN, Filters.RFID_LOGGER);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    public class SyncTxCallBack implements Callback<CorrelationTxDTO> {
        @Override
        public void onResponse(Call<CorrelationTxDTO> call, Response<CorrelationTxDTO> response) {
            CorrelationTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
                tvCorrBinBarcode.setText("");
                tvCorrTempLoggerBarcode.setText("");
                etAssetBarcode.setText("");
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<CorrelationTxDTO> call, Throwable error) {
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
        private final WeakReference<CorrelationBinActivity> mActivity;

        public ScanHandler(CorrelationBinActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> tags = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!CollectionUtils.isEmpty(tags)) {
                            for (CharSequence tag : tags) {
                                String epc = tag.toString();
                                if (epc.indexOf(Filters.RFID_BIN) > -1) {
                                    GlobalState.recWHCorrelation.assetRFID = epc;
                                    tvCorrBinBarcode.setText(epc);
                                }
                                else if (epc.indexOf(Filters.RFID_LOGGER) > -1){
                                    GlobalState.recWHCorrelation.rfid = epc;
                                    tvCorrTempLoggerBarcode.setText(epc);
                                }
                            }
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