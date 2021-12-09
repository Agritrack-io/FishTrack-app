package io.agritrack.fish.ui.packageQuality;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.caen.api.EncodingUtils.parseTemperature;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.R;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.caen.api.CAENCommander;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.dialog.GetTempDataDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.ProcessingRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.service.LocalPreferences;

public class PackageQualityStartActivity extends AppCompatActivity {

    private MobileDB db;

    private RecyclerView rvBinsForTransport;
    private TextView tvBinsCount;

    private boolean scanning = false;

    private Button btnScanBin;
    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private GetTempDataDialog tempLoggerDialog;
    private String currentBin;
    private List<String[]> values;
    private LinkedList<String> listMeasurements = new LinkedList<>();

    private TemplateRecyclerAdapter adapterBins;

    private ImageButton ivAddBin, ivDeleteBin;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private Set<String> scannedBinEPCs;
    private String binBarcode;
    private String logger_rfid;

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
        setContentView(R.layout.activity_package_quality_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProcessBins);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBinsForTransport.setLayoutManager(layoutManager);
        rvBinsForTransport.setItemAnimator(new DefaultItemAnimator());
        adapterBins = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvBinsForTransport.setAdapter(adapterBins);
        rvBinsForTransport.setNestedScrollingEnabled(false);

        // instantiate a set to hold scanned EPCS.it will be passed to adapter shich feeds the ListView.
        scannedBinEPCs = new LinkedHashSet<>();

        // =================================
        // RFID scanning functionality
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
            supportDialog = new SupportDialog(PackageQualityStartActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private String scanCloserEPC(UhfReader uhfReader) {
        SingleShotScanner scanner = new SingleShotScanner();
        scanner.setUhfReader(uhfReader);
        scanner.setFilter(Filters.RFID_BIN);

        try {
            String epcStr = scanner.call();
            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                // after bin is identified, initialize the temperatures logger.
                IotLogger logger = db.iotLoggerDAO().getByAssetRFID(epcStr);
                if (logger != null) {
                    logger_rfid = logger.rfid;
                    scannedBinEPCs.add(epcStr.substring(11));
                    tvBinsCount.setText(String.valueOf(scannedBinEPCs.size()));
                    adapterBins.setValues(new ArrayList<>(scannedBinEPCs));
                    adapterBins.notifyDataSetChanged();
                    if (IsDemo) {
                        return logger.rfid;
                    } else {
                        return epcStr;
                    }
                } else if (!IsDemo) {
                    CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_LONG);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CAENCommander.Response resetLogger(CAENCommander cmd) {
        return cmd.RESET();
    }

    private String initializeLogger(CAENCommander cmd) {
        try {
            short lastTemperature = cmd.INIT();
            return parseTemperature(lastTemperature) + "\u2103";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearSelectedItem(){
        if(selectedItem!=null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        btnScanBin = findViewById(R.id.btnScanBin);
        tvBinsCount = findViewById(R.id.tvBinsCount);
        rvBinsForTransport = findViewById(R.id.rvBinsForTransport);
        ivDeleteBin = (ImageButton) findViewById(R.id.ivDeleteBin);
        ivAddBin = (ImageButton) findViewById(R.id.ivAddBin);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToPackageQualityInfo);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackageQualityInfoActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ProcessingRecord prcRecord = GlobalState.recProcessing;

        /*if (prcRecord.availBins != null) {
            adapterBins.setValues(new LinkedList<String>(prcRecord.availBins));
            adapterBins.notifyDataSetChanged();
            //Get reference of binsCount textView
            TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvBinsCount.setText(String.valueOf(prcRecord.availBins.size()));
        }*/
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
        GlobalState.initProcessingRecord();

        GlobalState.recProcessing.availBins = new LinkedList<>(adapterBins.getValues());
        GlobalState.recProcessing.tempValues = listMeasurements;
        GlobalState.recProcessing.retrievedAt = System.currentTimeMillis();
        GlobalState.recProcessing.logger_rfid = logger_rfid;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recProcessing.availBins == null || GlobalState.recProcessing.availBins.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Received bins'"));
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onClick(View view) {
        //update scanning, uhfReader, tvPlatformName values in thread
        UhfReader _uhfReader = UhfReader.getInstance();
        _uhfReader.setWorkArea(3);
        _uhfReader.setOutputPower(24);

        String strEPC = scanCloserEPC(_uhfReader);
        if (!Strings.isEmptyOrWhitespace(strEPC)) {
            if (IsDemo) {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
                try {
                    CAENCommander cmd = new CAENCommander(_uhfReader, strEPC);
                    cmd.HighSensitivity();
                    Thread.sleep(1000);
                    short cnt = cmd.READ_SAMPLES_COUNT();
                    Thread.sleep(1000);
                    if (cnt > 0) {
                        try {
                            this.values = cmd.READ_SAMPLES(cnt);
                            displayMeasurementsDialog(values);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cmd.LowSensitivity();

                    /*if (values !=null) {
                        CAENCommander.Response rs = resetLogger(cmd);
                        CToast(getApplicationContext(), "Logger resetted!"*//*temp*//*, Toast.LENGTH_LONG);
                    }*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONArray jsArray = new JSONArray(values);
                String aa = jsArray.toString();
                listMeasurements.add(aa);
//                }
//            });
//            t.start();
            } else {
                tempLoggerDialog = new GetTempDataDialog(PackageQualityStartActivity.this, R.string.init_temp_logger);
                if (!Strings.isEmptyOrWhitespace(strEPC)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            currentBin = strEPC.substring(11);
                            adapterBins.addUniqueItem(currentBin);
                            adapterBins.notifyDataSetChanged();
                            tvBinsCount.setText(String.valueOf(adapterBins.getItemCount()));

                            // after bin is identified, initialize the temperatures logger.
                            IotLogger logger = db.iotLoggerDAO().getByAssetRFID(strEPC);
                            if (logger != null) {
                                tempLoggerDialog.showDialog(logger.rfid);
                            } else if (!IsDemo) {
                                CToast(getApplicationContext(), render("No IOT Logger was found linked to this BIN!!"), Toast.LENGTH_LONG);
                            }
                        }
                    });
                }
            }
        } else {
            CToast(getApplicationContext(), "No Logger Found. Please scan again!!", Toast.LENGTH_LONG);
        }

    }

    private void displayMeasurementsDialog(List<String[]> values) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(PackageQualityStartActivity.this);
        dlgBuilder.setTitle("Logger Data");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PackageQualityStartActivity.this, R.layout.agri_list_item_12dp);

        int idx = 1;
        for (String[] value : values) {
            arrayAdapter.add(String.format("%3d. [%s] --> %s", idx++, value[0], value[1]));
        }
        dlgBuilder.setAdapter(arrayAdapter, null);
        dlgBuilder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        dlgBuilder.create().show();
    }
}