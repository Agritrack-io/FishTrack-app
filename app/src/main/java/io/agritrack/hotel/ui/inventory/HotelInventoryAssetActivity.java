package io.agritrack.hotel.ui.inventory;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHInventory;
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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import io.agritrack.common.Constants;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.RFIDInventoryDTO;
import io.agritrack.data.dto.wh.RFIDInventoryItemDTO;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HotelInventoryAssetActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;
    private ToggleGroup tgChooseAssetType;
    private MobileDB db;
    private ExpandableListView xvInventoryItems;

    private TreelikeAdapter adapterInventoryItems;
    private String selectedAssetType = AssetType.ALL;
    private String activeFilter = null;
    private int selectedToggleButton = -1;
    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private Integer selectedParent, selectedChild;
    private ConstraintLayout selectedItem;
    private String selectedBarcode;

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
        setContentView(R.layout.activity_hotel_inventory_asset);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

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

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(HotelInventoryAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        // display items counter
        tvItemsCnt.setVisibility(View.VISIBLE);

        xvInventoryItems.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            clearSelectedItem();
            selectedParent = null;
            selectedChild = null;
            return false;
        });

        xvInventoryItems.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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
                        adapterInventoryItems.removeItem(selectedParent, selectedChild);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
                        selectedBarcode = null;
                        selectedChild = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else if (adapterInventoryItems.getGroupCount() > 0) {
                // <delete> Button was pressed without selecting a Bin first.
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                //confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    adapterInventoryItems.removeAll();
                    adapterInventoryItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
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

        // display support dialog
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(HotelInventoryAssetActivity.this);
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
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        tgChooseAssetType = findViewById(R.id.tgChooseAssetType);
        xvInventoryItems = findViewById(R.id.xvInventoryItems);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivSupport = findViewById(R.id.ivSupport);
        tgChooseAssetType.setOnCheckedChangeListener(this);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }
            if (adapterInventoryItems != null) {
                recWHInventory.items = adapterInventoryItems.getValues();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            recWHInventory.assetType = this.selectedAssetType;

            if (mLastLocation != null) {
                recWHInventory.longitude = mLastLocation.getLongitude();
                recWHInventory.latitude = mLastLocation.getLatitude();
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
            Intent i = new Intent(getApplicationContext(), HotelInventoryStartActivity.class);
            startActivity(i);
        });
    }


    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            RFIDInventory invtx = GlobalState.commitWHRFIDInventory(db);
            List<RFIDInventoryItem> invItemtxs = GlobalState.commitWHRFIDInventoryItem(db, invtx);

            // sync WH Inventory Tx
            RFIDInventoryDTO inventoryDto = RFIDInventoryDTO.convert(invtx);
            List<RFIDInventoryItemDTO> invItemsDto = RFIDInventoryItemDTO.convert(invItemtxs);
            inventoryDto.rfid_items = invItemsDto.stream().map(x -> new RFIDInventoryItemDTO(x.rfid)).collect(Collectors.groupingBy(g -> g.code, Collectors.toCollection(ArrayList::new)));

            Call<RFIDInventoryDTO> syncInvTxCallBack = updService.syncRFIDInventoryTx(inventoryDto, "Bearer " + token);
            syncInvTxCallBack.enqueue(new HotelInventoryAssetActivity.SyncInvTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanner_runnable.setFilter(activeFilter);
            scanner_runnable.startReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
        } else {
            scanner_runnable.stopReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanButton.setText(R.string.scan_assets);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if (selectedToggleButton == checkedId) {
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch (checkedId) {
            case R.id.tbCage:
                selectedAssetType = Constants.ftCage;
                activeFilter = schemeSvc.codeOf(selectedAssetType);
                break;
            case R.id.tbNet:
                selectedAssetType = Constants.ftNet;
                activeFilter = schemeSvc.codeOf(selectedAssetType);
                break;
            case R.id.tbBin:
                selectedAssetType = Constants.ftBin;
                activeFilter = schemeSvc.codeOf(selectedAssetType);
                break;
            /*case R.id.tbPlatform:
                selectedAssetType = Constants.ftPlatform;
                activeFilter = schemeSvc.codeOf(selectedAssetType);
                break;*/
            default:
                selectedAssetType = Constants.ftAll;
                activeFilter = null;
                selectedToggleButton = -1;
                break;
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (recWHInventory.items == null || recWHInventory.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Inventory items'"));
            }
        }
        return sb.toString();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    public class SyncInvTxCallBack implements Callback<RFIDInventoryDTO> {
        @Override
        public void onResponse(Call<RFIDInventoryDTO> call, Response<RFIDInventoryDTO> response) {
            RFIDInventoryDTO rs = response.body();
            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory update failure!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<RFIDInventoryDTO> call, Throwable error) {
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
        private final WeakReference<HotelInventoryAssetActivity> mActivity;

        public ScanHandler(HotelInventoryAssetActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    clearSelectedItem();
                    Map<String, List<String>> values = epcList.stream().map(m -> m.toString()).collect(Collectors.groupingBy(g -> schemeSvc.schemeCode(g), Collectors.toCollection(ArrayList::new)));
                    if (adapterInventoryItems == null) {
                        adapterInventoryItems = new TreelikeAdapter(HotelInventoryAssetActivity.this, values);
                        xvInventoryItems.setAdapter(adapterInventoryItems);
                    } else {
                        adapterInventoryItems.appendItems(values);
                    }
                    adapterInventoryItems.notifyDataSetChanged();
                    tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                    tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Inventory scanning is over!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}