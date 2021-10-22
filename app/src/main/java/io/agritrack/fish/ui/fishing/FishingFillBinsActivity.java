package io.agritrack.fish.ui.fishing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
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

import cn.pda.serialport.Tools;
import io.agritrack.R;

import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TempLoggerDialog;

import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.fish.ui.bo.BinLoadsMap;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;


public class FishingFillBinsActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    FishingRecord fishingRecord = recFishing;
    private MobileDB db;
    private Button btnCurrentBinScan, btnNextCatch, btnDeleteCatch, btnFillBin;
    private TextView tvCurrentBin, tvBinWeight, tvTotalWeightCount, tvUsedBinsCount, tvAvailableBinsCount;
    private RecyclerView rvWeightBatchesBin;
    private TemplateRecyclerAdapter adapterCatches;
    private String mCatchWeight = "";
    private String currentBin;
    private BinLoadsMap loadsMap;
    private String selectedCatch;
    private ConstraintLayout selectedItem;
    // Instantiate a clickListener to be passed to adapterCatches.
    // It will be used to set the catch var to the selected catch.
    private final View.OnClickListener catchesOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedCatch = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;

            btnDeleteCatch.setEnabled(true);
            btnDeleteCatch.setTextColor(getColor(R.color.aqua));
        }
    };
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_fill_bins);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingFillBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initialize the map for each bin's loads.
        loadsMap = new BinLoadsMap();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvWeightBatchesBin.setLayoutManager(layoutManager);
        rvWeightBatchesBin.setItemAnimator(new DefaultItemAnimator());
        adapterCatches = new TemplateRecyclerAdapter(this, new ArrayList<>(), catchesOnClickListener);
        rvWeightBatchesBin.setAdapter(adapterCatches);
        rvWeightBatchesBin.setNestedScrollingEnabled(false);

        // initially only scan button is active.
        btnCurrentBinScan.setEnabled(true);
        btnNextCatch.setEnabled(false);
        btnNextCatch.setTextColor(Color.DKGRAY);
        btnDeleteCatch.setEnabled(false);
        btnDeleteCatch.setTextColor(Color.DKGRAY);
        btnFillBin.setEnabled(false);
        btnFillBin.setTextColor(Color.DKGRAY);

        // =================================
        // RFID scanning functionality
        btnCurrentBinScan.setOnClickListener(view -> {

            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
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
                            currentBin = epcStr;
                            adapterCatches.setValues(loadsMap.getLoads(currentBin));
                            adapterCatches.notifyDataSetChanged();
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }

            tvUsedBinsCount.setText(loadsMap.loadsCnt());
            tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
        });


        // =================================
        // Adding fish catch functionality
        btnNextCatch.setOnClickListener(view -> {
            clearSelectedItem();
            btnCurrentBinScan.setEnabled(false);
            btnCurrentBinScan.setTextColor(Color.DKGRAY);
            btnFillBin.setEnabled(true);
            btnFillBin.setTextColor(getColor(R.color.aqua));
            btnDeleteCatch.setEnabled(true);
            btnDeleteCatch.setTextColor(getColor(R.color.aqua));

            //show Message box
            showCatchDialog();
        });

        // =================================
        // Adding bin load completion functionality
        btnFillBin.setOnClickListener(view -> {
            clearSelectedItem();
            btnCurrentBinScan.setEnabled(true);
            btnCurrentBinScan.setTextColor(getColor(R.color.aqua));
            btnNextCatch.setEnabled(false);
            btnNextCatch.setTextColor(Color.DKGRAY);
            btnDeleteCatch.setEnabled(false);
            btnDeleteCatch.setTextColor(Color.DKGRAY);
            view.setEnabled(false);
            ((Button) view).setTextColor(Color.DKGRAY);
            rvWeightBatchesBin.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                    return true;
                }
            });
        });

        btnDeleteCatch.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(selectedCatch)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedCatch", selectedCatch);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedCatch + " kg");

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String aCatch = bundle.getString("selectedCatch");
                    if (aCatch != null) {
                        adapterCatches.removeItem(aCatch);
                        adapterCatches.notifyDataSetChanged();

                        tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                        tvTotalWeightCount.setText(loadsMap.totalWeight().toString() + " " + "(" + fishingRecord.reqWeight + ")");

                        //tvInventoryItemsCount.setText(String.valueOf(adapterIncomingItems.getItemCount()));
                        selectedCatch = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                clearSelectedItem();
            } else {
                // <delete> Button was pressed without selecting a Catch first.
                CToast(getApplicationContext(), render("Plz select a Catch to delete!!"), Toast.LENGTH_LONG);
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingFillBinsActivity.this);
            supportDialog.showDialog();
        });

        // ============
        configFooter();
    }

    private void assignCtrlVars() {
        btnCurrentBinScan = findViewById(R.id.btnScanCurrentBin);
        btnNextCatch = findViewById(R.id.btnAddCatch);
        btnDeleteCatch = findViewById(R.id.btnDeleteCatch);
        btnFillBin = findViewById(R.id.btnEndBin);
        tvCurrentBin = findViewById(R.id.tvBinName);
        tvBinWeight = findViewById(R.id.tvBinWeight);
        tvTotalWeightCount = findViewById(R.id.tvTotalWeightCount);
        tvUsedBinsCount = findViewById(R.id.tvUsedBinsCount);
        tvAvailableBinsCount = findViewById(R.id.tvAvailableBinsCount);
        rvWeightBatchesBin = findViewById(R.id.rvWeightBatchesBin);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        tvAvailableBinsCount.setText(String.valueOf(hvst.availBins.size()));
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToDetails);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishingDetailsActivity.class);
            startActivity(i);
        });
    }

    private FishingRecord updateState() {
        FishingRecord fishingRecord = recFishing;

        if (tvTotalWeightCount.getText() != null) {
            fishingRecord.totalFishWeight = loadsMap.totalWeight();
        }

        if (tvUsedBinsCount.getText() != null && !Strings.isEmptyOrWhitespace(tvUsedBinsCount.getText().toString())) {
            fishingRecord.totalBinsUsed = Short.valueOf(tvUsedBinsCount.getText().toString());
        }

        GlobalState.commitFishing(db, Boolean.FALSE);

        return fishingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (GlobalState.recFishing.totalBinsUsed == null) {
            sb.append(String.format("\n%s is missing", "'Harvest bins'"));
        }

        return sb.toString();
    }

    private void showCatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fish Catch Weight");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCatchWeight = input.getText().toString();
                adapterCatches.addItem(mCatchWeight);
                adapterCatches.notifyDataSetChanged();

                tvBinWeight.setText(loadsMap.weightOf(currentBin).toString());
                tvTotalWeightCount.setText(loadsMap.totalWeight().toString() + " " + "(" + fishingRecord.reqWeight + ")");

                btnDeleteCatch.setEnabled(true);
                btnDeleteCatch.setTextColor(getColor(R.color.aqua));
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