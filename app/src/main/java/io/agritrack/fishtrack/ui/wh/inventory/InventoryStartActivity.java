package io.agritrack.fishtrack.ui.wh.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.HarvestRequest;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.enums.TxStatus;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.adapter.InventoryMenuAdapter;
import io.agritrack.fishtrack.ui.adapter.MenuItem;
import io.agritrack.fishtrack.ui.custom.CustomToast;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.fishing.HarvestRequestsActivity;
import io.agritrack.fishtrack.ui.login.LoginActivity;
import io.agritrack.fishtrack.ui.maintenance.MaintenanceMenuActivity;
import io.agritrack.fishtrack.ui.process.ProcessBinsActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import io.agritrack.fishtrack.ui.transport.TransportStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class InventoryStartActivity extends AppCompatActivity {

    private MobileDB db;
    private static final int Asset_Idx = 0, Consumable_Idx = 1;
    private TextView tvSelectedItemType;
    private GridView gvInventoryMenu;
    private String selectedInventoryItemType;
    private Spinner spSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> sites = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (sites != null && !sites.isEmpty()) {
            String[] site = sites.stream().map(x -> x.name).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, site);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spSite.setAdapter(hrAdapter);
        }

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.item_inventory_asset), getString(R.string.up_item_inventory_asset), InventoryAssetActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.item_inventory_consumable), getString(R.string.down_item_inventory_consumable), InventoryConsumableActivity.class));

        InventoryMenuAdapter adapter = new InventoryMenuAdapter(this, menuItemsList);

        gvInventoryMenu.setAdapter(adapter);
        gvInventoryMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Asset_Idx:
                        selectedInventoryItemType = Constants.ftAsset;
                        updateState();
                        String vl = validate();
                        if (!Strings.isEmptyOrWhitespace(vl)) {
                            CToast(getApplicationContext(), render("Invalid inputs : " + vl), Toast.LENGTH_LONG);
                        } else {
                            i = new Intent(appCtx, InventoryAssetActivity.class);
                            break;
                        }
                    case Consumable_Idx:
                        selectedInventoryItemType = Constants.ftConsumable;
                        updateState();
                        String vld = validate();
                        if (!Strings.isEmptyOrWhitespace(vld)) {
                            CToast(getApplicationContext(), render("Invalid inputs : " + vld), Toast.LENGTH_LONG);
                        } else {
                            i = new Intent(appCtx, InventoryConsumableActivity.class);
                            break;
                        }
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        //tvSelectedItemType = findViewById(R.id.tvSelectedItemType);
        spSite = findViewById(R.id.spSite);
        gvInventoryMenu = findViewById(R.id.gvInventoryMenu);
    }

    private InventoryWHRecord updateState() {
        InventoryWHRecord inventoryRecord = GlobalState.initWHInventoryRecord();

        inventoryRecord.site = LocalPreferences.getCurrentSiteName();

        if (spSite.getSelectedItem() != null) {
            inventoryRecord.subSite = spSite.getSelectedItem().toString();
        }
        inventoryRecord.subSitePos = spSite.getSelectedItemPosition();
        inventoryRecord.inventoryItemType = selectedInventoryItemType;

        return inventoryRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHInventory.subSite)) {
            sb.append(String.format("\n%s is missing", "'Subsite'"));
        }

        return sb.toString();
    }
}