package io.agritrack.fish.ui;

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

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.fish.ui.wh.correlation.CorrelationActivity;
import io.agritrack.fish.ui.wh.incoming.IncomingStartActivity;
import io.agritrack.fish.ui.wh.inventory.InventoryStartActivity;
import io.agritrack.fish.ui.wh.outgoing.OutgoingStartActivity;
import io.agritrack.fish.ui.wh.search.SearchActivity;
import io.agritrack.ui.service.LocalPreferences;

public class WhMenuActivity extends AppCompatActivity {

    private static final int Incoming_Idx = 0, Outgoing_Idx = 1, Inventory_Idx = 2, Correlation_Idx = 3, Search_Idx = 4;
    GridView gvWhMainMenu;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wh_menu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderWhMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        gvWhMainMenu = findViewById(R.id.gvWhMainMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_incoming), IncomingStartActivity.class, R.drawable.incoming));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_outgoing), OutgoingStartActivity.class, R.drawable.outgoing));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_inventory), InventoryStartActivity.class, R.drawable.inventory));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), CorrelationActivity.class, R.drawable.correlation));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_search), SearchActivity.class, R.drawable.search));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvWhMainMenu.setAdapter(adapter);

        gvWhMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Incoming_Idx:
                        GlobalState.initWHIncomingRecord();
                        i = new Intent(appCtx, IncomingStartActivity.class);
                        break;
                    case Outgoing_Idx:
                        GlobalState.initWHOutgoingRecord();
                        i = new Intent(appCtx, OutgoingStartActivity.class);
                        break;
                    case Inventory_Idx:
                        i = new Intent(appCtx, InventoryStartActivity.class);
                        break;
                    case Correlation_Idx:
                        i = new Intent(appCtx, CorrelationActivity.class);
                        break;
                    case Search_Idx:
                        i = new Intent(appCtx, SearchActivity.class);
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
            supportDialog = new SupportDialog(WhMenuActivity.this);
            supportDialog.showDialog();
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