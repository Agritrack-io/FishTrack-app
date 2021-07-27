package io.agritrack.fishtrack.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuAdapter;
import io.agritrack.fishtrack.ui.activity.adapter.HomeMenuItem;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportStartActivity;

public class HomeActivity extends AppCompatActivity {
    private static final int Fishing_Idx = 0, Transport_Idx = 1, Processing_Idx = 2, Warehouse_Idx = 3, Maintenace_Idx = 4;
    GridView gvMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gvMainMenu = findViewById(R.id.gvMainMenu);

        ArrayList<HomeMenuItem> courseModelArrayList = new ArrayList<HomeMenuItem>();
        courseModelArrayList.add(new HomeMenuItem("Fishing", "", R.drawable.fishing));
        courseModelArrayList.add(new HomeMenuItem("Transport", "", R.drawable.transport));
        //courseModelArrayList.add(new HomeMenuItem("Processing", "", R.drawable.ic_barang_masuk));
        courseModelArrayList.add(new HomeMenuItem("Warehouse", "", R.drawable.ic_product_assets));
        courseModelArrayList.add(new HomeMenuItem("Maintenance", "", R.drawable.repair));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, courseModelArrayList);
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
                        //i = new Intent(appCtx, WarLoginActivity.class);
                        break;
                    case Maintenace_Idx:
                        //i = new Intent(appCtx, LoginActivity.class);
                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                startActivity(i);
            }
        });
    }
}