package io.agritrack.fish.ui.wh.inventory;

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

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.InventoryWHRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.InventoryMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class InventoryStartActivity extends AppCompatActivity {

    private MobileDB db;
    private static final int Asset_Idx = 0, Consumable_Idx = 1;
    private GridView gvInventoryMenu;
    private Spinner spSite;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

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
                Intent i = new Intent(appCtx, InventoryStartActivity.class);

                switch (position) {
                    case Asset_Idx:
                        updateState();
                        String vl = validate();
                        if (!Strings.isEmptyOrWhitespace(vl)) {
                            CToast(getApplicationContext(), render("Invalid inputs : " + vl), Toast.LENGTH_LONG);
                        } else {
                            i = new Intent(appCtx, InventoryAssetActivity.class);
                            break;
                        }
                    case Consumable_Idx:
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

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(InventoryStartActivity.this);
            supportDialog.showDialog();
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
        spSite = findViewById(R.id.spSite);
        gvInventoryMenu = findViewById(R.id.gvInventoryMenu);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private InventoryWHRecord updateState() {
        InventoryWHRecord inventoryRecord = GlobalState.initWHInventoryRecord();

        inventoryRecord.selectedSite = LocalPreferences.getCurrentSiteName();

        if (spSite.getSelectedItem() != null) {
            inventoryRecord.subSite = spSite.getSelectedItem().toString();
        }
        inventoryRecord.subSitePos = spSite.getSelectedItemPosition();

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