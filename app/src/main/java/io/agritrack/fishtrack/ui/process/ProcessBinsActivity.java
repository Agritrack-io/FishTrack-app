package io.agritrack.fishtrack.ui.process;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.ProcessingRecord;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class ProcessBinsActivity extends AppCompatActivity {
    private MobileDB db;

    private Spinner spFishFarmSite;
    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread processingBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterBins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // get  references of the controls
        assignCtrlVars();

        // load all SeaFarms and fill in the spFishFarmSite Spinner.
        List<Site> seaFarms = db.siteDAO().getAllSeaFarms();
        if (seaFarms != null && !seaFarms.isEmpty()) {
            String[] seaFarmsArray = seaFarms.stream().map(x -> x.name).toArray(String[]::new);
            ArrayAdapter<String> sfAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, seaFarmsArray);
            sfAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spFishFarmSite.setAdapter(sfAdapter);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>());
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        //Get reference of binsCount textView
        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvBinsCount.setText(String.valueOf(response.size()));
            adapterBins.setValues(new ArrayList<>(response));
            adapterBins.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    private void assignCtrlVars() {
        tvBinsCount = findViewById(R.id.tvBinsCount);
        spFishFarmSite = findViewById(R.id.spFishFarmSite);
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        if(uhfReader!=null)
            uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanBin);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (processingBinsThread.getState() == Thread.State.TERMINATED) {
                processingBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            processingBinsThread.setScanInProgress(scanning);
            processingBinsThread.setUhfReader(uhfReader);
            processingBinsThread.setAdapter(adapterBins);
            processingBinsThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (processingBinsThread.getState() == Thread.State.NEW) {
                    processingBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_bin);
                try {
                    processingBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToSupervisorConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), ProcessConfirmActivity.class);
            startActivity(i);
        });

         ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartProcess);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ProcessStartActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        if (prcRecord.availBins != null) {
            adapterBins.setValues((ArrayList<String>) prcRecord.availBins);
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }

        if (prcRecord.seaFarmPos > -1) {
            spFishFarmSite.setSelection(prcRecord.seaFarmPos);
        }
    }

    private void updateState() {
        GlobalState.recProcessing.availBins = adapterBins.getValues();
        GlobalState.recProcessing.seaFarmPos = spFishFarmSite.getSelectedItemPosition();
        GlobalState.recProcessing.seaFarm = spFishFarmSite.getSelectedItem().toString();
    }

    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }
}