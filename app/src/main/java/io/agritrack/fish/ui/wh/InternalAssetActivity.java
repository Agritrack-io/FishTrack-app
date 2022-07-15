package io.agritrack.fish.ui.wh;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
import static io.agritrack.fish.state.GlobalState.recWHInternal;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

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

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.model.CageDetails;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.ExpandableListDialog;
import io.agritrack.dialog.ScanAssetDialog;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.fishing.FishingBinsActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingAssetActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingConsumableActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingStartActivity;
import io.agritrack.fish.ui.wh.outgoing.OutgoingAssetActivity;
import io.agritrack.fish.ui.wh.outgoing.OutgoingStartActivity;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.SiteInfoRS;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InternalAssetActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private Button btnScanAsset;
    private SingleShotScanner singleShot_runnable;
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> toAssetSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromAssetSelection = new MutableLiveData<>();
    private TextView tvInternalFrom, tvInternalTo, tvAssetEPC, tvAssetEPCTo, tvAssetEPCFrom;
    private ToggleGroup tgInternalSource, tgInternalDestination;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private SimpleListDialog siteDialog;
    private ScanAssetDialog assetDialog;
    private SiteInfoRS site;
    private boolean proceedWithoutLocation = false;
    private String toSite, fromSite, toAsset, fromAsset;
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
                siteDialog.dismiss();
            }
        });

        fromSiteSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvAssetEPCFrom.setText("");
                tvInternalFrom.setText(fromSite);
                siteDialog.dismiss();
            }
        });

        toAssetSelection.observe(this, response -> {
            if (response != null) {
                toAsset = response;
                tvInternalTo.setText(toAsset);
                assetDialog.dismiss();
            }
        });

        fromAssetSelection.observe(this, response -> {
            if (response != null) {
                fromAsset = response;
                tvInternalFrom.setText(fromAsset);
                assetDialog.dismiss();
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
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (singleShot_runnable!=null) {
                stopScanner();
            }
            if (!Strings.isEmptyOrWhitespace(selectedToggleButtonFrom)) {
                recWHInternal.selectedToggleButtonFrom = selectedToggleButtonFrom;
            }

            if (!Strings.isEmptyOrWhitespace(selectedToggleButtonTo)) {
                recWHInternal.selectedToggleButtonTo = selectedToggleButtonTo;
            }

            if (!Strings.isEmptyOrWhitespace(String.valueOf(tvInternalFrom))) {
                recWHInternal.from = tvInternalFrom.getText().toString();
            }

            if (!Strings.isEmptyOrWhitespace(String.valueOf(tvInternalTo))) {
                recWHInternal.to = tvInternalTo.getText().toString();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            GlobalState.recWHIncoming.state = WarehouseTxState.Internal;
            GlobalState.recWHIncoming.site = LocalPreferences.getCurrentSiteName();

            if (mLastLocation != null) {
                recWHIncoming.longitude = mLastLocation.getLongitude();
                recWHIncoming.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (singleShot_runnable!=null) {
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

    private List<String> loadCagesFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<String> result = new ArrayList<>();
        List<Asset> assetsList = db.assetDAO().getAssetsForTypeAndSite(Constants.ftCage, LocalPreferences.getCurrentSiteName());
        if (assetsList != null && !assetsList.isEmpty()) {
            result = assetsList.stream().map(s -> s.code).collect(Collectors.toList());

            /*List<io.agritrack.ui.bo.GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);*/
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
            /*assetDialog = new ScanAssetDialog(InternalAssetActivity.this, fromAssetSelection, R.string.select_cage);
            assetDialog.showDialog();
            selectedToggleButtonFrom = Constants.ftCageFrom;*/
        } else if (checkedId == R.id.tbAssetTo) {
            tgInternalDestination.findViewById(R.id.tbAssetTo).setOnClickListener(this::onClick);
            selectedToggleButtonTo = Constants.ftCageTo;
            /*assetDialog = new ScanAssetDialog(InternalAssetActivity.this, toAssetSelection, R.string.select_cage);
            assetDialog.showDialog();
            selectedToggleButtonTo = Constants.ftCageTo;*/
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
            AssetTransaction tx = GlobalState.commitWHRFIDIncoming(db);

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
            if (Strings.isEmptyOrWhitespace(recWHInternal.internalItem)) {
                sb.append(String.format("\n%s is missing", "'Asset'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.from)) {
                sb.append(String.format("\n%s is missing", "'Source'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHInternal.to)) {
                sb.append(String.format("\n%s is missing", "'Target'"));
            }
        }

        return sb.toString();
    }

    protected void onClick(View view) {
        /*singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_NET);
        singleShot_runnable.startReading();*/

        MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
        scanner_runnable.LowEnergy();
        if(view!=null){
            if(view.getId() == tgInternalSource.findViewById(R.id.tbAssetFrom).getId() || view.getId() == tgInternalDestination.findViewById(R.id.tbAssetTo).getId() ){
                scanner_runnable.setFilter(new String[]{Filters.RFID_CAGE});
            } else if (view.getId() == btnScanAsset.getId()) {
                scanner_runnable.setFilter(new String[]{Filters.RFID_NET});
            }
        }
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.singleShot_runnable != null) {
            this.singleShot_runnable.stopReading();
            mScanHandler.removeCallbacks(this.singleShot_runnable);
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
                    /*String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            tvAssetEPC.setText(epcStr.substring(epcStr.length()-10));
                            recWHInternal.internalItem = epcStr;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!epcList.isEmpty()) {
                            for (CharSequence epcCharSeq : epcList) {
                                String epc = epcCharSeq.toString();
                                String tag = epc.substring(11);
                                String label = tag.substring(3);
                                if (tag.startsWith(Filters.RFID_NET)) {
                                    tvAssetEPC.setText(label);
                                    recWHInternal.internalItem = epc;
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
                                        recWHInternal.from = tvInternalFrom.getText().toString();
                                    } else if (tgInternalDestination.getCheckedRadioButtonId() == R.id.tbAssetTo){
                                        tvAssetEPCTo.setText(label);
                                        Asset cage = db.assetDAO().getAssetByEpc(epc);
                                        if (cage == null){
                                            CToast(getApplicationContext(), render("No cage was found linked to this RFID! Please correlate cage!"), Toast.LENGTH_LONG);
                                            tvInternalTo.setText("");
                                            return;
                                        }
                                        tvInternalTo.setText(cage.code);
                                        recWHInternal.to = tvInternalTo.getText().toString();
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