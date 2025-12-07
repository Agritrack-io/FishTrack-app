package io.agritrack.philosofish.fish.ui.quality.receipt;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recFishing;
import static io.agritrack.philosofish.fish.state.GlobalState.recQuality;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityReceipt;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.data.repo.MeasurementRepository;
import io.agritrack.philosofish.data.dto.tx.ReceiptQualityTxDTO;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.api.upload.UploadingApi;
import io.agritrack.philosofish.common.FileUtils;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.repo.IFishTrackRepository;
import io.agritrack.philosofish.data.repo.TemperatureDataRepository;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.QualityStepsState;
import io.agritrack.philosofish.fish.state.ReceiptQualityRecord;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.ui.LocationAwareActivity;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;
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
    private IFishTrackRepository tempDataRepo, measRepo;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private ProgressDialog progressDialog;
    private TextView tvLot, tvSpecies, tvStartTime, tvFishDate, tvUsername;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_quality_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderReceiptQualityConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        this.tempDataRepo = new TemperatureDataRepository();
        this.measRepo = new MeasurementRepository();

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
            Boolean proceed = updateState();

            if (proceed) {

                QualityStepsState.completed[1] = true; // Step 2 receipt quality completed

                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
                startActivity(i);
            }
        }
    }


    protected void configFooter() {
        ivNext.setOnClickListener(v -> {
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
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), ReceiptQualityFreshCheckActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvLot = findViewById(R.id.tvReceiptLot);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvFishDate = findViewById(R.id.tvFishDate);
        tvSpecies = findViewById(R.id.tvSpecies);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToPackageQualityInfo);
        etPIN = findViewById(R.id.etPasswordProcessing);
    }

    private void initControlsFromState() {
        ReceiptQualityRecord qltRecord = GlobalState.recQualityReceipt;

        if (!Strings.isEmptyOrWhitespace(qltRecord.lot)) {
            tvLot.setText(qltRecord.lot);
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.fishSpecies)) {
            tvSpecies.setText(recFishing.speciesName);
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.startTime)) {
            tvStartTime.setText(qltRecord.startTime);
        }

        if (!Strings.isEmptyOrWhitespace(qltRecord.fishingDate)) {
            tvFishDate.setText(qltRecord.fishingDate);
        }

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
        }
    }

    private boolean isAuthenticated() {
        String login = LocalPreferences.getLoggedInUser("").trim();
        String pin = etPIN.getText().toString().trim();

        // use typed-in PIN to compare credentials with those stored in the Local DB.
        AuthenticationService authSvc = new AuthenticationService();
        return authSvc.authenticateUser(this.db, login, pin);
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            syncAllPhotos();

            // persist Processing Record data to local DB.
            ReceiptQualityTransaction tx = GlobalState.commitReceiptQuality(db, Boolean.TRUE);


            if (IsOnline) {
                // sync Processing records
                Call<ReceiptQualityTxDTO> syncTxAsyncCall = updService.syncRecQualityTx(ReceiptQualityTxDTO.convert(tx), "Bearer " + token);
                syncTxAsyncCall.enqueue(new ReceiptQualityConfirmActivity.SyncTxCallBack());

            } else {
                for (int i = 0; i < 3; i++) {
                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
                }
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

    private boolean qualityTxMarkSynced() {
        try {
            System.out.println("About to delete quality tx");
            ReceiptQualityTransaction delObj = db.receiptQualityTransactionDAO().getByLot(recQualityReceipt.lot);
            delObj.isSynced = true;
            db.receiptQualityTransactionDAO().update(delObj);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }

    public class SyncTxCallBack implements Callback<ReceiptQualityTxDTO> {
        @Override
        public void onResponse(Call<ReceiptQualityTxDTO> call, Response<ReceiptQualityTxDTO> response) {
            if (response.isSuccessful() || IsDemo) {
                qualityTxMarkSynced();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_postquality_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ReceiptQualityTxDTO> call, Throwable error) {
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

    public class PhotoFileUploadCallBack implements Callback<ResponseBody> {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.isSuccessful()) {
                    String fileName = response.body().string();
                    FileUtils.deletePhotoFile(ReceiptQualityConfirmActivity.this, fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + e.getMessage()), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable error) {
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.files_failed_to_sync), Toast.LENGTH_LONG));
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