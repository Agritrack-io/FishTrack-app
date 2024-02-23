package io.agritrack.kefalonia.fish.ui.fishing;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recFishing;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.model.BinInfo;
import io.agritrack.kefalonia.dialog.InfoDialog;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.fish.state.FishingRecord;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.ui.bo.BinLoadsMap;
import io.agritrack.kefalonia.fish.ui.bo.BinWeightRecord;
import io.agritrack.kefalonia.scale.diniargeo.MCWScale;
import io.agritrack.kefalonia.ui.adapter.BinLoadAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;


public class FishingFillBinsActivity extends AppCompatActivity implements ISummaryActivity {

    protected BroadcastReceiver keyReceiver;
    private TextView tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;
    private ImageView ivAddTemp;
    private RecyclerView rvWeightBatchesBin;
    private BinLoadAdapter adapterCatches;
    private boolean intentForFillBinActivity = false;
    private String mCatchWeight = "";
    private String currentBin;
    private Integer weightOfBin;
    private BinLoadsMap loadsMap;
    private long epochFrom;
    private ImageView ivSupport, ivInfo, ivCheckLastTemp;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private boolean isClicked = true;
    private boolean showTemp = true;
    private MobileDB db;

    // Bluetooth variables
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private MCWScale scale = null;
    private ProgressBar pbBluetooth;
    private String currState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        loadsMap = new BinLoadsMap();

        ArrayList<BinLoadAdapter.BinLoadItem> list = recFishing.availBins != null
                ? recFishing.availBins.stream()
                .map(BinLoadAdapter.BinLoadItem::new)
                .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        rvWeightBatchesBin.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterCatches = new BinLoadAdapter(this, list);
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

        initControlsFromState();

        int yellowColor = ContextCompat.getColor(this, R.color.yellow);
        int turquoiseColor = ContextCompat.getColor(this, R.color.turquoise);

        ivAddTemp.setOnClickListener(v -> {
            adapterCatches.showTemp(showTemp);
            if (showTemp) {
                ivAddTemp.setImageDrawable(getDrawable(R.drawable.weight));
                ivAddTemp.setColorFilter(yellowColor);
            } else {
                ivAddTemp.setImageDrawable(getDrawable(R.drawable.quality));
                ivAddTemp.setColorFilter(turquoiseColor);
            }
            showTemp = !showTemp;
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingFillBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingFillBinsActivity.this);
            infoDialog.showDialog();
        });

        // ============
        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregister the receiver, pairs with registration in onStart()!!!
        if (keyReceiver != null) {
            unregisterReceiver(keyReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            if (!isClicked) {
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            if (!isClicked) { // && loadsMap.hasLoads()
                CToast(getApplicationContext(), render(R.string.fill_bin), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            Intent i = new Intent(getApplicationContext(), FishingBinsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
        ivCheckLastTemp = findViewById(R.id.ivCheckLastTemp);
        ivAddTemp = findViewById(R.id.ivAddTemp);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
        // bluetooth progress bar
        pbBluetooth = findViewById(R.id.pbBluetooth);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (recFishing.totalFishWeight != null) {
            tvTotalWeightCount.setText(recFishing.totalFishWeight.toString());
        }

        if (recFishing.totalBinsUsed != null) {
            tvUsedBinsCount.setText(recFishing.totalBinsUsed.toString());
        }

        List<BinWeightRecord.BinRecord> bins = recFishing.binWeightRecord.getBinsData();

        List<BinInfo> binInfos = db.binInfoDAO().getAll();

        for (BinWeightRecord.BinRecord bin : bins) {
            Optional<BinInfo> binInfo = binInfos.stream().filter(b -> b.rfid.equals(bin.binEPC)).findFirst();
            if (binInfo.isPresent()) {
                bin.init = binInfo.get().initedAt;
                binInfos.remove(binInfo);
            }
        }

        // sometimes 'hvst.availBins' is null!!!
        int availBinsCnt = hvst.availBins != null ? hvst.availBins.size() : 0;
        tvAvailableBinsCount.setText(String.valueOf(availBinsCnt));

        if (!recFishing.binWeightRecord.isEmpty()) {
            for (BinWeightRecord.BinRecord bin : recFishing.binWeightRecord.getBinsData()) {
                loadsMap.addLoad(bin.binEPC, bin.weight + "");
            }
            adapterCatches.setValues(recFishing.binWeightRecord.getBinsData().stream().map(x -> new BinLoadAdapter.BinLoadItem(x.binEPC, x.weight, x.temp)).collect(Collectors.toList()));
            if (recFishing.reqWeight != null) {
                tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
            } else {
                tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), "N/A"));
            }
            isClicked = true;
        }

        if (loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBinsData();
            tvUsedBinsCount.setText(loadsMap.loadsCnt());
        }
    }

    @Override
    public void refreshSummary() {
        if (adapterCatches.getValues() != null) {
            for (BinLoadAdapter.BinLoadItem binLoad : adapterCatches.getValues()) {
                loadsMap.addLoad(binLoad.epc, binLoad.weight + "");
            }
        }
        if (recFishing.reqWeight != null) {
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), recFishing.reqWeight));
        } else {
            tvTotalWeightCount.setText(String.format("%s (%s)", loadsMap.totalWeight().toString(), "N/A"));
        }

        if (loadsMap.hasLoads()) {
            recFishing.binWeightRecord.getBinsData();
            tvUsedBinsCount.setText(loadsMap.loadsCnt());
        }
    }

    private FishingRecord updateState() {
        // get an instance of local DB
        MobileDB db = MobileDB.getInstance(getAppContext());

        if (adapterCatches.getValues() != null) {
            BinWeightRecord.BinRecord currRec = null;
            for (BinLoadAdapter.BinLoadItem binLoad : adapterCatches.getValues()) {
                loadsMap.addLoad(binLoad.epc, binLoad.weight + "");
                currRec = recFishing.binWeightRecord.getRecordForEPC(binLoad.epc);
                epochFrom = System.currentTimeMillis() / 1000l;
                GlobalState.recFishing.binWeightRecord.addRecord(binLoad.epc, binLoad.weight, binLoad.temperature, currRec.init, epochFrom, null);
            }
        }
        if (tvTotalWeightCount.getText() != null) {
            recFishing.totalFishWeight = loadsMap.totalWeight();
        }

        if (tvUsedBinsCount.getText() != null && !Strings.isEmptyOrWhitespace(tvUsedBinsCount.getText().toString())) {
            recFishing.totalBinsUsed = Short.valueOf(tvUsedBinsCount.getText().toString());
        }
        GlobalState.commitFishing(db, Boolean.FALSE);

        return recFishing;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recFishing.totalBinsUsed == null) {
                sb.append(String.format("\n%s" + R.string.invalid_inputs, "'Harvest bins'"));
            }
            recFishing.binWeightRecord.getBinsData().stream().filter(item -> item.weight != null)
                    .forEach(item -> {
                        if (item.temp != null) {

                        } else {
                            sb.append(String.format("\n" + item.binEPC.substring(item.binEPC.length() - 5) + " has no registered temperature"));
                        }
                    });

            recFishing.binWeightRecord.getBinsData().stream().filter(item -> item.temp != null)
                    .forEach(item -> {
                        if (item.weight != null) {

                        } else {
                            sb.append(String.format("\n" + item.binEPC.substring(item.binEPC.length() - 5) + " has no registered weight"));
                        }
                    });

        }
        return sb.toString();
    }
}