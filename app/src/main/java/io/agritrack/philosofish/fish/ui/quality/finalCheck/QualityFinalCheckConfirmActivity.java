package io.agritrack.philosofish.fish.ui.quality.finalCheck;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityFinal;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.api.upload.UploadingApi;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.common.MediaDTO;
import io.agritrack.philosofish.data.dto.tx.FinalQualityTxDTO;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.FinalQualityRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.philosofish.ui.custom.CaptureSignatureView;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QualityFinalCheckConfirmActivity extends AppCompatActivity {

    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmAllOkDlg;
    private EditText etComments, etNotAccepted;
    private TextView tvLot, tvFishLot, tvNotAccepted;
    private SwitchCompat swLotAccepted, swForeignBody;
    private CaptureSignatureView signatureView;
    private ProgressDialog progressDialog;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_final_check_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFinalConfirm);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        confirmAllOkDlg = YesNoDialogFragment.instance();
        confirmAllOkDlg.setMessage(getText(R.string.confirm_submission));
        confirmAllOkDlg.onConfirm(bundle -> {
            moveToNextScreen();
        });
        confirmAllOkDlg.onReject(bundle -> {
            //proceedWithoutLocation = false;
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(QualityFinalCheckConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        swForeignBody.setOnClickListener(v -> {
            if (!swForeignBody.isChecked()) {
                etComments.setVisibility(View.VISIBLE);
            } else {
                etComments.setVisibility(View.GONE);
                recQualityFinal.corrAction = null;
            }
        });

        swLotAccepted.setOnClickListener(v -> {

            if (!swLotAccepted.isChecked()) {
                etNotAccepted.setVisibility(View.VISIBLE);
                tvNotAccepted.setVisibility(View.VISIBLE);
            } else {
                etNotAccepted.setVisibility(View.GONE);
                tvNotAccepted.setVisibility(View.GONE);
                recQualityFinal.discardedQty = null;
            }
        });


        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(QualityFinalCheckConfirmActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void moveToNextScreen() {
        // Update state and proceed to next
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
                FinalQualityTransaction tx = GlobalState.commitFinalQuality(db, Boolean.TRUE);

                if (IsOnline) {

                    Call<MediaDTO> syncQualitySigAsyncCall = updService.syncQualityTxSignature(MediaDTO.convert(tx), "Bearer " + token);
                    syncQualitySigAsyncCall.enqueue(new Callback<MediaDTO>() {
                        @Override
                        public void onResponse(Call<MediaDTO> call, Response<MediaDTO> response) {

                        }

                        @Override
                        public void onFailure(Call<MediaDTO> call, Throwable t) {

                        }
                    });

                    // sync Processing records
                    Call<FinalQualityTxDTO> syncTxAsyncCall = updService.syncFinalQualityTx(FinalQualityTxDTO.convert(tx), "Bearer " + token);
                    syncTxAsyncCall.enqueue(new QualityFinalCheckConfirmActivity.SyncTxCallBack());
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
                // move to next activity.
                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
                startActivity(i);
            }
        }


    }


    private String validate() {
        StringBuilder sb = new StringBuilder();
        if (!IsDemo) {
            if (!signatureView.isSigned()) {
                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.signature)));
            }
        }

        return sb.toString();
    }

    protected void configFooter() {
        ivNext.setOnClickListener(v -> {
            FragmentManager fm = getSupportFragmentManager();
            confirmAllOkDlg.showNow(fm, getString(R.string.confirm_selection));
        });

        ivBack.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), QualityFinalCheckSecondActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvFishLot = findViewById(R.id.tvFishLot);
        tvLot = findViewById(R.id.tvPackagingLot);
        tvNotAccepted = findViewById(R.id.tvNotAcceptedLabel);
        etNotAccepted = findViewById(R.id.etNotAccepted);
        etComments = findViewById(R.id.etComments);
        swLotAccepted = findViewById(R.id.swIsAccepted);
        swForeignBody = findViewById(R.id.swForeignBody);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackFinalCheckStart);
        signatureView = findViewById(R.id.signatureView);
    }

    private void initControlsFromState() {
        FinalQualityRecord qltRecord = GlobalState.recQualityFinal;

        if (!Strings.isEmptyOrWhitespace(recQualityFinal.lot)) {
            tvLot.setText(recQualityFinal.lot);
        }
        if (!Strings.isEmptyOrWhitespace(recQualityFinal.fishLot)) {
            tvFishLot.setText(recQualityFinal.fishLot);
        }
        if (recQualityFinal.lotAccepted != null) {
            swLotAccepted.setChecked(recQualityFinal.lotAccepted);
            if (!recQualityFinal.lotAccepted) {
                etNotAccepted.setVisibility(View.VISIBLE);
                etNotAccepted.setText(recQualityFinal.discardedQty.toString());
            }
        }
        if (recQualityFinal.foreignBody != null) {
            swForeignBody.setChecked(recQualityFinal.foreignBody);
            if (!recQualityFinal.foreignBody) {
                tvNotAccepted.setVisibility(View.VISIBLE);
                etComments.setVisibility(View.VISIBLE);
                etComments.setText(recQualityFinal.corrAction);
            }
        }


    }


    private void updateState() {


        recQualityFinal.foreignBody = swForeignBody.isChecked();
        recQualityFinal.lotAccepted = swLotAccepted.isChecked();
        recQualityFinal.corrAction = etComments.getText().toString();

        if (!swForeignBody.isChecked() && etComments.getText() != null && !Strings.isEmptyOrWhitespace(etComments.getText().toString())) {
            recQualityFinal.corrAction = etComments.getText().toString();
        }
        if (!swLotAccepted.isChecked() && etNotAccepted.getText() != null && !Strings.isEmptyOrWhitespace(etNotAccepted.getText().toString())) {
            recQualityFinal.discardedQty = Double.valueOf(etNotAccepted.getText().toString());
        }


        recQualityFinal.signature = signatureView.getBitmap();
        recQualityFinal.signatureBytes = signatureView.getBytes();

       // return true;
    }

    private boolean qualityTxMarkSynced() {
        try {
            System.out.println("About to delete quality tx");
            FinalQualityTransaction delObj = db.finalQualityTransactionDAO().getByLot(recQualityFinal.lot);
            if (delObj != null) {
                delObj.isSynced = true;
                db.finalQualityTransactionDAO().update(delObj);
                return true;
            }
            return false;
        } catch (Exception x) {
            x.printStackTrace();
            return false;
        }
    }



    public class SyncTxCallBack implements Callback<FinalQualityTxDTO> {
        @Override
        public void onResponse(Call<FinalQualityTxDTO> call, Response<FinalQualityTxDTO> response) {
            if (response.isSuccessful() || IsDemo) {
                qualityTxMarkSynced();
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Processing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_postquality_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<FinalQualityTxDTO> call, Throwable error) {
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
