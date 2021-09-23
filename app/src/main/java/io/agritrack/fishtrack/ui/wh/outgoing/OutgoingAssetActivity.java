package io.agritrack.fishtrack.ui.wh.outgoing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.AssetTxDTO;
import io.agritrack.fishtrack.data.model.tx.AssetTransaction;
import io.agritrack.fishtrack.dialog.YesNoDialogFragment;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.WarehouseTxState;
import io.agritrack.fishtrack.rfid.ScanInventoryThread;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.WHTxRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TreelikeAdapter;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class OutgoingAssetActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private ToggleGroup tgChooseAssetType;
    private String selectedAssetType = AssetType.ALL.name();
    private String activeFilter = null;
    private  int selectedToggleButton = -1;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<Set<String>> scanResult = new MutableLiveData<>();
    private MobileDB db;
    private UhfReader uhfReader;
    private ScanInventoryThread processingBinsThread = new ScanInventoryThread();
    private boolean scanning = false;

    private TreelikeAdapter adapterOutgoingItems;

    private TextView tvOutgoingProcessFrom, tvOutgoingProcessTo;
    private ExpandableListView xvOutgoingAssets;

    private ImageButton ivAddItem, ivDeleteItem;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;
    private Integer selectedParent, selectedChild;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_asset);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingProcess);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(OutgoingAssetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        xvOutgoingAssets.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                clearSelectedItem();

                selectedParent = null;
                selectedChild = null;
                return false;
            }
        });

        xvOutgoingAssets.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
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

        //Get reference of binsCount textView
        scanResult.observe(this, response -> {
            if (response == null) {
                return;
            }
            Map<String, List<String>> values = response.stream().collect(Collectors.groupingBy(g -> g.substring(0, 4), Collectors.toCollection(ArrayList::new)));;//(SiteInfo::getLevel2, Collectors.toCollection(ArrayList::new)));

            if (adapterOutgoingItems == null) {
            adapterOutgoingItems = new TreelikeAdapter(this, values);
            xvOutgoingAssets.setAdapter(adapterOutgoingItems);
            } else {
                adapterOutgoingItems.appendItems(values);
            }
            adapterOutgoingItems.notifyDataSetChanged();
        });

        // initialize scanning threads
        prepareScanAvailableBinsButton();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

            if (selectedParent!=null && selectedChild!=null) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterOutgoingItems.removeItem(selectedParent, selectedChild);
                        adapterOutgoingItems.notifyDataSetChanged();
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

    /*    ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        configFooter();
    }

    private void clearSelectedItem() {
        if (selectedItem != null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            processingBinsThread.setScanInProgress(scanning);

            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToStartOutgoing);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            processingBinsThread.setScanInProgress(scanning);

            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgChooseAssetType = findViewById(R.id.tgChooseAssetType);
        xvOutgoingAssets = findViewById(R.id.xvOutgoingItems);
        tvOutgoingProcessFrom = findViewById(R.id.tvOutgoingProcessFrom);
        tvOutgoingProcessTo = findViewById(R.id.tvOutgoingProcessTo);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);

        tgChooseAssetType.setOnCheckedChangeListener(this);
    }

    private void updateState() {
        GlobalState.recWHOutgoing.items = adapterOutgoingItems.getValues();
        GlobalState.recWHOutgoing.state = WarehouseTxState.Outgoing;
        GlobalState.recWHOutgoing.assetType = AssetType.valueOf(this.selectedAssetType);
        GlobalState.recWHOutgoing.site = LocalPreferences.getCurrentSiteName();

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
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
        } finally {
            progressDialog.dismiss();
        }

    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

        if (GlobalState.recWHOutgoing.items == null || GlobalState.recWHOutgoing.items.isEmpty()) {
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

        if (outgoingWHRecord.items != null) {
            adapterOutgoingItems.setValues(outgoingWHRecord.items);
            adapterOutgoingItems.notifyDataSetChanged();
        }
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
            if (processingBinsThread.getState() == Thread.State.TERMINATED) {
                processingBinsThread = new ScanInventoryThread();
            }
            //update scanning, uhfReader, tvPlatformName values in thread
            processingBinsThread.setScanInProgress(scanning);
            processingBinsThread.setUhfReader(uhfReader);
            processingBinsThread.setScanResult(scanResult);
            processingBinsThread.setFilter(activeFilter);

            if (scanning) {
                scanButton.setText(R.string.stop_scan);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
                    }
                });
                if (processingBinsThread.getState() == Thread.State.NEW) {
                    processingBinsThread.start();
                }
            } else {
                scanButton.setText(R.string.scan_assets);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
                    }
                });
                try {
                    processingBinsThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if( selectedToggleButton == checkedId){
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch(checkedId){
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

    public class SyncTxCallBack implements Callback<AssetTxDTO> {
        @Override
        public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
            AssetTxDTO rs = response.body();

            if (rs != null) {
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

}