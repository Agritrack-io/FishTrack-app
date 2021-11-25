package io.agritrack.fish.ui.wh.incoming;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
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
import io.agritrack.barcode.SoundUtil;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.ConsumableTxDTO;
import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.enums.ConsumableType;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.BarcodeRecyclerAdapter;
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
import static io.agritrack.fish.state.GlobalState.recWHIncoming;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class IncomingConsumableActivity extends LocationAwareActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgChooseConsumableType;

    private String selectedConsumableType = ConsumableType.ALL.name();
    private String activeFilter = null;
    private  int selectedToggleButton = -1;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private boolean scanning = false;
    private BarcodeScanService scanService;
    private BarcodeRecyclerAdapter adapterIncomingItems = null;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                adapterIncomingItems.addItem(barcode);
                adapterIncomingItems.notifyDataSetChanged();
                //tvInventoryItemsCount.setText("# "+String.valueOf(adapterIncomingItems.getItemCount()));
                scanning = false;
            }
        }
    };

    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvItemDescription);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    private TextView tvIncomingProcessFrom, tvIncomingProcessTo;
    private RecyclerView rvIncomingItems;

    private ImageButton ivAddItem, ivDeleteItem;
    private Button btnScanConsumable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_consumable);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(IncomingConsumableActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvIncomingItems.setLayoutManager(layoutManager);
        rvIncomingItems.setItemAnimator(new DefaultItemAnimator());
        adapterIncomingItems = new BarcodeRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvIncomingItems.setAdapter(adapterIncomingItems);
        rvIncomingItems.setNestedScrollingEnabled(false);

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
                        adapterIncomingItems.removeItem(barcode);
                        adapterIncomingItems.notifyDataSetChanged();
                        //tvInventoryItemsCount.setText(String.valueOf(adapterIncomingItems.getItemCount()));
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

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        btnScanConsumable.setOnClickListener(view -> {
            clearSelectedItem();
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(IncomingConsumableActivity.this);
            supportDialog.showDialog();
        });

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

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recWHIncoming.longitude = mLastLocation.getLongitude();
                    recWHIncoming.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(IncomingConsumableActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToStartIncoming);
        ivBack.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), IncomingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgChooseConsumableType = findViewById(R.id.tgChooseConsumableType);
        rvIncomingItems = findViewById(R.id.rvIncomingItems);
        tvIncomingProcessFrom = findViewById(R.id.tvIncomingProcessFrom);
        tvIncomingProcessTo = findViewById(R.id.tvIncomingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        btnScanConsumable = findViewById(R.id.btnScanConsumable);
        ivSupport = findViewById(R.id.ivSupport);
        tgChooseConsumableType.setOnCheckedChangeListener(this);
    }

    private boolean updateState() {
        if (adapterIncomingItems != null) {
            GlobalState.recWHIncoming.barcodeItems = adapterIncomingItems.getValues();
        }
        String v = validate();
        if (!Strings.isEmptyOrWhitespace(v)) {
            CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            return false;
        }
        GlobalState.recWHIncoming.state = WarehouseTxState.Incoming;
        GlobalState.recWHIncoming.assetType = AssetType.valueOf(this.selectedConsumableType);
        GlobalState.recWHIncoming.site = LocalPreferences.getCurrentSiteName();

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            List<ConsumableTransaction> tx = GlobalState.commitWHBarcodeIncoming(db);

            // sync WH Incoming Tx
            Call<List<ConsumableTxDTO>> syncTxAsyncCall = updService.syncBarcodeIOTx(ConsumableTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new IncomingConsumableActivity.SyncTxCallBack());

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
            if (GlobalState.recWHIncoming.barcodeItems == null || GlobalState.recWHIncoming.barcodeItems.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Incoming items'"));
            }
        }

        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord WHTxRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.from)) {
            tvIncomingProcessFrom.setText(WHTxRecord.from);
        }

        if (!Strings.isEmptyOrWhitespace(WHTxRecord.to)) {
            tvIncomingProcessTo.setText(WHTxRecord.to);
        }
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
                adapterIncomingItems.addItem(itemBarcode);
                adapterIncomingItems.notifyDataSetChanged();
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

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if( selectedToggleButton == checkedId){
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch(checkedId){
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

    public class SyncTxCallBack implements Callback<List<ConsumableTxDTO>> {
        @Override
        public void onResponse(Call<List<ConsumableTxDTO>> call, Response<List<ConsumableTxDTO>> response) {
            List<ConsumableTxDTO> rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<ConsumableTxDTO>> call, Throwable error) {
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