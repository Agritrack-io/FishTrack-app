package io.agritrack.fishtrack.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.activity.adapter.MenuItem;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.maintenance.MaintenanceInternalStartActivity;
import io.agritrack.fishtrack.ui.activity.maintenance.MaintenanceMenuActivity;
import io.agritrack.fishtrack.ui.activity.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class HomeActivity extends AppCompatActivity {
    private static final int Fishing_Idx = 0, Transport_Idx = 1, Processing_Idx = 2, Warehouse_Idx = 3, Maintenance_Idx = 4;
    GridView gvMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gvMainMenu = findViewById(R.id.gvMainMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.main_menu_title_fishing), "", R.drawable.fishing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_transport), "", R.drawable.transport));
        menuItemsList.add(new MenuItem("Processing", "", R.drawable.ic_barang_masuk));
        menuItemsList.add(new MenuItem("Warehouse", "", R.drawable.ic_product_assets));
        menuItemsList.add(new MenuItem("Maintenance", "", R.drawable.repair));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvMainMenu.setAdapter(adapter);

        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch(position) {
                    case Fishing_Idx:
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
            Toast.makeText(getContext(), "Logout!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });

    }
}