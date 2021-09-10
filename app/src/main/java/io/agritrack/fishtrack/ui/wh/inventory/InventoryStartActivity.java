package io.agritrack.fishtrack.ui.wh.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class InventoryStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private TextView tvSelectedItemType;
    private ToggleGroup tgInventoryItemType;
    private String selectedInventoryItemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToInventory);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else if (selectedInventoryItemType == Constants.ftAsset){
                Intent i = new Intent(getApplicationContext(), InventoryAssetActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), InventoryConsumableActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgInventoryItemType = findViewById(R.id.tgInventoryItemType);
        tvSelectedItemType = findViewById(R.id.tvSelectedItemType);

        tgInventoryItemType.setOnCheckedChangeListener(this);
    }

    private InventoryWHRecord updateState() {
        InventoryWHRecord inventoryRecord = GlobalState.initWHInventoryRecord();

        inventoryRecord.site = LocalPreferences.getCurrentSiteName();

        if (tvSelectedItemType.getText() != null) {
            inventoryRecord.selectedItemType = tvSelectedItemType.getText().toString();
        }

        if (!Strings.isEmptyOrWhitespace(selectedInventoryItemType)) {
            inventoryRecord.inventoryItemType = selectedInventoryItemType;
        }
        return inventoryRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHInventory.inventoryItemType)) {
            sb.append(String.format("\n%s is missing", "'Item type'"));
        }

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbAsset) {
            selectedInventoryItemType = Constants.ftAsset;
            tvSelectedItemType.setText(R.string.asset_type);
        } else if (checkedId == R.id.tbConsumable) {
            selectedInventoryItemType = Constants.ftConsumable;
            tvSelectedItemType.setText(R.string.consumable_type);
        }
    }
}