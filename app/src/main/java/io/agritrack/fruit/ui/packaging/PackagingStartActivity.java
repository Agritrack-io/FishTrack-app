package io.agritrack.fruit.ui.packaging;

import static java.time.temporal.ChronoUnit.MINUTES;
import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.FishTrackUtils.LotToDate;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recPackaging;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.sync.CollectionLotsEnquiryCallBack;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.LotDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fruit.state.PackagingRecord;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.EnquiryApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;

public class PackagingStartActivity extends AppCompatActivity {
    // Local handler that receives the RFID scanner results.
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<List<LotDTO>> enquiryResult = new MutableLiveData<>();
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ScanInventoryThread scanner_runnable;
    private Button btnScanPole, btnScanTotes;
    private MobileDB db;
    private ImageView ivSupport, ivNext;
    private SupportDialog supportDialog;
    private TextView tvPoleName;
    private TemplateRecyclerAdapter adapterTotes;
    private RecyclerView rvTotesForPackage;
    private TextView tvTotesCount;
    private ImageButton ivAddTote, ivDeleteTote;
    private String toteBarcode;
    private String warehouse, packagingLot;
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
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaging_start);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackagingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTotesForPackage.setLayoutManager(layoutManager);
        rvTotesForPackage.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvTotesForPackage.setAdapter(adapterTotes);
        rvTotesForPackage.setNestedScrollingEnabled(false);

        enquiryResult.observe(this, response -> {
            if (response == null) {
                CToast(getApplicationContext(), render("No harvest LOT returned for these totes"), Toast.LENGTH_LONG);
                ivNext.setVisibility(View.VISIBLE);
                return;
            }
            if (response.size() == 1) {
                recPackaging.collectionLot = response.get(0).lot;
                String collectionLot = response.get(0).lot.substring(0, 3);
                if (Strings.isEmptyOrWhitespace(collectionLot)) {
                    //CToast(getApplicationContext(), render("No harvest LOT returned for these totes"), Toast.LENGTH_LONG);
                    return;
                }
                LocalDateTime dateStart = LotToDate(collectionLot);

                long minutesBetween = MINUTES.between(dateStart, LocalDateTime.now());
                String hexMinutes = Long.toHexString(minutesBetween).toUpperCase();
                //Decoding hex to minutes dec
                //Long aa = new BigInteger(hexMinutes, 16).longValue();
                packagingLot = String.format("%s%s", collectionLot, hexMinutes);
            /*//Decoding packaging lot to date time
            LocalDateTime tt = LotToDate(packagingLot);
            Long aa = new BigInteger(packagingLot.substring(3), 16).longValue();
            LocalDateTime ttt = tt.plusMinutes(aa.intValue());*/
                ivNext.setVisibility(View.VISIBLE);
            } else if (response.size() > 1) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.lot_restriction)
                        .setMessage(R.string.lot_restriction_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                //CToast(getApplicationContext(), render("More than one harvest LOT returned for these totes. Please select totes of a single LOT and scan again."), Toast.LENGTH_LONG);
                //ivNext.setVisibility(View.VISIBLE);
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        // =================================
        // RFID scanning functionality
        btnScanPole.setOnClickListener(this::onClick);
        btnScanTotes.setOnClickListener(this::onClick);

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
        stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);

        super.onDestroy();
    }

    protected void configFooter() {
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
                Intent i = new Intent(getApplicationContext(), PackagingLotActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitHome);
        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void invokeEnquiryLot(List<String> toteRfids) {
        try {
            EnquiryApi enquiryService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();

            // sync collection lot for current Site
            Call<List<LotDTO>> enquiryCollectionLotAsyncCall = enquiryService.getCollectionLotsByToteRfids(toteRfids, "Bearer " + token);
            enquiryCollectionLotAsyncCall.enqueue(new CollectionLotsEnquiryCallBack(this.enquiryResult));


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

        if (trns.packagingLot != null && !Strings.isEmptyOrWhitespace(trns.packagingLot)) {
            ivNext.setVisibility(View.VISIBLE);
        }

        warehouse = trns.warehouse;
    }

    private PackagingRecord updateState() {
        PackagingRecord packagingRecord = recPackaging;

        packagingRecord.poleRFID = tvPoleName.getText().toString();

        if (!Strings.isEmptyOrWhitespace(this.warehouse)) {
            packagingRecord.warehouse = this.warehouse;
        }

        if (!Strings.isEmptyOrWhitespace(this.packagingLot)) {
            packagingRecord.packagingLot = this.packagingLot;
        }

        packagingRecord.totesForPackaging = new LinkedList<>(adapterTotes.getValues());

        if (tvTotesCount.getText() != null && !Strings.isEmptyOrWhitespace(tvTotesCount.getText().toString())) {
            packagingRecord.totalTotesForPackaging = Integer.valueOf(tvTotesCount.getText().toString());
        }

        return packagingRecord;
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recPackaging.poleRFID)) {
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
        ivDeleteTote = findViewById(R.id.ivDeleteTote);
        ivAddTote = findViewById(R.id.ivAddTote);
        btnScanTotes = findViewById(R.id.btnScanTotes);
        ivNext = findViewById(R.id.ivToPackagingLot);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type item BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toteBarcode = input.getText().toString();
                adapterTotes.addItem(toteBarcode);
                tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
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

    protected void onClick(View view) {
        if (view != null && view.getId() == R.id.btnScanPole) {
            SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_POLE);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else if (view != null && view.getId() == R.id.btnScanTotes) {
            if (scanner_runnable == null) {
                btnScanTotes.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                scanner_runnable = new ScanInventoryThread(mScanHandler);
                scanner_runnable.setFilter(Filters.RFID_TOTE);
                scanner_runnable.startReading();
                btnScanTotes.setText(R.string.stop_scan);
            } else if (!scanner_runnable.isReading()) {
                btnScanTotes.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                scanner_runnable.setFilter(Filters.RFID_TOTE);
                scanner_runnable.startReading();
                btnScanTotes.setText(R.string.stop_scan);
            } else {
                btnScanTotes.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                scanner_runnable.stopReading();
                btnScanTotes.setText(R.string.scan_totes);
            }
            mScanHandler.postDelayed(scanner_runnable, 0);
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

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<PackagingStartActivity> mActivity;

        public ScanHandler(PackagingStartActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            tvPoleName.setText(epcStr);
                            Asset pole = db.assetDAO().getAssetByEpc(epcStr);
                            Site tempSite = db.siteDAO().getBySiteNameAndCode(LocalPreferences.getCurrentSiteLevel3(), pole.siteCode);
                            if (tempSite != null) {
                                warehouse = tempSite.name;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    if (epcList != null && !epcList.isEmpty()) {
                        List<String> epcs = epcList.stream().map(x -> x.toString()).collect(Collectors.toList());
                        epcList.stream().forEach(x -> adapterTotes.addUniqueItem(x.toString()));
                        tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
                        adapterTotes.notifyDataSetChanged();
                    }
                    break;
                case 1980:
                    if (!IsDemo && adapterTotes.getValues() != null) {
                        List<String> toteRfids = adapterTotes.getValues();
                        if (toteRfids != null) {
                            invokeEnquiryLot(toteRfids);
                        }
                    }
                    break;
            }
        }
    }
}