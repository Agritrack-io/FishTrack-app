package io.agritrack.fish.ui;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.query.EnquiryApi;
import io.agritrack.api.sync.PendingCorrelationTxCallBack;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.api.sync.SyncCurrentEpcsCallBack;
import io.agritrack.api.sync.SyncStepCallBack;
import io.agritrack.api.sync.SyncUsersCallBack;
import io.agritrack.api.upload.UploadingApi;
import io.agritrack.common.DeviceUtils;
import io.agritrack.common.FileUtils;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.ReaderDTO;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.type.ConfigDevice;
import io.agritrack.data.type.EpcPerDevice;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.SyncAssetDialog;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.wh.zebra.incoming.IncomingStartActivity;
import io.agritrack.fish.ui.wh.zebra.outgoing.OutgoingStartActivity;
import io.agritrack.fish.ui.wh.zebra.correlation.ZebraCorrelationMenuActivity;
import io.agritrack.fish.ui.wh.zebra.inventory.ZebraInventoryAssetActivity;
import io.agritrack.fish.ui.wh.zebra.search.ZebraSearchActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhMenuActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Outgoing_Idx = 1, Inventory_Idx = 2, Correlation_Idx = 3, Search_Idx = 4; // InternalIdx = 2,
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    GridView gvWhMainMenu;
    private ImageView ivSupport, ivRefresh;
    private SyncAssetDialog syncAssetDialog;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;
    private MobileDB db;
    private int syncCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wh_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderWhMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        gvWhMainMenu = findViewById(R.id.gvWhMainMenu);

        if (LocalPreferences.getCurrentEpcList() != null) {

        } else {
            getCurrentEpcsDevice();
        }

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_incoming), IncomingStartActivity.class, R.drawable.incoming));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_outgoing), OutgoingStartActivity.class, R.drawable.outgoing));
//        menuItemsList.add(new MenuItem(getString(R.string.menu_title_internal), InternalAssetActivity.class, R.drawable.internal_asset));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_inventory), ZebraInventoryAssetActivity.class, R.drawable.inventory));
        menuItemsList.add(new MenuItem(getString(R.string.program), ZebraCorrelationMenuActivity.class, R.drawable.program));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_search), ZebraSearchActivity.class, R.drawable.search));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);

        gvWhMainMenu.setAdapter(adapter);

        gvWhMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Incoming_Idx:
                        GlobalState.initWHIncomingRecord();
                        i = new Intent(appCtx, IncomingStartActivity.class);
                        break;
                    case Outgoing_Idx:
                        GlobalState.initWHOutgoingRecord();
                        i = new Intent(appCtx, OutgoingStartActivity.class);
                        break;
//                    case InternalIdx:
//                        GlobalState.initWHInternalRecord();
//                        i = new Intent(appCtx, InternalAssetActivity.class);
//                        break;
                    case Inventory_Idx:
                        i = new Intent(appCtx, ZebraInventoryAssetActivity.class);
                        break;
                    case Correlation_Idx:
                        i = new Intent(appCtx, ZebraCorrelationMenuActivity.class);
                        break;
                    case Search_Idx:
                        i = new Intent(appCtx, ZebraSearchActivity.class);
                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(WhMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        syncResult.observe(this, response -> {
            syncCounter++;
            if (response == null) {
                hideProgressDialog();
                return;
            }
            if (response != null) {
                progressDialog.setMessage(render(response));
                if (syncCounter > 6) {
                    hideProgressDialog();
                }
            }
        });

        ivRefresh = findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(view -> {
            invokeSyncAll();
            invokeUploadPendingAll();
            syncAssetDialog = new SyncAssetDialog(WhMenuActivity.this);
            syncAssetDialog.showDialog();
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(WhMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            LocalPreferences.resetLogin();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void getCurrentEpcsDevice() {
        try {
            EnquiryApi syncService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();
            String deviceID = DeviceUtils.getIMEIDeviceId(this);

            // sync sites for current cluster
            Call<ConfigDevice> getStepCall = syncService.getCurrentEpcsByDevice(deviceID, "Bearer " + token);
            getStepCall.enqueue(new SyncStepCallBack(this.syncResult));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void setCurrentEpcsDevice() {
        try {
            EnquiryApi syncService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();
            String deviceID = DeviceUtils.getIMEIDeviceId(this);
            ConfigDevice cDev = new ConfigDevice();
            List<EpcPerDevice> epcs = LocalPreferences.getCurrentEpcList();
            String prefix = LocalPreferences.getPrefix();

            // sync sites for current cluster
            Call<ReaderDTO> setCurrentEPcsCall = syncService.setCurrentEpcsByDevice(deviceID, cDev, "Bearer " + token);
            setCurrentEPcsCall.enqueue(new SyncCurrentEpcsCallBack(this.syncResult));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void invokeSyncAll() {
        try {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
            String token = LocalPreferences.getToken();
            UUID siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            //Traverse the crash folder in the sd card to get each file
            File file = new File(WhMenuActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "agriLogs");
            File[] files = file.listFiles();

            for (File f : files) {
                String strFileName = f.getName();
                //Upload file using okhttp post
                //File jsonFile = new File(FishHomeActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), strFileName);

                // create RequestBody instance from file
                RequestBody requestFile = RequestBody.create(f, MediaType.parse("text/plain"));

                // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part filePart = MultipartBody.Part.createFormData("crashLog", strFileName, requestFile);

                Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadCrashLog(filePart, "Bearer " + token);
                uploadJsonFileAsyncCall.enqueue(new WhMenuActivity.CrashFileUploadCallBack());

                //Delete the uploaded file crash folder
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void invokeUploadPendingAll() {
        try {
            TransactionApi pendingTxSvc = APIServiceGenerator.createAPI(TransactionApi.class);
            String token = LocalPreferences.getToken();

            //===================================================================================================
            // select all SIMPLE pending correlation TXs (identification events, e.g. correlate rfid <--> code)
            List<CorrelationTransaction> correlationTXs = db.correlationTransactionDAO().getAll();
            if (!correlationTXs.isEmpty()) {
                // filter out the simple correlation transactions
                List<CorrelationTransaction> identifications = correlationTXs.stream().filter(f -> f.assetRFID == null).collect(Collectors.toList());
                // filter out the inter-correlation transactions
                List<CorrelationTransaction> interCorrelations = correlationTXs.stream().filter(f -> f.assetRFID != null).collect(Collectors.toList());

                if (!identifications.isEmpty()) {
                    List<CorrelationTxDTO> identificationDTOs = identifications.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetIdentificationAsyncCall = pendingTxSvc.syncAssetCorrelationTx(identificationDTOs, "Bearer " + token);
                    assetIdentificationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }

                if (!interCorrelations.isEmpty()) {
                    List<CorrelationTxDTO> interCorrelationDTOs = interCorrelations.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetInterCorrelationAsyncCall = pendingTxSvc.syncAssetWithAssetCorrelationTx(interCorrelationDTOs, "Bearer " + token);
                    assetInterCorrelationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }
            }
        } catch (Exception e) {

        } finally {

        }
    }

    // hide/dismiss Progress bar
    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    public class CrashFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.isSuccessful()) {
                    String fileName = response.body().string();
                    FileUtils.deleteCrashFile(WhMenuActivity.this, fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.file_failed_to_sync), Toast.LENGTH_LONG));
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