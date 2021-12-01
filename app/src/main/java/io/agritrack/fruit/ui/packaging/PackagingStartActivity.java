package io.agritrack.fruit.ui.packaging;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recHarvest;
import static io.agritrack.fruit.state.FruitGlobalState.recPackaging;
import static io.agritrack.ui.custom.CustomToast.CToast;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.login.api.SyncApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class PackagingStartActivity extends AppCompatActivity {

    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private TextView tvPoleName;
    private Button btnScanPole;
    private UhfReader uhfReader;
    private boolean scanning = false;
    private ScanInventoryThread inventoryTotesThread = new ScanInventoryThread();
    private TemplateRecyclerAdapter adapterTotes;
    private RecyclerView rvTotesForPackage;
    private TextView tvTotesCount;
    private ImageButton ivAddTote, ivDeleteTote;
    private String toteBarcode;
    private String warehouse;
    private Optional<String> firstToteRfid;
    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    // Instantiate a clickListener to be passed to adapterBins.
    // It will be used to set the selectedBarcode var to the selected item barcode.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaging_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackagingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTotesForPackage.setLayoutManager(layoutManager);
        rvTotesForPackage.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvTotesForPackage.setAdapter(adapterTotes);
        rvTotesForPackage.setNestedScrollingEnabled(false);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvTotesCount.setText(String.valueOf(response.size()));
            adapterTotes.setValues(new ArrayList<>(response));
            adapterTotes.notifyDataSetChanged();
            firstToteRfid = response.stream().findFirst();
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilter(Filters.RFID_POLE);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvPoleName.setText(epcStr);
                            Asset pole = db.assetDAO().getAssetByEpc(epcStr);
                            Site tempSite = db.siteDAO().getBySiteNameAndCode(LocalPreferences.getCurrentSiteLevel3(), pole.siteCode);
                            if (tempSite != null) {
                                warehouse = tempSite.name;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

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
            supportDialog = new SupportDialog(PackagingStartActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToPackagingLot);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), PackagingLotActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackagingSelectOrder);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PackagingSelectOrderActivity.class);
            startActivity(i);
        });
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void invokeSyncAll() {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void initControlsFromState() {
        PackagingRecord trns = recPackaging;

        if (!Strings.isEmptyOrWhitespace(trns.poleRFID)) {
            tvPoleName.setText(trns.poleRFID);
        }

        if (trns.totesForPackaging != null) {
            adapterTotes.setValues(new LinkedList<>(trns.totesForPackaging));
            adapterTotes.notifyDataSetChanged();
            //Get reference of binsCount textView
            //TextView tvBinsCount = findViewById(R.id.tvBinsCount);
            tvTotesCount.setText(String.valueOf(trns.totesForPackaging.size()));
        }
    }

    private PackagingRecord updateState() {
        PackagingRecord packagingRecord = FruitGlobalState.initPackagingRecord();

        packagingRecord.poleRFID = tvPoleName.getText().toString();

        if (!Strings.isEmptyOrWhitespace(this.warehouse)) {
            packagingRecord.warehouse = this.warehouse;
        }

        packagingRecord.totesForPackaging = new LinkedList<>(adapterTotes.getValues());

        if (tvTotesCount.getText() != null && !Strings.isEmptyOrWhitespace(tvTotesCount.getText().toString())) {
            packagingRecord.totalTotesForPackaging = Short.valueOf(tvTotesCount.getText().toString());
        }

        return packagingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recHarvest.poleRFID)) {
                sb.append(String.format("\n%s is missing", "'Warehouse tag'"));
            }
            if (recPackaging.totesForPackaging == null || recPackaging.totesForPackaging.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Totes for packaging'"));
            }
        }

        return sb.toString();
    }

    private void assignCtrlVars() {
        btnScanPole = findViewById(R.id.btnScanPole);
        tvPoleName = findViewById(R.id.tvPoleName);
        ivSupport = findViewById(R.id.ivSupport);
        rvTotesForPackage = findViewById(R.id.rvTotesForPackage);
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
            if (inventoryTotesThread.getState() == Thread.State.TERMINATED) {
                inventoryTotesThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            inventoryTotesThread.setScanInProgress(scanning);
            inventoryTotesThread.setUhfReader(uhfReader);
            inventoryTotesThread.setScanResult(scanResult);
            inventoryTotesThread.setFilter(Filters.RFID_TOTE);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (inventoryTotesThread.getState() == Thread.State.NEW) {
                    inventoryTotesThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_totes);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    inventoryTotesThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type item BARCODE");

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
                adapterTotes.addItem(toteBarcode);
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
}