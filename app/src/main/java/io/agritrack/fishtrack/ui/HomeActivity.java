package io.agritrack.fishtrack.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.tx.FishingTransaction;
import io.agritrack.fishtrack.enums.TxStatus;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.adapter.MenuItem;
import io.agritrack.fishtrack.ui.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.login.LoginActivity;
import io.agritrack.fishtrack.ui.maintenance.MaintenanceMenuActivity;
import io.agritrack.fishtrack.ui.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import io.agritrack.fishtrack.ui.transport.TransportStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class HomeActivity extends AppCompatActivity {
    private static final int Fishing_Idx = 0, Transport_Idx = 1, Processing_Idx = 2, Warehouse_Idx = 3, Maintenance_Idx = 4;
    GridView gvMainMenu;
    private MobileDB db;

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


        gvMainMenu = findViewById(R.id.gvMainMenu);
        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvMainMenu.setAdapter(adapter);

        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Fishing_Idx:
                        FishingTransaction openTx = db.fishingTransactionDAO().getMostRecentOpenTx();
                        FishingRecord fishingRecord;
                        if (openTx != null) {
                            fishingRecord = FishingRecord.convert(openTx);
                            GlobalState.recFishing = fishingRecord;
                        } else {
                            openTx = new FishingTransaction();
                            fishingRecord = GlobalState.initFishingTx();
                            openTx.txStatus = TxStatus.PENDING;
                            fishingRecord.txKey = db.fishingTransactionDAO().insert(openTx);
                        }

                        i = new Intent(appCtx, FishingStartActivity.class);
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
        configHeader();
    }

    protected void configHeader() {
        ImageButton ivBack = (ImageButton) findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });

    }
}