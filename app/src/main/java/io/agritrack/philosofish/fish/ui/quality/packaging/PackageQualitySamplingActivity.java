package io.agritrack.philosofish.fish.ui.quality.packaging;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.commitPackageSampleQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityPackage;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Time;
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
import io.agritrack.philosofish.data.model.common.SortingSample;
import io.agritrack.philosofish.data.model.common.TonneSample;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import io.agritrack.philosofish.dialog.DataListener;
import io.agritrack.philosofish.dialog.SetSortingSampleDialog;
import io.agritrack.philosofish.dialog.SetTonnageSampleDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.PackageQualityRecord;
import io.agritrack.philosofish.fish.state.PackageStepsState;
import io.agritrack.philosofish.sound.SoundUtil;
import io.agritrack.philosofish.ui.adapter.SortingSampleAdapter;
import io.agritrack.philosofish.ui.adapter.TonnageSampleAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PackageQualitySamplingActivity extends AppCompatActivity implements DataListener {

    private MobileDB db;
    private ImageView ivSupport;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private ProgressDialog progressDialog;
    private EditText tvCurrentLot;
    private SupportDialog supportDialog;
    private RecyclerView rvSortingSamples, rvTonnageSample;
    private boolean scanning = false;
    private YesNoDialogFragment confirmNewLotDialog, confirmSaveDataDialog;
    private String currentLot, bestBefore;
    private Spinner spFishLot;
    private Set<String> fishLotSet;
    private List<BinInfo> binInfos;
    private List<String> fishLots = new ArrayList<>();
    private ArrayAdapter<String> lotListAdapter;
    private ImageView ivAddTonnage, ivAddSorting, ivClearSorting, ivClearTonnage;
    private SortingSampleAdapter adapterSorting;
    private TonnageSampleAdapter adapterTonnage;
    private SetSortingSampleDialog setSortingDialog;
    private SetTonnageSampleDialog setTonnageSampleDialog;


    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiverSample = new BroadcastReceiver() {
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

        lotListAdapter = new ArrayAdapter<>(PackageQualitySamplingActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);

        if (txQuality == null) {
            tvCurrentLot.setText(lot);
            recQualityPackage.lot = currentLot;
            updateNextButtonState();
            scanning = false;
        } else if (!txQuality.isSampleSynced) {

            if (!Strings.isEmptyOrWhitespace(txQuality.fishingLot)) {
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
            CToast(getAppContext(), String.format(getResources().getString(R.string.lot_sample_control_done), txQuality.lot), Toast.LENGTH_LONG);
            scanning = false;
        }
    }

    private void loadStatefromDB(PackageQualityTransaction txQuality) {

        recQualityPackage.lot = txQuality.lot;
        updateNextButtonState();
        recQualityPackage.fishLot = txQuality.fishingLot;

        recQualityPackage.sortingSamples = txQuality.sortingSamples;
        recQualityPackage.tonneSamples = txQuality.tonneSamples;

        initControlsFromState();
    }

    private BarcodeScanService scanService;
    private Button btnScanBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_package_sampling);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(PackageQualitySamplingActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageSampling);
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

        lotListAdapter = new ArrayAdapter<>(PackageQualitySamplingActivity.this, R.layout.simple_spinner_item, fishLots);
        spFishLot.setAdapter(lotListAdapter);


        spFishLot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recQualityPackage.fishLot = parent.getItemAtPosition(position).toString(); //this is your selected item
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do
            }
        });

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        rvTonnageSample.setLayoutManager(layoutManager1);
        rvTonnageSample.setItemAnimator(new DefaultItemAnimator());
        rvTonnageSample.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterTonnage = new TonnageSampleAdapter(this, new ArrayList<>()); //, itemsClickListener
        adapterTonnage.addUniqueItem(new TonnageSampleAdapter.TonnageDetails());
        adapterTonnage.notifyDataSetChanged();
        rvTonnageSample.setAdapter(adapterTonnage);
        rvTonnageSample.setNestedScrollingEnabled(false);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);

        rvSortingSamples.setLayoutManager(layoutManager2);
        rvSortingSamples.setItemAnimator(new DefaultItemAnimator());
        rvSortingSamples.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapterSorting = new SortingSampleAdapter(this, new ArrayList<>()); //, itemsClickListener
        adapterSorting.addUniqueItem(new SortingSampleAdapter.SortingDetails());
        adapterTonnage.notifyDataSetChanged();
        rvSortingSamples.setAdapter(adapterSorting);
        rvSortingSamples.setNestedScrollingEnabled(false);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiverSample, filter);

        setSortingDialog = new SetSortingSampleDialog(this);
        setSortingDialog.setMyDialogListener(this);

        setTonnageSampleDialog = new SetTonnageSampleDialog(this);
        setTonnageSampleDialog.setMyDialogListener(this);

        confirmNewLotDialog = YesNoDialogFragment.instance();
        confirmNewLotDialog.onConfirm(bundle -> {
            PackageQualityTransaction tx = commitPackageSampleQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot), Toast.LENGTH_LONG);
            }
            loadBarcodeInfo(currentLot);
        });
        confirmNewLotDialog.onReject(bundle -> {

        });

        confirmSaveDataDialog = YesNoDialogFragment.instance();
        confirmSaveDataDialog.onConfirm(bundle -> {
            updateState();
            PackageQualityTransaction tx = commitPackageSampleQuality(db, false);
            if (tx == null) {
                CToast(getAppContext(), String.format(getResources().getString(R.string.save_quality_failed), recQualityPackage.lot), Toast.LENGTH_LONG);
            }
            PackageStepsState.completed[1] = true;
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSample);
            unregisterReceiver(receiverSample);

            Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
            startActivity(i);
        });
        confirmSaveDataDialog.onReject(bundle -> {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSample);
            unregisterReceiver(receiverSample);

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
            supportDialog = new SupportDialog(PackageQualitySamplingActivity.this);
            supportDialog.showDialog();
        });

        ivAddSorting.setOnClickListener(view -> {
            setSortingDialog.showDialog();
        });

        ivAddTonnage.setOnClickListener(view -> {
            setTonnageSampleDialog.showDialog();
        });

        ivClearSorting.setOnClickListener(view -> {
            List<SortingSampleAdapter.SortingDetails> selectedSamples = adapterSorting.getValues().stream().filter(x -> x.isSelected()).collect(Collectors.toList());
            if (!selectedSamples.isEmpty()) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_selected_samples));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    for (SortingSampleAdapter.SortingDetails sample : selectedSamples) {
                        adapterSorting.removeItem(sample.timestamp);
                    }

                    adapterSorting.notifyDataSetChanged();

                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterSorting.notifyDataSetChanged();
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(getString(R.string.delete_item)), Toast.LENGTH_LONG);
            }
        });

        ivClearTonnage.setOnClickListener(view -> {
            List<TonnageSampleAdapter.TonnageDetails> selectedSamples = adapterTonnage.getValues().stream().filter(x -> x.isSelected()).collect(Collectors.toList());
            if (!selectedSamples.isEmpty()) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_all_selected_samples));

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    for (TonnageSampleAdapter.TonnageDetails sample : selectedSamples) {
                        adapterTonnage.removeItem(sample.timestamp);
                    }

                    adapterTonnage.notifyDataSetChanged();

                });

                confirmSiteSelectionDlg.onReject(bundle -> {
                    adapterTonnage.notifyDataSetChanged();
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                CToast(getApplicationContext(), render(getString(R.string.delete_item)), Toast.LENGTH_LONG);
            }
        });

        configFooter();
    }

    private void updateNextButtonState() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        boolean enabled = !Strings.isEmptyOrWhitespace(recQualityPackage.lot);
        ivNext.setEnabled(enabled);
        ivNext.setAlpha(enabled ? 1f : 0.3f);
    }


    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);

        ivNext.setEnabled(false);
        ivNext.setAlpha(0.3f);


        ivNext.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
            } else {
                Boolean proceed;
                try {

                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(render("Synchronizing data..."));
                    progressDialog.show();

                    String token = LocalPreferences.getToken();

                    // persist Processing Record data to local DB.
                    PackageQualityTransaction tx = GlobalState.commitPackageSampleQuality(db, Boolean.TRUE);

                    if (IsOnline) {
                        // sync Processing records
                        Call<PackageQualityTxDTO> syncTxAsyncCall = updService.syncPackQualityTx(PackageQualityTxDTO.convertSample(tx), "Bearer " + token);
                        syncTxAsyncCall.enqueue(new PackageQualitySamplingActivity.SyncTxCallBack());
                    } else {
                        for (int i = 0; i < 3; i++) {
                            runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                        }
                    }

                    proceed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
                    proceed = false;
                } finally {
                    progressDialog.dismiss();
                }
                if (proceed) {
                    PackageStepsState.completed[1] = true;
                    PackageStepsState.updateParentQualityStep();   // <-- REQUIRED FOR GREEN BUTTON

                    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSample);
                    unregisterReceiver(receiverSample);

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
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSample);
                unregisterReceiver(receiverSample);

                Intent i = new Intent(getApplicationContext(), PackageQualityMenuActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        btnScanBox = findViewById(R.id.btnScanBin);

        tvCurrentLot = findViewById(R.id.lotNumber);

        spFishLot = findViewById(R.id.spFishLot);

        ivAddSorting = findViewById(R.id.ibAddLaundry);

        ivAddTonnage = findViewById(R.id.ibAddTonnage);

        ivClearSorting = findViewById(R.id.ivDeleteLaundry);
        ivClearTonnage = findViewById(R.id.ivDeleteTonnage);

        rvSortingSamples = findViewById(R.id.lvSortingTemps);
        rvTonnageSample = findViewById(R.id.lvTempsPer5);

        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        PackageQualityRecord packQualityRecord = recQualityPackage;


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

        if (packQualityRecord.sortingSamples != null && !packQualityRecord.sortingSamples.isEmpty()) {
            adapterSorting.setValues(packQualityRecord.sortingSamples.stream()
                    .map(x -> new SortingSampleAdapter.SortingDetails(x.getSampleTime(), x.getWaterTemp(), x.getFishTemp()))
                    .collect(Collectors.toList()));
            adapterSorting.notifyDataSetChanged();
        }

        if (packQualityRecord.tonneSamples != null && !packQualityRecord.tonneSamples.isEmpty()) {
            adapterTonnage.setValues(packQualityRecord.tonneSamples.stream()
                    .map(x -> new TonnageSampleAdapter.TonnageDetails(x.getSampleTime(), x.getFishTemp(), x.getCorrAction()))
                    .collect(Collectors.toList()));
            adapterTonnage.notifyDataSetChanged();
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

        recQualityPackage.lot = tvCurrentLot.getText() == null ? null : tvCurrentLot.getText().toString();
        recQualityPackage.fishLot = spFishLot.getSelectedItem() == null ? null : spFishLot.getSelectedItem().toString();
        recQualityPackage.sortingSamples = adapterSorting.getValues().stream()
                .map(x -> new SortingSample(x.timestamp, x.fishTemp, x.waterTemp))
                .collect(Collectors.toList());

        recQualityPackage.sortingSamples.remove(0);


        recQualityPackage.tonneSamples = adapterTonnage.getValues().stream()
                .map(x -> new TonneSample(x.timestamp, x.fishTemp, x.corrAction))
                .collect(Collectors.toList());

        recQualityPackage.tonneSamples.remove(0);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recQualityPackage.lot)) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.lot_number)));
            } else {
                if (recQualityPackage.sortingSamples == null || recQualityPackage.sortingSamples.isEmpty()) {
                    sb.append(String.format("\n%s is missing", "'Θερμοκρασίες Πλυντηρίου Διαλογής'"));
                }
                if (recQualityPackage.tonneSamples == null || recQualityPackage.tonneSamples.isEmpty()) {
                    sb.append(String.format("\n%s is missing", "'Θερμοκρασίες Ανά 5 Τόνους'"));
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSample);
        super.onStop();
    }

    @Override
    public void onDataPassed(Double fishT, Double waterT, String corrAction) {
        if (waterT != null) {
            adapterSorting.addUniqueItem(new SortingSampleAdapter.SortingDetails(new Time(System.currentTimeMillis()), waterT, fishT));
            adapterSorting.notifyDataSetChanged();
        } else {
            adapterTonnage.addUniqueItem(new TonnageSampleAdapter.TonnageDetails(new Time(System.currentTimeMillis()), fishT, corrAction));
            adapterTonnage.notifyDataSetChanged();
        }
    }

    private void qualityTxMarkSampleSynced() {
        try {
            System.out.println("About to delete quality tx");
            PackageQualityTransaction delObj = db.packageQualityTransactionDAO().getByLot(recQualityPackage.lot);
            if (delObj != null) {
                delObj.isSampleSynced = true;
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
                qualityTxMarkSampleSynced();
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
