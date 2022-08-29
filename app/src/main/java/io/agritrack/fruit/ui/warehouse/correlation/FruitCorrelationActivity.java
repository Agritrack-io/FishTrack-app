package io.agritrack.fruit.ui.warehouse.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fruit.state.FruitGlobalState.recCorrelation;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.common.Constants;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.model.Site;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.ui.bo.GenericListModel;
import io.agritrack.fruit.state.FruitGlobalState;
import io.agritrack.fruit.ui.FruitWhMenuActivity;
import io.agritrack.rfid.MultipleFilterSingleShotScanner;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FruitCorrelationActivity extends LocationAwareActivity implements AdapterView.OnItemClickListener {
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private MobileDB db;
    private ListView lvGreenhouse;
    private List<GenericListModel> greenhouse;
    private ArrayAdapter<GenericListModel> greenhouseAdapter;
    private Button btnScanAssetTag;
    private TextView tvCorrPoleBarcode, tvCorrTempLoggerBarcode;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_correlation);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFruitCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

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
        progressDialog = new ProgressDialog(FruitCorrelationActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // load employees belonging to current Site and fill in the spFishingTeam Spinner.
        List<Site> ghouses = db.siteDAO().getCurrentSiteSubSites(LocalPreferences.getCurrentSiteLevel3());
        if (ghouses != null && !ghouses.isEmpty()) {
            this.greenhouse = ghouses.stream().map(x -> new GenericListModel(x.id, x.name)).collect(Collectors.toList());
            greenhouseAdapter = new ArrayAdapter<GenericListModel>(this, android.R.layout.simple_list_item_checked, greenhouse) {
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
        btnScanAssetTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(FruitCorrelationActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen(){
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = correlate();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
                startActivity(i);
            }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyReceiver);
        super.onPause();
    }

    protected void configFooter() {
        ivNext.setOnClickListener(view -> {
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
                return;
            }
            FruitGlobalState.recCorrelation.assetType = Constants.ftPole;
            FruitGlobalState.recCorrelation.poleBarcode = tvCorrPoleBarcode.getText() != null ? tvCorrPoleBarcode.getText().toString() : null;
            FruitGlobalState.recCorrelation.loggerType = Constants.ftDataLogger;
            FruitGlobalState.recCorrelation.loggerRFID = tvCorrTempLoggerBarcode.getText() != null ? tvCorrTempLoggerBarcode.getText().toString() : null;

            if (mLastLocation != null) {
                recCorrelation.longitude = mLastLocation.getLongitude();
                recCorrelation.latitude = mLastLocation.getLatitude();
                proceedWithoutLocation = true;
                moveToNextScreen();
            } else {
                FragmentManager fm = getSupportFragmentManager();
                confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
            }
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitWhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {

        tvCorrPoleBarcode = findViewById(R.id.tvCorrPoleBarcode);
        tvCorrTempLoggerBarcode = findViewById(R.id.tvCorrTempLoggerBarcode);
        btnScanAssetTag = findViewById(R.id.btnScanAssetTag);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToFruitWareHouseMenu);
        // get main controls references
        lvGreenhouse = findViewById(R.id.lvGreenhouse);
        // define if single or multiple choice mode will be used to display the checkboxes.
        lvGreenhouse.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private boolean correlate() {
        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            CorrelationTransaction tx = FruitGlobalState.commitWHCorrelation(db);

            // sync WH Correlation Tx
            Call<CorrelationTxDTO> syncTxAsyncCall = updService.syncLoggerCorrelationTx(CorrelationTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new FruitCorrelationActivity.SyncTxCallBack());

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
            if (FruitGlobalState.recCorrelation.subSite == null || FruitGlobalState.recCorrelation.subSite.isEmpty()) {
                sb.append(String.format("\n%s is missing", "'Site'"));
            }
            if (Strings.isEmptyOrWhitespace(recCorrelation.poleRFID)) {
                sb.append(String.format("\n%s is missing", "'Pole'"));
            }

            if (Strings.isEmptyOrWhitespace(recCorrelation.loggerRFID)) {
                sb.append(String.format("\n%s is missing", "'Temp Logger'"));
            }
        }
        return sb.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvGreenhouse.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        int sp = this.lvGreenhouse.getCheckedItemPosition();
        FruitGlobalState.recCorrelation.subSite = this.lvGreenhouse.getAdapter().getItem(sp).toString();
    }

    public class SyncTxCallBack implements Callback<CorrelationTxDTO> {
        @Override
        public void onResponse(Call<CorrelationTxDTO> call, Response<CorrelationTxDTO> response) {
            CorrelationTxDTO rs = response.body();

            if (rs != null || IsDemo) {
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

    protected void onClick(View view) {
        MultipleFilterSingleShotScanner scanner_runnable = new MultipleFilterSingleShotScanner(mScanHandler);
        scanner_runnable.setFilters(Filters.RFID_POLE, Filters.RFID_LOGGER);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<FruitCorrelationActivity> mActivity;

        public ScanHandler(FruitCorrelationActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<CharSequence> tags = msg.getData().getCharSequenceArrayList("epc");
                    try {
                        if (!CollectionUtils.isEmpty(tags)) {
                            for (CharSequence tag : tags) {
                                String epc = tag.toString();
                                if (epc.indexOf(Filters.RFID_POLE) > -1) {
                                    FruitGlobalState.recCorrelation.poleRFID = epc;
                                    tvCorrPoleBarcode.setText(epc);
                                }
                                else if (epc.indexOf(Filters.RFID_LOGGER) > -1){
                                    FruitGlobalState.recCorrelation.loggerRFID = epc;
                                    tvCorrTempLoggerBarcode.setText(epc);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render("No Pole Tag was detected!!"), Toast.LENGTH_SHORT);
                    }
                    break;
            }
        }
    }
}