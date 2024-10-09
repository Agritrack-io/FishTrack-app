package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitPackageLabelCheckQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityPackage;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.barcode.BarcodeScanService;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.PackageQualityTxDTO;
import io.agritrack.philosofish.data.model.BinInfo;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackageQualityCheckLabelActivity extends AppCompatActivity {


    private MobileDB db;
    private ImageView ivSupport;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private ProgressDialog progressDialog;
    private EditText tvCurrentLot;
    private Spinner spFishLot;
    private Set<String> fishLotSet;
    private List<BinInfo> binInfos;
    private List<String> fishLots = new ArrayList<>();
    private ArrayAdapter<String> lotListAdapter;
    private SupportDialog supportDialog;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog,  confirmSaveDataDialog;
    private String currentLot, bestBefore;
    private EditText etComments, etBins;
    private Spinner spStart, spChange, spMid, spEnd;



    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiverLabel = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (!barcode.isEmpty() && barcode.length() >= 12) {
                    currentLot = barcode.substring(barcode.length() - 11);
                    //bestBefore = barcode.substring(barcode.length() - 21, barcode.length() - 15);
                    if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (currentLot.equals(recQualityPackage.lot)) {
                            CToast(getAppContext(), String.format(getResources().getString(R.string.lot_already_loaded), currentLot), Toast.LENGTH_LONG);
                            scanning = false;
                        } else {
                            FragmentManager fm = getSupportFragmentManager();
                            confirmNewLotDialog.setMessage(String.format(getResources().getString(R.string.box_on_another_lot), currentLot));
                            confirmNewLotDialog.showNow(fm, getString(R.string.confirm_selection));
                        }
                    }
                }
            }
        }
    };

    private void loadBarcodeInfo(String lot) {

        PackageQualityTransaction txQuality = db.packageQualityTransactionDAO().getByLot(lot);

        binInfos = db.binInfoDAO().getAll();

        fishLotSet = new TreeSet<>(binInfos.stream().filter(x -> x.lot != null).map(x -> x.lot).collect(Collectors.toList()));

        fishLots = new ArrayList<>();
        fishLots.addAll(fishLotSet);

        lotListAdapter = new ArrayAdapter<>(PackageQualityCheckLabelActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);
        if (txQuality == null) {
            tvCurrentLot.setText(lot);
            String labelBB = null;
//            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
//                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
//            }
//            recQualityPackage.bestBefore = labelBB;
            recQualityPackage.lot = currentLot;
            scanning = false;
        } else if (!txQuality.isLabelSynced) {

            if (!Strings.isEmptyOrWhitespace(txQuality.fishingLot))  {
                int position = lotListAdapter.getPosition(txQuality.fishingLot);
                if (position != -1) {
                    spFishLot.setSelection(position);
                    spFishLot.setClickable(false);
                } else {
                    fishLots.add(txQuality.fishingLot);
                    lotListAdapter.notifyDataSetChanged();
                    position = lotListAdapter.getPosition(txQuality.fishingLot);
                    spFishLot.setSelection(position);
                    spFishLot.setClickable(false);
                }
            }
            tvCurrentLot.setText(txQuality.lot);
            loadStatefromDB(txQuality);
            scanning = false;
        } else {
            CToast(getAppContext(),String.format(getResources().getString(R.string.lot_label_control_done), txQuality.lot), Toast.LENGTH_LONG );
            scanning = false;
        }
    }

    private void loadStatefromDB(PackageQualityTransaction txQuality) {

        recQualityPackage.lot = txQuality.lot;
        recQualityPackage.fishLot = txQuality.fishingLot;
        if (txQuality.bestBefore != null) {
            recQualityPackage.bestBefore = txQuality.bestBefore.toString();
        }
        recQualityPackage.startPacking = txQuality.startPacking;
        recQualityPackage.middlePacking = txQuality.middlePacking;
        recQualityPackage.changePacking = txQuality.changePacking;
        recQualityPackage.endPacking = txQuality.endPacking;
        recQualityPackage.labelComments = txQuality.labelComments;
        recQualityPackage.disinfectedBins = txQuality.disinfectedBins;

        initControlsFromState();
    }

    private BarcodeScanService scanService;
    private Button btnScanBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_package_label_check);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(PackageQualityCheckLabelActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderLabelCheck);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        // get  references of the controls
        assignCtrlVars();


        binInfos = db.binInfoDAO().getAll();

        fishLotSet = new TreeSet<>(binInfos.stream().filter(x -> x.lot != null).map(x -> x.lot).collect(Collectors.toList()));

        fishLots = new ArrayList<>();
        fishLots.addAll(fishLotSet);

        lotListAdapter = new ArrayAdapter<>(PackageQualityCheckLabelActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);

        spFishLot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recQualityPackage.fishLot = parent.getItemAtPosition(position).toString(); //this is your selected item
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        // load all sites with (Packaging role?) and fill in the spPackagingSite Spinner.
        List<String> packStatus = new ArrayList<>();
        packStatus.add("");
        packStatus.add("OK");
        packStatus.add("NOT OK");

        ArrayAdapter<String> hrAdapter = new ArrayAdapter(this, R.layout.simple_spinner_item_1, packStatus) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position % 2 == 0) { // we're on an even row
                    view.setBackgroundColor(getColor(R.color.white));
                } else {
                    view.setBackgroundColor(getColor(R.color.light_grey));
                }
                return view;
            }
        };

        hrAdapter.setDropDownViewResource(R.layout.simple_spinner_item_1);
        spStart.setAdapter(hrAdapter);
        spMid.setAdapter(hrAdapter);
        spChange.setAdapter(hrAdapter);
        spEnd.setAdapter(hrAdapter);



        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiverLabel, filter);


        confirmNewLotDialog= YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            PackageQualityTransaction tx = commitPackageLabelCheckQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot),Toast.LENGTH_LONG);
            }
            loadBarcodeInfo(currentLot);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });

        confirmSaveDataDialog= YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            PackageQualityTransaction tx = commitPackageLabelCheckQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot),Toast.LENGTH_LONG);
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverLabel);
            unregisterReceiver(receiverLabel);

            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverLabel);
            unregisterReceiver(receiverLabel);

            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });

        btnScanBox.setOnClickListener(view -> {
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackageQualityCheckLabelActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                boolean proceed;
                try {

                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(render("Synchronizing data..."));
                    progressDialog.show();

                    String token = LocalPreferences.getToken();

                    // persist Processing Record data to local DB.
                    PackageQualityTransaction tx = GlobalState.commitPackageLabelCheckQuality(db, Boolean.TRUE);

                    if (IsOnline) {
                        // sync Processing records
                        Call<PackageQualityTxDTO> syncTxAsyncCall = updService.syncPackQualityTx(PackageQualityTxDTO.convertLabel(tx), "Bearer " + token);
                        syncTxAsyncCall.enqueue(new PackageQualityCheckLabelActivity.SyncTxCallBack());
                    } else {
                        for (int i = 0; i < 3; i++) {
                            runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                        }
                    }

                    proceed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
                    proceed =  false;
                } finally {
                    progressDialog.dismiss();
                }
                if (proceed) {
                    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverLabel);
                    unregisterReceiver(receiverLabel);

                    Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToPackageQualityMenu);
        ivBack.setOnClickListener(view -> {
            scanning = false;
            stopScanning();
            if (!Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                FragmentManager fm = getSupportFragmentManager();
                confirmSaveDataDialog.setMessage(getString(R.string.save_lot_quality, recQualityPackage.lot));
                confirmSaveDataDialog.showNow(fm, getString(R.string.confirm_selection));
            } else {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverLabel);
                unregisterReceiver(receiverLabel);

                Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        btnScanBox = findViewById(R.id.btnScanBin);
        etBins = findViewById(R.id.etBins);

        tvCurrentLot = findViewById(R.id.lotNumber);
        spFishLot = findViewById(R.id.spFishLot);

        spStart = findViewById(R.id.spStartpack);
        spChange = findViewById(R.id.spChangePack);
        spMid = findViewById(R.id.spMidPack);
        spEnd = findViewById(R.id.spEndPack);
        etComments = findViewById(R.id.tvComments);

        ivSupport = findViewById(R.id.ivSupport);


    }

    private void initControlsFromState() {

        if (Strings.isEmptyOrWhitespace(recQualityPackage.labelComments)) {
            etComments.setText(recQualityPackage.labelComments);
        }


        if (!Strings.isEmptyOrWhitespace(recQualityPackage.fishLot)) {
            int position = lotListAdapter.getPosition(recQualityPackage.fishLot);
            if (position != -1) {
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            } else {
                fishLots.add(recQualityPackage.fishLot);
                lotListAdapter.notifyDataSetChanged();
                position = lotListAdapter.getPosition(recQualityPackage.fishLot);
                spFishLot.setSelection(position);
                spFishLot.setClickable(false);
            }
        }

        if (recQualityPackage.disinfectedBins != null) {
            etBins.setText(String.valueOf(recQualityPackage.disinfectedBins));
        }

        if (recQualityPackage.startPacking == null) {
            spStart.setSelection(0);
        } else if (recQualityPackage.startPacking) {
            spStart.setSelection(1);
        } else {
            spStart.setSelection(2);
        }

        if (recQualityPackage.changePacking == null) {
            spChange.setSelection(0);
        } else if (recQualityPackage.changePacking) {
            spChange.setSelection(1);
        } else {
            spChange.setSelection(2);
        }

        if (recQualityPackage.middlePacking == null) {
            spMid.setSelection(0);
        } else if (recQualityPackage.middlePacking) {
            spMid.setSelection(1);
        } else {
            spMid.setSelection(2);
        }

        if (recQualityPackage.endPacking == null) {
            spEnd.setSelection(0);
        } else if (recQualityPackage.endPacking) {
            spEnd.setSelection(1);
        } else {
            spEnd.setSelection(2);
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

    private void updateState() {

       // recQualityPackage.lot = tvCurrentLot.getText().toString();
        recQualityPackage.lot = Strings.isEmptyOrWhitespace(tvCurrentLot.getText().toString()) ? null : tvCurrentLot.getText().toString();
        recQualityPackage.fishLot = spFishLot.getSelectedItem() == null ? null : spFishLot.getSelectedItem().toString();
        recQualityPackage.labelComments = Strings.isEmptyOrWhitespace(etComments.getText().toString()) ? null : etComments.getText().toString();

        switch (spStart.getSelectedItemPosition()) {
            case 0:
                recQualityPackage.startPacking = null;
                break;
            case 1:
                recQualityPackage.startPacking = true;
                break;
            case 2:
                recQualityPackage.startPacking = false;
                break;
            default:
                recQualityPackage.startPacking = null;
        }

        switch (spChange.getSelectedItemPosition()) {
            case 0:
                recQualityPackage.changePacking = null;
                break;
            case 1:
                recQualityPackage.changePacking = true;
                break;
            case 2:
                recQualityPackage.changePacking = false;
                break;
            default:
                recQualityPackage.changePacking = null;
        }

        switch (spMid.getSelectedItemPosition()) {
            case 0:
                recQualityPackage.middlePacking = null;
                break;
            case 1:
                recQualityPackage.middlePacking = true;
                break;
            case 2:
                recQualityPackage.middlePacking = false;
                break;
            default:
                recQualityPackage.middlePacking = null;
        }

        switch (spEnd.getSelectedItemPosition()) {
            case 0:
                recQualityPackage.endPacking = null;
                break;
            case 1:
                recQualityPackage.endPacking = true;
                break;
            case 2:
                recQualityPackage.endPacking = false;
                break;
            default:
                recQualityPackage.endPacking = null;
        }

        if (etBins.getText() != null && !Strings.isEmptyOrWhitespace(etBins.getText().toString())) {
            recQualityPackage.disinfectedBins = Integer.valueOf(etBins.getText().toString());
        }

    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_number)));
            } else {
                if (recQualityPackage.startPacking == null) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.start_packing)));
                }
                if (recQualityPackage.changePacking == null) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.change_packing)));
                }
                if (recQualityPackage.middlePacking == null) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.middle_packing)));
                }
                if (recQualityPackage.endPacking == null) {
                    sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.end_packaging)));
                }
            }

            if (Strings.isEmptyOrWhitespace(recQualityPackage.fishLot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_fishing)));
            }
        }

        return sb.toString();
    }


    @Override
    protected void onResume() {
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverLabel);
        super.onStop();
    }


    private void qualityTxMarkLabelCheckSynced() {
        try {
            System.out.println("About to delete quality tx");
            PackageQualityTransaction delObj = db.packageQualityTransactionDAO().getByLot(recQualityPackage.lot);
            if (delObj != null) {
                delObj.isLabelSynced = true;
                db.packageQualityTransactionDAO().update(delObj);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public class SyncTxCallBack implements Callback<PackageQualityTxDTO> {
        @Override
        public void onResponse(@NonNull Call<PackageQualityTxDTO> call, Response<PackageQualityTxDTO> response) {
            if (response.isSuccessful() || IsDemo) {
                qualityTxMarkLabelCheckSynced();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_postquality_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<PackageQualityTxDTO> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
            } else if (error instanceof IOException) {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + error.getLocalizedMessage()), Toast.LENGTH_LONG));
                }
            }
        }
    }
}
