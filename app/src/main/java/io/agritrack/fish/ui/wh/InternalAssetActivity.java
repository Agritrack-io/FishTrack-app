package io.agritrack.fish.ui.wh;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHInternal;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InternalAssetActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
    private Button btnScanAsset;
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();
    private TextView tvInternalFrom, tvInternalTo, tvAssetEPC, tvAssetEPCTo, tvAssetEPCFrom;
    private ToggleGroup tgInternalSource, tgInternalDestination;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private SimpleListDialog siteDialog;
    private boolean proceedWithoutLocation = false;
    private String toSite, fromSite;
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private ProgressDialog progressDialog;

    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_asset);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInternalAsset);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // =================================
        // RFID scanning functionality
        btnScanAsset.setOnClickListener(this::onClick);

        toSiteSelection.observe(this, response -> {
            if (response != null) {
                toSite = response;
                tvAssetEPCTo.setText("");
                tvInternalTo.setText(toSite);
                recWHInternal.toSite = toSite;
                siteDialog.dismiss();
            }
        });

        fromSiteSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvAssetEPCFrom.setText("");
                tvInternalFrom.setText(fromSite);
                recWHInternal.fromSite = fromSite;
                siteDialog.dismiss();
            }
        });

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
        progressDialog = new ProgressDialog(InternalAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(InternalAssetActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        btnScanAsset = findViewById(R.id.btnScanAsset);
        tvAssetEPC = findViewById(R.id.tvAssetEPC);
        tgInternalSource = findViewById(R.id.tgInternalSource);
        tgInternalDestination = findViewById(R.id.tgInternalDestination);
        tvInternalFrom = findViewById(R.id.tvInternalFrom);
        tvInternalTo = findViewById(R.id.tvInternalTo);
        tvAssetEPCFrom = findViewById(R.id.tvAssetEPCFrom);
        tvAssetEPCTo = findViewById(R.id.tvAssetEPCTo);
        tgInternalSource.setOnCheckedChangeListener(this);
        tgInternalDestination.setOnCheckedChangeListener(this);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
    }

    private void moveToNextScreen(){
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = updateState();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
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
        super.onStop();
        stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanner();
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable!=null) {
                stopScanner();
            }

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            recWHInternal.state = WarehouseTxState.Internal;
            recWHInternal.site = LocalPreferences.getCurrentSiteName();

            if (mLastLocation != null) {
                recWHInternal.longitude = mLastLocation.getLongitude();
                recWHInternal.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable!=null) {
                stopScanner();
            }

            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private List<String> fillSubSiteData() {
        List<String> result = new ArrayList<>();
        List<Site> subSites = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (subSites != null && !subSites.isEmpty()) {
            result = subSites.stream().map(s -> s.name).collect(Collectors.toList());
        }

        return result;
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbSiteFrom) {
            siteDialog = new SimpleListDialog(InternalAssetActivity.this, fillSubSiteData(), fromSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftSiteFrom;
        } else if (checkedId == R.id.tbSiteTo) {
            siteDialog = new SimpleListDialog(InternalAssetActivity.this, fillSubSiteData(), toSiteSelection, R.string.select_subsite);
            siteDialog.showDialog();
            selectedToggleButtonTo = Constants.ftSiteTo;
        } else if (checkedId == R.id.tbAssetFrom) {
            tgInternalSource.findViewById(R.id.tbAssetFrom).setOnClickListener(this::onClick);
            selectedToggleButtonFrom = Constants.ftCageFrom;
        } else if (checkedId == R.id.tbAssetTo) {
            tgInternalDestination.findViewById(R.id.tbAssetTo).setOnClickListener(this::onClick);
            selectedToggleButtonTo = Constants.ftCageTo;
        }
    }

    private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHRFIDInternal(db);

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncRFIDIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new InternalAssetActivity.SyncTxCallBack());

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
            if (recWHInternal.items == null || recWHInternal.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Asset'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.fromSite) && Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.fromAsset)) {
                sb.append(String.format("\n%s is missing", "'Source'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.toSite) && Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.toAsset)) {
                sb.append(String.format("\n%s is missing", "'Target'"));
            }
        }

        return sb.toString();
    }

    protected void onClick(View view) {
        scanner_runnable.HighEnergy();
        if(view!=null){
            if (view.getId() == tgInternalSource.findViewById(R.id.tbAssetFrom).getId()) {
                scanner_runnable.setFilter(new String[]{Filters.RFID_CAGE});
            } else if (view.getId() == tgInternalDestination.findViewById(R.id.tbAssetTo).getId()){
                //tgInternalSource.clearCheck();
                scanner_runnable.setFilter(new String[]{Filters.RFID_CAGE});
            } else if (view.getId() == btnScanAsset.getId()) {
                scanner_runnable.setFilter(new String[]{Filters.RFID_NET});
            }
        } else {
            scanner_runnable.setFilters(Filters.RFID_NET);
        }
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
            AssetTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_AssetTx_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<AssetTxDTO> call, Throwable error) {
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

    private class ScanHandler extends Handler {
        private final WeakReference<InternalAssetActivity> mActivity;

        public ScanHandler(InternalAssetActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!epcList.isEmpty()) {
                            for (CharSequence epcCharSeq : epcList) {
                                String epc = epcCharSeq.toString();
                                String tag = epc.substring(11);
                                String label = tag.substring(3);
                                if (tag.startsWith(Filters.RFID_NET)) {
                                    tvAssetEPC.setText(label);
                                    Map<String, List<String>> _items = new TreeMap<>();
                                    _items.put(Filters.RFID_NET, Collections.singletonList(epc));
                                    recWHInternal.items = _items;
                                    recWHInternal.assetType = Constants.ftNet;
                                } else if (tag.startsWith(Filters.RFID_CAGE)) {
                                    if (tgInternalSource.getCheckedRadioButtonId() == R.id.tbAssetFrom){
                                        tvAssetEPCFrom.setText(label);
                                        Asset cage = db.assetDAO().getAssetByEpc(epc);
                                        if (cage == null){
                                            CToast(getApplicationContext(), render("No cage was found linked to this RFID! Please correlate cage!"), Toast.LENGTH_LONG);
                                            tvInternalFrom.setText("");
                                            return;
                                        }
                                        tvInternalFrom.setText(cage.code);
                                        recWHInternal.fromAsset = epc;
                                    } else if (tgInternalDestination.getCheckedRadioButtonId() == R.id.tbAssetTo){
                                        tvAssetEPCTo.setText(label);
                                        Asset cage = db.assetDAO().getAssetByEpc(epc);
                                        if (cage == null){
                                            CToast(getApplicationContext(), render("No cage was found linked to this RFID! Please correlate cage!"), Toast.LENGTH_LONG);
                                            tvInternalTo.setText("");
                                            return;
                                        }
                                        tvInternalTo.setText(cage.code);
                                        recWHInternal.toAsset = epc;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}