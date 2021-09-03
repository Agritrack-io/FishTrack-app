package io.agritrack.fishtrack.ui.wh.outgoing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.AssetTxDTO;
import io.agritrack.fishtrack.data.model.tx.AssetTransaction;
import io.agritrack.fishtrack.enums.WarehouseTxState;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.WHTxRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class OutgoingProcessActivity extends AppCompatActivity {
    private MobileDB db;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread processingBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterOutgoingItems;

    private TextView tvOutgoingProcessFrom, tvOutgoingProcessTo, tvOutAssetsCount;
    private RecyclerView rvOutgoingAssets;

    private ImageButton ivAddItem, ivDeleteItem;
    private String selectedBarcode;
    private AppCompatTextView selectedItem;

    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedBarcode = ((AppCompatTextView) v).getText().toString();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_process);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOutgoingAssets.setLayoutManager(layoutManager);
        rvOutgoingAssets.setItemAnimator(new DefaultItemAnimator());
        adapterOutgoingItems = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvOutgoingAssets.setAdapter(adapterOutgoingItems);
        rvOutgoingAssets.setNestedScrollingEnabled(false);

        //Get reference of binsCount textView
        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvOutAssetsCount.setText(String.valueOf(response.size()));
            adapterOutgoingItems.setValues(new ArrayList<>(response));
            adapterOutgoingItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteItem.setOnClickListener(view -> {
            if(selectedBarcode != null){
                adapterOutgoingItems.removeItem(selectedBarcode);
                adapterOutgoingItems.notifyDataSetChanged();
                tvOutAssetsCount.setText(String.valueOf(adapterOutgoingItems.getItemCount()));
            }
        });

        ivAddItem.setOnClickListener(view -> {

        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            processingBinsThread.setScanInProgress(scanning);

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartOutgoing);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        rvOutgoingAssets = findViewById(R.id.rvOutgoingAssets);
        tvOutgoingProcessFrom = findViewById(R.id.tvOutgoingProcessFrom);
        tvOutgoingProcessTo = findViewById(R.id.tvOutgoingProcessTo);
        tvOutAssetsCount = findViewById(R.id.tvOutAssetsCount);
        ivDeleteItem = (ImageButton) findViewById(R.id.ivDeleteItem);
        ivAddItem = (ImageButton) findViewById(R.id.ivAddItem);
    }

    private void updateState() {
        GlobalState.recWHOutgoing.items = adapterOutgoingItems.getValues();
        GlobalState.recWHOutgoing.state = WarehouseTxState.Outgoing;

        // get an instance of local DB
        this.db = MobileDB.getInstance(getContext());

        try {
            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHOutgoing(db);

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // hideSyncProgress();
        }

    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(GlobalState.recWHOutgoing.items==null || GlobalState.recWHOutgoing.items.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Outgoing items'"));
        }

        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord outgoingWHRecord = GlobalState.recWHOutgoing;

        if(!Strings.isEmptyOrWhitespace(outgoingWHRecord.from)) {
            tvOutgoingProcessFrom.setText(outgoingWHRecord.from);
        }

        if(!Strings.isEmptyOrWhitespace(outgoingWHRecord.to)) {
            tvOutgoingProcessTo.setText(outgoingWHRecord.to);
        }

        if (outgoingWHRecord.items != null) {
            adapterOutgoingItems.setValues((ArrayList<String>) outgoingWHRecord.items);
            adapterOutgoingItems.notifyDataSetChanged();
            tvOutAssetsCount.setText(String.valueOf(outgoingWHRecord.items.size()));
        }
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanAsset);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (processingBinsThread.getState() == Thread.State.TERMINATED) {
                processingBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            processingBinsThread.setScanInProgress(scanning);
            processingBinsThread.setUhfReader(uhfReader);
            processingBinsThread.setAdapter(adapterOutgoingItems);
            processingBinsThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (processingBinsThread.getState() == Thread.State.NEW) {
                    processingBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    processingBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
            AssetTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Tx successfully updated!!!", Toast.LENGTH_LONG).show());
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.error_AssetTx_tx_update_failure, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<AssetTxDTO> call, Throwable error) {
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