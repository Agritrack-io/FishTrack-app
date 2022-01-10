package io.agritrack.fruit.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.IncomingRecord;
import io.agritrack.fruit.state.InventoryRecord;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.fruit.ui.warehouse.correlation.FruitCorrelationActivity;
import io.agritrack.fruit.ui.warehouse.incoming.IncomingIfcoActivity;
import io.agritrack.fruit.ui.warehouse.inventory.FruitInventoryStartActivity;
import io.agritrack.fruit.ui.warehouse.measurements.DailyTemperatureMeasurementsActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;

public class FruitWhMenuActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Inventory_Idx = 1, Correlation_Idx = 2, Temp_measure_Idx = 3;
    GridView gvFruitWhMainMenu;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_wh_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFruitWhMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        gvFruitWhMainMenu = findViewById(R.id.gvFruitWhMainMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_incoming), IncomingIfcoActivity.class, R.drawable.incoming));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_inventory), FruitInventoryStartActivity.class, R.drawable.inventory));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), FruitCorrelationActivity.class, R.drawable.correlation));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_measurements), DailyTemperatureMeasurementsActivity.class, R.drawable.quality));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvFruitWhMainMenu.setAdapter(adapter);

        gvFruitWhMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Incoming_Idx:
                        IncomingRecord incomingRecord = FruitGlobalState.initIncomingRecord();
                        i = new Intent(appCtx, IncomingIfcoActivity.class);
                        break;
                    case Inventory_Idx:
                        InventoryRecord inventoryRecord = FruitGlobalState.initInventoryRecord();
                        i = new Intent(appCtx, FruitInventoryStartActivity.class);
                        break;
                    case Correlation_Idx:
                        FruitGlobalState.initCorrelationRecord();
                        i = new Intent(appCtx, FruitCorrelationActivity.class);
                        break;
                    case Temp_measure_Idx:
                        i = new Intent(appCtx, DailyTemperatureMeasurementsActivity.class);
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
            supportDialog = new SupportDialog(FruitWhMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }
}