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
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
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

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.barcode.BarcodeScanService;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.PackageQualityTxDTO;
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
    private EditText tvCurrentLot, tvBestBefore;
    private SupportDialog supportDialog;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog,  confirmSaveDataDialog;
    private String currentLot, bestBefore;
    private EditText etComments;
    private Spinner spStart, spChange, spMid, spEnd;



    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                if (!barcode.isEmpty() && barcode.length() >= 10) {
                    currentLot = barcode.substring(barcode.length() - 11);
                    bestBefore = barcode.substring(barcode.length() - 21, barcode.length() - 15);
                    if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                        loadBarcodeInfo(currentLot);
                    } else {
                        if (barcode.equals(recQualityPackage.lot)) {
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

        if (txQuality == null) {
            tvCurrentLot.setText(lot);
            String labelBB = null;
            if (Strings.isEmptyOrWhitespace(bestBefore) && bestBefore.length() == 6) {
                labelBB = bestBefore.substring(4) + "/" + bestBefore.substring(2,4) + "/20" + bestBefore.substring(0,2);
            }
            tvBestBefore.setText(labelBB);
            recQualityPackage.bestBefore = labelBB;
            recQualityPackage.lot = currentLot;
            scanning = false;
        } else if (!txQuality.isLabelSynced) {
            tvCurrentLot.setText(txQuality.lot);
            tvBestBefore.setText(txQuality.bestBefore.toString());
            loadStatefromDB(txQuality);
            scanning = false;
        } else {
            CToast(getAppContext(),String.format(getResources().getString(R.string.lot_label_control_done), txQuality.lot), Toast.LENGTH_LONG );
            scanning = false;
        }
    }

    private void loadStatefromDB(PackageQualityTransaction txQuality) {

        recQualityPackage.lot = txQuality.lot;
        if (txQuality.bestBefore != null) {
            recQualityPackage.bestBefore = txQuality.bestBefore.toString();
        }
        recQualityPackage.startPacking = txQuality.startPacking;
        recQualityPackage.middlePacking = txQuality.middlePacking;
        recQualityPackage.changePacking = txQuality.changePacking;
        recQualityPackage.endPacking = txQuality.endPacking;
        recQualityPackage.labelComments = txQuality.labelComments;

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
            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
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
                Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        btnScanBox = findViewById(R.id.btnScanBin);

        tvCurrentLot = findViewById(R.id.lotNumber);
        tvBestBefore = findViewById(R.id.tvBB);

        spStart = findViewById(R.id.spStartpack);
        spChange = findViewById(R.id.spChangePack);
        spMid = findViewById(R.id.spMidPack);
        spEnd = findViewById(R.id.spEndPack);
        etComments = findViewById(R.id.tvComments);

        ivSupport = findViewById(R.id.ivSupport);

        tvBestBefore.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

    }

    private void initControlsFromState() {

        if (Strings.isEmptyOrWhitespace(recQualityPackage.labelComments)) {
            etComments.setText(recQualityPackage.labelComments);
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
        recQualityPackage.bestBefore = Strings.isEmptyOrWhitespace(tvBestBefore.getText().toString()) ? null : tvBestBefore.getText().toString();
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
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
