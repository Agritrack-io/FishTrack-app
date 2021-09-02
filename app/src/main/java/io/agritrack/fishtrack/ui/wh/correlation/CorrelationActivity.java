package io.agritrack.fishtrack.ui.wh.correlation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.fishtrack.data.model.tx.CorrelationTransaction;
import io.agritrack.fishtrack.data.model.wh.Asset;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.FilterableAdapter;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class CorrelationActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);

    private ToggleGroup tgSearchAssetType;
    private SearchView svSearchAsset;
    private RecyclerView rvAssets;
    private Button btnScanAssetTag;
    private TextView tvCorrAssetBarcode;

    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private String selectedAssetType;
    private String selectedBarcode = "";

    private AppCompatTextView selectedItem;
    // Instantiate a clickListener to be passed to adapterAssets.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedBarcode = ((AppCompatTextView) v).getText().toString();
            //svSearchAsset.setQuery(selectedBarcode, false);

            if(selectedItem!=null) {
                selectedItem.setTextColor(Color.GRAY);
                selectedItem.setBackgroundColor(Color.WHITE);
            }
            v.setSelected(true);
            ((AppCompatTextView) v).setTextColor(Color.BLUE);
            ((AppCompatTextView) v).setBackgroundColor(Color.GRAY);
            selectedItem = (AppCompatTextView) v;
            //adapterAssets.notifyDataSetChanged();
        }
    };
    private UhfReader uhfReader;
    private ScanInventoryThread correlateAssetThread = new ScanInventoryThread();
    private boolean scanning = false;

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
            if (correlateAssetThread.getState() == Thread.State.TERMINATED) {
                correlateAssetThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            correlateAssetThread.setScanInProgress(scanning);
            correlateAssetThread.setUhfReader(uhfReader);
            correlateAssetThread.setRfidTag(tvCorrAssetBarcode);

            if (scanning) {
                btnScanAssetTag.setText(R.string.stop_scan);
                if (correlateAssetThread.getState() == Thread.State.NEW) {
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

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
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
            int kk = 0;
        });
    }

    private void updateState() {
        GlobalState.recWHCorrelation.assetType = AssetType.valueOf(selectedAssetType);
        GlobalState.recWHCorrelation.barcode = !Strings.isEmptyOrWhitespace(selectedBarcode) ? selectedBarcode : null; //tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;
        GlobalState.recWHCorrelation.rfid = tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;

        // get an instance of local DB
        this.db = MobileDB.getInstance(getContext());

        try {
            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);

            // sync WH Correlation Tx
            Call<CorrelationTxDTO> syncTxAsyncCall = updService.syncCorrelationTx(CorrelationTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // hideSyncProgress();
        }
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.barcode)){
            sb.append(String.format("\n%s is missing", "'Asset BARCODE'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)){
            sb.append(String.format("\n%s is missing", "'Asset RFID'"));
        }

        return sb.toString();
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

    public class SyncTxCallBack implements Callback<CorrelationTxDTO> {
        @Override
        public void onResponse(Call<CorrelationTxDTO> call, Response<CorrelationTxDTO> response) {
            CorrelationTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Tx successfully updated!!!", Toast.LENGTH_LONG).show());
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.error_CorrelationTx_update_failure, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<CorrelationTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.error_connection_timeout, Toast.LENGTH_LONG).show());
            } else if (error instanceof IOException) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.error_timeout, Toast.LENGTH_LONG).show());
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.error_cancelled_call, Toast.LENGTH_LONG).show());
                } else {
                    //Generic error handling
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Network Error :: " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }
    }
}