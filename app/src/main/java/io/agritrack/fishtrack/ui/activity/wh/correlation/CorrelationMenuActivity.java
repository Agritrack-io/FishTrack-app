package io.agritrack.fishtrack.ui.activity.wh.correlation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.activity.adapter.MenuItem;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.wh.incoming.IncomingStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.inventory.InventoryActivity;
import io.agritrack.fishtrack.ui.activity.wh.outgoing.OutgoingStartActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class CorrelationMenuActivity extends AppCompatActivity {

    private static final int Cage_Idx = 0, Net_Idx = 1, Bin_Idx = 2, Platform_Idx = 3;
    GridView gvCorrelationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_cage), CorrelationCageActivity.class, R.drawable.cage));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_net), CorrelationNetActivity.class, R.drawable.net));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_bin), CorrelationBinActivity.class, R.drawable.bin));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_platform), CorrelationPlatformActivity.class, R.drawable.platform));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvCorrelationMenu.setAdapter(adapter);

        gvCorrelationMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Cage_Idx:
                        i = new Intent(appCtx, CorrelationCageActivity.class);
                        break;
                    case Net_Idx:
                        i = new Intent(appCtx, CorrelationNetActivity.class);
                        break;
                    case Bin_Idx:
                        i = new Intent(appCtx, CorrelationBinActivity.class);
                        break;
                    case Platform_Idx:
                        i = new Intent(appCtx, CorrelationPlatformActivity.class);
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
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}