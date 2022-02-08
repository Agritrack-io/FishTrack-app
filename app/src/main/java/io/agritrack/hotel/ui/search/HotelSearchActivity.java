package io.agritrack.hotel.ui.search;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.appcompat.app.AppCompatActivity;
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
import io.agritrack.sound.SoundUtil;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.service.LocalPreferences;

public class HotelSearchActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private static final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final ICAEN_API uhfReader = RFIDModuleFactory.getInstance();

    // **************************************************************
    private final Runnable search_runnable = new SearchRunnable();

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
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
    private ProgressBar searchProgressBar;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_search);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        SoundUtil.initSoundPool(HotelSearchActivity.this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSearch);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate Local Handler that will process the scanning stream.
        //mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        btnSearchAsset.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelSearchActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void loadCagesFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftCage);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<io.agritrack.ui.bo.GenericListModel> selectedAssets = assetsList.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.rfid)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<io.agritrack.ui.bo.GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    private void loadNetsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftNet); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<io.agritrack.ui.bo.GenericListModel> selectedAssets = assetsList.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.rfid)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<io.agritrack.ui.bo.GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    private void loadBinsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftBin); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<io.agritrack.ui.bo.GenericListModel> selectedAssets = assetsList.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.rfid)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<io.agritrack.ui.bo.GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbSheet) {
            selectedAssetType = Constants.ftSheet;
            loadCagesFromLocalDB();
        } else if (checkedId == R.id.tbTowel) {
            selectedAssetType = Constants.ftTowel;
            loadNetsFromLocalDB();
        } else if (checkedId == R.id.tbBlanket) {
            selectedAssetType = Constants.ftBlanket;
            loadBinsFromLocalDB();
        }
        svSearchAsset.setVisibility(View.VISIBLE);
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToHotelHome);
        ivBack.setOnClickListener(view -> {
            //Stop searching since we navigate to previous activity
            if (mScanHandler !=null) {
                mScanHandler.removeCallbacks(search_runnable);
            }

            Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
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
        searchProgressBar = findViewById(R.id.searchProgressBar);
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

    @Override
    protected void onStart() {
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
        this.uhfReader.HighPowerLevel();
        super.onStart();
    }

    @Override
    protected void onStop() {
        this.uhfReader.LowPowerLevel();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    protected void onClick(View view) {
        selectedBarcode = etAssetBarcode.getText().toString();
        if (Strings.isEmptyOrWhitespace(selectedBarcode)) {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.no_epc_filter_selected), Toast.LENGTH_LONG));
            return;
        }

        if (!isScanning) {
            isScanning = true;
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            btnSearchAsset.setText(R.string.stop_search);
            uhfReader.setFilterEPC(selectedBarcode);
            uhfReader.startSearching();
            mScanHandler.postDelayed(search_runnable, 0);
        } else {
            isScanning = false;
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            btnSearchAsset.setText(R.string.title_search);
            pbProximity.setProgress(0);
            tvProximity.setText(R.string.proximity);
            uhfReader.stopSearching();
            mScanHandler.removeCallbacks(search_runnable);
        }
    }

    // ###################################################
    private static class ScanHandler extends Handler {
        private final WeakReference<HotelSearchActivity> mActivity;

        public ScanHandler(HotelSearchActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    int rssi_from_tag = msg.getData().getInt("rssi");
                    //System.out.println("RSSI:" + rssi_from_tag);
                    int rssi_norm = normalize(rssi_from_tag);

                    if (rssi_norm > 5 && rssi_norm < 95) {
                        mActivity.get().tvProximity.setText(String.valueOf(rssi_norm));
                        mActivity.get().pbProximity.setProgress(rssi_norm);
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
                        mActivity.get().tvProximity.setText(">= 95%");
                        mActivity.get().pbProximity.setProgress(100);
                    } else {
                        toneG.startTone(ToneGenerator.TONE_DTMF_1, 10);
                        mActivity.get().tvProximity.setText("<= 5%");
                        mActivity.get().pbProximity.setProgress(0);
                    }
                    break;
                case 1980:
                    mActivity.get().tvProximity.setText("");
                    mActivity.get().pbProximity.setProgress(0);
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

    private final class SearchRunnable implements Runnable {

        @Override
        public void run() {
            try {
                List<RFIDTag> tagList = uhfReader.search();

                if (tagList != null && !tagList.isEmpty()) {
                    RFIDTag tag = tagList.get(0);
                    Message msg = new Message();
                    msg.what = 10;
                    Bundle b = new Bundle();
                    b.putInt("rssi", tag.getRssi());
                    b.putString("epc", tag.getEpc());
                    msg.setData(b);
                    mScanHandler.sendMessage(msg);
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            mScanHandler.post(search_runnable);
        }
    }
}