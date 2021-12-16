package io.agritrack.fruit.ui.warehouse.inventory;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recInventory;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TotesInventoryActivity extends LocationAwareActivity {

    private MobileDB db;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);

    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    private UhfReader uhfReader;
    private boolean scanning = false;
    private ScanInventoryThread inventoryTotesThread = new ScanInventoryThread();
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private TemplateRecyclerAdapter adapterTotes;
    private RecyclerView rvInventoryTotes;
    private TextView tvTotesCount;
    private ImageButton ivAddTote, ivDeleteTote;
    private String toteBarcode;

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
        setContentView(R.layout.activity_totes_inventory);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTotesInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(TotesInventoryActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryTotes.setLayoutManager(layoutManager);
        rvInventoryTotes.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvInventoryTotes.setAdapter(adapterTotes);
        rvInventoryTotes.setNestedScrollingEnabled(false);

        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            tvTotesCount.setText(String.valueOf(response.size()));
            adapterTotes.setValues(new ArrayList<>(response));
            adapterTotes.notifyDataSetChanged();
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
            supportDialog = new SupportDialog(TotesInventoryActivity.this);
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
        ivSupport = findViewById(R.id.ivSupport);
        rvInventoryTotes = findViewById(R.id.rvInventoryTotes);
        tvTotesCount = findViewById(R.id.tvTotesCount);
        ivDeleteTote = (ImageButton) findViewById(R.id.ivDeleteTote);
        ivAddTote = (ImageButton) findViewById(R.id.ivAddTote);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recInventory.longitude = mLastLocation.getLongitude();
                    recInventory.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(TotesInventoryActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToFruitInventoryStart);
        ivBack.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            inventoryTotesThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), FruitInventoryStartActivity.class);
            startActivity(i);
        });
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

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (FruitGlobalState.recInventory.totesItems == null || FruitGlobalState.recInventory.totesItems.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Inventory items'"));
            }
        }

        return sb.toString();
    }

    private boolean updateState() {
        if (adapterTotes != null) {
            recInventory.totesItems = adapterTotes.getValues();
        }
        String v = validate();
        if (!Strings.isEmptyOrWhitespace(v)) {
            CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            return false;
        }

        FruitGlobalState.recInventory.assetType = AssetType.valueOf(Constants.ftTote);

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            RFIDInventory invtx = FruitGlobalState.commitWHRFIDInventory(db);
            List<RFIDInventoryItem> invItemtxs = FruitGlobalState.commitWHRFIDInventoryItem(db, invtx);

            // sync WH Inventory Tx
            Call<RFIDInventoryDTO> syncInvTxCallBack = updService.syncRFIDInventoryTx(RFIDInventoryDTO.convert(invtx), "Bearer " + token);
            Call<List<RFIDInventoryItemDTO>> syncInvItemTxCallBack = updService.syncRFIDInventoryItemTx(RFIDInventoryItemDTO.convert(invItemtxs), "Bearer " + token);
            syncInvTxCallBack.enqueue(new TotesInventoryActivity.SyncInvTxCallBack());
            syncInvItemTxCallBack.enqueue(new TotesInventoryActivity.SyncInvItemTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
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
    public class SyncInvItemTxCallBack implements Callback<List<RFIDInventoryItemDTO>> {
        @Override
        public void onResponse(Call<List<RFIDInventoryItemDTO>> call, Response<List<RFIDInventoryItemDTO>> response) {
            List<RFIDInventoryItemDTO> rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory items update failure!!!!!!"), Toast.LENGTH_LONG));
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