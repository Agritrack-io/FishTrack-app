package io.agritrack.fishtrack.ui.fishing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.rfid.SingleShotScanner;
import io.agritrack.fishtrack.state.FishingRecord;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;


public class FishingFillBinsActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MobileDB db;

    private Button btnCurrentBinScan, btnNextCatch, btnFillBin;
    private TextView tvCurrentBin, tvBinWeight, tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;

    private RecyclerView rvWeightBatchesBin;
    private TemplateRecyclerAdapter adapterCatches;

    private String mCatchWeight = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        adapterCatches = new TemplateRecyclerAdapter(this, new ArrayList<>(), null);
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);


        // initially only scan button is active.
        btnCurrentBinScan.setEnabled(true);
        btnNextCatch.setEnabled(false);
        btnNextCatch.setTextColor(Color.DKGRAY);
        btnFillBin.setEnabled(false);
        btnFillBin.setTextColor(Color.DKGRAY);

        // =================================
        // RFID scanning functionality
        btnCurrentBinScan.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            scanner.setUhfReader(UhfReader.getInstance());
            scanner.setFilter(Filters.RFID_BIN);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvCurrentBin.setText(epcStr);
                            btnNextCatch.setEnabled(true);
                            btnNextCatch.setTextColor(getColor(R.color.aqua));
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        // =================================
        // Adding fish catch functionality
        btnNextCatch.setOnClickListener(view -> {
            btnCurrentBinScan.setEnabled(false);
            btnCurrentBinScan.setTextColor(Color.DKGRAY);
            btnFillBin.setEnabled(true);
            btnFillBin.setTextColor(getColor(R.color.aqua));

            //show Message box
            showCatchDialog();
        });

        // =================================
        // Adding bin load completion functionality
        btnFillBin.setOnClickListener(view -> {
            btnCurrentBinScan.setEnabled(true);
            btnCurrentBinScan.setTextColor(getColor(R.color.aqua));
            btnNextCatch.setEnabled(false);
            btnNextCatch.setTextColor(Color.DKGRAY);
            ((Button) view).setEnabled(false);
            ((Button) view).setTextColor(Color.DKGRAY);

        });


        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // ============
        configFooter();
    }

    private void assignCtrlVars() {
        btnCurrentBinScan = findViewById(R.id.btnScanCurrentBin);
        btnNextCatch = findViewById(R.id.btnAddBatch);
        btnFillBin = findViewById(R.id.btnEndBin);
        tvCurrentBin = findViewById(R.id.tvBinName);
        tvBinWeight = findViewById(R.id.tvBinWeight);
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        tvAvailableBinsCount.setText(String.valueOf(hvst.availBins.size()));
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
            startActivity(i);
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }

    private void showCatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fish Catch Weight");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCatchWeight = input.getText().toString();
                adapterCatches.addItem(mCatchWeight );
                adapterCatches.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

}