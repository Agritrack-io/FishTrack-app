package io.agritrack.fruit.ui;

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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.FishTrackApplication;
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
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.ui.harvesting.HarvestingStartActivity;
import io.agritrack.fruit.ui.planting.PlantingStartActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.login.api.SyncApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;

public class FruitHomeActivity extends AppCompatActivity {
    private static final int Planting_Idx = 0, Harvest_Idx = 1, Storage_semi_ready = 2, Packaging_Idx = 3, Storage_ready = 4, Shipping_Idx = 5, Warehouse_Idx = 6, Temp_measure_Idx = 7;
    private static final Map<Integer, String[]> Privileges = new HashMap<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private GridView gvMainMenu;
    private ProgressDialog progressDialog;
    private MobileDB db;
    private int syncCounter = 1;

    private ImageView ivRefresh, ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_home);

        //Initialize mapping of roles to menus
        assignPrivilegesToRoles();

        List<String> userRoles = LocalPreferences.getUserRoles();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        Set<MenuItem> menuItemsSet = new LinkedHashSet<MenuItem>();
        if (roleCanAccessMenu(userRoles, Planting_Idx)) {
            menuItemsSet.add(new MenuItem(Planting_Idx, getString(R.string.menu_title_planting), PlantingStartActivity.class, R.drawable.planting));
        }
        if (roleCanAccessMenu(userRoles, Harvest_Idx)) {
            menuItemsSet.add(new MenuItem(Harvest_Idx, getString(R.string.menu_title_harvest), HarvestingStartActivity.class, R.drawable.harvest));
        }
        /*if (roleCanAccessMenu(userRoles, Storage_semi_ready)) {
            menuItemsSet.add(new MenuItem(Storage_semi_ready, getString(R.string.menu_title_transport), TransportStartActivity.class, R.drawable.transport));
        }
        if (roleCanAccessMenu(userRoles, Packaging_Idx)) {
            menuItemsSet.add(new MenuItem(Packaging_Idx, getString(R.string.menu_title_warehouse), WhMenuActivity.class, R.drawable.warehouse));
        }
        if (roleCanAccessMenu(userRoles, Storage_ready)) {
            menuItemsSet.add(new MenuItem(Storage_ready, getString(R.string.menu_title_maintenance), MaintenanceMenuActivity.class, R.drawable.maintenance));
        }
        if (roleCanAccessMenu(userRoles, Shipping_Idx)) {
            menuItemsSet.add(new MenuItem(Shipping_Idx, getString(R.string.menu_title_sea_temp), SeaTemperatureActivity.class, R.drawable.sea_temp));
        }*/
        if (roleCanAccessMenu(userRoles, Warehouse_Idx)) {
            menuItemsSet.add(new MenuItem(Warehouse_Idx, getString(R.string.menu_title_warehouse), FruitWhMenuActivity.class, R.drawable.warehouse));
        }
        /*if (roleCanAccessMenu(userRoles, Temp_measure_Idx)) {
            menuItemsSet.add(new MenuItem(Temp_measure_Idx, getString(R.string.menu_title_sea_temp), SeaTemperatureActivity.class, R.drawable.sea_temp));
        }*/

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(FruitHomeActivity.this);
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

                switch (mi.getLoc()) {
                    case Planting_Idx:
                        i = new Intent(appCtx, PlantingStartActivity.class);
                        break;
                    case Harvest_Idx:
                        i = new Intent(appCtx, HarvestingStartActivity.class);
                        break;
                    case Warehouse_Idx:
                        i = new Intent(appCtx, FruitWhMenuActivity.class);
                        break;
                    /*case Warehouse_Idx:
                        i = new Intent(appCtx, WhMenuActivity.class);
                        break;
                    case Maintenance_Idx:
                        i = new Intent(appCtx, MaintenanceMenuActivity.class);
                        break;*/
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FruitHomeActivity.this);
            supportDialog.showDialog();
        });

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
            Call<List<SpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCodeAndType(FishTrackApplication.COUNTRY, FishTrackApplication.PRODUCT, "Bearer " + token);
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

    private void assignPrivilegesToRoles() {
        Privileges.put(Planting_Idx, new String[]{"ROLE_SEEDING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Harvest_Idx, new String[]{"ROLE_SEEDING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Storage_semi_ready, new String[]{"ROLE_PACKAGING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Packaging_Idx, new String[]{"ROLE_PACKAGING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Storage_ready, new String[]{"ROLE_PACKAGING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Shipping_Idx, new String[]{"ROLE_PACKAGING","ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Warehouse_Idx, new String[]{"ROLE_SEEDING", "ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
        Privileges.put(Temp_measure_Idx, new String[]{"ROLE_SEEDING", "ROLE_PACKAGING", "ROLE_SUPER_USER", "ROLE_ADMIN"});
    }

    private boolean roleCanAccessMenu(List<String> roles, Integer menuId) {
        String[] privileges = Privileges.get(menuId);
        if (privileges != null && privileges.length>0) {
            for (String role:roles){
                if (Arrays.stream(privileges).anyMatch(role::equalsIgnoreCase)){
                    return true;
                }
            }
        }
        return false;
    }
}
