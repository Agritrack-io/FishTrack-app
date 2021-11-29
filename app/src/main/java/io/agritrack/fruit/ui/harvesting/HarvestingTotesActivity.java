package io.agritrack.fruit.ui.harvesting;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.TransportationRecord;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.HarvestRecord;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class HarvestingTotesActivity extends AppCompatActivity {

    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private UhfReader uhfReader;
    private ScanInventoryThread harvestTotesThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TemplateRecyclerAdapter adapterTotes;

    private RecyclerView rvUsedTotesHarvest;
    private TextView tvTotesCount;

    private ImageButton ivAddTote, ivDeleteTote;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private String toteBarcode;

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    // Instantiate a clickListener to be passed to adapterBins.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
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
        setContentView(R.layout.activity_harvesting_totes);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderHarvestingTotes);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvUsedTotesHarvest.setLayoutManager(layoutManager);
        rvUsedTotesHarvest.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvUsedTotesHarvest.setAdapter(adapterTotes);
        rvUsedTotesHarvest.setNestedScrollingEnabled(false);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvTotesCount.setText(String.valueOf(response.size()));
            adapterTotes.setValues(new ArrayList<>(response));
            adapterTotes.notifyDataSetChanged();
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteTote.setOnClickListener(view -> {
            clearSelectedItem();

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterTotes.removeItem(barcode);
                        adapterTotes.notifyDataSetChanged();
                        tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Tote to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddTote.setOnClickListener(view -> {
            showAddDialog();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HarvestingTotesActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToConfirm);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), HarvestingConfirmActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToHarvestingStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), HarvestingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        ivSupport = findViewById(R.id.ivSupport);
        rvUsedTotesHarvest = findViewById(R.id.rvUsedTotesHarvest);
        tvTotesCount = findViewById(R.id.tvTotesCount);
        ivDeleteTote = (ImageButton) findViewById(R.id.ivDeleteTote);
        ivAddTote = (ImageButton) findViewById(R.id.ivAddTote);
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setWorkArea(3);
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanTotes);
        scanButton.setOnClickListener(view -> {
            clearSelectedItem();
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (harvestTotesThread.getState() == Thread.State.TERMINATED) {
                harvestTotesThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            harvestTotesThread.setScanInProgress(scanning);
            harvestTotesThread.setUhfReader(uhfReader);
            harvestTotesThread.setScanResult(scanResult);
            harvestTotesThread.setFilter(Filters.RFID_TOTE);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (harvestTotesThread.getState() == Thread.State.NEW) {
                    harvestTotesThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_totes);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    harvestTotesThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void clearSelectedItem(){
        if(selectedItem!=null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void initControlsFromState() {
        HarvestRecord trns = FruitGlobalState.recHarvest;

        if (trns.totes != null) {
            adapterTotes.setValues(new LinkedList<>(trns.totes));
            adapterTotes.notifyDataSetChanged();
            //Get reference of binsCount textView
            //TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvTotesCount.setText(String.valueOf(trns.totes.size()));
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type tote BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toteBarcode = input.getText().toString();
                adapterTotes.addUniqueItem(toteBarcode);
                adapterTotes.notifyDataSetChanged();
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

    private void updateState() {
        FruitGlobalState.recHarvest.totes = new LinkedList<>(adapterTotes.getValues());

        if (tvTotesCount.getText() != null && !Strings.isEmptyOrWhitespace(tvTotesCount.getText().toString())) {
            FruitGlobalState.recHarvest.totalTotesUsed = Short.valueOf(tvTotesCount.getText().toString());
        }
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (FruitGlobalState.recHarvest.totes == null || FruitGlobalState.recHarvest.totes.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Used totes'"));
            }
        }
        return sb.toString();
    }
}