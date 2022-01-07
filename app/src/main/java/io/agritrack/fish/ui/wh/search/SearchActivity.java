package io.agritrack.fish.ui.wh.search;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.rfid.ScanFilterThread;
import io.agritrack.ui.TriggerKeyAwareActivity;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class SearchActivity extends TriggerKeyAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    private static final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

    private ScanHandler mScanHandler;
    private ScanFilterThread search_runnable;

    private ProgressBar pbProximity;
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private ToggleGroup tgSearchAssetType;
    private EditText etAssetBarcode;
    private SearchView svSearchAsset;
    private TextView tvProximity;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

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

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        btnSearchAsset.setOnClickListener(this::onClick);

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
            stopScanner();
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

    @Override
    protected void onClick(View view) {
        selectedBarcode = etAssetBarcode.getText().toString();
        if (Strings.isEmptyOrWhitespace(selectedBarcode)) {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.no_epc_filter_selected), Toast.LENGTH_LONG));
            return;
        }

        if (search_runnable == null) {
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            search_runnable = new ScanFilterThread(mScanHandler);
            search_runnable.setFilterEPC(selectedBarcode);
            search_runnable.startReading();
            btnSearchAsset.setText(R.string.stop_search);
            mScanHandler.postDelayed(search_runnable, 0);
        } else if (!search_runnable.isReading()) {
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            search_runnable.setFilterEPC(selectedBarcode);
            search_runnable.startReading();
            btnSearchAsset.setText(R.string.stop_search);
            mScanHandler.postDelayed(search_runnable, 0);
        } else {
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            btnSearchAsset.setText(R.string.scan_bin);
            pbProximity.setProgress(0);
            tvProximity.setText(R.string.proximity);
            stopScanner();
        }
    }

    @Override
    protected void onStop() {
        this.stopScanner();
        super.onStop();
    }

    // ###################################################
    private void stopScanner() {
        if(this.search_runnable !=null) {
            this.search_runnable.stopReading();
            mScanHandler.removeCallbacks(this.search_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<SearchActivity> mActivity;

        public ScanHandler(SearchActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    int rssi_from_tag = msg.getData().getInt("rssi");
                    System.out.println("RSSI:" + rssi_from_tag);
                    int rssi_norm = normalize(rssi_from_tag);

                    if (rssi_norm > 5 && rssi_norm < 95) {
                        tvProximity.setText(String.valueOf(rssi_norm));
                        pbProximity.setProgress(rssi_norm);
                        if (rssi_norm < 95 && rssi_norm >= 80) {
                            toneG.startTone(ToneGenerator.TONE_DTMF_D, 200);
                        } else if (rssi_norm < 80 && rssi_norm >= 60) {
                            toneG.startTone(ToneGenerator.TONE_DTMF_9, 130);
                        } else if (rssi_norm < 60 && rssi_norm >= 40) {
                            toneG.startTone(ToneGenerator.TONE_DTMF_5, 100);
                        } else {
                            toneG.startTone(ToneGenerator.TONE_DTMF_1, 50);
                        }
                    } else if (rssi_norm >= 95) {
                        toneG.startTone(ToneGenerator.TONE_DTMF_D, 300);
                        tvProximity.setText(">= 95%");
                        pbProximity.setProgress(100);
                    } else {
                        toneG.startTone(ToneGenerator.TONE_DTMF_1, 10);
                        tvProximity.setText("<= 5%");
                        pbProximity.setProgress(0);
                    }
                    break;
                case 1980:
                    tvProximity.setText("");
                    pbProximity.setProgress(0);
            }
        }

        private int normalize(double rssi) {
            final double MAX_RSSI = -35d;
            final double MIN_RSSI = -70;
            rssi = rssi > MAX_RSSI ? MAX_RSSI : rssi;
            rssi = rssi < MIN_RSSI ? MIN_RSSI : rssi;
            return (int) (Math.abs(rssi - MIN_RSSI) / (MAX_RSSI - MIN_RSSI) * 100);
        }
    }
}
