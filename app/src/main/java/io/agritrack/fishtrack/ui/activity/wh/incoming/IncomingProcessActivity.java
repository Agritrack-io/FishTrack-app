package io.agritrack.fishtrack.ui.activity.wh.incoming;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.tx.IncomingWHTransaction;
import io.agritrack.fishtrack.rfid.ScanThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.IncomingWHRecord;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class IncomingProcessActivity extends AppCompatActivity {
    private MobileDB db;
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanThread processingBinsThread = new ScanThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterIncomingItems;

    private TextView tvIncomingProcessFrom, tvIncomingProcessTo, tvBinsCount;
    private RecyclerView rvIncomingItems;

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
        adapterIncomingItems = new TemplateRecyclerAdapter(this, new ArrayList<>());
        rvIncomingItems.setAdapter(adapterIncomingItems);
        rvIncomingItems.setNestedScrollingEnabled(false);

        //Get reference of binsCount textView
        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvBinsCount.setText(String.valueOf(response.size()));
            adapterIncomingItems.setValues(new ArrayList<>(response));
            adapterIncomingItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartIncoming);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), IncomingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        rvIncomingItems = findViewById(R.id.rvIncomingItems);
        tvIncomingProcessFrom = findViewById(R.id.tvIncomingProcessFrom);
        tvIncomingProcessTo = findViewById(R.id.tvIncomingProcessTo);
        tvBinsCount = findViewById(R.id.tvBinsCount);
    }

    private void updateState() {
        GlobalState.recWHIncoming.incomingItems = adapterIncomingItems.getValues();

        // get an instance of local DB
        this.db = MobileDB.getInstance(getContext());

        // persist Transportation Record data to local DB.
        IncomingWHTransaction tx = GlobalState.commitWHIncoming(db);
    }

    private void initControlsFromState() {
        IncomingWHRecord incomingWHRecord = GlobalState.recWHIncoming;

        if(!Strings.isEmptyOrWhitespace(incomingWHRecord.incomingFrom)) {
            tvIncomingProcessFrom.setText(incomingWHRecord.incomingFrom);
        }

        if(!Strings.isEmptyOrWhitespace(incomingWHRecord.incomingTo)) {
            tvIncomingProcessTo.setText(incomingWHRecord.incomingTo);
        }

        if (incomingWHRecord.incomingItems != null) {
            adapterIncomingItems.setValues((ArrayList<String>) incomingWHRecord.incomingItems);
            adapterIncomingItems.notifyDataSetChanged();

            tvBinsCount.setText(String.valueOf(incomingWHRecord.incomingItems.size()));
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
                processingBinsThread = new ScanThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            processingBinsThread.setScanInProgress(scanning);
            processingBinsThread.setUhfReader(uhfReader);
            processingBinsThread.setAdapter(adapterIncomingItems);
            processingBinsThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (processingBinsThread.getState() == Thread.State.NEW) {
                    processingBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                try {
                    processingBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}