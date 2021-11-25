package io.agritrack.fruit.ui.warehouse.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recCorrelation;
import static io.agritrack.fruit.state.FruitGlobalState.recHarvest;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.hdhe.uhf.reader.UhfReader;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.fruit.ui.harvesting.HarvestingConfirmActivity;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FruitCorrelationActivity extends LocationAwareActivity implements AdapterView.OnItemClickListener {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MultipleFilterSingleShotScanner scanner = new MultipleFilterSingleShotScanner();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private MobileDB db;
    private ListView lvGreenhouse;
    private List<io.agritrack.ui.bo.GenericListModel> greenhouse;
    private ArrayAdapter<io.agritrack.ui.bo.GenericListModel> greenhouseAdapter;
    private Button btnScanAssetTag, btnCorrelate;
    private TextView tvCorrPoleBarcode, tvCorrTempLoggerBarcode;
    private ProgressDialog progressDialog;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_correlation);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFruitCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(FruitCorrelationActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Site> ghouses = db.siteDAO().getAllGreenhouses();
        if (ghouses != null && !ghouses.isEmpty()) {
            this.greenhouse = ghouses.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, x.name)).collect(Collectors.toList());
            greenhouseAdapter = new ArrayAdapter<io.agritrack.ui.bo.GenericListModel>(this, android.R.layout.simple_list_item_checked, greenhouse) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(25);
                    return view;
                }
            };

            this.lvGreenhouse.setAdapter(greenhouseAdapter);
            this.lvGreenhouse.setOnItemClickListener(this);
        }

        // =================================
        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(view -> {
            //update scanning, uhfReader, tvPlatformName values in thread
            UhfReader _uhfReader = UhfReader.getInstance();
            _uhfReader.setWorkArea(3);
            scanner.setUhfReader(_uhfReader);
            scanner.setFilters(Filters.RFID_POLE, Filters.RFID_LOGGER);

            Future<?> future = executor.submit(scanner);
            try {
                String epcStr = future.get(2000, TimeUnit.MILLISECONDS).toString();
                if (!Strings.isEmptyOrWhitespace(epcStr)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            String[] epcs = epcStr.split(",");
                            for (String epc : epcs) {
                                if (epc.indexOf(Filters.RFID_POLE) > 0)
                                    tvCorrPoleBarcode.setText(epc);
                                else if (epc.indexOf(Filters.RFID_LOGGER) > 0)
                                    tvCorrTempLoggerBarcode.setText(epc);
                            }

                        }
                    });
                    //tvCageName.setText(result);
                }
            } catch (Exception e) {
                future.cancel(true);
            }
        });

        btnCorrelate.setOnClickListener(view -> {
            /*GlobalState.recWHCorrelation.assetType = !Strings.isEmptyOrWhitespace(selectedAssetType) ? AssetType.valueOf(selectedAssetType) : null;
            GlobalState.recWHCorrelation.barcode = !Strings.isEmptyOrWhitespace(selectedBarcode) ? selectedBarcode : null; //tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;
            GlobalState.recWHCorrelation.rfid = tvCorrAssetBarcode.getText() != null ? tvCorrAssetBarcode.getText().toString() : null;*/

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            correlate();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FruitCorrelationActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recCorrelation.longitude = mLastLocation.getLongitude();
                    recCorrelation.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(FruitCorrelationActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = correlate();

                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitWareHouseMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvCorrPoleBarcode = findViewById(R.id.tvCorrPoleBarcode);
        tvCorrTempLoggerBarcode = findViewById(R.id.tvCorrTempLoggerBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        btnCorrelate = findViewById(R.id.btnCorrelate);
        ivSupport = findViewById(R.id.ivSupport);
        // get main controls references
        lvGreenhouse = findViewById(R.id.lvGreenhouse);
        // define if single or multiple choice mode will be used to display the checkboxes.
        lvGreenhouse.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
            syncTxAsyncCall.enqueue(new FruitCorrelationActivity.SyncTxCallBack());

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
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.barcode)) {
                sb.append(String.format("\n%s is missing", "'Asset BARCODE'"));
            }

            if (Strings.isEmptyOrWhitespace(GlobalState.recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Asset RFID'"));
            }
        }
        return sb.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        io.agritrack.ui.bo.GenericListModel member = (io.agritrack.ui.bo.GenericListModel) this.lvGreenhouse.getItemAtPosition(position);
        member.setChecked(!currentCheck);
    }

    public class SyncTxCallBack implements Callback<CorrelationTxDTO> {
        @Override
        public void onResponse(Call<CorrelationTxDTO> call, Response<CorrelationTxDTO> response) {
            CorrelationTxDTO rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
                tvCorrPoleBarcode.setText("");
                tvCorrTempLoggerBarcode.setText("");
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
}