package io.agritrack.tomato.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.SyncAssetsCallBack;
import io.agritrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.api.sync.SyncHarvestRequestCallBack;
import io.agritrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.api.sync.SyncSuppliersCallBack;
import io.agritrack.api.sync.SyncUsersCallBack;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.HarvestRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.FishSpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.enums.TxStatus;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.fishing.FishingStartActivity;
import io.agritrack.fish.ui.fishing.HarvestRequestsActivity;
import io.agritrack.fish.ui.maintenance.MaintenanceMenuActivity;
import io.agritrack.fish.ui.process.ProcessBinsActivity;
import io.agritrack.fish.ui.transport.TransportStartActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.login.api.SyncApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;

public class TomatoHomeActivity extends AppCompatActivity {
    private static final int Seeding_Idx = 0, Harvest_Idx = 1, Transport_Idx = 2, Packaging_Idx = 3, Shipping_Idx = 4;
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private GridView gvMainMenu;
    private ImageButton ivRefresh;
    private ProgressDialog progressDialog;
    private MobileDB db;
    private int syncCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato_home);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_seeding), FishingStartActivity.class, R.drawable.fishing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_harvest), TransportStartActivity.class, R.drawable.transport));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_transport), ProcessBinsActivity.class, R.drawable.processing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_packaging), WhMenuActivity.class, R.drawable.warehouse));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_shipping), MaintenanceMenuActivity.class, R.drawable.maintenance));

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(TomatoHomeActivity.this);
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

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);

        gvMainMenu = findViewById(R.id.gvMainMenu);
        gvMainMenu.setAdapter(adapter);
       /* gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Seeding_Idx:
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

                            // load Harvest Request fetched via Synch op.
                            List<HarvestRequest> harvestRequests = db.harvestRequestsDAO().getAll();
                            if (harvestRequests != null && !harvestRequests.isEmpty()) {
                                i = new Intent(appCtx, HarvestRequestsActivity.class);
                            } else {
                                // NO FishingTx in progress
                                openTx = new FishingTransaction();
                                openTx.txStatus = TxStatus.PENDING;
                                fishingRecord.txKey = db.fishingTransactionDAO().insert(openTx);
                            }
                        }
                        break;
                    case Transport_Idx:
                        i = new Intent(appCtx, TransportStartActivity.class);
                        break;
                    case Processing_Idx:
                        i = new Intent(appCtx, ProcessBinsActivity.class);
                        break;
                    case Warehouse_Idx:
                        i = new Intent(appCtx, WhMenuActivity.class);
                        break;
                    case Maintenance_Idx:
                        i = new Intent(appCtx, MaintenanceMenuActivity.class);
                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });*/

        ivRefresh = findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(view -> {
            syncCounter = 1;
            showProgressDialog(getString(R.string.syncing));
            invokeSyncAll();
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

    private void invokeSyncAll() {
        try {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            Long siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync harvestRequests for current Site
            Call<List<HarvestRequestDTO>> syncHarvestResAsyncCall = syncService.getHarvestRequestsBySiteId(siteId, "Bearer " + token);
            syncHarvestResAsyncCall.enqueue(new SyncHarvestRequestCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack(this.syncResult));

            // sync suppliers
            Call<List<SupplierDTO>> syncSuppliersAsyncCall = syncService.getSuppliersBySiteId(siteId, "Bearer " + token);
            syncSuppliersAsyncCall.enqueue(new SyncSuppliersCallBack(this.syncResult));

            // sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync fish species
            Call<List<FishSpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCode("gr", "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

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
}
