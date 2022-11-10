package io.agritrack.hotel.ui;

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
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;
import io.agritrack.ui.tools.ProgramEveryLinenTagsActivity;
import io.agritrack.ui.tools.ProgramLinenTagsActivity;

public class HotelMenuProgramActivity extends AppCompatActivity {

    private static final int Unprogrammed_Idx = 0, False_Idx = 1;
    GridView gvMainMenu;
    private SupportDialog supportDialog;
    private ImageView ivSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_menu_program);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHome);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        gvMainMenu = findViewById(R.id.gvMainMenu);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.unprogrammed_tags), ProgramLinenTagsActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.false_tags), ProgramEveryLinenTagsActivity.class));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvMainMenu.setAdapter(adapter);

        gvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Unprogrammed_Idx:
                        i = new Intent(appCtx, ProgramLinenTagsActivity.class);
                        break;
                    case False_Idx:
                        i = new Intent(appCtx, ProgramEveryLinenTagsActivity.class);
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
            supportDialog = new SupportDialog(HotelMenuProgramActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);

            startActivity(i);
        });
    }
}