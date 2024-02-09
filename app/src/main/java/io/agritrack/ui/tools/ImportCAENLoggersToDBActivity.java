package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.IotLoggerDTO;
import io.agritrack.data.model.common.IotLogger;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.api.tx.TransactionApi;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.rfid.ScanInventoryThread;
import io.agritrack.sound.SoundUtil;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportCAENLoggersToDBActivity extends AppCompatActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;
    // Local handler that receives the RFID scanner results.
    private ScanHandler mScanHandler;
    private ScanInventoryThread scanner_runnable;
    private MobileDB db;
    private RecyclerView rvInventoryItems;
    private TemplateRecyclerAdapter adapterInventoryItems;
    private Button scanButton;
    private EditText etVendor, etType, etModel;
    private String loggerBarcode;
    private ImageButton ivAddItem, ivDeleteItem;
    private TextView tvItemsCnt;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_caenloggers_to_dbactivity);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderImportDataLoggers);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // instantiate Local Handler that will process the scanning stream.
        mScanHandler = new ScanHandler(this);

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryItems.setLayoutManager(layoutManager);
        rvInventoryItems.setItemAnimator(new DefaultItemAnimator());
        rvInventoryItems.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterInventoryItems = new TemplateRecyclerAdapter(this, new ArrayList<>(),true);
        rvInventoryItems.setAdapter(adapterInventoryItems);
        rvInventoryItems.setNestedScrollingEnabled(false);

        // link trigger/scan button to ClickListener
        scanButton.setOnClickListener(this::onClick);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(ImportCAENLoggersToDBActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ivDeleteItem.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(adapterInventoryItems.getSelectedValue())) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", adapterInventoryItems.getSelectedValue());
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + adapterInventoryItems.getSelectedLabel());

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterInventoryItems.removeItem(barcode);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemCount()));
                        adapterInventoryItems.clearSelectedValue();
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else if (adapterInventoryItems.getItemCount()>0) {
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                //confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_items));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    adapterInventoryItems.removeAll();
                    adapterInventoryItems.notifyDataSetChanged();
                    tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemCount()));
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

//        ivAddItem.setOnClickListener(view -> {
//            showAddDialog();
//        });

        // create Footer
        configFooter();
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
        stopScanner();
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //unregister the receiver
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);

        super.onDestroy();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            //Stop scanning since we navigate to next activity
            if (scanner_runnable != null) {
                scanner_runnable.stopReading();
            }
            if (adapterInventoryItems != null) {
                GlobalState.assetData.loggers = adapterInventoryItems.getValues();
            }
            GlobalState.assetData.type = etType.getText().toString();
            GlobalState.assetData.model = etModel.getText().toString();
            GlobalState.assetData.vendor = etVendor.getText().toString();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            updateState();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);

        });

        ImageView ivBack = findViewById(R.id.ivBackToLogin);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        rvInventoryItems = findViewById(R.id.rvInventoryItems);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        etModel = findViewById(R.id.etModel);
        etVendor = findViewById(R.id.etVendor);
        etType = findViewById(R.id.etType);
        scanButton = findViewById(R.id.btnScanAsset);
    }

    protected void onClick(View view) {
        if (scanner_runnable == null) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable = new ScanInventoryThread(mScanHandler);
            scanner_runnable.setFilter(Filters.RFID_LOGGER);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else if (!scanner_runnable.isReading()) {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_button, null));
            scanner_runnable.setFilter(Filters.RFID_LOGGER);
            scanner_runnable.startReading();
            scanButton.setText(R.string.stop_scan);
        } else {
            scanButton.setBackground(getResources().getDrawable(R.drawable.bg_rounded_btn_login, null));
            scanner_runnable.stopReading();
            scanButton.setText(R.string.scan_totes);
        }
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loggerBarcode = input.getText().toString();
                adapterInventoryItems.addUniqueItem(loggerBarcode);
                tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemCount()));
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
    }

    private boolean updateState() {
        try {
            String token = LocalPreferences.getToken();

            // persist loggers to local DB.
            List<IotLogger> tx = GlobalState.commitIotLoggers(db);

            // sync loggers
            Call<List<IotLoggerDTO>> syncTxAsyncCall = updService.syncIotLoggers(IotLoggerDTO.convertListDTO(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new ImportCAENLoggersToDBActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (GlobalState.assetData.loggers == null || GlobalState.assetData.loggers.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Loggers'"));
            }
        }
        return sb.toString();
    }

    // ###################################################
    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(null);
            //mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<ImportCAENLoggersToDBActivity> mActivity;

        public ScanHandler(ImportCAENLoggersToDBActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<CharSequence> epcList = msg.getData().getCharSequenceArrayList("epc");
                    //clearSelectedItem();
                    if (epcList != null && !epcList.isEmpty()) {
                        epcList.stream().forEach(x -> adapterInventoryItems.addUniqueItem(x.toString()));
                        tvItemsCnt.setText(String.valueOf(adapterInventoryItems.getItemCount()));
                        adapterInventoryItems.notifyDataSetChanged();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("Scanning is over!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }

    public class SyncTxCallBack implements Callback<List<IotLoggerDTO>> {
        @Override
        public void onResponse(Call<List<IotLoggerDTO>> call, Response<List<IotLoggerDTO>> response) {
            List<IotLoggerDTO> rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_SHORT));
                db.iotLoggerDAO().deleteAll();
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_import_daqta_loggers), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<IotLoggerDTO>> call, Throwable error) {
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