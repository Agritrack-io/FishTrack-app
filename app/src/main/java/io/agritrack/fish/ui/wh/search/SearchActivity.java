package io.agritrack.fish.ui.wh.search;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.rfid.ScanFilterThread;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class SearchActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private static final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    private UhfReader uhfReader;
    private ScanFilterThread assetSearchThread = new ScanFilterThread();
    private boolean scanning = false;
    private ProgressBar pbProximity;
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private ToggleGroup tgSearchAssetType;
    private EditText etAssetBarcode;
    private SearchView svSearchAsset;
    private TextView tvProximity;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    int rssi_from_tag = msg.getData().getInt("rssi");
                    System.out.println("RSSI:" + rssi_from_tag);
                    int rssi_norm = normalize(rssi_from_tag);

                    if (rssi_norm > 5 && rssi_norm < 95) {
                        tvProximity.setText(String.valueOf(rssi_norm));
                        pbProximity.setProgress(rssi_norm);
                        if (rssi_norm < 95 && rssi_norm >= 80) {
//                            SoundUtil.play(5, 5, 0, 2.0f);
                            toneG.startTone(ToneGenerator.TONE_DTMF_D, 200);
                        } else if (rssi_norm < 80 && rssi_norm >= 60) {
//                            SoundUtil.play(4, 4, 0, 1.5f);
                            toneG.startTone(ToneGenerator.TONE_DTMF_9, 130);
                        } else if (rssi_norm < 60 && rssi_norm >= 40) {
//                            SoundUtil.play(3, 3, 0, 1.0f);
                            toneG.startTone(ToneGenerator.TONE_DTMF_5, 100);
                        } else {
//                            SoundUtil.play(3, 2, 0, 0.5f);
                            toneG.startTone(ToneGenerator.TONE_DTMF_1, 50);
                        }
                    } else if (rssi_norm >= 95) {
//                        SoundUtil.play(5, 6, 0, 2.5f);
                        toneG.startTone(ToneGenerator.TONE_DTMF_D, 300);
                        tvProximity.setText(">= 95%");
                        pbProximity.setProgress(100);
                    } else {
//                        SoundUtil.play(3, 2, 0, 0.5f);
                        toneG.startTone(ToneGenerator.TONE_DTMF_1, 10);
                        tvProximity.setText("<= 5%");
                        pbProximity.setProgress(0);
                    }
                }
            }
        }

        private int normalize(double rssi) {
            final double MAX_RSSI = -35d;
            final double MIN_RSSI = -70;
            rssi = rssi > MAX_RSSI ? MAX_RSSI : rssi;
            rssi = rssi < MIN_RSSI ? MIN_RSSI : rssi;
            return (int) (Math.abs(rssi - MIN_RSSI) / (MAX_RSSI - MIN_RSSI) * 100);
        }
    };

    private RecyclerView rvAssets;
    private Button btnSearchAsset;
    private String selectedAssetType;
    private String selectedBarcode = "";
    private ConstraintLayout selectedItem;

    // Instantiate a clickListener to be passed to adapterAssets.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();
            etAssetBarcode.setText(selectedBarcode);

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
        setContentView(R.layout.activity_search);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        SoundUtil.initSoundPool(SearchActivity.this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSearch);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(SearchActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbCage) {
            selectedAssetType = Constants.ftCage;
            loadCagesFromLocalDB();
        } else if (checkedId == R.id.tbNet) {
            selectedAssetType = Constants.ftNet;
            loadNetsFromLocalDB();
        } else if (checkedId == R.id.tbBin) {
            selectedAssetType = Constants.ftBin;
            loadBinsFromLocalDB();
        }
        svSearchAsset.setVisibility(View.VISIBLE);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSearchAssetType = findViewById(R.id.tgSearchAssetType);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        etAssetBarcode = findViewById(R.id.etAssetBarcode);
        rvAssets = findViewById(R.id.rvAssets);
        btnSearchAsset = findViewById(R.id.btnSearchAsset);
        pbProximity = findViewById(R.id.pbProximity);
        tvProximity = findViewById(R.id.tvProximity);
        ivSupport = findViewById(R.id.ivSupport);
        tgSearchAssetType.setOnCheckedChangeListener(this);
        rvAssets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvAssets.setItemAnimator(new DefaultItemAnimator());

        if (adapterAssets == null) {
            svSearchAsset.setVisibility(View.GONE);
        }
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
            pbProximity.setProgress(0);
            tvProximity.setText(null);
        });

    }

    private void loadCagesFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftCage); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    private void loadNetsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftNet); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    private void loadBinsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftBin); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    private void prepareScanAvailableBinsButton() {
        assetSearchThread.setHandler(this.handler);

        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();

        final Button scanButton = findViewById(R.id.btnSearchAsset);
        scanButton.setOnClickListener(view -> {
            scanning = !scanning;

            selectedBarcode = etAssetBarcode.getText().toString();
            if (Strings.isEmptyOrWhitespace(selectedBarcode)) {
                runOnUiThread(() -> CToast(getAppContext(), render(R.string.no_epc_filter_selected), Toast.LENGTH_LONG));
            }

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (assetSearchThread.getState() == Thread.State.TERMINATED) {
                assetSearchThread = new ScanFilterThread();
            }

            //update scanning, uhfReader, tvPlatformName values in thread
            assetSearchThread.setScanInProgress(scanning);
            assetSearchThread.setUhfReader(uhfReader);
            assetSearchThread.setHandler(this.handler);
            assetSearchThread.setFilterEPC(selectedBarcode);

            if (scanning) {
                scanButton.setText(R.string.stop_search);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (assetSearchThread.getState() == Thread.State.NEW) {
                    assetSearchThread.start();
                }
            } else {
                scanButton.setText(R.string.title_search);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                        pbProximity.setProgress(0);
                        tvProximity.setText(R.string.proximity);
                    }
                });
                try {
                    assetSearchThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}