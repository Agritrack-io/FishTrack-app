package io.agritrack.fish.ui;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.agritrack.FishTrackApplication;
import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.PendingCorrelationTxCallBack;
import io.agritrack.api.sync.PendingFishingTxCallBack;
import io.agritrack.api.sync.SyncApi;
import io.agritrack.api.sync.SyncAssetsCallBack;
import io.agritrack.api.sync.SyncBinInfo;
import io.agritrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.api.sync.SyncCustomersCallBack;
import io.agritrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.api.sync.SyncFoodSkuCallBack;
import io.agritrack.api.sync.SyncFishingRequestCallBack;
import io.agritrack.api.sync.SyncIOTLoggersCallBack;
import io.agritrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.api.sync.SyncSuppliersCallBack;
import io.agritrack.api.sync.SyncUsersCallBack;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.BinInfoDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.FishingRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.CustomerDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.IotLoggerDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.dto.tx.FishingTxDTO;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.data.dto.wh.FoodSkuDTO;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.enums.TxStatus;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.fishing.FishingStartActivity;
import io.agritrack.fish.ui.fishing.HarvestRequestsActivity;
import io.agritrack.fish.ui.process.ProcessBinsActivity;
import io.agritrack.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.fish.ui.seaTemperature.SeaTemperatureActivity;
import io.agritrack.fish.ui.transport.TransportStartActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class FishHomeActivity extends AppCompatActivity {
    private static final int Fishing_Idx = 0, Transport_Idx = 1, Receiving_Idx = 2, Packaging_Quality_Idx = 3, Warehouse_Idx = 4, /*Maintenance_Idx = 5,*/
            SeaTemp_Idx = 6;
    private static final Map<Integer, String[]> Privileges = new HashMap<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private GridView gvMainMenu;
    private ImageView ivSupport, ivRefresh;
    private ProgressDialog progressDialog;
    private SupportDialog supportDialog;
    private MobileDB db;
    private int syncCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_home);

        //Initialize mapping of roles to menus
        assignPrivilegesToRoles();

        List<String> userRoles = LocalPreferences.getUserRoles();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        Set<MenuItem> menuItemsSet = new LinkedHashSet<MenuItem>();
        if (roleCanAccessMenu(userRoles, Fishing_Idx)) {
            menuItemsSet.add(new MenuItem(Fishing_Idx, getString(R.string.menu_title_fishing), FishingStartActivity.class, R.drawable.fishing));
        }
        if (roleCanAccessMenu(userRoles, Receiving_Idx)) {
            menuItemsSet.add(new MenuItem(Receiving_Idx, getString(R.string.menu_title_fish_receiving), ProcessBinsActivity.class, R.drawable.processing));
        }
        if (roleCanAccessMenu(userRoles, Packaging_Quality_Idx)) {
            menuItemsSet.add(new MenuItem(Packaging_Quality_Idx, getString(R.string.menu_title_fish_packaging), QualitySelectStepsActivity.class, R.drawable.quality));
        }
        if (roleCanAccessMenu(userRoles, Transport_Idx)) {
            menuItemsSet.add(new MenuItem(Transport_Idx, getString(R.string.menu_title_transport), TransportStartActivity.class, R.drawable.transport));
        }
        if (roleCanAccessMenu(userRoles, Warehouse_Idx)) {
            menuItemsSet.add(new MenuItem(Warehouse_Idx, getString(R.string.menu_title_warehouse), WhMenuActivity.class, R.drawable.warehouse));
        }
//        if (roleCanAccessMenu(userRoles, Maintenance_Idx)) {
//            menuItemsSet.add(new MenuItem(Maintenance_Idx, getString(R.string.menu_title_maintenance), MaintenanceMenuActivity.class, R.drawable.maintenance));
//        }
        if (roleCanAccessMenu(userRoles, SeaTemp_Idx)) {
            menuItemsSet.add(new MenuItem(SeaTemp_Idx, getString(R.string.menu_title_sea_temp), SeaTemperatureActivity.class, R.drawable.sea_temp));
        }


        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(FishHomeActivity.this);
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

        List<MenuItem> miList = menuItemsSet.stream().sorted(Comparator.comparingInt(MenuItem::getLoc)).collect(Collectors.toList());
        HomeMenuAdapter adapter = new HomeMenuAdapter(this, (ArrayList<MenuItem>) miList);

        gvMainMenu = findViewById(R.id.gvMainMenu);
        gvMainMenu.setAdapter(adapter);
        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);
                MenuItem mi = (MenuItem) gvMainMenu.getItemAtPosition(position);
                //String clickedText = yourGridView.getItemAtPosition(position).toString());

                switch (mi.getLoc()) {
                    case Fishing_Idx:
                        FishingTransaction openTx = db.fishingTransactionDAO().getMostRecentOpenTx(LocalPreferences.getLoggedInUser(""));
                        FishingRecord fishingRecord;

                        // default Next Activity is FishingStart...
                        i = new Intent(appCtx, FishingStartActivity.class);
                        if (openTx != null) {
                            // there is a FishingTx in progress
                            fishingRecord = FishingRecord.convert(openTx);
                            GlobalState.recFishing = fishingRecord;
                        } else {
                            // instantiate a new Fishing Record.
                            fishingRecord = GlobalState.initFishingRecord();

                            // NO FishingTx in progress
                            openTx = new FishingTransaction();
                            openTx.txStatus = TxStatus.PENDING;
                            fishingRecord.txKey = db.fishingTransactionDAO().insert(openTx);

                            // load Harvest Request fetched via Synch op.
//                            List<FishingRequest> harvestRequests = db.harvestRequestsDAO().getAll();
                            i = new Intent(appCtx, HarvestRequestsActivity.class);
                        }
                        break;
                    case Transport_Idx:
                        GlobalState.initTransportationRecord();
                        i = new Intent(appCtx, TransportStartActivity.class);
                        break;
                    case Receiving_Idx:
                        GlobalState.initProcessingRecord();
                        i = new Intent(appCtx, ProcessBinsActivity.class);
                        break;
                    case Packaging_Quality_Idx:
                        i = new Intent(appCtx, QualitySelectStepsActivity.class);
                        break;
                    case Warehouse_Idx:
                        i = new Intent(appCtx, WhMenuActivity.class);
                        break;
//                    case Maintenance_Idx:
//                        i = new Intent(appCtx, MaintenanceMenuActivity.class);
//                        break;
                    case SeaTemp_Idx:
                        i = new Intent(appCtx, SeaTemperatureActivity.class);
                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishHomeActivity.this);
            supportDialog.showDialog();
        });


        ivRefresh = findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(view -> {
            syncCounter = 1;
            showProgressDialog(getString(R.string.syncing));
            invokeSyncAll();
            invokeUploadPendingAll();
        });

        configHeader();
    }

    protected void configHeader() {
        ImageButton ivBack = findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            LocalPreferences.resetLogin();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void invokeUploadPendingAll() {
        try {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            TransactionApi pendingTxSvc = APIServiceGenerator.createAPI(TransactionApi.class);
            String token = LocalPreferences.getToken();
            UUID siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();


            // select all SIMPLE pending correlation TXs (identification events, e.g. correlate rfid <--> code)
            List<FishingTransaction> fishingTXs = db.fishingTransactionDAO().getAll();
            if(!fishingTXs.isEmpty()) {
                for (FishingTransaction fishingTX : fishingTXs) {
                    Call<FishingTxDTO> fishingTxAsyncCall = pendingTxSvc.syncFishingTx(FishingTxDTO.convert(fishingTX), "Bearer " + token);
                    fishingTxAsyncCall.enqueue(new PendingFishingTxCallBack(this.syncResult));
                }
            }



            //===================================================================================================
            // select all SIMPLE pending correlation TXs (identification events, e.g. correlate rfid <--> code)
            List<CorrelationTransaction> correlationTXs = db.correlationTransactionDAO().getAll();
            if(!correlationTXs.isEmpty()) {
                // filter out the simple correlation transactions
                List<CorrelationTransaction> identifications = correlationTXs.stream().filter(f -> f.assetRFID == null).collect(Collectors.toList());
                // filter out the inter-correlation transactions
                List<CorrelationTransaction> interCorrelations = correlationTXs.stream().filter(f -> f.assetRFID != null).collect(Collectors.toList());

                if(!identifications.isEmpty()) {
                    List<CorrelationTxDTO> identificationDTOs = identifications.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetIdentificationAsyncCall = pendingTxSvc.syncAssetCorrelationTx(identificationDTOs, "Bearer " + token);
                    assetIdentificationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }

                if(!interCorrelations.isEmpty()) {
                    List<CorrelationTxDTO> interCorrelationDTOs = interCorrelations.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetInterCorrelationAsyncCall = pendingTxSvc.syncAssetWithAssetCorrelationTx(interCorrelationDTOs, "Bearer " + token);
                    assetInterCorrelationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private void invokeSyncAll() {
        try {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            UUID siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync harvestRequests for current Site
            Call<List<FishingRequestDTO>> syncHarvestResAsyncCall = syncService.getFishingRequestsBySiteId(siteId, "Bearer " + token);
            syncHarvestResAsyncCall.enqueue(new SyncFishingRequestCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack(this.syncResult));

            // sync suppliers
            Call<List<SupplierDTO>> syncSuppliersAsyncCall = syncService.getSuppliersBySiteId(siteId, "Bearer " + token);
            syncSuppliersAsyncCall.enqueue(new SyncSuppliersCallBack(this.syncResult));

            // sync customers
            Call<List<CustomerDTO>> syncCustomersAsyncCall = syncService.getCustomersBySiteId(siteId, "Bearer " + token);
            syncCustomersAsyncCall.enqueue(new SyncCustomersCallBack(this.syncResult));

            // sync assets  (cages, nets, bins, platforms)
//            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
//            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync only Harvest_Bins assets
            Call<List<AssetDTO>> syncHarvestBinsAsyncCall = syncService.getAssetsByHarvestBinType("Bearer " + token);
            syncHarvestBinsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync Bin Info (complete BinLedger)
            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));

            // sync fish species
            Call<List<SpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCodeAndType(FishTrackApplication.COUNTRY, FishTrackApplication.getProduct(), "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

            // sync IOT Loggers
            Call<List<IotLoggerDTO>> syncIOTLoggersAsyncCall = syncService.getIOTLoggersBySiteId(siteId, "Bearer " + token);
            syncIOTLoggersAsyncCall.enqueue(new SyncIOTLoggersCallBack(this.syncResult));

            // sync Food sku
            Call<List<FoodSkuDTO>> syncFoodSkuAsyncCall = syncService.getFoodSkus("Bearer " + token);
            syncFoodSkuAsyncCall.enqueue(new SyncFoodSkuCallBack(this.syncResult));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    // show Progress bar
    private void showProgressDialog(String substring) {
        //Without this user can hide loader by tapping outside screen
        progressDialog.setCancelable(false);
        progressDialog.setMessage(render(substring));
        progressDialog.show();
    }

    // hide/dismiss Progress bar
    private void hideProgressDialog() {
        progressDialog.dismiss();
    }

    private void assignPrivilegesToRoles() {
        Privileges.put(Fishing_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Receiving_Idx, new String[]{"ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Packaging_Quality_Idx, new String[]{"ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Transport_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Warehouse_Idx, new String[]{"ROLE_FISHING", "ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
//        Privileges.put(Maintenance_Idx, new String[]{"ROLE_PACKAGING", "ROLE_FISHING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(SeaTemp_Idx, new String[]{"ROLE_FISHING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
    }

    private boolean roleCanAccessMenu(List<String> roles, Integer menuId) {
        String[] privileges = Privileges.get(menuId);
        if (privileges != null && privileges.length > 0) {
            for (String role : roles) {
                if (Arrays.stream(privileges).anyMatch(role::equalsIgnoreCase)) {
                    return true;
                }
            }
        }
        return false;
    }
}