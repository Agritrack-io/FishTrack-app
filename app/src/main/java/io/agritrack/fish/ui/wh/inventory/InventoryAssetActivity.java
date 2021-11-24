package io.agritrack.fish.ui.wh.inventory;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.RFIDInventoryDTO;
import io.agritrack.data.dto.wh.RFIDInventoryItemDTO;
import io.agritrack.data.model.wh.RFIDInventory;
import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.fish.ui.wh.incoming.IncomingAssetActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TreelikeAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.fish.state.GlobalState.recWHInventory;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class InventoryAssetActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgChooseAssetType;
    private MobileDB db;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private ExpandableListView xvInventoryItems;
    private UhfReader uhfReader;
    private ScanInventoryThread transportationBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TreelikeAdapter adapterInventoryItems;
    private String selectedAssetType = AssetType.ALL.name();
    private String activeFilter = null;
    private int selectedToggleButton = -1;
    private ImageButton ivAddItem, ivDeleteItem;
    private Integer selectedParent, selectedChild;
    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_asset);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(InventoryAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        xvInventoryItems.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                clearSelectedItem();

                selectedParent = null;
                selectedChild = null;
                return false;
            }
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

        // initiate RFID scanner behaviour

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            Map<String, List<String>> values = response.stream().collect(Collectors.groupingBy(g -> g.substring(0, 4), Collectors.toCollection(ArrayList::new)));

            if (adapterInventoryItems == null) {
                adapterInventoryItems = new TreelikeAdapter(this, values);
                xvInventoryItems.setAdapter(adapterInventoryItems);
            } else {
                adapterInventoryItems.appendItems(values);
            }
            adapterInventoryItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

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
                        selectedBarcode = null;
                        selectedChild = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG);
            }
        });

       /* ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(InventoryAssetActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        tgChooseAssetType = findViewById(R.id.tgChooseAssetType);
        xvInventoryItems = findViewById(R.id.xvInventoryItems);
        //tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
        ivDeleteItem = (ImageButton) findViewById(R.id.ivDeleteItem);
        ivAddItem = (ImageButton) findViewById(R.id.ivAddItem);
        ivSupport = findViewById(R.id.ivSupport);
        tgChooseAssetType.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recTransport.longitude = mLastLocation.getLongitude();
                    recTransport.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(InventoryAssetActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // stop GPS location updates.
                    stopListener();

                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            transportationBinsThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), InventoryStartActivity.class);
            startActivity(i);
        });
    }

    /*private void showAddDialog() {
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
                itemBarcode = input.getText().toString();
                adapterInventoryItems.addItem(itemBarcode);
                adapterInventoryItems.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }*/

    private boolean updateState() {
        if (adapterInventoryItems != null) {
            GlobalState.recWHInventory.items = adapterInventoryItems.getValues();
        }
        String v = validate();
        if (!Strings.isEmptyOrWhitespace(v)) {
            CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            return false;
        }
        GlobalState.recWHInventory.assetType = AssetType.valueOf(this.selectedAssetType);

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            RFIDInventory invtx = GlobalState.commitWHRFIDInventory(db);
            List<RFIDInventoryItem> invItemtxs = GlobalState.commitWHRFIDInventoryItem(db, invtx);

            // sync WH Inventory Tx
            Call<RFIDInventoryDTO> syncInvTxCallBack = updService.syncRFIDInventoryTx(RFIDInventoryDTO.convert(invtx), "Bearer " + token);
            Call<List<RFIDInventoryItemDTO>> syncInvItemTxCallBack = updService.syncRFIDInventoryItemTx(RFIDInventoryItemDTO.convert(invItemtxs), "Bearer " + token);
            syncInvTxCallBack.enqueue(new SyncInvTxCallBack());
            syncInvItemTxCallBack.enqueue(new SyncInvItemTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    private void prepareScanAvailableBinsButton() {
        // RFID scanning functionality
        uhfReader = UhfReader.getInstance();
        uhfReader.setWorkArea(3);
        uhfReader.setOutputPower(33);

        final Button scanButton = findViewById(R.id.btnScanAsset);
        scanButton.setOnClickListener(view -> {
            clearSelectedItem();
            scanning = !scanning;

            // Following check is required to instantiate a ScanningThread that was stopped previously.
            if (transportationBinsThread.getState() == Thread.State.TERMINATED) {
                transportationBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            transportationBinsThread.setScanInProgress(scanning);
            transportationBinsThread.setUhfReader(uhfReader);
            transportationBinsThread.setScanResult(scanResult);
            transportationBinsThread.setFilter(activeFilter);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (transportationBinsThread.getState() == Thread.State.NEW) {
                    transportationBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    transportationBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
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
                activeFilter = Filters.RFID_CAGE;
                break;
            case R.id.tbNet:
                selectedAssetType = Constants.ftNet;
                activeFilter = Filters.RFID_NET;
                break;
            case R.id.tbBin:
                selectedAssetType = Constants.ftBin;
                activeFilter = Filters.RFID_BIN;
                break;
            case R.id.tbPlatform:
                selectedAssetType = Constants.ftPlatform;
                activeFilter = Filters.RFID_PLATFORM;
                break;
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
            if (GlobalState.recWHInventory.items == null || GlobalState.recWHInventory.items.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Inventory items'"));
            }
        }
        return sb.toString();
    }

    public class SyncInvTxCallBack implements Callback<RFIDInventoryDTO> {
        @Override
        public void onResponse(Call<RFIDInventoryDTO> call, Response<RFIDInventoryDTO> response) {
            RFIDInventoryDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
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

    public class SyncInvItemTxCallBack implements Callback<List<RFIDInventoryItemDTO>> {
        @Override
        public void onResponse(Call<List<RFIDInventoryItemDTO>> call, Response<List<RFIDInventoryItemDTO>> response) {
            List<RFIDInventoryItemDTO> rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<RFIDInventoryItemDTO>> call, Throwable error) {
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
}