package io.agritrack.fishtrack.ui.wh.correlation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.wh.Asset;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.FilterableAdapter;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class CorrelationActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgSearchAssetType;
    private SearchView svSearchAsset;
    private RecyclerView rvAssets;
    private Button btnScanAssetTag;
    private TextView tvCorrAssetBarcode;

    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private String selectedAssetType;
    private String selectedBarcode = "";

    private UhfReader uhfReader;
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private ScanInventoryThread correlateAssetThread = new ScanInventoryThread();
    private boolean scanning = false;

    // Instantiate a clickListener to be passed to adapterAssets.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedBarcode = ((AppCompatTextView) v).getText().toString();
            svSearchAsset.setQuery(selectedBarcode, false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);
        btnScanAssetTag.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (correlateAssetThread.getState() == Thread.State.TERMINATED)
            {
                correlateAssetThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            correlateAssetThread.setScanInProgress(scanning);
            correlateAssetThread.setUhfReader(uhfReader);
            correlateAssetThread.setRfidTag(tvCorrAssetBarcode);

            if (scanning) {
                btnScanAssetTag.setText(R.string.stop_scan);
                if (correlateAssetThread.getState() == Thread.State.NEW)
                {
                    correlateAssetThread.start();
                }
            } else {
                btnScanAssetTag.setText(R.string.scan_asset_tag);
                try {
                    correlateAssetThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            //this.selectedBarcode = response;
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWareHouseMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSearchAssetType = findViewById(R.id.tgSearchAssetType);
        tvCorrAssetBarcode = findViewById(R.id.tvCorrAssetBarcode);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvAssets = findViewById(R.id.rvAssets);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);

        tgSearchAssetType.setOnCheckedChangeListener(this);
        rvAssets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvAssets.setItemAnimator(new DefaultItemAnimator());

        svSearchAsset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAssets.getFilter().filter(newText);
                return false;
            }
        });
        svSearchAsset.setOnClickListener(view -> {
            int  kk=0;
        });
    }

    private void updateState() {

    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbCage) {
            selectedAssetType = Constants.ftCage;
        } else if (checkedId == R.id.tbNet) {
            selectedAssetType = Constants.ftNet;
        } else if (checkedId == R.id.tbBin) {
            selectedAssetType = Constants.ftBin;
        } else if (checkedId == R.id.tbPlatform) {
            selectedAssetType = Constants.ftPlatform;
        }

        loadAssetsFromLocalDB();
    }

    private void loadAssetsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAll(); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.barcode)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }
}