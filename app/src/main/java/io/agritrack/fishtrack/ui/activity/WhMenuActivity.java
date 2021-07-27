package io.agritrack.fishtrack.ui.activity;

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
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.activity.adapter.MenuItem;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.process.ProcessBinsActivity;
import io.agritrack.fishtrack.ui.activity.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.IncomingStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.OutgoingStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class WhMenuActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Outgoing_Idx = 1, Inventory_Idx = 2, Tools_Idx = 3;
    GridView gvWhMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wh_menu);

        gvWhMainMenu = findViewById(R.id.gvWhMainMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem("INCOMING", "", R.drawable.ic_incoming));
        menuItemsList.add(new MenuItem("OUTGOING", "", R.drawable.transport));
        menuItemsList.add(new MenuItem("INVENTORY", "", R.drawable.ic_barang_masuk));
        menuItemsList.add(new MenuItem("TOOLS", "", R.drawable.ic_product_assets));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvWhMainMenu.setAdapter(adapter);

        gvWhMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Incoming_Idx:
                        i = new Intent(appCtx, IncomingStartActivity.class);
                        break;
                    case Outgoing_Idx:
                        i = new Intent(appCtx, OutgoingStartActivity.class);
                        break;
                    /*case Inventory_Idx:
                        i = new Intent(appCtx, InventoryStartActivity.class);
                        break;
                    case Tools_Idx:
                        i = new Intent(appCtx, ToolsStartActivity.class);
                        break;     */
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