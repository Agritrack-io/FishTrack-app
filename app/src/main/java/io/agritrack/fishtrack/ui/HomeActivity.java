package io.agritrack.fishtrack.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.api.sync.SyncAssetsCallBack;
import io.agritrack.fishtrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.fishtrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.fishtrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.fishtrack.api.sync.SyncHarvestRequestCallBack;
import io.agritrack.fishtrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.fishtrack.api.sync.SyncUsersCallBack;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.AppUserDTO;
import io.agritrack.fishtrack.data.dto.CageDetailsDTO;
import io.agritrack.fishtrack.data.dto.HarvestRequestDTO;
import io.agritrack.fishtrack.data.dto.SiteDTO;
import io.agritrack.fishtrack.data.dto.common.EmployeeDTO;
import io.agritrack.fishtrack.data.dto.common.FishSpeciesDTO;
import io.agritrack.fishtrack.data.dto.wh.AssetDTO;
import io.agritrack.fishtrack.data.model.HarvestRequest;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.enums.TxStatus;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.adapter.MenuItem;
import io.agritrack.fishtrack.ui.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.fishing.HarvestRequestsActivity;
import io.agritrack.fishtrack.ui.login.LoginActivity;
import io.agritrack.fishtrack.ui.login.api.SyncApi;
import io.agritrack.fishtrack.ui.maintenance.MaintenanceMenuActivity;
import io.agritrack.fishtrack.ui.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import io.agritrack.fishtrack.ui.transport.TransportStartActivity;
import retrofit2.Call;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class HomeActivity extends AppCompatActivity {
    private static final int Fishing_Idx = 0, Transport_Idx = 1, Processing_Idx = 2, Warehouse_Idx = 3, Maintenance_Idx = 4;
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    GridView gvMainMenu;
    ImageButton ivRefresh;
    ProgressDialog progressDialog;
    private MobileDB db;
    private int syncCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_fishing), FishingStartActivity.class, R.drawable.fishing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_transport), TransportStartActivity.class, R.drawable.transport));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_processing), ProcessStartActivity.class, R.drawable.processing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_warehouse), WhMenuActivity.class, R.drawable.warehouse));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_maintenance), MaintenanceMenuActivity.class, R.drawable.maintenance));

        progressDialog = new ProgressDialog(this);

        syncResult.observe(this, response -> {
            syncCounter++;
            if (response == null) {
                hideProgressDialog();
                return;
            }
            if (response != null) {
                progressDialog.setMessage(response);
                if (syncCounter > 6) {
                    hideProgressDialog();
                }
            }
        });

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);

        gvMainMenu = findViewById(R.id.gvMainMenu);
        gvMainMenu.setAdapter(adapter);
        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
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
                            // NO FishingTx in progress
                            openTx = new FishingTransaction();
                            fishingRecord = GlobalState.initFishingTx();
                            openTx.txStatus = TxStatus.PENDING;
                            fishingRecord.txKey = db.fishingTransactionDAO().insert(openTx);

                            // load Harvest Request fetched via Synch op.
                            List<HarvestRequest> harvestRequests = db.harvestRequestsDAO().getAll();
                            if (harvestRequests != null && !harvestRequests.isEmpty()) {
                                i = new Intent(appCtx, HarvestRequestsActivity.class);
                            }
                        }
                        break;
                    case Transport_Idx:
                        i = new Intent(appCtx, TransportStartActivity.class);
                        break;
                    case Processing_Idx:
                        i = new Intent(appCtx, ProcessStartActivity.class);
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
        });

        ivRefresh = (ImageButton) findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(view -> {
            syncCounter = 1;
            showProgressDialog(getString(R.string.syncing));
            invokeSyncAll();
        });

        configHeader();
    }

    protected void configHeader() {
        ImageButton ivBack = (ImageButton) findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
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
            boolean syncResult = true;

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
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Without this user can hide loader by tapping outside screen
        progressDialog.setCancelable(false);
        progressDialog.setMessage(substring);
        progressDialog.show();
    }

    // hide/dismiss Progress bar
    private void hideProgressDialog() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.dismiss();
    }
}