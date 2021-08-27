package io.agritrack.fishtrack.ui.maintenance;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.adapter.MenuItem;
import io.agritrack.fishtrack.ui.login.LoginActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class MaintenanceMenuActivity extends AppCompatActivity {

    private static final int Internal_Idx = 0, External_Idx = 1;
    GridView gvMaintenanceMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        gvMaintenanceMenu = findViewById(R.id.gvMaintenanceMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_internal), MaintenanceInternalStartActivity.class, R.drawable.internal));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_external), MaintenanceExternalStartActivity.class, R.drawable.external));

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
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        });
    }
}