package io.agritrack.hotel.ui;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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
import io.agritrack.api.sync.SyncAssetsCallBack;
import io.agritrack.api.sync.SyncBinsByPackagingSite;
import io.agritrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.api.sync.SyncCustomersCallBack;
import io.agritrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.api.sync.SyncHarvestRequestCallBack;
import io.agritrack.api.sync.SyncIOTLoggersCallBack;
import io.agritrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.api.sync.SyncSuppliersCallBack;
import io.agritrack.api.sync.SyncUsersCallBack;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.BinInfoDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.HarvestRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.CustomerDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.IotLoggerDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.fishing.FishingStartActivity;
import io.agritrack.fish.ui.process.ProcessBinsActivity;
import io.agritrack.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.fish.ui.seaTemperature.SeaTemperatureActivity;
import io.agritrack.fish.ui.transport.TransportStartActivity;
import io.agritrack.hotel.ui.incoming.HotelIncomingStartActivity;
import io.agritrack.hotel.ui.inventory.HotelInventoryStartActivity;
import io.agritrack.hotel.ui.outgoing.HotelOutgoingStartActivity;
import io.agritrack.hotel.ui.search.HotelSearchActivity;
import io.agritrack.hotel.ui.status.HotelChangeStatusActivity;
import io.agritrack.hotel.ui.sync.HotelSynchronizeActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.login.api.SyncApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class HotelHomeActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Outgoing_Idx = 1, Inventory_Idx = 2, Status_Idx = 3, Search_Idx = 4, Sync_Idx = 5;
    GridView gvMainMenu;
    private static final Map<Integer, String[]> Privileges = new HashMap<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private ProgressDialog progressDialog;
    private ImageView ivSupport, ivRefresh;
    private SupportDialog supportDialog;
    private MobileDB db;
    private int syncCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_home);

        //Initialize mapping of roles to menus
        assignPrivilegesToRoles();

        List<String> userRoles = LocalPreferences.getUserRoles();

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        Set<MenuItem> menuItemsSet = new LinkedHashSet<MenuItem>();
        if (roleCanAccessMenu(userRoles, Incoming_Idx)) {
            menuItemsSet.add(new MenuItem(Incoming_Idx, getString(R.string.menu_title_incoming), HotelIncomingStartActivity.class, R.drawable.incoming));
        }
        if (roleCanAccessMenu(userRoles, Outgoing_Idx)) {
            menuItemsSet.add(new MenuItem(Outgoing_Idx, getString(R.string.menu_title_outgoing), HotelOutgoingStartActivity.class, R.drawable.outgoing));
        }
        if (roleCanAccessMenu(userRoles, Inventory_Idx)) {
            menuItemsSet.add(new MenuItem(Inventory_Idx, getString(R.string.menu_title_inventory), HotelInventoryStartActivity.class, R.drawable.inventory));
        }
        if (roleCanAccessMenu(userRoles, Status_Idx)) {
            menuItemsSet.add(new MenuItem(Status_Idx, getString(R.string.menu_title_status), HotelChangeStatusActivity.class, R.drawable.status));
        }
        if (roleCanAccessMenu(userRoles, Search_Idx)) {
            menuItemsSet.add(new MenuItem(Search_Idx, getString(R.string.menu_title_search), HotelSearchActivity.class, R.drawable.search));
        }
        if (roleCanAccessMenu(userRoles, Sync_Idx)) {
            menuItemsSet.add(new MenuItem(Sync_Idx, getString(R.string.menu_title_synchronize), HotelSynchronizeActivity.class, R.drawable.syncfile));
        }

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HotelHomeActivity.this);
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

        /*ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(Incoming_Idx, new MenuItem(getString(R.string.menu_title_incoming), HotelIncomingStartActivity.class, R.drawable.incoming));
        menuItemsList.add(Outgoing_Idx, new MenuItem(getString(R.string.menu_title_outgoing), HotelOutgoingStartActivity.class, R.drawable.outgoing));
        menuItemsList.add(Inventory_Idx, new MenuItem(getString(R.string.menu_title_inventory), HotelInventoryStartActivity.class, R.drawable.inventory));
        menuItemsList.add(Search_Idx, new MenuItem(getString(R.string.menu_title_search), HotelSearchActivity.class, R.drawable.search));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_synchronize), HotelSynchronizeActivity.class, R.drawable.synchronization));*/

        //HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvMainMenu = findViewById(R.id.gvMainMenu);
        gvMainMenu.setAdapter(adapter);

        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);
                MenuItem mi = (MenuItem) gvMainMenu.getItemAtPosition(position);

                switch (mi.getLoc()) {
                    case Incoming_Idx:
                        GlobalState.initWHIncomingRecord();
                        i = new Intent(appCtx, HotelIncomingStartActivity.class);
                        break;
                    case Outgoing_Idx:
                        GlobalState.initWHOutgoingRecord();
                        i = new Intent(appCtx, HotelOutgoingStartActivity.class);
                        break;
                    case Inventory_Idx:
                        i = new Intent(appCtx, HotelInventoryStartActivity.class);
                        break;
                    case Status_Idx:
                        i = new Intent(appCtx, HotelChangeStatusActivity.class);
                        break;
                    case Search_Idx:
                        i = new Intent(appCtx, HotelSearchActivity.class);
                        break;
                    case Sync_Idx:
                        i = new Intent(appCtx, HotelSynchronizeActivity.class);
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
            supportDialog = new SupportDialog(HotelHomeActivity.this);
            supportDialog.showDialog();
        });


        ivRefresh = findViewById(R.id.ivRefresh);
        ivRefresh.setOnClickListener(view -> {
            syncCounter = 1;
            showProgressDialog(getString(R.string.syncing));
            invokeSyncAll();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToLogin);
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
            UUID siteId = LocalPreferences.getCurrentSiteId();
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

            // sync customers
            Call<List<CustomerDTO>> syncCustomersAsyncCall = syncService.getCustomersBySiteId(siteId, "Bearer " + token);
            syncCustomersAsyncCall.enqueue(new SyncCustomersCallBack(this.syncResult));

            // sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getBinsByPlant(siteId, "Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinsByPackagingSite(this.syncResult));

            // sync fish species
            Call<List<SpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCodeAndType(FishTrackApplication.COUNTRY, FishTrackApplication.getProduct(), "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

            // sync IOT Loggers
            Call<List<IotLoggerDTO>> syncIOTLoggersAsyncCall = syncService.getIOTLoggersBySiteId(siteId, "Bearer " + token);
            syncIOTLoggersAsyncCall.enqueue(new SyncIOTLoggersCallBack(this.syncResult));

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
        Privileges.put(Incoming_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Outgoing_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Inventory_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Status_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Search_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Sync_Idx, new String[]{"ROLE_WAREHOUSE", "ROLE_SUPER_USER", "ROLE_ADMIN"});
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