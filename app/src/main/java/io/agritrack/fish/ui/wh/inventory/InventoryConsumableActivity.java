package io.agritrack.fish.ui.wh.inventory;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHInventory;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.barcode.BarcodeScanService;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.CoInventoryDTO;
import io.agritrack.data.dto.wh.CoInventoryItemDTO;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.CoInventoryItem;
import io.agritrack.data.model.wh.FoodSku;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.ConsumableType;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.BarcodeRecyclerAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InventoryConsumableActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private ToggleGroup tgChooseConsumableType;
    private String selectedConsumableType = ConsumableType.ALL.name();
    private String activeFilter = null;
    private int selectedToggleButton = -1;
    private MobileDB db;

    private RecyclerView rvInventoryItems;
    private TextView tvInventoryItemsCount;
    private boolean scanning = false;
    private BarcodeScanService scanService;
    private BarcodeRecyclerAdapter adapterInventoryItems;
    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                // get an instance of local DB
                db = MobileDB.getInstance(getAppContext());
                String barcode = new String(data);
                if (barcode.length()>=24 && barcode.startsWith("02")) {
                    String gtin = barcode.substring(2, 16);
                    int items = Integer.parseInt(barcode.substring(18, 20));
                    FoodSku food = db.foodSkuDAO().getByGtin(gtin);
                    if (food!=null) {
                        adapterInventoryItems.addItems(food.description, items);
                        adapterInventoryItems.notifyDataSetChanged();
                    }
                } else {

                }
                if (barcode.length()>=24 && barcode.startsWith("01")) {
                    String gtin = barcode.substring(2, 16);
                    FoodSku food = db.foodSkuDAO().getByGtin(gtin);
                    if (food!=null) {
                        adapterInventoryItems.addItem(food.description);
                        adapterInventoryItems.notifyDataSetChanged();
                    }
                } else {

                }
                tvInventoryItemsCount.setText("# " + adapterInventoryItems.getItemCount());
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
            TextView tvRecyclerItem = view.findViewById(R.id.tvItemDescription);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;
    private ImageButton ivAddItem, ivDeleteItem;
    private Button btnScanConsumable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_consumable);

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

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(InventoryConsumableActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryItems.setLayoutManager(layoutManager);
        rvInventoryItems.setItemAnimator(new DefaultItemAnimator());
        adapterInventoryItems = new BarcodeRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvInventoryItems.setAdapter(adapterInventoryItems);
        rvInventoryItems.setNestedScrollingEnabled(false);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        ivDeleteItem.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterInventoryItems.removeItem(barcode);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvInventoryItemsCount.setText(String.valueOf(adapterInventoryItems.getItemCount()));
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

       /* ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        btnScanConsumable.setOnClickListener(view -> {
            clearSelectedItem();
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(InventoryConsumableActivity.this);
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

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            if (adapterInventoryItems != null) {
                GlobalState.recWHInventory.barcodeItems = adapterInventoryItems.getValues();
            }
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }

            GlobalState.recWHInventory.consumableType = ConsumableType.valueOf(this.selectedConsumableType);
            GlobalState.recWHInventory.site = LocalPreferences.getCurrentSiteName();
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
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), InventoryStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgChooseConsumableType = findViewById(R.id.tgChooseConsumableType);
        rvInventoryItems = findViewById(R.id.rvInventoryItems);
        tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToWhMenu);
        btnScanConsumable = findViewById(R.id.btnScanConsumable);
        ivSupport = findViewById(R.id.ivSupport);
        tgChooseConsumableType.setOnCheckedChangeListener(this);
    }

 /*   private void showAddDialog() {
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
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            CoInventory invtx = GlobalState.commitWHCoInventory(db);
            List<CoInventoryItem> invItemtxs = GlobalState.commitWHCoInventoryItem(db, invtx);

            // sync WH Inventory Tx
            Call<CoInventoryDTO> syncInvTxCallBack = updService.syncCoInventoryTx(CoInventoryDTO.convert(invtx), "Bearer " + token);
            //Call<List<CoInventoryItemDTO>> syncInvItemTxCallBack = updService.syncCoInventoryItemTx(CoInventoryItemDTO.convert(invItemtxs), "Bearer " + token);
            syncInvTxCallBack.enqueue(new InventoryConsumableActivity.SyncInvTxCallBack());
            //syncInvItemTxCallBack.enqueue(new InventoryConsumableActivity.SyncInvItemTxCallBack());

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
            if (GlobalState.recWHInventory.barcodeItems == null || GlobalState.recWHInventory.barcodeItems.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Inventory items'"));
            }
        }
        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if (selectedToggleButton == checkedId) {
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch (checkedId) {
            case R.id.tbFood:
                selectedConsumableType = Constants.ftFood;
                activeFilter = Filters.BARCODE_FOOD;
                break;
            case R.id.tbVaccine:
                selectedConsumableType = Constants.ftVaccine;
                activeFilter = Filters.BARCODE_VACCINE;
                break;
            case R.id.tbAntibiotic:
                selectedConsumableType = Constants.ftAntibiotic;
                activeFilter = Filters.BARCODE_ANTIBIOTIC;
                break;
            default:
                selectedConsumableType = Constants.ftAll;
                activeFilter = null;
                selectedToggleButton = -1;
                break;
        }
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

    public class SyncInvTxCallBack implements Callback<CoInventoryDTO> {
        @Override
        public void onResponse(Call<CoInventoryDTO> call, Response<CoInventoryDTO> response) {
            CoInventoryDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory update failure!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<CoInventoryDTO> call, Throwable error) {
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

    public class SyncInvItemTxCallBack implements Callback<List<CoInventoryItemDTO>> {
        @Override
        public void onResponse(Call<List<CoInventoryItemDTO>> call, Response<List<CoInventoryItemDTO>> response) {
            List<CoInventoryItemDTO> rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory items update failure!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<CoInventoryItemDTO>> call, Throwable error) {
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