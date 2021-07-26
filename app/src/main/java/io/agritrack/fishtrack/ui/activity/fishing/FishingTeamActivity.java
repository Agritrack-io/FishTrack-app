package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class FishingTeamActivity extends AppCompatActivity {

    private String[] fishingTeam = {"Nikos","George","Vlasis","Stelios","Marios"};
    String[] fishTeam = {"Nikos","George","Vlasis","Stelios","Marios"};

    Spinner fishingTeamSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_team);

        getSupportActionBar().hide();

        Spinner fishingTeamSpinner = (Spinner) findViewById(R.id.spFishingTeam);
        ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, fishTeam);
        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
        fishingTeamSpinner.setAdapter(hrAdapter);

        RecyclerView rvTeam = (RecyclerView) findViewById(R.id.rvTeam);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTeam.setLayoutManager(layoutManager);
        TemplateRecyclerAdapter adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(Arrays.asList(fishingTeam)));
        rvTeam.setItemAnimator(new DefaultItemAnimator());
        rvTeam.setAdapter(adapterBins);
        rvTeam.setNestedScrollingEnabled(false);

        configFooter();
    }

    protected void configFooter() {

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToBins);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Bins!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCage);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Cage!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
            startActivity(i);
        });
    }
}