package io.agritrack.fishtrack.ui.wh.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;

public class InventoryStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private MobileDB db;

    private TextView tvSelectedItemType;
    private ToggleGroup tgInventoryItemType;
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

        configFooter();
    }

    protected void configFooter() {
        /*ImageView ivNext = (ImageView) findViewById(R.id.ivToInventory);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else if (selectedInventoryItemType == Constants.ftAsset){
                Intent i = new Intent(getApplicationContext(), InventoryAssetActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), InventoryConsumableActivity.class);
                startActivity(i);
            }
        });*/

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgInventoryItemType = findViewById(R.id.tgInventoryItemType);
        //tvSelectedItemType = findViewById(R.id.tvSelectedItemType);
        spSite = findViewById(R.id.spSite);

        tgInventoryItemType.setOnCheckedChangeListener(this);
    }

    private InventoryWHRecord updateState() {
        InventoryWHRecord inventoryRecord = GlobalState.initWHInventoryRecord();

        inventoryRecord.site = LocalPreferences.getCurrentSiteName();

        if (spSite.getSelectedItem() != null) {
            inventoryRecord.subSite = spSite.getSelectedItem().toString();
        }
        inventoryRecord.subSitePos = spSite.getSelectedItemPosition();

        if (!Strings.isEmptyOrWhitespace(selectedInventoryItemType)) {
            inventoryRecord.inventoryItemType = selectedInventoryItemType;
        }
        return inventoryRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHInventory.subSite)) {
            sb.append(String.format("\n%s is missing", "'Subsite'"));
        }

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbAsset) {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else {
                selectedInventoryItemType = Constants.ftAsset;
                Intent i = new Intent(getApplicationContext(), InventoryAssetActivity.class);
                startActivity(i);
            }
            //tvSelectedItemType.setText(R.string.asset_type);
        } else if (checkedId == R.id.tbConsumable) {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else {
                selectedInventoryItemType = Constants.ftConsumable;
                Intent i = new Intent(getApplicationContext(), InventoryConsumableActivity.class);
                startActivity(i);
            }
            //tvSelectedItemType.setText(R.string.consumable_type);
        }
    }
}