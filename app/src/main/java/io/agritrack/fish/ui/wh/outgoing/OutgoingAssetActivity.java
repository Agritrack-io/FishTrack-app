package io.agritrack.fish.ui.wh.outgoing;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHOutgoing;
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

import com.google.android.gms.common.util.ArrayUtils;
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
import io.agritrack.fish.ui.WhMenuActivity;
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

public class OutgoingAssetActivity extends LocationAwareActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;
    private String selectedAssetType = AssetType.ALL;
    private String activeFilter = null;
    private MobileDB db;
    private Spinner spAssetType;

    private TreelikeAdapter adapterOutgoingItems;

    private TextView tvOutgoingProcessFrom, tvOutgoingProcessTo;
    private ExpandableListView xvOutgoingAssets;
    private TextView tvGroupsCnt, tvItemsCnt;

    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    private Integer selectedParent, selectedChild;

    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_asset);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        ArrayAdapter<String> hrAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item_1, schemeSvc.allNames()) {
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
        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spAssetType.setAdapter(hrAdapter);

        spAssetType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(OutgoingAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        // display items counter
        tvItemsCnt.setVisibility(View.VISIBLE);

        xvOutgoingAssets.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                clearSelectedItem();
                selectedParent = 0;
                selectedChild = 0;
                return false;
            }
        });

        final int[] taps = {0};

        xvOutgoingAssets.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childPosition==0) {
                    return false;
                }

                if (groupPosition == selectedParent && childPosition == selectedChild  && (taps[0] % 2)==0) {
                    clearSelectedItem();
                    selectedBarcode = null;
                    taps[0]++;
                    v.setSelected(false);
                    return false;
                }
                taps[0] = 0;

                ConstraintLayout view = (ConstraintLayout) v;
                TextView tvSiteName = v.findViewById(R.id.tvCode);
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

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        // onClick button event handling...
        ivDeleteItem.setOnClickListener(view -> {

            if (selectedParent != null && selectedChild != null && selectedBarcode != null) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        clearSelectedItem();
                        adapterOutgoingItems.removeItem(selectedParent, selectedChild);
                        adapterOutgoingItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterOutgoingItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterOutgoingItems.getItemsCount()));
                        selectedBarcode = null;
                        selectedChild = 0;
                    }
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    clearSelectedItem();
                    selectedBarcode = null;
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else if (adapterOutgoingItems!=null && adapterOutgoingItems.getGroupCount() > 0) {
                // <delete> Button was pressed without selecting a Bin first.
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                //confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    adapterOutgoingItems.removeAll();
                    adapterOutgoingItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterOutgoingItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterOutgoingItems.getItemsCount()));
                });

                confirmSiteSelectionDlg.onReject(bundle -> {
//                    CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                CToast(getApplicationContext(), render(R.string.empty_list), Toast.LENGTH_LONG);
            }
        });

    /*    ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(OutgoingAssetActivity.this);
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
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
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
        stopScanner();
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

            if (adapterOutgoingItems != null) {
                GlobalState.recWHOutgoing.items = adapterOutgoingItems.getValues();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }

            GlobalState.recWHOutgoing.state = WarehouseTxState.Outgoing;
            GlobalState.recWHOutgoing.assetType = this.selectedAssetType;
            GlobalState.recWHOutgoing.site = LocalPreferences.getCurrentSiteName();

            if (mLastLocation != null) {
                recWHOutgoing.longitude = mLastLocation.getLongitude();
                recWHOutgoing.latitude = mLastLocation.getLatitude();
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

            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        xvOutgoingAssets = findViewById(R.id.xvOutgoingItems);
        tvOutgoingProcessFrom = findViewById(R.id.tvOutgoingProcessFrom);
        tvOutgoingProcessTo = findViewById(R.id.tvOutgoingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToStartOutgoing);
    }

    private boolean updateState() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            AssetTransaction tx = GlobalState.commitWHRFIDOutgoing(db);

            // sync WH Incoming Tx
            Call<AssetTxDTO> syncTxAsyncCall = updService.syncRFIDIOTx(AssetTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());

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
            if (GlobalState.recWHOutgoing.items == null || GlobalState.recWHOutgoing.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Outgoing items'"));
            }
        }
        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord outgoingWHRecord = GlobalState.recWHOutgoing;

        if (!Strings.isEmptyOrWhitespace(outgoingWHRecord.fromSite)) {
            tvOutgoingProcessFrom.setText(outgoingWHRecord.fromSite);
        }

        if (!Strings.isEmptyOrWhitespace(outgoingWHRecord.toSite)) {
            tvOutgoingProcessTo.setText(outgoingWHRecord.toSite);
        }

        if (outgoingWHRecord.items != null) {
            adapterOutgoingItems.setValues(outgoingWHRecord.items);
            adapterOutgoingItems.notifyDataSetChanged();
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

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {

            if (response.isSuccessful()) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
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
        private final WeakReference<OutgoingAssetActivity> mActivity;

        public ScanHandler(OutgoingAssetActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    String[] acceptedCodes = schemeSvc.distinctNamesOnly();
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    clearSelectedItem();
                    if (epcList != null && !epcList.isEmpty()) {
                        Map<String, List<String>> values = epcList.stream().filter(f -> ArrayUtils.contains(acceptedCodes, schemeSvc.nameOf(schemeSvc.nativeSchemeCode(f)))).map(m -> m.toString()).collect(Collectors.groupingBy(g -> schemeSvc.nativeSchemeCode(g), Collectors.toCollection(ArrayList::new)));

                        if (adapterOutgoingItems == null) {
                            adapterOutgoingItems = new TreelikeAdapter(mActivity.get(), values);
                            xvOutgoingAssets.setAdapter(adapterOutgoingItems);
                        } else {
                            adapterOutgoingItems.appendItems(values);
                        }
                        adapterOutgoingItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterOutgoingItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterOutgoingItems.getItemsCount()));
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No Assets detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}