package io.agritrack.fish.ui.wh.correlation;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recWHCorrelation;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.query.EnquiryApi;
import io.agritrack.api.sync.SyncCurrentEpcsCallBack;
import io.agritrack.caen.api.BX6100Programmer;
import io.agritrack.common.Constants;
import io.agritrack.common.DeviceUtils;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.ReaderDTO;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.data.type.ConfigDevice;
import io.agritrack.data.type.EpcPerDevice;
import io.agritrack.dialog.CheckTagDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.rfid.X9KeyReceiver;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTagActivity extends LocationAwareActivity {
    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private final NewTagActivity.ScanHandler mScanHandler = new NewTagActivity.ScanHandler(this);
    private final SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
    protected BroadcastReceiver keyReceiver;
    private BX6100Programmer x9programmer;
    private MobileDB db;
    private String selectedAssetType;
    private TextView tvInfo, tvImportantNote;
    private Button btnProgramTag;
    private ProgressDialog progressDialog;
    private YesNoDialogFragment confirmGPSSelectionDlg;
    private boolean proceedWithoutLocation = false;
    private ImageView ivSupport, ivNext, ivBack;
    private SupportDialog supportDialog;
    private CheckTagDialog checkTagDialog;
    private int assetType;
    private String filter, rfid, code;
    private int noOfAssets, noOfTags, index = 0;
    private String lastEpcPerAsset, prefix;
    private int syncCounter = 1;
    private Map<String, String> epcTid = new HashMap<>();

    @Override
    @SuppressLint("StringFormatMatches")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_tag);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            assetType = bundle != null ? bundle.getInt("assetType") : 0;
        }

        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);

        x9programmer = new BX6100Programmer();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProgramTagCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        syncResult.observe(this, response -> {
            syncCounter++;
            if (response == null) {
                hideProgressDialog();
                return;
            }
            if (response != null) {
                progressDialog.setMessage(render(response));
                if (syncCounter > 6) {
                    hideProgressDialog();
                }
            }
        });

        prefix = LocalPreferences.getPrefix();

        switch (assetType) {
            case 0:
                filter = Filters.RFID_CAGE;
                selectedAssetType = Constants.ftCage;
                index = 2;
                lastEpcPerAsset = LocalPreferences.getCurrentEpcList().get(2).getEpc();
                noOfTags = 4;
                break;
            case 1:
                filter = Filters.RFID_NET;
                selectedAssetType = Constants.ftNet;
                lastEpcPerAsset = LocalPreferences.getCurrentEpcList().get(1).getEpc();
                index = 1;
                noOfTags = 4;
                break;
            case 2:
                filter = Filters.RFID_BIN;
                selectedAssetType = Constants.ftBin;
                lastEpcPerAsset = LocalPreferences.getCurrentEpcList().get(0).getEpc();
                index = 0;
                noOfTags = 2;
                break;
            case 3:
                filter = Filters.RFID_PLATFORM;
                selectedAssetType = Constants.ftPlatform;
                lastEpcPerAsset = LocalPreferences.getCurrentEpcList().get(3).getEpc();
                index = 3;
                noOfTags = 1;
                break;
            default:
        }

        tvImportantNote.setText(getString(R.string.important_note_msg, noOfTags));

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
        progressDialog = new ProgressDialog(NewTagActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        btnProgramTag.setOnClickListener(this::onClick);

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(NewTagActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
//            Boolean proceed = correlate();

            if (true) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), NewTagActivity.class);
                startActivity(i);
            }
        }
    }

    private void stopScanner() {
        if (this.scanner_runnable != null) {
            this.scanner_runnable.stopReading();
            mScanHandler.removeCallbacks(this.scanner_runnable);
        }
    }

    // hide/dismiss Progress bar
    private void hideProgressDialog() {
        progressDialog.dismiss();
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
            setCurrentEpcsDevice();
            Intent i = new Intent(getApplicationContext(), CorrelationSubMenuActivity.class);
            i.putExtra("id", assetType);
            startActivity(i);
        });

        ivNext.setOnClickListener(view -> {
            stopScanner();

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
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
        tvInfo = findViewById(R.id.tvInfo);
        tvImportantNote = findViewById(R.id.tvImportantNote);
        btnProgramTag = findViewById(R.id.btnProgramTag);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToCorrelationMenu);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (Strings.isEmptyOrWhitespace(recWHCorrelation.code)) {
                sb.append(String.format("\n%s is missing", "'Net code'"));
            }

            if (Strings.isEmptyOrWhitespace(recWHCorrelation.rfid)) {
                sb.append(String.format("\n%s is missing", "'Net RFID'"));
            }
        }
        return sb.toString();
    }

    protected void onClick(View view) {
        scanner_runnable.setFilter(""); //Filters.RFID_NET
        scanner_runnable.startReading();
        scanner_runnable.LowEnergy();
        scanner_runnable.readEpcList();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    private void setCurrentEpcsDevice() {
        try {
            EnquiryApi syncService = APIServiceGenerator.createAPI(EnquiryApi.class);
            String token = LocalPreferences.getToken();
            String deviceID = DeviceUtils.getIMEIDeviceId(this);
            ConfigDevice cDev = new ConfigDevice();
            List<EpcPerDevice> epcs = LocalPreferences.getCurrentEpcList();
            String prefix = LocalPreferences.getPrefix();
            cDev.setPrefix(prefix);
            cDev.setEpcs(epcs);

            // sync sites for current cluster
            Call<ReaderDTO> setCurrentEPcsCall = syncService.setCurrentEpcsByDevice(deviceID, cDev, "Bearer " + token);
            setCurrentEPcsCall.enqueue(new SyncCurrentEpcsCallBack(this.syncResult));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    private void writeEpcByTid(String epcToWrite, String tid) {
        Reader.READER_ERR res = x9programmer.writeTagEPCByTIDFilter(epcToWrite, tid);
        if (Reader.READER_ERR.MT_OK_ERR.compareTo(res) == 0) {
        } else {
            return;
        }
    }

    private int validateTags(String epcToWrite, String tid) {
        String res = x9programmer.getTagEpcDataByTIDFilter(tid);
        if (res.equalsIgnoreCase(epcToWrite)) {
            return 1;
        }
        return 0;
    }

    public class SyncTxCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (response.isSuccessful()) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_LONG));
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
        private final WeakReference<NewTagActivity> mActivity;

        public ScanHandler(NewTagActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ArrayList<String> epcList = msg.getData().getStringArrayList("epcList");
                    try {
                        String outStr = getString(R.string.reading_tags);
                        String finalOutStr3 = outStr;
                        runOnUiThread(() -> tvInfo.setText(finalOutStr3));

                        for (String epcStr : epcList) {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                if (epcStr.substring(11).startsWith(filter)) {
                                    tvInfo.setText(R.string.programmed_tags_scan_again);
                                    return;
                                } else if (epcStr.substring(11).startsWith("141")) {
                                    tvInfo.setText(R.string.associated_tags_scan_again);
                                    return;
                                }
                            }
                        }

                        if (epcList.size() < noOfTags) {
                            tvInfo.setText(getString(R.string.less_tags, noOfTags));
                            return;
                        } else if (epcList.size() > noOfTags) {
                            tvInfo.setText(getString(R.string.more_tags, noOfTags));
                            return;
                        }

                        for (String epcStr : epcList) {
                            String tid = x9programmer.getTagTIDDataByFilter(epcStr);
                            epcTid.put(tid, epcStr);
                        }

                        String epcToWrite = "BE0019A0000" + filter + prefix + lastEpcPerAsset;
                        outStr = outStr + getString(R.string.programming_tags);
                        String finalOutStr = outStr;
                        runOnUiThread(() -> tvInfo.setText(finalOutStr));

                        int programmedTags = 0;
                        for (String tid : epcTid.keySet()) {
                            writeEpcByTid(epcToWrite, tid);
                            int res = validateTags(epcToWrite, tid);
                            int counter = 1;
                            while (res == 0 && counter > 0) {
                                res = validateTags(epcToWrite, tid);
                                counter--;
                            }
                            programmedTags+=res;

                            outStr = outStr + getString(R.string.programmed_tags, programmedTags);
                            String finalOutStr1 = outStr;
                            runOnUiThread(() -> tvInfo.setText(finalOutStr1));
                        }

                        if (programmedTags == noOfTags) {
                            outStr = outStr + getString(R.string.successfully_validated_tag) + getString(R.string.write_tag, epcToWrite.substring(14));
                            String finalOutStr2 = outStr;
                            runOnUiThread(() -> tvInfo.setText(finalOutStr2));
                        } else {
                            runOnUiThread(() -> tvInfo.setText(getString(R.string.contact_admin)));
                        }

                        String finalEpc = String.format("%0" + epcToWrite.substring(17).length() + "d", Long.parseLong(epcToWrite.substring(17)) + 1);
                        List<EpcPerDevice> oldList = LocalPreferences.getCurrentEpcList();
                        EpcPerDevice epcPerDevice = new EpcPerDevice();
                        epcPerDevice.setType(filter);
                        epcPerDevice.setEpc(finalEpc);
                        oldList.set(index, epcPerDevice);
                        LocalPreferences.putCurrentEpcList(oldList);
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