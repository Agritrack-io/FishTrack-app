package io.agritrack.fish.ui.wh.zebra.inventory;

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
import com.zebra.rfid.api3.TagData;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.caen.api.ICAEN_API;
import io.agritrack.caen.api.RFIDModuleFactory;
import io.agritrack.caen.api.ZebraTC26Commander;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.RFIDInventoryRqDTO;
import io.agritrack.data.dto.wh.RFIDInventoryItemDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZebraInventoryAssetActivity extends LocationAwareActivity implements ZebraTC26Commander.ResponseHandlerInterface {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    private Boolean scanner_running = null;
    private ICAEN_API uhfReader = null;
    private MobileDB db;
    private ExpandableListView xvInventoryItems;
    private Spinner spAssetType, spSite;

    private TreelikeAdapter adapterInventoryItems;
    private String selectedAssetType = AssetType.ALL;
    private String activeFilter = null;
    private ImageButton ivAddItem, ivDeleteItem;
    private Button scanButton;
    private Integer selectedParent, selectedChild;
    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    private ProgressDialog progressDialog;

    private ImageView ivSupport, ivNext, ivBack;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private TextView tvGroupsCnt, tvItemsCnt;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_inventory_asset);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        uhfReader = RFIDModuleFactory.getInstance(this);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<Site> sites = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (sites != null && !sites.isEmpty()) {
            String[] site = sites.stream().map(x -> x.name).toArray(String[]::new);
            ArrayAdapter<String> hrAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item_1, site);
            hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
            spSite.setAdapter(hrAdapter);
        }

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

        spAssetType.setSelection(3);

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

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(ZebraInventoryAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // display groups counter
        tvGroupsCnt.setVisibility(View.VISIBLE);

        // display items counter
        tvItemsCnt.setVisibility(View.VISIBLE);

        xvInventoryItems.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            clearSelectedItem();
            selectedParent = 0;
            selectedChild = 0;
            return false;
        });

        final int[] taps = {0};

        xvInventoryItems.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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
                        adapterInventoryItems.removeItem(selectedParent, selectedChild);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
                        tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
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
            } else if (adapterInventoryItems!=null && adapterInventoryItems.getGroupCount() > 0) {
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
//                    CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                CToast(getApplicationContext(), render(R.string.empty_list), Toast.LENGTH_LONG);
            }
        });

        // display support dialog
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ZebraInventoryAssetActivity.this);
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
        closeScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeScanner();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        spSite = findViewById(R.id.spSite);
        spAssetType = findViewById(R.id.spAssetType);
        xvInventoryItems = findViewById(R.id.xvInventoryItems);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivSupport = findViewById(R.id.ivSupport);
        scanButton = findViewById(R.id.btnScanAsset);
        tvGroupsCnt = findViewById(R.id.tvGroupsCnt);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (uhfReader != null) {
                uhfReader.StopReading();
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
            if (uhfReader != null) {
                uhfReader.StopReading();
            }

            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private boolean updateState() {
        recWHInventory.selectedSite = LocalPreferences.getCurrentSiteName();

        if (spSite.getSelectedItem() != null) {
            recWHInventory.subSite = spSite.getSelectedItem().toString();
        }
        recWHInventory.subSitePos = spSite.getSelectedItemPosition();

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            RFIDInventory invtx = GlobalState.commitWHRFIDInventory(db);
            List<RFIDInventoryItem> invItemtxs = GlobalState.commitWHRFIDInventoryItem(db, invtx);

            // sync WH Inventory Tx
            RFIDInventoryRqDTO inventoryDto = RFIDInventoryRqDTO.convert(invtx);
            List<RFIDInventoryItemDTO> invItemsDto = RFIDInventoryItemDTO.convert(invItemtxs);
            inventoryDto.rfid_items = invItemsDto.stream().map(x -> new RFIDInventoryItemDTO(x.rfid)).collect(Collectors.groupingBy(g -> g.code, Collectors.toCollection(ArrayList::new)));

            Call<RFIDInventoryRqDTO> syncInvTxCallBack = updService.syncRFIDInventoryTx(inventoryDto, "Bearer " + token);
            syncInvTxCallBack.enqueue(new SyncInvTxCallBack());

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
        if (scanner_running == null) {

            if (uhfReader != null) {
                uhfReader.clearEPCFilter();
            }

//            uhfReader.setFilter(activeFilter);
            uhfReader.inventoryRealTime();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
            scanner_running = true;
        } else if (!scanner_running) {
//            scanner_runnable.setFilter(activeFilter);
            uhfReader.inventoryRealTime();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanButton.setText(R.string.stop_scan);
            scanner_running = true;
        } else {
            uhfReader.StopReading();
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanButton.setText(R.string.scan_assets);
            scanner_running = false;
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
    private void closeScanner() {
        if (uhfReader != null) {
            uhfReader.StopReading();
        }
    }

    public class SyncInvTxCallBack implements Callback<RFIDInventoryRqDTO> {
        @Override
        public void onResponse(Call<RFIDInventoryRqDTO> call, Response<RFIDInventoryRqDTO> response) {
            RFIDInventoryRqDTO rs = response.body();
            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory update failure!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<RFIDInventoryRqDTO> call, Throwable error) {
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

    @Override
    public void handleTagsdata(TagData[] tagData) {
        String[] acceptedCodes = schemeSvc.distinctNamesOnly();
        clearSelectedItem();

        List<TagData> tagList = Arrays.asList(tagData);

        // Extract epcs from tagData
        List<String> epcList = tagList.stream().map(t -> t.getTagID()).collect(Collectors.toList());
        //Filter tags by accepted codes
        List<String> acceptedEpcs = epcList.stream().filter(f -> ArrayUtils.contains(acceptedCodes, schemeSvc.nameOf(schemeSvc.nativeSchemeCode(f)))).collect(Collectors.toList());
        //Filter acceptedEpcs by activeFilter
        Map<String, List<String>> values = null;

        //Filter AcceptedEpcs by activeFilter and group by schemeCode
        if (activeFilter != null) {
            values = acceptedEpcs.stream().filter(f -> f.substring(11).startsWith(activeFilter)).map(m -> m.toString()).collect(Collectors.groupingBy(g -> schemeSvc.nativeSchemeCode(g), Collectors.toCollection(ArrayList::new)));
        } else {
            values = acceptedEpcs.stream().map(m -> m.toString()).collect(Collectors.groupingBy(g -> schemeSvc.nativeSchemeCode(g), Collectors.toCollection(ArrayList::new)));
        }

        final Map<String, List<String>> treeLikeValues = values;

        //Create treeLikeAdapter and pass the values
        if (adapterInventoryItems == null) {
            adapterInventoryItems = new TreelikeAdapter(this, treeLikeValues);
            runOnUiThread(() -> xvInventoryItems.setAdapter(adapterInventoryItems));
        } else {
            runOnUiThread(() -> adapterInventoryItems.appendItems(treeLikeValues));
        }
        runOnUiThread(() -> {
            adapterInventoryItems.notifyDataSetChanged();
            tvGroupsCnt.setText(String.valueOf(adapterInventoryItems.getGroupCount()));
            tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemsCount()));
        });
    }

    @Override
    public void handleTagdata(TagData tagData) {

    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
//            rfidHandler.performInventory();
        }
//            rfidHandler.stopInventory();
    }
}