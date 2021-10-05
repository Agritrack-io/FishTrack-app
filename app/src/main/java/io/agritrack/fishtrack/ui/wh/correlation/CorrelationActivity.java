package io.agritrack.fishtrack.ui.wh.correlation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.fishtrack.data.model.tx.CorrelationTransaction;
import io.agritrack.fishtrack.data.model.wh.Asset;
import io.agritrack.fishtrack.dialog.TimeOutProgressDlg;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.rfid.SingleShotScanner;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.FilterableAdapter;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.custom.CustomToast;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.login.api.TransactionApi;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.state.GlobalState.recFishing;
import static io.agritrack.fishtrack.state.GlobalState.recWHCorrelation;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class CorrelationActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener, LocationListener {
    private final int REQUEST_FINE_LOCATION = 1234;

    private LocationManager locationManager;
    private TimeOutProgressDlg syncProgressDialog;
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final SingleShotScanner scanner = new SingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ToggleGroup tgSearchAssetType;
    private SearchView svSearchAsset;
    private RecyclerView rvAssets;
    private Button btnScanAssetTag, btnCorrelate;
    private TextView tvCorrAssetBarcode;
    private MobileDB db;
    private FilterableAdapter adapterAssets;
    private String selectedAssetType;
    private String selectedBarcode = "";
    private String activeFilter = null;
    private ConstraintLayout selectedItem;
    private ProgressDialog progressDialog;
    // Instantiate a clickListener to be passed to adapterAssets.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if (selectedItem != null) {
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
        setContentView(R.layout.activity_correlation);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // get references to Location Manager Instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request permission to use GPS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // =================================
        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilter(activeFilter);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(1000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tvCorrAssetBarcode.setText(epcStr);
                        }
                    });
                    //tvCageName.setText(result);
                } else {
                    CToast(getApplicationContext(), render(String.format("No item of type %s was found!", selectedAssetType)), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        btnCorrelate.setOnClickListener(view -> {
            GlobalState.recWHCorrelation.assetType = !Strings.isEmptyOrWhitespace(selectedAssetType) ? AssetType.valueOf(selectedAssetType) : null;
            GlobalState.recWHCorrelation.barcode = !Strings.isEmptyOrWhitespace(selectedBarcode) ? selectedBarcode : null; //tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;
            GlobalState.recWHCorrelation.rfid = tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            correlate();
        });

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        syncProgressDialog = new TimeOutProgressDlg(10000l, 500l, this) {
            @Override
            protected void doTasks() {
                locationManager.removeUpdates(CorrelationActivity.this);

                // Update state and proceed to next
                Boolean proceed = correlate();
                toggleProgress(false, R.string.app_name);

                if (proceed) {
                    Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(i);
                }
            }
        };
        syncProgressDialog.setMessage(R.string.acquire_coordinates);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // check if permission has been granted
                if (ActivityCompat.checkSelfPermission(CorrelationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CorrelationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, CorrelationActivity.this);
                // show Progress Dialog
                toggleProgress(true, R.string.acquire_coordinates);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWareHouseMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgSearchAssetType = findViewById(R.id.tgSearchAssetType);
        tvCorrAssetBarcode = findViewById(R.id.tvCorrAssetBarcode);
        svSearchAsset = findViewById(R.id.svSearchAsset);
        rvAssets = findViewById(R.id.rvAssets);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        btnCorrelate = findViewById(R.id.btnCorrelate);

        tgSearchAssetType.setOnCheckedChangeListener(this);
        rvAssets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvAssets.setItemAnimator(new DefaultItemAnimator());

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

            // sync WH Correlation Tx
            Call<CorrelationTxDTO> syncTxAsyncCall = updService.syncCorrelationTx(CorrelationTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new SyncTxCallBack());

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

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.barcode)) {
            sb.append(String.format("\n%s is missing", "'Asset BARCODE'"));
        }

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
            sb.append(String.format("\n%s is missing", "'Asset RFID'"));
        }

        return sb.toString();
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbCage) {
            selectedAssetType = Constants.ftCage;
            activeFilter = Filters.RFID_CAGE;
        } else if (checkedId == R.id.tbNet) {
            selectedAssetType = Constants.ftNet;
            activeFilter = Filters.RFID_NET;
        } else if (checkedId == R.id.tbBin) {
            selectedAssetType = Constants.ftBin;
            activeFilter = Filters.RFID_BIN;
        } else if (checkedId == R.id.tbPlatform) {
            selectedAssetType = Constants.ftPlatform;
            activeFilter = Filters.RFID_PLATFORM;
        }

        loadAssetsFromLocalDB();
    }

    private void loadAssetsFromLocalDB() {
        // load assets for current Site and filter by asset type (if selected).
        List<Asset> assetsList = db.assetDAO().getAll(); //getAssetsForType(selectedAssetType);
        if (assetsList != null && !assetsList.isEmpty()) {
            List<GenericListModel> selectedAssets = assetsList.stream().map(x -> new GenericListModel(x.id, x.barcode)).collect(Collectors.toList()); // .toArray(GenericListModel[]::new);
            adapterAssets = new FilterableAdapter(this, (ArrayList<GenericListModel>) selectedAssets, itemsClickListener);
            adapterAssets.getFilter().filter("");
            this.rvAssets.setAdapter(adapterAssets);
        }
    }

    @Override
    protected void onDestroy() {
        if (executor != null)
            executor.shutdown();
        if (this.progressDialog != null)
            this.progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (executor != null)
            executor.shutdown();
    }

    public class SyncTxCallBack implements Callback<CorrelationTxDTO> {
        @Override
        public void onResponse(Call<CorrelationTxDTO> call, Response<CorrelationTxDTO> response) {
            CorrelationTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
                tvCorrAssetBarcode.setText("");
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<CorrelationTxDTO> call, Throwable error) {
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
        recWHCorrelation.longitude = location.getLongitude();
        recWHCorrelation.latitude = location.getLatitude();
        locationManager.removeUpdates(this);
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