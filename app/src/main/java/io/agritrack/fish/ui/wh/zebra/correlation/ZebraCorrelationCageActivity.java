package io.agritrack.fish.ui.wh.zebra.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.ui.custom.CustomToast.CToast;

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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.bo.GenericListModel;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZebraCorrelationCageActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final ZebraCorrelationCageActivity.ScanHandler mScanHandler = new ZebraCorrelationCageActivity.ScanHandler(this);
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private Button btnScanAssetTag, btnCorrelate;
    private SearchView svSearchAsset;
    private RecyclerView rvCages;
    private TextView tvCorrCageBarcode;
    private FilterableAdapter adapterAssets;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_correlation_cage);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCageCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        svSearchAsset.setIconifiedByDefault(false);

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
        progressDialog = new ProgressDialog(ZebraCorrelationCageActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        loadCagesFromLocalDB(Constants.ftCage);

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ZebraCorrelationCageActivity.this);
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
                Intent i = new Intent(getApplicationContext(), ZebraCorrelationCageActivity.class);
                startActivity(i);
            }
        }
    }

    private void loadCagesFromLocalDB(String assetType) {
        // load assets for current Site and filter by asset type (if selected).
        this.rvCages.setAdapter(null);
        this.rvCages.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<Asset> assetsList = db.assetDAO().getAssetsForType(assetType.toUpperCase(Locale.ROOT));
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid, x.code, x.netEyeGirth, x.perimeter)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            adapterAssets.notifyDataSetChanged();
            this.rvCages.setAdapter(adapterAssets);
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
            Intent i = new Intent(getApplicationContext(), ZebraCorrelationSubMenuActivity.class);
            i.putExtra("id", 0);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();
            GlobalState.recWHCorrelation.type = Constants.ftCage;
            GlobalState.recWHCorrelation.code = adapterAssets.getSelectedValue();
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
        });
    }

    private void assignCtrlVars() {
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvCages = findViewById(R.id.rvCages);
        tvCorrCageBarcode = findViewById(R.id.tvCorrCageBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);

        rvCages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvCages.setItemAnimator(new DefaultItemAnimator());

        svSearchAsset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapterAssets != null && adapterAssets.getFilter() != null) {
                    adapterAssets.getFilter().filter(newText);
                }
                return false;
            }
        });
        svSearchAsset.setOnClickListener(view -> {
            int kk = 0;
        });
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
            Asset cage = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
            cage.rfid = GlobalState.recWHCorrelation.rfid;
            db.assetDAO().update(cage);

            // sync WH Correlation Tx
            ArrayList<CorrelationTxDTO> dtos = new ArrayList<>();
            dtos.add(CorrelationTxDTO.convert(tx));
            Call<ResponseBody> syncTxAsyncCall = updService.syncAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new ZebraCorrelationCageActivity.SyncTxCallBack());

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
                sb.append(String.format("\n%s is missing", "'Cage code'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Cage RFID'"));
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
        tvCorrCageBarcode.setText("");
        scanner_runnable.setFilter(Filters.RFID_CAGE);
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
        private final WeakReference<ZebraCorrelationCageActivity> mActivity;

        public ScanHandler(ZebraCorrelationCageActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            if (adapterAssets.getSelectedValue() != null) {
                                String label = epcStr.length() > 15 ? epcStr.substring(14) : epcStr;
                                GlobalState.recWHCorrelation.rfid = epcStr;
                                tvCorrCageBarcode.setText(label);
                            } else {
                                CToast(getApplicationContext(), render("Please first select asset!"), Toast.LENGTH_LONG);
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