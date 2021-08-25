package io.agritrack.fishtrack.ui.activity.wh.search;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.wh.Asset;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.activity.adapter.FilterableAdapter;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class SearchActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private ToggleGroup tgSearchAssetType;
    private EditText etSearchAsset, etAssetBarcode;
    private RecyclerView rvAssets;
    private Button btnSearchAsset;

    private String selectedAssetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSearch);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();


        configFooter();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbCage) {
            selectedAssetType = Constants.ftCage;
        } else if (checkedId == R.id.tbNet) {
            selectedAssetType = Constants.ftNet;
        } else if (checkedId == R.id.tbBin) {
            selectedAssetType = Constants.ftBin;
        }
        loadAssetsFromLocalDB();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSearchAssetType = findViewById(R.id.tgSearchAssetType);
        etSearchAsset = findViewById(R.id.etSearchAsset);
        etAssetBarcode = findViewById(R.id.etAssetBarcode);
        rvAssets = findViewById(R.id.rvAssets);
        btnSearchAsset = findViewById(R.id.btnSearchAsset);

        tgSearchAssetType.setOnCheckedChangeListener(this);
        rvAssets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvAssets.setItemAnimator(new DefaultItemAnimator());
    }

    private void loadAssetsFromLocalDB() {

        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAll(); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.barcode)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);

            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }
}