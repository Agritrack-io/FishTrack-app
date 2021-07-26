package io.agritrack.fishtrack.ui.activity.fishing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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

public class FishingBinsActivity extends AppCompatActivity {

    private String[] bins = {"1","2","3","4","5"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_bins);

        getSupportActionBar().hide();

        RecyclerView rvBins = (RecyclerView) findViewById(R.id.rvBins);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBins.setLayoutManager(layoutManager);
        TemplateRecyclerAdapter adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(Arrays.asList( bins)));
        rvBins.setItemAnimator(new DefaultItemAnimator());
        rvBins.setAdapter(adapterBins);
        rvBins.setNestedScrollingEnabled(false);



        configFooter();
    }



    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fishing team!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Fishing!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), FishingStartActivity.class);
            startActivity(i);
        });
    }
}