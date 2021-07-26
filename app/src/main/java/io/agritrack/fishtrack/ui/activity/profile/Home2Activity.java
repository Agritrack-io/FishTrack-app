package io.agritrack.fishtrack.ui.activity.profile;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuyenmonkey.mkloader.MKLoader;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.profile.adapter.MainMenuAdapter;
import io.agritrack.fishtrack.ui.bo.MenuItemData;

public class Home2Activity extends AppCompatActivity {

    private ArrayList<MenuItemData> listMainMenu = new ArrayList<>();
    private MainMenuAdapter mainMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        getSupportActionBar().hide();

        //resources = getApplicationContext().getResources();

        final MKLoader pbLoading = findViewById(R.id.pbLoading);
        pbLoading.setVisibility(View.GONE);

        setData();

        // Create adapter
        mainMenuAdapter = new MainMenuAdapter(listMainMenu);
        final RecyclerView rvList = findViewById(R.id.rvList);
        rvList.setAdapter(mainMenuAdapter);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvList.setLayoutManager(lm);
        rvList.setNestedScrollingEnabled(false);

    }

    private void setData() {
        listMainMenu.clear();

        listMainMenu.add(new MenuItemData(1, "Fishing", "Fishing details 1", "Fishing details 2", R.drawable.fishing));
        listMainMenu.add(new MenuItemData(2, "Transport", "Transport details 1", "Transport details 2", R.drawable.transport));
        listMainMenu.add(new MenuItemData(3, "Package", "Package details 1", "Package details 2", R.drawable.ic_gudang));
        listMainMenu.add(new MenuItemData(4, "Warehouse", "Warehouse details 1", "Warehouse details 2", R.drawable.ic_product_assets));
        listMainMenu.add(new MenuItemData(5, "Repair", "Repair details 1", "Repair details 2", R.drawable.repair));
    }
}