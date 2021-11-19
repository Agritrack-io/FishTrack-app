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
import io.agritrack.fruit.ui.warehouse.correlation.FruitCorrelationActivity;
import io.agritrack.fruit.ui.warehouse.inventory.FruitInventoryStartActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;

public class FruitWhMenuActivity extends AppCompatActivity {

    private static final int Inventory_Idx = 0, Correlation_Idx = 1;
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
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_inventory), FruitInventoryStartActivity.class, R.drawable.inventory));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), FruitCorrelationActivity.class, R.drawable.correlation));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvFruitWhMainMenu.setAdapter(adapter);

        gvFruitWhMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Inventory_Idx:
                        i = new Intent(appCtx, FruitInventoryStartActivity.class);
                        break;
                    case Correlation_Idx:
                        i = new Intent(appCtx, FruitCorrelationActivity.class);
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