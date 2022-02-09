package io.agritrack.fish.ui.fishing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import io.agritrack.R;
import io.agritrack.fish.ui.process.ProcessBinsActivity;
import io.agritrack.fish.ui.transport.TransportBinsActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.sound.SoundUtil;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.dialog.GetTempDataDialog;
import io.agritrack.dialog.InfoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.FishingRecord;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class FishingBinsActivity extends AppCompatActivity {

    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    // Local handler that receives the RFID scanner results.
    private ScanHandler mScanHandler;

    private ScanInventoryThread scanner_runnable;
    private MobileDB db;
    private TemplateRecyclerAdapter adapterBins;
    private RecyclerView rvBins;
    private TextView tvBinsCount;
    private Button btnScanBin;
    private ImageButton ivAddBin, ivDeleteBin;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    // Instantiate a clickListener to be passed to adapterBins Adapter.
    // It will be used to point the selectedBarcode variable to the selected item barcode value.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };
    private String binBarcode;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private InfoDialog infoDialog;
    private ImageView ivInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishing_bins);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFishingBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBins.setLayoutManager(layoutManager);
        rvBins.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvBins.setAdapter(adapterBins);
        rvBins.setNestedScrollingEnabled(false);

        // link trigger/scan button to ClickListener
        btnScanBin.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteBin.setOnClickListener(view -> {
            clearSelectedItem();

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterBins.removeItem(barcode);
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Bin to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddBin.setOnClickListener(view -> {
            showAddDialog();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FishingBinsActivity.this);
            supportDialog.showDialog();
        });

        ivInfo.setOnClickListener(view -> {
            infoDialog = new InfoDialog(FishingBinsActivity.this);
            infoDialog.showDialog();
        });

        // create Footer
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
        this.stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToTeam);
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable!=null) {
                scanner_runnable.stopReading();
            }
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), FishingCageActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToMain);
        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable!=null) {
                scanner_runnable.stopReading();
            }
            Intent i = new Intent(getApplicationContext(), FishingTeamActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        rvBins = findViewById(R.id.rvBins);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = findViewById(R.id.ivDeleteBin1);
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        ivInfo = findViewById(R.id.ivInfo);
    }

    private void initControlsFromState() {
        FishingRecord hvst = GlobalState.recFishing;

        if (hvst.availBins != null) {
            adapterBins.setValues(new LinkedList<>(hvst.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvBinsCount.setText(String.valueOf(hvst.availBins.size()));
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type bin BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                binBarcode = input.getText().toString();
                adapterBins.addUniqueItem(binBarcode);
                adapterBins.notifyDataSetChanged();
                tvBinsCount.setText(String.valueOf(adapterBins.getValues().size()));
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
        GlobalState.recFishing.availBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.commitFishing(db, Boolean.FALSE);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recFishing.availBins == null || GlobalState.recFishing.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Bins for usage'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            btnScanBin.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else if (!scanner_runnable.isReading()) {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            btnScanBin.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            btnScanBin.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            btnScanBin.setText(R.string.scan_bin);
            mScanHandler.removeCallbacks(scanner_runnable);
        }
    }

    // ###################################################
    private void stopScanner() {
        if(this.scanner_runnable !=null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(null);
            //mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<FishingBinsActivity> mActivity;

        public ScanHandler(FishingBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    if (epcList != null && !epcList.isEmpty()) {
                        epcList.stream().forEach(x->adapterBins.addUniqueItem(x.toString()));
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        adapterBins.notifyDataSetChanged();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Scanning is over!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}