package io.agritrack.hotel.ui.incoming;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelIncomingLinenActivity<uploadSvc> extends LocationAwareActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;
    private String selectedAssetType = AssetType.ALL;
    private String activeFilter = null;
    private MobileDB db;

    private TreelikeAdapter adapterIncomingItems;
    private Spinner spLinenType;

    private TextView tvIncomingProcessFrom, tvIncomingProcessTo;
    private ExpandableListView xvIncomingItems;

    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    private Integer selectedParent, selectedChild;

    private ProgressDialog progressDialog;

    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private final boolean storeLocation = true;
    private ImageView ivSupport, ivNext, ivBack;
    private TextView tvGroupsCnt, tvItemsCnt;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_incoming_linen);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // get  references of the controls
        assignCtrlVars();

        ArrayAdapter<String> linenTypeAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item_1, schemeSvc.allNames()) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position % 2 == 0) { // we're on an even row
                    view.setBackgroundColor(getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getColor(R.color.light_grey));
                }
                return view;
            }
        };
        linenTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spLinenType.setAdapter(linenTypeAdapter);

        spLinenType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedAssetType = parent.getItemAtPosition(position).toString(); //this is your selected item
                activeFilter = schemeSvc.codeOf(selectedAssetType);
            }
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        confirmGPSSelectionDlg = YesNoDialogFragment.instance();
        confirmGPSSelectionDlg.setMessage(getText(R.string.procced_without_location));
        confirmGPSSelectionDlg.onConfirm(bundle -> {
            proceedWithoutLocation = true;
            moveToNextScreen();
        });
        confirmGPSSelectionDlg.onReject(bundle -> {
            mLastLocation = findLocation();
            proceedWithoutLocation = false;
        });

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HotelIncomingLinenActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        // display items counter
        tvItemsCnt.setVisibility(View.VISIBLE);

        xvIncomingItems.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                clearSelectedItem();

                selectedParent = null;
                selectedChild = null;
                return false;
            }
        });

        xvIncomingItems.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ConstraintLayout view = (ConstraintLayout) v;
                TextView tvSiteName = v.findViewById(R.id.tvSiteName);
                selectedBarcode = tvSiteName.getText().toString();

                clearSelectedItem();

                v.setSelected(true);
                view.setBackgroundColor(Color.GRAY);
                selectedItem = view;

                selectedParent = groupPosition;
                selectedChild = childPosition;

                return true;
            }
        });

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // onClick button event handling...
        ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

            if (selectedParent != null && selectedChild != null) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterIncomingItems.removeItem(selectedParent, selectedChild);
                        adapterIncomingItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterIncomingItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterIncomingItems.getItemsCount()));
                        selectedBarcode = null;
                        selectedChild = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else if (adapterIncomingItems.getGroupCount() > 0) {
                // <delete> Button was pressed without selecting a Bin first.
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                //confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    adapterIncomingItems.removeAll();
                    adapterIncomingItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterIncomingItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterIncomingItems.getItemsCount()));
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                CToast(getApplicationContext(), render("Item list is empty!!"), Toast.LENGTH_LONG);
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelIncomingLinenActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = updateState();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), HotelHomeActivity.class);
                startActivity(i);
            }
        }
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
        stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }
            if (adapterIncomingItems != null) {
                GlobalState.recWHIncoming.items = adapterIncomingItems.getValues();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            GlobalState.recWHIncoming.state = WarehouseTxState.Incoming;
            GlobalState.recWHIncoming.assetType = this.selectedAssetType;
            GlobalState.recWHIncoming.site = LocalPreferences.getCurrentSiteName();

            if (mLastLocation != null) {
                recWHIncoming.longitude = mLastLocation.getLongitude();
                recWHIncoming.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        ivBack.setOnClickListener(view -> {
            //Stop scanning since we navigate to previous activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), HotelIncomingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        xvIncomingItems = findViewById(R.id.xvIncomingItems);
        spLinenType = findViewById(R.id.spLinenType);
        tvIncomingProcessFrom = findViewById(R.id.tvIncomingProcessFrom);
        tvIncomingProcessTo = findViewById(R.id.tvIncomingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHRFIDIncoming(db);

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncHotelRFIDIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new HotelIncomingLinenActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.recWHIncoming.items == null || GlobalState.recWHIncoming.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Incoming items'"));
            }
        }
        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord WHTxRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.fromSite)) {
            tvIncomingProcessFrom.setText(WHTxRecord.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.toSite)) {
            tvIncomingProcessTo.setText(WHTxRecord.toSite);
        }

        if (WHTxRecord.items != null) {
            adapterIncomingItems.setValues(WHTxRecord.items);
            adapterIncomingItems.notifyDataSetChanged();
        }
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            scanButton.setText(R.string.scan_assets);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    private boolean deleteTx(){
        try {
            System.out.println("About to delete asset tx");
            AssetTransaction delObj = new AssetTransaction();
            delObj.id = recWHIncoming.txKey;
            db.assetTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
            AssetTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                deleteTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_AssetTx_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<AssetTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_timeout), Toast.LENGTH_LONG));
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render("Network Error :: " + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }

    private class ScanHandler extends Handler {
        private final WeakReference<HotelIncomingLinenActivity> mActivity;

        public ScanHandler(HotelIncomingLinenActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    clearSelectedItem();
                    if (epcList != null && !epcList.isEmpty()) {
                        Map<String, List<String>> values = epcList.stream().map(m -> m.toString()).collect(Collectors.groupingBy(g -> schemeSvc.schemeCode(g), Collectors.toCollection(ArrayList::new)));
                        if (adapterIncomingItems == null) {
                            adapterIncomingItems = new TreelikeAdapter(mActivity.get(), values);
                            xvIncomingItems.setAdapter(adapterIncomingItems);
                        } else {
                            adapterIncomingItems.appendItems(values);
                        }
                        adapterIncomingItems.notifyDataSetChanged();
                    }
                    if (adapterIncomingItems != null) {
                        tvGroupsCnt.setText(String.valueOf(adapterIncomingItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterIncomingItems.getItemsCount()));
                    }
                    break;
                case 1980:
                    if (adapterIncomingItems.getValues().containsKey("XXXX")){
                        for(int i=0; i<2; i++) {
                            CToast(getApplicationContext(), render(adapterIncomingItems.getValues().get("XXXX").size() + getResources().getString(R.string.not_encoded_tags)), Toast.LENGTH_LONG);
                        }
                    }
                    break;
            }
        }
    }
}