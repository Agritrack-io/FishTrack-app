package io.agritrack.fish.ui.quality.receipt;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recLoggerData;
import static io.agritrack.fish.state.GlobalState.recQuality;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.upload.UploadingApi;
import io.agritrack.common.FileUtils;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.data.dto.tx.QualityTxDTO;
import io.agritrack.data.model.common.TemperatureTimeSeries;
import io.agritrack.data.model.tx.QualityTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.state.QualityRecord;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.fish.api.tx.TransactionApi;
import io.agritrack.ui.service.AuthenticationService;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptQualityConfirmActivity extends LocationAwareActivity {
    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ProgressDialog progressDialog;
    private TextView tvNumberOfBinsCount, tvPackagingLot, tvEvaluation, tvUsername;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;
    private long filesLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

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
        progressDialog = new ProgressDialog(ReceiptQualityConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ReceiptQualityConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        if (proceedWithoutLocation) {
            // Update state and proceed to next
            Boolean proceed = updateState();

            if (proceed) {
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                startActivity(i);
            }
        }
    }

    protected void configFooter() {
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etPIN.getText().toString())) {
                    CToast(ReceiptQualityConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(ReceiptQualityConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recQuality.longitude = mLastLocation.getLongitude();
                    recQuality.latitude = mLastLocation.getLatitude();
                    proceedWithoutLocation = true;
                    moveToNextScreen();
                } else if (!proceedWithoutLocation) {
                    FragmentManager fm = getSupportFragmentManager();
                    confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                }
            }
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityMoreInfo3Activity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvPackagingLot = findViewById(R.id.tvPackagingLot);
        tvEvaluation = findViewById(R.id.tvEvaluation);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToPackageQualityInfo);
        etPIN = findViewById(R.id.etPasswordProcessing);
    }

    private void initControlsFromState() {
        QualityRecord qltRecord = GlobalState.recQuality;

        if (!Strings.isEmptyOrWhitespace(qltRecord.pLot)) {
            tvPackagingLot.setText(qltRecord.pLot);
        }

        if (qltRecord.qualityBins != null) {
            tvNumberOfBinsCount.setText(String.valueOf(qltRecord.qualityBins.size()));
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.evaluation)) {
            tvEvaluation.setText(qltRecord.evaluation);
        }

        //tvNumberOfBinsCount.setText(prcRecord.totalBinsUsed != null ? prcRecord.totalBinsUsed.toString() : "N/A");

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean syncAllPhotos() {
        try {
            String token = LocalPreferences.getToken();

            final String extension = ".png";
            final File documentsFolder = new File(ReceiptQualityConfirmActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
            File[] files = documentsFolder.listFiles((File pathname) -> pathname.getName().endsWith(extension));
            for (File file : files) {
                // create RequestBody instance from file
                RequestBody requestFile = RequestBody.create(file, MediaType.parse("application/json"));

                if (file.getName().startsWith("Photo")) {
                    // MultipartBody.Part is used to send also the actual file name
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", file.getName(), requestFile);

                    Call<ResponseBody> uploadJpegPhotoFileAsyncCall = upldSvc.uploadPhoto(filePart, "Bearer " + token);
                    uploadJpegPhotoFileAsyncCall.enqueue(new ReceiptQualityConfirmActivity.PhotoFileUploadCallBack());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {

        }
    }

    private boolean isAuthenticated() {
        String login = LocalPreferences.getLoggedInUser("").trim();
        String pin = etPIN.getText().toString().trim();

        // use typed-in PIN to compare credentials with those stored in the Local DB.
        AuthenticationService authSvc = new AuthenticationService();
        boolean authentication = authSvc.authenticateUser(this.db, login, pin);

        return authentication;
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();
            //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

            syncAllPhotos();

            // persist Processing Record data to local DB.
            QualityTransaction tx = GlobalState.commitQuality(db);

            // persist Measurements Record data to local DB.
            List<TemperatureTimeSeries> measurements = GlobalState.commitMeasurements(db);
            List<TemperatureTimeSeriesDTO> temperatureTimeSeriesDTOs = new ArrayList<>();
            for (TemperatureTimeSeries ts : measurements) {
                TemperatureTimeSeriesDTO measurementDTO = TemperatureTimeSeriesDTO.convert(ts);
                measurementDTO.lot = tx.plot;
                temperatureTimeSeriesDTOs.add(measurementDTO);
            }

            // sync Processing records
            Call<QualityTxDTO> syncTxAsyncCall = updService.syncQualityTx(QualityTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new ReceiptQualityConfirmActivity.SyncTxCallBack());

            // sync Measurements records
            if (!temperatureTimeSeriesDTOs.isEmpty()) {
                Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall = updService.syncMeasurements(temperatureTimeSeriesDTOs, "Bearer " + token);
                syncMsAsyncCall.enqueue(new ReceiptQualityConfirmActivity.SyncMsCallBack());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
            return false;
        } finally {
            progressDialog.dismiss();
        }
    }

    public class SyncTxCallBack implements Callback<QualityTxDTO> {
        @Override
        public void onResponse(Call<QualityTxDTO> call, Response<QualityTxDTO> response) {
            QualityTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_processing_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<QualityTxDTO> call, Throwable error) {
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

    public class SyncMsCallBack implements Callback<List<TemperatureTimeSeriesDTO>> {
        @Override
        public void onResponse(Call<List<TemperatureTimeSeriesDTO>> call, Response<List<TemperatureTimeSeriesDTO>> response) {
            List<TemperatureTimeSeriesDTO> rs = response.body();

            if (rs != null || IsDemo) {
                // reset existing Temperature values in stateRecord.
                recLoggerData.clearData();
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_temperatures_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<List<TemperatureTimeSeriesDTO>> call, Throwable error) {
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

    public class PhotoFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.body()!=null) {
                    String fileName = response.body().string();
                    boolean res = FileUtils.deletePhotoFile(ReceiptQualityConfirmActivity.this, fileName);
                    if (res) {
                        //runOnUiThread(() -> CToast(getApplicationContext(), render("File " + fileName + " was uploaded successfully!!!"), Toast.LENGTH_LONG));
                    } else {
                        //runOnUiThread(() -> CToast(getApplicationContext(), render("Failed to remove file" +fileName+ " from local folder!!!"), Toast.LENGTH_LONG));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> CToast(getApplicationContext(), render("Error:" + e.getMessage()), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.files_failed_to_sync), Toast.LENGTH_LONG));
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