package io.agritrack.fish.ui.wh;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.fishing.FishingBinsActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingAssetActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingConsumableActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingStartActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.SiteInfoRS;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.LoggerInitDialogFragment;
import retrofit2.Call;

public class InternalAssetActivity extends AppCompatActivity {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    //private final ScanHandler mScanHandler = new ScanHandler(this);
    private Button btnScanAsset;
    private SingleShotScanner singleShot_runnable;
    private final MutableLiveData<String> toSiteSelection = new MutableLiveData<>();
    private final MutableLiveData<String> fromSiteSelection = new MutableLiveData<>();
    private TextView tvInternalFrom, tvInternalTo, tvAssetEPC;
    private ToggleGroup tgInternalSource, tgInternalDestination;
    private String selectedToggleButtonFrom, selectedToggleButtonTo;
    private SimpleListDialog siteDialog;
    private SiteInfoRS site;
    private boolean proceedWithoutLocation = false;
    private String toSite, fromSite;
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    /*@Override
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
                tvInternalTo.setText(toSite);
                siteDialog.dismiss();
            }
        });

        fromSiteSelection.observe(this, response -> {
            if (response != null) {
                fromSite = response;
                tvInternalFrom.setText(fromSite);
                siteDialog.dismiss();
            }
        });

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
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            GlobalState.recWHIncoming.state = WarehouseTxState.Incoming;
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

            Intent i = new Intent(getApplicationContext(), IncomingStartActivity.class);
            startActivity(i);
        });
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
            syncTxAsyncCall.enqueue(new IncomingAssetActivity.SyncTxCallBack());

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
            *//*if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.incomingItemType)) {
                sb.append(String.format("\n%s is missing", "'Item type'"));
            }*//*

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)) {
                sb.append(String.format("\n%s is missing", "'Source site'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)) {
                sb.append(String.format("\n%s is missing", "'Target site'"));
            }
        }

        return sb.toString();
    }

    protected void onClick(View view) {
        singleShot_runnable = new SingleShotScanner(mScanHandler);
        singleShot_runnable.setFilter(Filters.RFID_NET);
        singleShot_runnable.startReading();
        mScanHandler.postDelayed(singleShot_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.singleShot_runnable != null) {
            this.singleShot_runnable.stopReading();
            mScanHandler.removeCallbacks(this.singleShot_runnable);
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
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            tvAssetEPC.setText(epcStr);
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
    }*/
}