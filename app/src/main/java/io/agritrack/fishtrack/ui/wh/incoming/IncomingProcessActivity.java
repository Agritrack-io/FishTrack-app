package io.agritrack.fishtrack.ui.wh.incoming;

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
import androidx.constraintlayout.widget.ConstraintLayout;
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

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class IncomingProcessActivity extends AppCompatActivity {
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private MobileDB db;
    private UhfReader uhfReader;
    private ScanInventoryThread processingBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterIncomingItems;

    private TextView tvIncomingProcessFrom, tvIncomingProcessTo, tvItemsCount;
    private RecyclerView rvIncomingItems;

    private ImageButton ivAddItem, ivDeleteItem;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_process);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIncomingItems.setLayoutManager(layoutManager);
        rvIncomingItems.setItemAnimator(new DefaultItemAnimator());
        adapterIncomingItems = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvIncomingItems.setAdapter(adapterIncomingItems);
        rvIncomingItems.setNestedScrollingEnabled(false);

        //Get reference of binsCount textView
        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvItemsCount.setText(String.valueOf(response.size()));
            adapterIncomingItems.setValues(new ArrayList<>(response));
            adapterIncomingItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

            if (selectedBarcode != null) {
                adapterIncomingItems.removeItem(selectedBarcode);
                adapterIncomingItems.notifyDataSetChanged();
                tvItemsCount.setText(String.valueOf(adapterIncomingItems.getItemCount()));
            }
        });

        ivAddItem.setOnClickListener(view -> {

        });

        configFooter();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
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

        ImageView ivBack = findViewById(R.id.ivBackToStartIncoming);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            processingBinsThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), IncomingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        rvIncomingItems = findViewById(R.id.rvIncomingItems);
        tvIncomingProcessFrom = findViewById(R.id.tvIncomingProcessFrom);
        tvIncomingProcessTo = findViewById(R.id.tvIncomingProcessTo);
        tvItemsCount = findViewById(R.id.tvItemsCount);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
    }

    private void updateState() {
        GlobalState.recWHIncoming.items = adapterIncomingItems.getValues();
        GlobalState.recWHIncoming.state = WarehouseTxState.Incoming;

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHIncoming(db);

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // hideSyncProgress();
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (GlobalState.recWHIncoming.items == null || GlobalState.recWHIncoming.items.isEmpty()) {
            sb.append(String.format("\n%s is missing", "'Incoming items'"));
        }

        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord WHTxRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.from)) {
            tvIncomingProcessFrom.setText(WHTxRecord.from);
        }

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.to)) {
            tvIncomingProcessTo.setText(WHTxRecord.to);
        }

        if (WHTxRecord.items != null) {
            adapterIncomingItems.setValues(WHTxRecord.items);
            adapterIncomingItems.notifyDataSetChanged();

            tvItemsCount.setText(String.valueOf(WHTxRecord.items.size()));
        }
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanAsset);
        scanButton.setOnClickListener(view -> {
            clearSelectedItem();
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (processingBinsThread.getState() == Thread.State.TERMINATED) {
                processingBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            processingBinsThread.setScanInProgress(scanning);
            processingBinsThread.setUhfReader(uhfReader);
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