package io.agritrack.kefalonia.fish.ui.transport;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.state.TransportationRecord;
import io.agritrack.kefalonia.fish.ui.FishHomeActivity;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.rfid.ScanInventoryThread;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import io.agritrack.kefalonia.rfid.ScanInventoryThread;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.sound.SoundUtil;
import io.agritrack.kefalonia.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class TransportBinsActivity extends AppCompatActivity {
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;

    private TemplateRecyclerAdapter adapterBins;

    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;

    private ImageButton ivAddBin, ivDeleteBin;
    private Button scanButton;
    private String binBarcode;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_bins);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        rvBinsForTransport.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), true);
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteBin.setOnClickListener(view -> {
            if (!Strings.isEmptyOrWhitespace(adapterBins.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", adapterBins.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterBins.getSelectedLabel());

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterBins.removeItem(barcode);
                        adapterBins.notifyDataSetChanged();
                        tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));
                        adapterBins.clearSelectedValue();
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
            supportDialog = new SupportDialog(TransportBinsActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    private void assignCtrlVars() {
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        ivDeleteBin = findViewById(R.id.ivDeleteBin);
        ivAddBin = findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanBin);
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
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
        this.stopScanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else if (!scanner_runnable.isReading()) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(Filters.RFID_BIN);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            scanButton.setText(R.string.scan_bin);
            mScanHandler.removeCallbacks(scanner_runnable);
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToDriverConfirm);
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), TransportInfoActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToStartTransport);
        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (trns.availBins != null) {
            adapterBins.setValues(new LinkedList<>(trns.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(trns.availBins.size()));
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

    private TransportationRecord updateState() {
        TransportationRecord transportationRecord = GlobalState.initTransportationRecord();

        transportationRecord.availBins = new LinkedList<>(adapterBins.getValues());

        return transportationRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recTransport.availBins == null || GlobalState.recTransport.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Bins for transport'"));
            }
        }
        return sb.toString();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(null);
            //mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<TransportBinsActivity> mActivity;

        public ScanHandler(TransportBinsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    if (epcList != null && !epcList.isEmpty()) {
                        epcList.stream().forEach(x -> adapterBins.addUniqueItem(x.toString()));
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
