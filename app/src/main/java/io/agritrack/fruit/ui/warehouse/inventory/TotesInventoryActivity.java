package io.agritrack.fruit.ui.warehouse.inventory;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recInventory;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.TotesInventoryDTO;
import io.agritrack.data.model.tx.items.TotesInventoryTxWithItems;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.enums.AssetType;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.state.HarvestRecord;
import io.agritrack.fruit.state.InventoryRecord;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TotesInventoryActivity extends LocationAwareActivity {

    private Button scanButton;
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;

    private MobileDB db;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);

    private ConstraintLayout selectedItem;
    private String selectedBarcode;

    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

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

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(TotesInventoryActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryTotes.setLayoutManager(layoutManager);
        rvInventoryTotes.setItemAnimator(new DefaultItemAnimator());
        adapterTotes = new TemplateRecyclerAdapter(this, new ArrayList<>(), itemsClickListener);
        rvInventoryTotes.setAdapter(adapterTotes);
        rvInventoryTotes.setNestedScrollingEnabled(false);

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
        ivDeleteTote = findViewById(R.id.ivDeleteTote);
        ivAddTote = findViewById(R.id.ivAddTote);
        scanButton = findViewById(R.id.btnScanTotes);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanner_runnable!=null) {
                    //Stop scanning since we navigate to next activity
                    scanner_runnable.stopReading();
                }

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
            if (scanner_runnable!=null) {
                //Stop scanning since we navigate to previous activity
                scanner_runnable.stopReading();
            }

            Intent i = new Intent(getApplicationContext(), FruitInventoryStartActivity.class);
            startActivity(i);
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
            TotesInventoryTxWithItems invtx = FruitGlobalState.commitWHRFIDInventory(db);

            // sync WH Inventory Tx
            Call<TotesInventoryDTO> syncInvTxCallBack = updService.syncTotesInventoryTx(TotesInventoryDTO.convert(invtx), "Bearer " + token);
            syncInvTxCallBack.enqueue(new TotesInventoryActivity.SyncInvTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    public class SyncInvTxCallBack implements Callback<TotesInventoryDTO> {
        @Override
        public void onResponse(Call<TotesInventoryDTO> call, Response<TotesInventoryDTO> response) {
            TotesInventoryDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render("Inventory update failure!!!"), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<TotesInventoryDTO> call, Throwable error) {
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
    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_TOTE);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(Filters.RFID_TOTE);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            scanButton.setText(R.string.scan_totes);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<TotesInventoryActivity> mActivity;

        public ScanHandler(TotesInventoryActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    //clearSelectedItem();
                    if (epcList != null && !epcList.isEmpty()) {
                        epcList.stream().forEach(x->adapterTotes.addUniqueItem(x.toString()));
                        tvTotesCount.setText(String.valueOf(adapterTotes.getItemCount()));
                        adapterTotes.notifyDataSetChanged();
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