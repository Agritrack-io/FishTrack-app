package io.agritrack.fishtrack.ui.activity.maintenance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.activity.adapter.MenuItem;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.wh.correlation.CorrelationMenuActivity;
import io.agritrack.fishtrack.ui.activity.wh.incoming.IncomingStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.inventory.InventoryActivity;
import io.agritrack.fishtrack.ui.activity.wh.outgoing.OutgoingStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.search.SearchActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class MaintenanceMenuActivity extends AppCompatActivity {

    private static final int Internal_Idx = 0, External_Idx = 1;
    GridView gvMaintenanceMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_menu);

        gvMaintenanceMenu = findViewById(R.id.gvMaintenanceMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem("Internal", "", R.drawable.ic_incoming));
        menuItemsList.add(new MenuItem("External", "", R.drawable.transport));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvMaintenanceMenu.setAdapter(adapter);

        gvMaintenanceMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Internal_Idx:
                        i = new Intent(appCtx, MaintenanceInternalStartActivity.class);
                        break;
                    case External_Idx:
                        i = new Intent(appCtx, MaintenanceExternalStartActivity.class);
                        break;
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
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Main menu!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }
}