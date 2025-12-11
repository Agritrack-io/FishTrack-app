package io.agritrack.philosofish.fish.ui.wh.correlation;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.Locale;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.common.Constants;
import io.agritrack.philosofish.common.Filters;
import io.agritrack.philosofish.crypto.Crypto;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.CorrelationTxDTO;
import io.agritrack.philosofish.data.model.tx.CorrelationTransaction;
import io.agritrack.philosofish.data.model.wh.Asset;
import io.agritrack.philosofish.data.service.EncodingSchemeService;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.bo.GenericListModel;
import io.agritrack.philosofish.rfid.SingleShotScanner;
import io.agritrack.philosofish.rfid.X9KeyReceiver;
import io.agritrack.philosofish.ui.LocationAwareActivity;
import io.agritrack.philosofish.ui.adapter.FilterableAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZebraCorrelationNetActivity extends LocationAwareActivity implements FilterableAdapter.OnItemClickListener {

    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    // BX6100 handler
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    // Zebra handler
    //CorrelationNetHandler rfidHandler;
    private MobileDB db;
    private Button btnScanAssetTag, btnUncorr;
    private SearchView svSearchAsset;
    private RecyclerView rvNets;
    private TextView tvCorrNetBarcode;
    private FilterableAdapter adapterAssets;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg, confirmUncorrelateDialog, confirmRfidReplacement;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;
    private Boolean scanner_running = null;
    private String epcStr, label, netCode;
    private SwitchCompat correlatedFilter;
    private List<Asset> netsList, uncorrNetsList;

    private YesNoDialogFragment confirmNetRfidDlg;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_correlation_net);

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderNetCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();


        svSearchAsset.setIconifiedByDefault(false);

        confirmRfidReplacement = YesNoDialogFragment.instance(); //dialog in the case where a specific net is already corr
        //confirmRfidReplacement.setMessage(getText(R.string.proceed_with_replacement));
        confirmRfidReplacement.onConfirm(bundle -> {
            GlobalState.recWHCorrelation.rfid = epcStr;
            runOnUiThread(() -> tvCorrNetBarcode.setText(label));
        });
        confirmRfidReplacement.onReject(bundle -> {
            runOnUiThread(() -> tvCorrNetBarcode.setText(""));
        });


        confirmUncorrelateDialog = YesNoDialogFragment.instance();
        confirmUncorrelateDialog.setMessage(getText(R.string.proceed_with_uncorrelation));
        confirmUncorrelateDialog.onConfirm(bundle -> {
            //proceedWithoutLocation = true;
            uncorrelateNet();
        });
        confirmUncorrelateDialog.onReject(bundle -> {
//            mLastLocation = findLocation();
//            proceedWithoutLocation = false;
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

        confirmNetRfidDlg = YesNoDialogFragment.instance();
        confirmNetRfidDlg.onConfirm(bundle -> {
            showConfirmDialog();
        });
        confirmNetRfidDlg.onReject(bundle -> {
            runOnUiThread(() -> tvCorrNetBarcode.setText(""));
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(ZebraCorrelationNetActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        updateNetsList();

        this.rvNets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        loadNetsFromLocalDB(netsList);

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ZebraCorrelationNetActivity.this);
            supportDialog.showDialog();
        });

        btnUncorr.setOnClickListener(view -> {
            stopScanner();
            FragmentManager fm = getSupportFragmentManager();
            Asset toDelete = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
            if (toDelete != null) {
                String rfid = toDelete.rfid.length() > 15 ? toDelete.rfid.substring(14) : toDelete.rfid;
                confirmUncorrelateDialog.setMessage(String.format(getResources().getString(R.string.proceed_with_uncorrelation), toDelete.code, rfid));
                confirmUncorrelateDialog.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        correlatedFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the switch is checked (on)
                btnUncorr.setVisibility(View.GONE);
                if (isChecked) {
                    // Perform actions for when the switch is on
                    System.out.println("Switch is ON");
                    correlatedFilter.setText(getResources().getString(R.string.uncorrelated_nets));
                    loadNetsFromLocalDB(uncorrNetsList);
                    // For example: start a service, enable a feature, etc.
                } else {
                    // Perform actions for when the switch is off
                    correlatedFilter.setText(getResources().getString(R.string.all_nets));
                    System.out.println("Switch is OFF");
                    loadNetsFromLocalDB(netsList);
                    // For example: stop a service, disable a feature, etc.
                }
            }
        });

        configFooter();
    }

    private void updateNetsList() {
        netsList = db.assetDAO().getAssetsForType(Constants.ftNet.toUpperCase(Locale.ROOT));

        uncorrNetsList = netsList.stream().filter(x -> (x.rfid == null || x.rfid.isEmpty())).collect(Collectors.toList());
    }

    private void uncorrelateNet() {
        recWHCorrelation.type = Constants.ftNet;
        if (adapterAssets != null) {
            recWHCorrelation.code = adapterAssets.getSelectedValue();
        }
        recWHCorrelation.rfid = null;

        CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);


        String token = LocalPreferences.getToken();
        // sync WH Correlation Tx
        List<CorrelationTxDTO> dtos = new ArrayList<>();
        dtos.add(CorrelationTxDTO.convert(tx));
        Call<ResponseBody> syncTxAsyncCall = updService.syncAssetCorrelationTx(dtos, "Bearer " + token);
        syncTxAsyncCall.enqueue(new SyncTxCallBack());
        recWHCorrelation.prev_code = null;
//        } else {
//            FragmentManager fm = getSupportFragmentManager();
//            confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
//        }

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
                            GlobalState.recWHCorrelation.rfid = epcStr;
                            recWHCorrelation.prev_code = netCode;
                            runOnUiThread(() -> tvCorrNetBarcode.setText(label));
                            wantToCloseDialog = true;
                        } else {
                            dialog.setTitle(getString(R.string.invalid_password));
                            runOnUiThread(() -> tvCorrNetBarcode.setText(""));
                            wantToCloseDialog = false;
                        }
                        //Do stuff, possibly set wantToCloseDialog to true then...
                        if (wantToCloseDialog) {
                            dialog.dismiss();
                        } else {
                            runOnUiThread(() -> tvCorrNetBarcode.setText(""));
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //.onCreate(this);
            } else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    protected void onPause() {
        super.onPause();
        //rfidHandler.onPause();
    }

    @Override
    protected void onResume() {
        // RFID Handler
        //rfidHandler = new CorrelationNetHandler();

        //Scanner Initializations
        //Handling Runtime BT permissions for Android 12 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                // rfidHandler.onCreate(this);
            }

        } else {
            // rfidHandler.onCreate(this);
        }
        //rfidHandler.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //rfidHandler.onDestroy();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = correlate();

//            if (proceed) {
//                // move to next activity.
//                Intent i = new Intent(getApplicationContext(), ZebraCorrelationNetActivity.class);
//                startActivity(i);
//            }
        }
    }

    private void loadNetsFromLocalDB(List<Asset> assetsList) {
        // load assets for current Site and filter by asset type (if selected).
        //this.rvNets.setAdapter(null);

        //List<Asset> assetsList = db.assetDAO().getAssetsForType(assetType.toUpperCase(Locale.ROOT));
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.rfid, x.code, x.netEyeGirth, x.perimeter)).collect(Collectors.toList());
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets);
            adapterAssets.getFilter().filter(svSearchAsset.getQuery());
            adapterAssets.setOnItemClickListener(this);
            adapterAssets.notifyDataSetChanged();
            this.rvNets.setAdapter(adapterAssets);
        }
    }

    @SuppressLint("StringFormatMatches")
    protected void configFooter() {
        ivBack.setOnClickListener(view -> {
            stopScanner();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            //i.putExtra("uid", 1);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();
            recWHCorrelation.type = Constants.ftNet;
            if (adapterAssets != null) {
                recWHCorrelation.code = adapterAssets.getSelectedValue();
            }


            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
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
        btnUncorr = findViewById(R.id.btnUncorrelate);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvNets = findViewById(R.id.rvNets);
        tvCorrNetBarcode = findViewById(R.id.tvCorrNetBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);
        correlatedFilter = findViewById(R.id.correlatedFilter);

        rvNets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvNets.setItemAnimator(new DefaultItemAnimator());

        svSearchAsset.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapterAssets == null) {
                    return true;
                }
                btnUncorr.setVisibility(View.GONE);
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
            progressDialog.setMessage(render(R.string.syncing));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = GlobalState.commitWHCorrelation(db);


            // sync WH Correlation Tx
            List<CorrelationTxDTO> dtos = new ArrayList<>();
            dtos.add(CorrelationTxDTO.convert(tx));
            Call<ResponseBody> syncTxAsyncCall = updService.syncAssetCorrelationTx(dtos, "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());
            recWHCorrelation.prev_code = null;


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);

            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    @SuppressLint("StringFormatMatches")
    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.code)) {
                sb.append(String.format(getResources().getString(R.string.is_missingg, getString(R.string.net_code))));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.rfid)) {
                sb.append(String.format(getResources().getString(R.string.is_missingg, getString(R.string.net_rfid))));
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
        if (adapterAssets == null) {
            runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.empty_list_nets), Toast.LENGTH_LONG));
            return;
        }
        // Check if device model is BX6100, else is Zebra
        if (LocalPreferences.getDeviceModel() != null && LocalPreferences.getDeviceModel().equals("BX6100")) {
            tvCorrNetBarcode.setText("");
            scanner_runnable.setFilter(Filters.RFID_NET);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else if (LocalPreferences.getDeviceModel() != null && LocalPreferences.getDeviceModel().equals("BX6200")) {
            tvCorrNetBarcode.setText("");
            scanner_runnable.setFilter(Filters.RFID_NET);
            scanner_runnable.startReading();
            mScanHandler.postDelayed(scanner_runnable, 0);
        } else {
            if (adapterAssets != null && adapterAssets.getSelectedValue() == null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.select_asset), Toast.LENGTH_LONG));
                return;
            }

            //rfidHandler.inventoryByTimer();
            runOnUiThread(() -> tvCorrNetBarcode.setText(""));
        }
    }

//    @SuppressLint("StringFormatMatches")
//    @Override
//    public void handleTagsdata(TagData[] tagData) {
//        String[] acceptedCodes = schemeSvc.distinctNamesOnly();
//
//        List<TagData> tagList = Arrays.asList(tagData);
//
//        //Filter tags by accepted codes
//        List<TagData> acceptedTags = tagList.stream()
//                .filter(f -> ArrayUtils.contains(acceptedCodes, schemeSvc.nameOf(schemeSvc.nativeSchemeCode(f.getTagID())))
//                        && f.getTagID().substring(11).startsWith(Filters.RFID_NET)).collect(Collectors.toList());
//
//        Optional<TagData> tag = acceptedTags.stream().sorted((y, x) -> Integer.compare(x.getPeakRSSI(), y.getPeakRSSI())).findFirst();
//
//        if (tag.isPresent()) {
//            epcStr = tag.get().getTagID();
//            label = epcStr.length() > 15 ? epcStr.substring(14) : epcStr;
//            Asset net = db.assetDAO().getAssetByEpc(epcStr);
//            if (net == null) {
//                GlobalState.recWHCorrelation.rfid = epcStr;
//                runOnUiThread(() -> tvCorrNetBarcode.setText(label));
//            } else {
//                netCode = net.code;
//                runOnUiThread(() -> {
//                    FragmentManager fm = getSupportFragmentManager();
//                    confirmNetRfidDlg.setMessage(String.format(getResources().getString(R.string.rfid_already_assigned_to_other_asset), label, netCode));
//                    confirmNetRfidDlg.showNow(fm, getString(R.string.confirm_selection));
//                });
//            }
//        } else {
//            runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.no_tag_detected), Toast.LENGTH_LONG));
//            return;
//        }
//    }
//
//    @Override
//    public void handleTriggerPress(boolean pressed) {
//        btnScanAssetTag.callOnClick();
//    }

    @Override
    public void onItemClick(boolean isCorrelated) {
        if (isCorrelated) {
            btnUncorr.setVisibility(View.VISIBLE);
        } else {
            btnUncorr.setVisibility(View.GONE);
        }
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<ZebraCorrelationNetActivity> mActivity;

        public ScanHandler(ZebraCorrelationNetActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    epcStr = msg.getData().getString("epc");
                    try {
                        if (!Strings.isEmptyOrWhitespace(epcStr)) {
                            if (adapterAssets.getSelectedValue() != null) {
                                label = epcStr.length() > 15 ? epcStr.substring(14) : epcStr;
                                Asset net = db.assetDAO().getAssetByEpc(epcStr);
                                if (net == null) {
                                    net = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                                    if (net.rfid != null) {
                                        String code = net.code;
                                        String rfid = net.rfid.length() > 15 ? net.rfid.substring(14) : net.rfid;
                                        runOnUiThread(() -> {
                                            FragmentManager fm = getSupportFragmentManager();
                                            confirmRfidReplacement.setMessage(String.format(getResources().getString(R.string.net_already_assigned_to_other_rfid), code, rfid));
                                            confirmRfidReplacement.showNow(fm, getString(R.string.confirm_selection));
                                        });

                                    } else {
                                        GlobalState.recWHCorrelation.rfid = epcStr;
                                        runOnUiThread(() -> tvCorrNetBarcode.setText(label));
                                        break;
                                    }

                                } else {
                                    netCode = net.code;
                                    runOnUiThread(() -> {
                                        FragmentManager fm = getSupportFragmentManager();
                                        confirmNetRfidDlg.setMessage(String.format(getResources().getString(R.string.rfid_already_assigned_to_other_asset), label, netCode));
                                        confirmNetRfidDlg.showNow(fm, getString(R.string.confirm_selection));
                                    });
                                }
                            } else {
                                CToast(getApplicationContext(), render(R.string.select_asset), Toast.LENGTH_LONG);
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

    public class SyncTxCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (response.isSuccessful()) {
                deleteCorrelationTx();
                if (recWHCorrelation.rfid != null) {
                    //Check if in the local DB asset with same rfid exists and set rfid to null
                    Asset currNet = db.assetDAO().getAssetByEpc(GlobalState.recWHCorrelation.rfid);
                    if (currNet != null) {
                        currNet.rfid = null;
                        currNet.rfidBarcode = null;
                        currNet.barcode = null;
                        db.assetDAO().update(currNet);
                    }
                    Asset net = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                    net.rfid = GlobalState.recWHCorrelation.rfid;
                    db.assetDAO().update(net);
                } else {
                    Asset currNet = db.assetDAO().getByCode(adapterAssets.getSelectedValue());
                    if (currNet != null) {
                        currNet.rfid = null;
                        currNet.rfidBarcode = null;
                        currNet.barcode = null;
                        db.assetDAO().update(currNet);
                    }
                }
                if (btnUncorr.isClickable()) {
                    btnUncorr.setVisibility(View.GONE);
                }
                updateNetsList();
                if (correlatedFilter.isChecked()) {
                    loadNetsFromLocalDB(uncorrNetsList);
                } else {
                    loadNetsFromLocalDB(netsList);
                }
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
                tvCorrNetBarcode.setText("");
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
}