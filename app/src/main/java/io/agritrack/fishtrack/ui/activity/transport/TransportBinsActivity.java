package io.agritrack.fishtrack.ui.activity.transport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class TransportBinsActivity extends AppCompatActivity {

    private String[] bins = {"1","2","3","4","5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_bins);

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
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartTransport);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Start Transport!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportStartActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToDriverConfirm);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Driver confirms!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
            startActivity(i);
        });
    }
}