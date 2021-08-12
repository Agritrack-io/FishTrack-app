package io.agritrack.fishtrack.ui.activity.wh.inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // Get reference of widgets from XML layout
        final Spinner spSite = (Spinner) findViewById(R.id.spSite);

        // Initializing a String Array
        String[] sites = new String[]{
                "Select site...",
                "Warehouse",
                "platform",
                "else",
                "else"
        };

        final List<String> siteList = new ArrayList<>(Arrays.asList(sites));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spSiteArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.simple_spinner_item,siteList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spSiteArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spSite.setAdapter(spSiteArrayAdapter);

        spSite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get reference of widgets from XML layout
        final Spinner spAssetType = (Spinner) findViewById(R.id.spAssetType);

        // Initializing a String Array
        String[] assetType = new String[]{
                "Select asset type...",
                "Cage",
                "Net",
                "Bin",
                "Feed",
                "Vaccine",
                "Antibiotics"
        };

        final List<String> assetTypeList = new ArrayList<>(Arrays.asList(assetType));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spAssetTypeArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.simple_spinner_item,assetTypeList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spAssetTypeArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        spAssetType.setAdapter(spAssetTypeArrayAdapter);

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Congratulations!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }
}
