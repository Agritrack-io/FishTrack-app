package io.agritrack.philosofish.fish.ui.wh.search;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
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

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.caen.api.ICAEN_API;
import io.agritrack.philosofish.caen.api.RFIDModuleFactory;
import io.agritrack.philosofish.caen.pojo.RFIDTag;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.data.service.EncodingSchemeService;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.ui.WhMenuActivity;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.FilterableAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class SearchActivity extends AppCompatActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private static final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private final String epcPrefix = "BE0019A0000";
    // BX6100 handler
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final ICAEN_API uhfReader = RFIDModuleFactory.getInstance();
    // **************************************************************
    private final Runnable search_runnable = new SearchRunnable();
    protected BroadcastReceiver keyReceiver;
    // Zebra handler
    //SearchHandler rfidHandler;
    private MutableLiveData<String> liveData;
    private ProgressBar pbProximity;
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private Spinner spAssetType;
    private TextView etAssetBarcode;
    private SearchView svSearchAsset;
    private TextView tvProximity, tvHeaders;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private RecyclerView rvAssets;
    private Button btnSearchAsset;
    private String selectedAssetType, code;
    private ProgressBar searchProgressBar;
    private boolean isScanning = false;
    private ImageButton ivPasteItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        SoundUtil.initSoundPool(SearchActivity.this);

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

        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spAssetType.setAdapter(hrAdapter);

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAssetType = parent.getItemAtPosition(position).toString(); //this is your selected item
                code = schemeSvc.codeOf(selectedAssetType);
                /*tvHeaders.setText(selectedAssetType.equalsIgnoreCase("HARVEST_BIN") || selectedAssetType.equalsIgnoreCase("PLATFORM") ?
                        getString(R.string.header_search_bin_platform) : (selectedAssetType.equalsIgnoreCase("NET") ? getString(R.string.header_search_net) : getString(R.string.header_search_cage)));
                */
                loadAssetsByTypeFromLocalDB(selectedAssetType);
                if (adapterAssets != null) {
                    adapterAssets.clearSelectedValue();
                }
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

        svSearchAsset.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(view, 0);
                    }
                }
            }
        });

        svSearchAsset.setIconifiedByDefault(false);

        /*if (svSearchAsset.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(svSearchAsset, InputMethodManager.RESULT_SHOWN);
        }*/

        etAssetBarcode.setOnClickListener(v -> {
            if (adapterAssets != null && adapterAssets.getSelectedValue() != null) {
                runOnUiThread(() -> adapterAssets.clearSelectedValue());
            }
        });

        // link trigger/scan button to ClickListener
        btnSearchAsset.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(SearchActivity.this);
            supportDialog.showDialog();
        });
        ivPasteItem.setOnClickListener(view -> pastePlate());

        configFooter();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //rfidHandler.onCreate(this);
            } else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // If it does contain data, decide if you can handle the data.
        if (!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            return;
        }

        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0); // get position 0 of the clipboard (last copied item)
        if (item.getText() == null || TextUtils.isEmpty(item.getText().toString())) {
            return;
        }

        ivPasteItem.setVisibility(View.VISIBLE);
        etAssetBarcode.setHint(R.string.paste_label);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)etAssetBarcode.getLayoutParams();
//        params.setMargins(0, 0, 10, 0);
//        etAssetBarcode.setLayoutParams(params);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
        if (uhfReader != null)
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
    protected void onPause() {
        super.onPause();
        //rfidHandler.onPause();
    }

    @Override
    protected void onResume() {
        // RFID Handler
        //rfidHandler = new SearchHandler();

        //Scanner Initializations
        //Handling Runtime BT permissions for Android 12 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                //rfidHandler.onCreate(this);
            }

        } else {
            //rfidHandler.onCreate(this);
        }
        //rfidHandler.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //rfidHandler.onDestroy();
    }

    private void loadAssetsByTypeFromLocalDB(String assetType) {
        // load assets for current Site and filter by asset type (if selected).
        this.rvAssets.setAdapter(null);
        this.rvAssets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<Asset> assetsList = db.assetDAO().getAssetsByTypeForSearch(assetType.toUpperCase(Locale.ROOT));
        if (assetsList != null && !assetsList.isEmpty()) {
//            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.uid, x.rfid.substring(x.rfid.length()-10))).collect(Collectors.toList());
            List<GenericListModel> selectedAssets = assetsList.stream()
                    .map(x -> new GenericListModel(x.id, x.rfid.substring(x.rfid.length() - 10), x.code, x.netEyeGirth, x.perimeter))
                    .collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            adapterAssets.notifyDataSetChanged();
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    protected void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            //Stop searching since we navigate to previous activity
            if (mScanHandler != null) {
                mScanHandler.removeCallbacks(search_runnable);
            }

            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void pastePlate() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
        etAssetBarcode.setHint(R.string.paste_label);
        etAssetBarcode.setText(pasteData);

    }

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
        ivPasteItem = findViewById(R.id.ivPasteItem);

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
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                    etAssetBarcode.setText("");
                }
                if (adapterAssets != null) {
                    adapterAssets.getFilter().filter(newText);
                }
                return false;
            }
        });
        svSearchAsset.setOnClickListener(view -> {
            pbProximity.setProgress(0);
            tvProximity.setText(null);
        });
    }

    protected void onClick(View view) {
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        String selectedBarcode = "";
        if (adapterAssets != null) {
            if (adapterAssets.getSelectedValue() != null) {
                selectedBarcode = adapterAssets.getSelectedValue();
                etAssetBarcode.setText(selectedBarcode);
            }
        }
        if (!Strings.isEmptyOrWhitespace(etAssetBarcode.getText().toString())) {
            if (adapterAssets != null && adapterAssets.getSelectedValue() != null) {
                runOnUiThread(() -> adapterAssets.clearSelectedValue());
            }
            selectedBarcode = etAssetBarcode.getText().toString();
        }
        if (Strings.isEmptyOrWhitespace(selectedBarcode)) {
            runOnUiThread(() -> CToast(getAppContext(), render(R.string.no_epc_filter_selected), Toast.LENGTH_LONG));
            return;
        }

        // Check if device model is BX6100, else is Zebra
        if (LocalPreferences.getDeviceModel() != null && LocalPreferences.getDeviceModel().equals("BX6100")) {
            if (!isScanning) {
                isScanning = true;
                btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                btnSearchAsset.setText(R.string.stop_search);
                pbProximity.setProgress(0);
                tvProximity.setText(R.string.proximity);
                //uhfReader.setFilterEPC(epcPrefix + code.substring(0, 3) + selectedBarcode);
                Asset searchAsset = db.assetDAO().getByCode(selectedBarcode);
                if (searchAsset != null) {
                    uhfReader.setFilterEPC(db.assetDAO().getByCode(selectedBarcode).rfid);
                } else {
                    uhfReader.setFilterEPC(epcPrefix + "141" + selectedBarcode);
                }
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
        } else {
            if (!isScanning) {
                isScanning = true;
                btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                btnSearchAsset.setText(R.string.stop_search);
                //rfidHandler.setFilterEPC(epcPrefix + code.substring(0, 3) + selectedBarcode);
                //rfidHandler.performSearching();
            } else {
                isScanning = false;
                //rfidHandler.stopSearching();
                btnSearchAsset.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                btnSearchAsset.setText(R.string.title_search);
                runOnUiThread(() -> {
                    pbProximity.setProgress(0);
                    tvProximity.setText(R.string.proximity);
                });
            }
        }
    }

//    @Override
//    public void handleTagsdata(TagData[] tagData) {
//        int rssi_from_tag = 0;
//        if (tagData != null) {
//            short distance = tagData[0].LocationInfo.getRelativeDistance();
//            rssi_from_tag = normalize(distance);
//        }
//
//        int rssi_norm = normalize(rssi_from_tag);
//
//        if (rssi_norm > 5 && rssi_norm < 95) {
//            runOnUiThread(() -> {
//                this.tvProximity.setText(String.valueOf(rssi_norm));
//                this.pbProximity.setProgress(rssi_norm);
//            });
//        } else if (rssi_norm >= 95) {
//            runOnUiThread(() -> {
//                this.tvProximity.setText(">= 95%");
//                this.pbProximity.setProgress(100);
//            });
//        } else {
//            runOnUiThread(() -> {
//                this.tvProximity.setText("<= 5%");
//                this.pbProximity.setProgress(0);
//            });
//        }
//    }

    private int normalize(double rssi) {
        final double MAX_RSSI = 80d;
        final double MIN_RSSI = 0;
        rssi = rssi > MAX_RSSI ? MAX_RSSI : rssi;
        rssi = rssi < MIN_RSSI ? MIN_RSSI : rssi;
        return (int) (Math.abs(rssi - MIN_RSSI) / (MAX_RSSI - MIN_RSSI) * 100);
    }

//    @Override
//    public void handleTriggerPress(boolean pressed) {
//        btnSearchAsset.callOnClick();
//    }
//
//    @Override
//    public void addToEditText(String selectedValue) {
//        etAssetBarcode.setText(selectedValue);
//    }

    // ###################################################
    private static class ScanHandler extends Handler {
        private final WeakReference<SearchActivity> mActivity;

        public ScanHandler(SearchActivity activity) {
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
