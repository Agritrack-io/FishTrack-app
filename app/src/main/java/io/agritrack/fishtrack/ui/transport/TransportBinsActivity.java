package io.agritrack.fishtrack.ui.transport;

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

import java.util.ArrayList;
import java.util.LinkedList;
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

    private ImageButton ivAddBin, ivDeleteBin;
    private String selectedBarcode;
    private AppCompatTextView selectedItem;

    // Instantiate a clickListener to be passed to adapterBins.
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
        setContentView(R.layout.activity_transport_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
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

        ivDeleteBin.setOnClickListener(view -> {
            if(selectedBarcode != null){
                adapterBins.removeItem(selectedBarcode);
                adapterBins.notifyDataSetChanged();
                tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
            }
        });

        ivAddBin.setOnClickListener(view -> {

        });

        // create Footer
        configFooter();
    }

    private void assignCtrlVars() {
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = (ImageButton) findViewById(R.id.ivDeleteBin);
        ivAddBin = (ImageButton) findViewById(R.id.ivAddBin);
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
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (transportationBinsThread.getState() == Thread.State.NEW) {
                    transportationBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_bin);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
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

            //Set scanning to false to stop running scan thread
            scanning = false;
            transportationBinsThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), TransportStartActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToDriverConfirm);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            transportationBinsThread.setScanInProgress(scanning);

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
            adapterBins.setValues(new LinkedList<>(trns.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(trns.availBins.size()));
        }
    }

    private void updateState() {
        GlobalState.recTransport.availBins = new LinkedList<>(adapterBins.getValues());
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