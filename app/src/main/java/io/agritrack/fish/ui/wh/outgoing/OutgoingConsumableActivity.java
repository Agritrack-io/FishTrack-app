package io.agritrack.fish.ui.wh.outgoing;

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
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.enums.ConsumableType;
import io.agritrack.enums.WarehouseTxState;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.WHTxRecord;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.BarcodeRecyclerAdapter;
import io.agritrack.ui.custom.ToggleGroup;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHOutgoing;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class OutgoingConsumableActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener, LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;

    private LocationManager locationManager;
    private TimeOutProgressDlg syncProgressDialog;

    private ToggleGroup tgChooseConsumableType;
    private String selectedConsumableType = ConsumableType.ALL.name();
    private String activeFilter = null;
    private int selectedToggleButton = -1;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private boolean scanning = false;

    private TextView tvOutgoingProcessFrom, tvOutgoingProcessTo;
    private RecyclerView rvOutgoingItems;

    private ImageButton ivAddItem, ivDeleteItem;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private Button btnScanConsumable;

    private ProgressDialog progressDialog;

    private BarcodeScanService scanService;
    private BarcodeRecyclerAdapter adapterOutgoingItems;

    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                adapterOutgoingItems.addItem(barcode);
                adapterOutgoingItems.notifyDataSetChanged();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_consumable);
        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(OutgoingConsumableActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOutgoingItems.setLayoutManager(layoutManager);
        rvOutgoingItems.setItemAnimator(new DefaultItemAnimator());
        adapterOutgoingItems = new BarcodeRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvOutgoingItems.setAdapter(adapterOutgoingItems);
        rvOutgoingItems.setNestedScrollingEnabled(false);

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
                        adapterOutgoingItems.removeItem(barcode);
                        adapterOutgoingItems.notifyDataSetChanged();
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

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        syncProgressDialog = new TimeOutProgressDlg(10000l, 500l, this) {
            @Override
            public void doTasks() {
                locationManager.removeUpdates(OutgoingConsumableActivity.this);

                // Update state and proceed to next
                updateState();

                //Set scanning to false to stop running scan thread
                scanning = false;
                stopScanning();

                toggleProgress(false, R.string.app_name);
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
        };
        syncProgressDialog.setMessage(R.string.acquire_coordinates);

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
        ivNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(OutgoingConsumableActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OutgoingConsumableActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (adapterOutgoingItems.getItemCount() == 0) {
                    String vd = validate();
                    if (!Strings.isEmptyOrWhitespace(vd)) {
                        CToast(getApplicationContext(), render("Invalid inputs : " + vd), Toast.LENGTH_LONG);
                    }
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, OutgoingConsumableActivity.this);
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToStartOutgoing);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgChooseConsumableType = findViewById(R.id.tgChooseConsumableType);
        rvOutgoingItems = findViewById(R.id.rvOutgoingItems);
        tvOutgoingProcessFrom = findViewById(R.id.tvOutgoingProcessFrom);
        tvOutgoingProcessTo = findViewById(R.id.tvOutgoingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        btnScanConsumable = findViewById(R.id.btnScanConsumable);

        tgChooseConsumableType.setOnCheckedChangeListener(this);
    }

    private void updateState() {
        if (adapterOutgoingItems.getItemCount() == 0) {
            return;
        }
        GlobalState.recWHOutgoing.barcodeItems = adapterOutgoingItems.getValues();
        GlobalState.recWHOutgoing.state = WarehouseTxState.Outgoing;
        GlobalState.recWHOutgoing.assetType = AssetType.valueOf(this.selectedConsumableType);
        GlobalState.recWHOutgoing.site = LocalPreferences.getCurrentSiteName();

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHIncomingAssetTX Record data to local DB.
            List<ConsumableTransaction> tx = GlobalState.commitWHBarcodeOutgoing(db);

            // sync WH Incoming Tx
            Call<List<ConsumableTxDTO>> syncTxAsyncCall = updService.syncBarcodeIOTx(ConsumableTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new OutgoingConsumableActivity.SyncTxCallBack());
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
        } finally {
            progressDialog.dismiss();
        }

    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (GlobalState.recWHOutgoing.barcodeItems == null || GlobalState.recWHOutgoing.barcodeItems.isEmpty()) {
            sb.append(String.format("\n%s is missing", "'Outgoing items'"));
        }

        return sb.toString();
    }

    private void initControlsFromState() {
        WHTxRecord outgoingWHRecord = GlobalState.recWHOutgoing;

        if (!Strings.isEmptyOrWhitespace(outgoingWHRecord.from)) {
            tvOutgoingProcessFrom.setText(outgoingWHRecord.from);
        }

        if (!Strings.isEmptyOrWhitespace(outgoingWHRecord.to)) {
            tvOutgoingProcessTo.setText(outgoingWHRecord.to);
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
                adapterOutgoingItems.addItem(itemBarcode);
                adapterOutgoingItems.notifyDataSetChanged();
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
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_AssetTx_tx_update_failure), Toast.LENGTH_LONG));
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

    // GPS Location-Related functionality
    @Override
    public void onLocationChanged(@NonNull Location location) {
        recWHOutgoing.longitude = location.getLongitude();
        recWHOutgoing.latitude = location.getLatitude();
        locationManager.removeUpdates(this);
        toggleProgress(false, R.string.app_name);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(i);
    }

    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            runOnUiThread(() -> {
                syncProgressDialog.show();
            });
        } else {
            runOnUiThread(() -> {
                syncProgressDialog.hide();
            });
        }
    }
}