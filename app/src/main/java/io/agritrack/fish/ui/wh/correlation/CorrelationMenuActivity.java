package io.agritrack.fish.ui.wh.correlation;

import android.app.ProgressDialog;
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
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.HomeMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;

public class CorrelationMenuActivity extends AppCompatActivity {

    private static final int Cage_Idx = 0, Net_Idx = 1, Cage_Net_Idx = 2, Bin_Idx = 3, Platform_Idx = 4;
    GridView gvCorrelationMenu;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_menu);
        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_cage), CorrelationCageActivity.class, R.drawable.cage));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_net), CorrelationNetActivity.class, R.drawable.net));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_cage_net), CorrelationCageNetActivity.class, R.drawable.cage_net));
        menuItemsList.add(new MenuItem(getString(R.string.menu_title_bin), CorrelationBinActivity.class, R.drawable.bin));
        menuItemsList.add(new MenuItem(getString(R.string.platform), CorrelationPlatformActivity.class, R.drawable.platform));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvCorrelationMenu.setAdapter(adapter);

        gvCorrelationMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Cage_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationCageActivity.class);
                        break;
                    case Net_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationNetActivity.class);
                        break;
                    case Cage_Net_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationCageNetActivity.class);
                        break;
                    case Bin_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationBinActivity.class);
                        break;
                    case Platform_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, CorrelationPlatformActivity.class);
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
            supportDialog = new SupportDialog(CorrelationMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}