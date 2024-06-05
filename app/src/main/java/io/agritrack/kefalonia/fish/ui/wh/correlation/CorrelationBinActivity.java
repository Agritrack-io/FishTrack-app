package io.agritrack.kefalonia.fish.ui.wh.correlation;

import static io.agritrack.kefalonia.FishTrackApplication.IsDemo;
import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.tx.TransactionApi;
import io.agritrack.kefalonia.common.Constants;
import io.agritrack.kefalonia.common.Filters;
import io.agritrack.kefalonia.crypto.Crypto;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.tx.CorrelationTxDTO;
import io.agritrack.kefalonia.data.model.tx.CorrelationTransaction;
import io.agritrack.kefalonia.data.model.wh.Asset;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.fish.ui.bo.GenericListModel;
import io.agritrack.kefalonia.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.kefalonia.rfid.X9KeyReceiver;
import io.agritrack.kefalonia.ui.LocationAwareActivity;
import io.agritrack.kefalonia.ui.adapter.FilterableAdapter;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CorrelationBinActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private MobileDB db;
    private Button btnScanAssetTag;
    private TextView tvCorrBinBarcode, tvCorrTempLoggerBarcode;
    private FilterableAdapter adapterAssets;
    private SearchView svSearchAsset;
    private RecyclerView rvBins;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg, confirmBinRfidDlg, confirmRfidReplacement;;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;
    private String epcStr, label, binCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_bin);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderBinCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        svSearchAsset.setIconifiedByDefault(false);

        confirmRfidReplacement = YesNoDialogFragment.instance(); //dialog in the case where a specific net is already corr
        //confirmRfidReplacement.setMessage(getText(R.string.proceed_with_replacement));
        confirmRfidReplacement.onConfirm(bundle -> {
            GlobalState.recWHCorrelation.assetRFID = epcStr;
            runOnUiThread(() -> tvCorrBinBarcode.setText(label));
        });
        confirmRfidReplacement.onReject(bundle -> {
            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
        });


        confirmBinRfidDlg = YesNoDialogFragment.instance();
        confirmBinRfidDlg.onConfirm(bundle -> {
            showConfirmDialog();
        });
        confirmBinRfidDlg.onReject(bundle -> {
            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
        });

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
        progressDialog = new ProgressDialog(CorrelationBinActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        loadBinsFromLocalDB(Constants.ftBin);

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationBinActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    @SuppressLint("StringFormatMatches")
    private void showConfirmDialog() {
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input)
                .setTitle(getString(R.string.confirm_with_pin))
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        boolean wantToCloseDialog;
                        String pin = input.getText().toString();
                        String userPin = null;
                        try {
                            userPin = Crypto.decodeAndDecrypt(db.userDAO().getByUsername(LocalPreferences.getLoggedInUser(null)).pin);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }

                        if (pin.equalsIgnoreCase(userPin)) {
                            input.getShowSoftInputOnFocus();
                            recWHCorrelation.assetRFID = epcStr;
                            runOnUiThread(() -> tvCorrBinBarcode.setText(label));
                            wantToCloseDialog = true;
                        } else {
                            dialog.setTitle(getString(R.string.invalid_password));
                            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog) {
                            dialog.dismiss();
                        } else {
                            runOnUiThread(() -> tvCorrBinBarcode.setText(""));
                        }

                        input.setText("");

                        /*//Dismiss once everything is OK.
                        dialog.dismiss();*/
                    }
                });
            }
        });
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = correlate();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), CorrelationBinActivity.class);
                startActivity(i);
            }

            GlobalState.initWHCorrelationRecord();
            tvCorrBinBarcode.setText("");
            tvCorrTempLoggerBarcode.setText("");
        }
    }

    private void loadBinsFromLocalDB(String assetType) {
        // load assets for current Site and filter by asset type (if selected).
        this.rvBins.setAdapter(null);
        this.rvBins.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        List<Asset> assetsList = db.assetDAO().getAssetsForType(assetType.toUpperCase(Locale.ROOT));
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid, x.code, x.netEyeGirth, x.perimeter)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter("");
            adapterAssets.notifyDataSetChanged();
            this.rvBins.setAdapter(adapterAssets);
        }
    }

    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
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
        if (keyReceiver != null)
            unregisterReceiver(keyReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanner();
    }

    protected void configFooter() {
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            //i.putExtra("id", 2);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();
            GlobalState.recWHCorrelation.assetType = Constants.ftBin;
            GlobalState.recWHCorrelation.assetCode = adapterAssets.getSelectedValue();
            GlobalState.recWHCorrelation.type = Constants.ftDataLogger;

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(R.string.invalid_inputs + v), Toast.LENGTH_LONG);
                return;
            }
            if (mLastLocation != null) {
                recWHCorrelation.longitude = mLastLocation.getLongitude();
                recWHCorrelation.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });
    }

    private void assignCtrlVars() {
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvBins = findViewById(R.id.rvBins);
        tvCorrBinBarcode = findViewById(R.id.tvCorrBinBarcode);
        tvCorrTempLoggerBarcode = findViewById(R.id.tvCorrTempLoggerBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);

        rvBins.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvBins.setItemAnimator(new DefaultItemAnimator());

        svSearchAsset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAssets.getFilter().filter(newText);
                return false;
            }
        });
        svSearchAsset.setOnClickListener(view -> {
            int kk = 0;
        });
    }

    private boolean correlate() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);

            Asset oldBinRfid = db.assetDAO().getAssetByEpc(recWHCorrelation.assetRFID);
            if (oldBinRfid != null) {
                oldBinRfid.rfid = null;
                db.assetDAO().update(oldBinRfid);
            }
            Asset oldBinLogger = db.assetDAO().getAssetByLoggerEpc(recWHCorrelation.rfid);
            if (oldBinLogger != null) {
                oldBinLogger.rfid = null;
                db.assetDAO().update(oldBinLogger);
            }
            Asset bin = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
            bin.rfid = recWHCorrelation.assetRFID;
            bin.loggerEPC = recWHCorrelation.rfid;
            db.assetDAO().update(bin);

            // sync WH Correlation Tx
            ArrayList<CorrelationTxDTO> dtos = new ArrayList<>();
            dtos.add(CorrelationTxDTO.convert(tx));
            Call<ResponseBody> syncTxAsyncCall = updService.syncAssetWithAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new CorrelationBinActivity.SyncTxCallBack());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);

            return false;
        } finally {
            progressDialog.dismiss();
            /*List<CorrelationTransaction> corrTx = db.correlationTransactionDAO().getAll();
            CToast(this, "Size:" + corrTx.size(), Toast.LENGTH_LONG);*/
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetCode)) {
                sb.append(String.format("\n%s is missing", "'Bin code'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.assetRFID)) {
                sb.append(String.format("\n%s is missing", "'Bin RFID'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Logger RFID'"));
            }
        }
        return sb.toString();
    }

    private boolean deleteCorrelationTx() {
        try {
            System.out.println("About to delete correlate tx");
            CorrelationTransaction delObj = new CorrelationTransaction();
            delObj.id = recWHCorrelation.txKey;
            db.correlationTransactionDAO().delete(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    protected void onClick(View view) {
        scanner_runnable.setFilters(Filters.RFID_BIN, Filters.RFID_LOGGER);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    public class SyncTxCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (response.isSuccessful()) {
                deleteCorrelationTx();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
                tvCorrBinBarcode.setText("");
                tvCorrTempLoggerBarcode.setText("");
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
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

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<CorrelationBinActivity> mActivity;

        public ScanHandler(CorrelationBinActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> tags = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!CollectionUtils.isEmpty(tags)) {
                            for (CharSequence tag : tags) {
                                String epc = tag.toString();
                                epcStr = epc;
                                label = epc.length() > 15 ? epc.substring(14) : epc;

                                if (epc.indexOf(Filters.RFID_BIN) > -1) {
                                   // label = epc.length() > 15 ? epc.substring(14) : epc;

                                    if (adapterAssets.getSelectedValue() != null) {
                                        Asset bin = db.assetDAO().getAssetByEpc(epc);
                                        if (bin == null) {
                                            bin = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                                            if (bin.rfid != null) {
                                                String code = bin.code;
                                                FragmentManager fm = getSupportFragmentManager();
                                                confirmRfidReplacement.setMessage(String.format(getResources().getString(R.string.bin_already_assigned_to_other_rfid), code, label));
                                                confirmRfidReplacement.showNow(fm, getString(R.string.confirm_selection));
                                                return;

                                            }else {
                                                recWHCorrelation.assetRFID = epcStr;
                                                runOnUiThread(() -> tvCorrBinBarcode.setText(label));
                                                break;
                                            }

                                        } else {
                                            binCode = bin.code;
                                                FragmentManager fm = getSupportFragmentManager();
                                                confirmBinRfidDlg.setMessage(String.format(getResources().getString(R.string.rfid_already_assigned_to_other_bin), label, binCode));
                                                confirmBinRfidDlg.showNow(fm, getString(R.string.confirm_selection));
                                                return;
                                        }
                                    } else {
                                        CToast(getApplicationContext(), render(R.string.select_asset), Toast.LENGTH_LONG);
                                    }
                                } else if (epc.indexOf(Filters.RFID_LOGGER) > -1) {
                                    GlobalState.recWHCorrelation.rfid = epc;
                                    tvCorrTempLoggerBarcode.setText(label);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render(String.format("No item of type %s was found!", selectedAssetType)), Toast.LENGTH_LONG);
                    }
                    break;
            }
        }
    }
}