package io.agritrack.fish.ui.wh.search;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.pojo.RFIDTag;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.fish.ui.bo.GenericListModel;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.FilterableAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class StandaloneSearchActivity extends AppCompatActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private static final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final ICAEN_API uhfReader = RFIDModuleFactory.getInstance();
    // **************************************************************
    private final Runnable search_runnable = new SearchRunnable();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ProgressBar pbProximity;
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private Spinner spAssetType;
    private EditText etAssetBarcode;
    private SearchView svSearchAsset;
    private TextView tvProximity, tvHeaders;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private RecyclerView rvAssets;
    private Button btnSearchAsset;
    private String selectedAssetType, code;
    private ProgressBar searchProgressBar;
    private final String epcPrefix = "BE0019A0000";
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_standalone);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        SoundUtil.initSoundPool(StandaloneSearchActivity.this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderSearch);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        String[] names = schemeSvc.distinctNamesOnly();
        ArrayAdapter<String> hrAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item_1, names) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position % 2 == 0) { // we're on an even row
                    view.setBackgroundColor(getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getColor(R.color.light_grey));
                }
                return view;
            }
        };

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAssetType = parent.getItemAtPosition(position).toString(); //this is your selected item
                code = schemeSvc.codeOf(selectedAssetType);
                tvHeaders.setText(selectedAssetType.equalsIgnoreCase("HARVEST_BIN") || selectedAssetType.equalsIgnoreCase("PLATFORM") ?
                        getString(R.string.header_search_bin_platform) : (selectedAssetType.equalsIgnoreCase("NET") ? getString(R.string.header_search_net) : getString(R.string.header_search_cage)));
                loadAssetsByTypeFromLocalDB(selectedAssetType);
                adapterAssets.clearSelectedValue();
                etAssetBarcode.setText("");
                svSearchAsset.setQuery("", false);
                svSearchAsset.setIconified(true);
                svSearchAsset.clearFocus();
                pbProximity.setProgress(0);
                tvProximity.setText(R.string.proximity);
                if (adapterAssets == null) {
                    svSearchAsset.setVisibility(View.GONE);
                } else {
                    svSearchAsset.setVisibility(View.VISIBLE);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (svSearchAsset.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(svSearchAsset, InputMethodManager.SHOW_IMPLICIT);
        }

        etAssetBarcode.setOnClickListener(v -> {
            if (adapterAssets != null && adapterAssets.getSelectedValue() != null) {
                adapterAssets.clearSelectedValue();
            }
        });

        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spAssetType.setAdapter(hrAdapter);

        // instantiate Local Handler that will process the scanning stream.
        //mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        btnSearchAsset.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(StandaloneSearchActivity.this);
            supportDialog.showDialog();
        });
    }

    private void loadAssetsByTypeFromLocalDB(String assetType) {
        // load assets for current Site and filter by asset type (if selected).
        this.rvAssets.setAdapter(null);
        this.rvAssets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<Asset> assetsList = db.assetDAO().getAssetsByTypeForSearch(assetType.toUpperCase(Locale.ROOT));
        if (assetsList != null && !assetsList.isEmpty()) {
//            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid.substring(x.rfid.length()-10))).collect(Collectors.toList());
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.rfid.substring(x.rfid.length()-10), x.code, x.netEyeGirth, x.perimeter)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            adapterAssets.notifyDataSetChanged();
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

   /* private void loadCagesFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAssetsForType(Constants.ftCage);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid)).collect(Collectors.toList());
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
    }*/

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        etAssetBarcode = findViewById(R.id.etAssetBarcode);
        tvHeaders = findViewById(R.id.tvHeaders);
        rvAssets = findViewById(R.id.rvAssets);
        btnSearchAsset = findViewById(R.id.btnSearchAsset);
        pbProximity = findViewById(R.id.pbProximity);
        tvProximity = findViewById(R.id.tvProximity);
        ivSupport = findViewById(R.id.ivSupport);
        searchProgressBar = findViewById(R.id.searchProgressBar);
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
                if (Strings.isEmptyOrWhitespace(newText)) {
                    if (adapterAssets != null) {
                        adapterAssets.clearSelectedValue();
                    }
                    etAssetBarcode.setText("");
                }
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
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
        this.uhfReader.HighPowerLevel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onClick(View view) {
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        String selectedBarcode = "";
        if (adapterAssets != null) {
            if (adapterAssets.getSelectedValue() != null) {
                selectedBarcode = adapterAssets.getSelectedValue();
//                String[] selectedCodes = selectedBarcode.split("/");
//                String selectedCode = selectedCodes[0];
//                Asset selectedAsset = db.assetDAO().getByCode(selectedCode);
                etAssetBarcode.setText(selectedBarcode);
            }
        }
        if (!Strings.isEmptyOrWhitespace(etAssetBarcode.getText().toString())) {
            if (adapterAssets != null && adapterAssets.getSelectedValue() != null) {
                adapterAssets.clearSelectedValue();
            }
            selectedBarcode = etAssetBarcode.getText().toString();
        }
        if (Strings.isEmptyOrWhitespace(selectedBarcode)) {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.no_epc_filter_selected), Toast.LENGTH_LONG));
            return;
        }

        if (!isScanning) {
            isScanning = true;
            btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            btnSearchAsset.setText(R.string.stop_search);
            uhfReader.setFilterEPC(epcPrefix + code.substring(0,3) + selectedBarcode);
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
        private final WeakReference<StandaloneSearchActivity> mActivity;

        public ScanHandler(StandaloneSearchActivity activity) {
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
