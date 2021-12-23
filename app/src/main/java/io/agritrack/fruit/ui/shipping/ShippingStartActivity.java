package io.agritrack.fruit.ui.shipping;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recShipping;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.IfcoBatchByIfcoBarcode;
import io.agritrack.barcode.BarcodeScanService;
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.ShippingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class ShippingStartActivity extends AppCompatActivity {

    private MobileDB db;
    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private TextView tvPoleName;
    private Button btnScanPole;
    private final SingleShotScanner scanner = null; //new SingleShotScanner(); //TODO: remove comment
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<String>> enquiryResult = new MutableLiveData<>();

    private RecyclerView rvIfcoForShipping;
    private TextView tvIfcoCount;
    private boolean scanning = false;
    private String warehouse;
    private BarcodeScanService scanService;
    private TemplateRecyclerAdapter adapterIfco;
    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                invokeEnquiryIfcoBatch(barcode);
                /*adapterIfco.addItem(barcode);
                adapterIfco.notifyDataSetChanged();*/
                tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
                scanning = false;
            }
        }
    };
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsOnClickListener = new View.OnClickListener() {
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

    private String ifcoBarcode;
    private ImageButton ivAddIfco, ivDeleteIfco;
    private Button btnScanIfco;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderShippingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIfcoForShipping.setLayoutManager(layoutManager);
        rvIfcoForShipping.setItemAnimator(new DefaultItemAnimator());
        adapterIfco = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvIfcoForShipping.setAdapter(adapterIfco);
        rvIfcoForShipping.setNestedScrollingEnabled(false);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            //scanner.setUhfReader(_uhfReader);
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
                            if (tempSite !=null) {
                                warehouse = tempSite.name;
                            }
                        }
                    });
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        ivDeleteIfco.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterIfco.removeItem(barcode);
                        adapterIfco.notifyDataSetChanged();
                        tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                clearSelectedItem();
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
            }
        });

        ivAddIfco.setOnClickListener(view -> {
            showAddDialog();
        });

        btnScanIfco.setOnClickListener(view -> {
            clearSelectedItem();
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ShippingStartActivity.this);
            supportDialog.showDialog();
        });

        enquiryResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No ifco batch returned for this ifco"), Toast.LENGTH_LONG);
                return;
            }
            adapterIfco.setValues(response);
            adapterIfco.notifyDataSetChanged();
            tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
        });

        // create Footer
        configFooter();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void startScanning() {
        if (scanService != null) {
            scanning = true;
            scanService.scan();
        }
    }

    private void stopScanning() {
        if (scanService != null) {
            scanService.stopScan();
            scanning = false;
        }
    }

    private void invokeEnquiryIfcoBatch(String ifcoBarcode) {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync collection lot for current Site
            Call<List<String>> enquiryIfcoBatchByIfcoBarcodeAsyncCall = enquiryService.getIfcoBatch(ifcoBarcode, "Bearer " + token);
            enquiryIfcoBatchByIfcoBarcodeAsyncCall.enqueue(new IfcoBatchByIfcoBarcode(this.enquiryResult));


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToShippingDetails);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), ShippingDetailsActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void initControlsFromState() {
        ShippingRecord trns = FruitGlobalState.recShipping;

        if (trns.packagedIfco != null) {
            adapterIfco.setValues(new LinkedList<>(trns.packagedIfco));
            adapterIfco.notifyDataSetChanged();
            //Get reference of binsCount textView
            tvIfcoCount.setText(String.valueOf(trns.packagedIfco.size()));
        }

        if (!Strings.isEmptyOrWhitespace(trns.poleRFID)) {
            tvPoleName.setText(trns.poleRFID);
        }

        warehouse = trns.warehouse;
    }

    private ShippingRecord updateState() {
        ShippingRecord shippingRecord = FruitGlobalState.initShippingRecord();

        shippingRecord.packagedIfco = new LinkedList<>(adapterIfco.getValues());

        if (tvIfcoCount.getText() != null && !Strings.isEmptyOrWhitespace(tvIfcoCount.getText().toString())) {
            shippingRecord.totalIfcoCnt = Integer.valueOf(tvIfcoCount.getText().toString());
        }

        shippingRecord.poleRFID = tvPoleName.getText().toString();

        if (!Strings.isEmptyOrWhitespace(this.warehouse)) {
            shippingRecord.warehouse = this.warehouse;
        }

        return shippingRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recShipping.warehouse)) {
                sb.append(String.format("\n%s is missing", "'Warehouse'"));
            }

            /*if (FruitGlobalState.recShipping.packagedIfco == null || FruitGlobalState.recStorage.packagedIfco.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'IFCO for shipping'"));
            }*/
        }
        return sb.toString();
    }

    private void assignCtrlVars() {
        btnScanPole = findViewById(R.id.btnScanPole);
        tvPoleName = findViewById(R.id.tvPoleName);
        rvIfcoForShipping = findViewById(R.id.rvIfcoForShipping);
        tvIfcoCount = findViewById(R.id.tvIfcoCount);
        ivDeleteIfco = findViewById(R.id.ivDeleteIfco);
        ivAddIfco = findViewById(R.id.ivAddIfco);
        btnScanIfco = findViewById(R.id.btnScanIfco);
        ivSupport = findViewById(R.id.ivSupport);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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
                ifcoBarcode = input.getText().toString();
                adapterIfco.addUniqueItem(ifcoBarcode);
                adapterIfco.notifyDataSetChanged();
                tvIfcoCount.setText(String.valueOf(adapterIfco.getItemCount()));
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