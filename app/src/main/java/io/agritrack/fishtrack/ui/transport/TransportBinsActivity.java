package io.agritrack.fishtrack.ui.transport;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.TransportationRecord;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class TransportBinsActivity extends AppCompatActivity {

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread transportationBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterBins;

    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>());
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

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

        // create Footer
        configFooter();
    }

    private void assignCtrlVars() {
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        tvBinsCount = findViewById(R.id.tvBinsCount);
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanBin);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (transportationBinsThread.getState() == Thread.State.TERMINATED) {
                transportationBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            transportationBinsThread.setScanInProgress(scanning);
            transportationBinsThread.setUhfReader(uhfReader);
            transportationBinsThread.setAdapter(adapterBins);
            transportationBinsThread.setScanResult(scanResult);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                if (transportationBinsThread.getState() == Thread.State.NEW) {
                    transportationBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_bin);
                try {
                    transportationBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToStartTransport);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportStartActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToDriverConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
                startActivity(i);
            }
        });
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (trns.availBins != null) {
            adapterBins.setValues((ArrayList<String>) trns.availBins);
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(trns.availBins.size()));
        }
    }

    private void updateState() {
        GlobalState.recTransport.availBins = adapterBins.getValues();
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(GlobalState.recTransport.availBins==null || GlobalState.recTransport.availBins.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Bins for transport'"));
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        if (uhfReader != null)
            uhfReader.close();
        scanning = false;
        super.onDestroy();
    }
}