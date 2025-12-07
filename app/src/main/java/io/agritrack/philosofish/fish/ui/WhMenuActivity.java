package io.agritrack.philosofish.fish.ui;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

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

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.query.EnquiryApi;
import io.agritrack.philosofish.api.sync.PendingCorrelationTxCallBack;
import io.agritrack.philosofish.api.sync.SyncApi;
import io.agritrack.philosofish.api.sync.SyncClusterSitesCallBack;
import io.agritrack.philosofish.api.sync.SyncCurrentEpcsCallBack;
import io.agritrack.philosofish.api.sync.SyncStepCallBack;
import io.agritrack.philosofish.api.sync.SyncUsersCallBack;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.api.upload.UploadingApi;
import io.agritrack.philosofish.common.DeviceUtils;
import io.agritrack.philosofish.common.FileUtils;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.AppUserDTO;
import io.agritrack.philosofish.data.dto.SiteDTO;
import io.agritrack.philosofish.data.dto.common.ReaderDTO;
import io.agritrack.philosofish.data.dto.tx.CorrelationTxDTO;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.type.ConfigDevice;
import io.agritrack.philosofish.data.type.EpcPerDevice;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.SyncAssetDialog;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.wh.InternalAssetActivity;
import io.agritrack.philosofish.fish.ui.wh.correlation.CorrelationMenuActivity;
import io.agritrack.philosofish.fish.ui.wh.incoming.IncomingStartActivity;
import io.agritrack.philosofish.fish.ui.wh.inventory.InventoryAssetActivity;
import io.agritrack.philosofish.fish.ui.wh.outgoing.OutgoingStartActivity;
import io.agritrack.philosofish.fish.ui.wh.search.SearchActivity;
import io.agritrack.philosofish.ui.adapter.HomeMenuAdapter;
import io.agritrack.philosofish.ui.adapter.MenuItem;
import io.agritrack.philosofish.ui.login.LoginActivity;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhMenuActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Outgoing_Idx = 1, Inventory_Idx = 2, Internal_Idx = 3, Correlation_Idx = 4, Search_Idx = 5;
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
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_inventory), InventoryAssetActivity.class, R.drawable.inventory));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_internal), InternalAssetActivity.class, R.drawable.internal_asset));
        menuItemsList.add(new MenuItem(getString(R.string.program), CorrelationMenuActivity.class, R.drawable.program));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_search), SearchActivity.class, R.drawable.search));

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
                    case Inventory_Idx:
                        i = new Intent(appCtx, InventoryAssetActivity.class);
                        break;
                    case Internal_Idx:
                        GlobalState.initWHInternalRecord();
                        i = new Intent(appCtx, InternalAssetActivity.class);
                        break;
                    case Correlation_Idx:
                        i = new Intent(appCtx, CorrelationMenuActivity.class);
                        break;
                    case Search_Idx:
                        i = new Intent(appCtx, SearchActivity.class);
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
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void getCurrentEpcsDevice() {
        try {
            EnquiryApi syncService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            Call<ConfigDevice> getStepCall =
                    syncService.getCurrentEpcsByDevice("Bearer " + token);

            getStepCall.enqueue(new SyncStepCallBack(this.syncResult));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setCurrentEpcsDevice() {
        try {
            EnquiryApi syncService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            ConfigDevice cDev = new ConfigDevice();
            List<EpcPerDevice> epcs = LocalPreferences.getCurrentEpcList();
            String prefix = LocalPreferences.getPrefix();

            // Build request object correctly using setters
            cDev.setPrefix(prefix);
            cDev.setEpcs(epcs);

            Call<ReaderDTO> setCurrentEPcsCall =
                    syncService.setCurrentEpcsByDevice(cDev, "Bearer " + token);

            setCurrentEPcsCall.enqueue(new SyncCurrentEpcsCallBack(this.syncResult));

        } catch (Exception e) {
            e.printStackTrace();
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

//                Call<ResponseBody> uploadJsonFileAsyncCall = upldSvc.uploadCrashLog(filePart, "Bearer " + token);
//                uploadJsonFileAsyncCall.enqueue(new WhMenuActivity.CrashFileUploadCallBack());

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