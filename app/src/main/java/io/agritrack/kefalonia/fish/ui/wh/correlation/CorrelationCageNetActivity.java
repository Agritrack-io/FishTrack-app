package io.agritrack.kefalonia.fish.ui.wh.correlation;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.tx.TransactionApi;
import io.agritrack.kefalonia.common.Constants;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.tx.CorrelationTxDTO;
import io.agritrack.kefalonia.data.model.tx.CorrelationTransaction;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.ui.LocationAwareActivity;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrelationCageNetActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final CorrelationCageNetActivity.ScanHandler mScanHandler = new CorrelationCageNetActivity.ScanHandler(this);
    private final MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private Button btnScanAssetTag, btnCorrelate;
    private TextView tvCorrCageBarcode, tvCorrNetBarcode, tvCageCode, tvNetCode;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_cage_net);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCageNetCorrelation);
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
        progressDialog = new ProgressDialog(CorrelationCageNetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        /*btnCorrelate.setOnClickListener(view -> {

            //correlate();
        });*/

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationCageNetActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = correlate();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
                startActivity(i);
            }
        }
    }

    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
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
        super.onStop();
        stopScanner();
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanner();
    }

    protected void configFooter() {
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();
            GlobalState.recWHCorrelation.assetType = Constants.ftCage;
            //GlobalState.recWHCorrelation.assetRFID = tvCorrCageBarcode.getText() != null ? tvCorrCageBarcode.getText().toString() : null;
            GlobalState.recWHCorrelation.type = Constants.ftNet;
            //GlobalState.recWHCorrelation.rfid = tvCorrNetBarcode.getText() != null ? tvCorrNetBarcode.getText().toString() : null;

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
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
        });
    }

    private void assignCtrlVars() {
        tvCorrCageBarcode = findViewById(R.id.tvCorrCageBarcode);
        tvCorrNetBarcode = findViewById(R.id.tvCorrNetBarcode);
        tvCageCode = findViewById(R.id.tvCageCode);
        tvNetCode = findViewById(R.id.tvNetCode);
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
            Call<ResponseBody> syncTxAsyncCall = updService.syncAssetWithAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new CorrelationCageNetActivity.SyncTxCallBack());

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
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetRFID)) {
                sb.append(String.format("\n%s is missing", "'Cage RFID'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetCode)) {
                sb.append(String.format("\n%s is missing", "'Cage code'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Net RFID'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.code)) {
                sb.append(String.format("\n%s is missing", "'Net code'"));
            }
        }
        return sb.toString();
    }

    private boolean deleteCorrelationTx() {
        try {
            System.out.println("About to delete correlate tx");
            CorrelationTransaction delObj = new CorrelationTransaction();
            delObj.id = recWHCorrelation.txKey;
            db.correlationTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    protected void onClick(View view) {
        scanner_runnable.setFilters(Filters.RFID_CAGE, Filters.RFID_NET);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    public class SyncTxCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (response.isSuccessful()) {
                deleteCorrelationTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
                tvCorrCageBarcode.setText("");
                tvCorrNetBarcode.setText("");
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
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
        private final WeakReference<CorrelationCageNetActivity> mActivity;

        public ScanHandler(CorrelationCageNetActivity activity) {
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
                                String label = epc.length() > 15 ? epc.substring(14) : epc;
                                if (epc.indexOf(Filters.RFID_CAGE) > -1) {
                                    GlobalState.recWHCorrelation.assetRFID = epc;
                                    tvCorrCageBarcode.setText(label);
                                    Asset cage = db.assetDAO().getAssetByEpc(epc);
                                    if (cage == null) {
                                        CToast(getApplicationContext(), render(R.string.correlate_cage), Toast.LENGTH_LONG);
                                    }
                                    tvCageCode.setText(cage.code);
                                    recWHCorrelation.assetCode = cage.code;
                                } else if (epc.indexOf(Filters.RFID_NET) > -1) {
                                    GlobalState.recWHCorrelation.rfid = epc;
                                    tvCorrNetBarcode.setText(label);
                                    Asset net = db.assetDAO().getAssetByEpc(epc);
                                    if (net == null) {
                                        CToast(getApplicationContext(), render(R.string.correlate_net), Toast.LENGTH_LONG);
                                    }
                                    tvNetCode.setText(net.code);
                                    recWHCorrelation.code = net.code;
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