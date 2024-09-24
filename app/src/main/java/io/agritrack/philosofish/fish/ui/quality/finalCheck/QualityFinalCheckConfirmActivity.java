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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import io.agritrack.philosofish.ui.custom.ToggleGroup;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QualityFinalCheckConfirmActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmAllOkDlg;
    private Integer selectedExfoRating, selectedPaletteRating, selectedBoxRating;

    private ToggleGroup tgExfo, tgPalette, tgBox;
    private CheckBox cbCylindrical, cbExpanded, cbSoft, cbHead, cbBody, cbAreas;
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
            Intent i = new Intent(getApplicationContext(), QualityFinalCheckActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tgBox = findViewById(R.id.tgBoxCondition);
        tgBox.setOnCheckedChangeListener(this);

        tgPalette = findViewById(R.id.tgPaletteCondition);
        tgPalette.setOnCheckedChangeListener(this);

        tgExfo = findViewById(R.id.tgExfoliation);
        tgExfo.setOnCheckedChangeListener(this);

        cbAreas = findViewById(R.id.cbAreas);
        cbBody = findViewById(R.id.cbBody);
        cbHead = findViewById(R.id.cbHead);
        cbCylindrical = findViewById(R.id.cbCylindrical);
        cbExpanded = findViewById(R.id.cbExpanded);
        cbSoft = findViewById(R.id.cbSoft);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackFinalCheckStart);
        signatureView = findViewById(R.id.signatureView);
    }

    private void initControlsFromState() {
        FinalQualityRecord qltRecord = GlobalState.recQualityFinal;

        if (qltRecord.exfoRating != null) {
            switch (qltRecord.exfoRating) {
                case 1:
                    tgExfo.check(R.id.tbHighExfo);
                    selectedExfoRating = 1;
                    break;
                case 2:
                    tgExfo.check(R.id.tbMidExfo);
                    selectedExfoRating = 2;
                    break;
                case 3:
                    tgExfo.check(R.id.tbLightExfo);
                    selectedExfoRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.paletteRating != null) {
            switch (qltRecord.paletteRating) {
                case 1:
                    tgPalette.check(R.id.tbPaletteC);
                    selectedPaletteRating = 1;
                    break;
                case 2:
                    tgPalette.check(R.id.tbPaletteB);
                    selectedPaletteRating = 2;
                    break;
                case 3:
                    tgPalette.check(R.id.tbPaletteA);
                    selectedPaletteRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.boxRating != null) {
            switch (qltRecord.boxRating) {
                case 1:
                    tgBox.check(R.id.tbBoxC);
                    selectedBoxRating = 1;
                    break;
                case 2:
                    tgBox.check(R.id.tbBoxB);
                    selectedBoxRating = 2;
                    break;
                case 3:
                    tgBox.check(R.id.tbBoxA);
                    selectedBoxRating = 3;
                    break;
                default:
                    break;
            }
        }
        if (qltRecord.areas != null && qltRecord.areas) {
            cbAreas.setChecked(true);
        }
        if (qltRecord.body != null && qltRecord.body) {
            cbBody.setChecked(true);
        }
        if (qltRecord.head != null && qltRecord.head) {
            cbHead.setChecked(true);
        }
        if (qltRecord.cylinrical != null && qltRecord.cylinrical) {
            cbCylindrical.setChecked(true);
        }
        if (qltRecord.expanded != null && qltRecord.expanded) {
            cbExpanded.setChecked(true);
        }
        if (qltRecord.soft != null &&qltRecord.soft) {
            cbSoft.setChecked(true);
        }

    }


    private boolean updateState() {

        recQualityFinal.exfoRating = selectedExfoRating;
        recQualityFinal.paletteRating = selectedPaletteRating;
        recQualityFinal.boxRating = selectedBoxRating;

        if(cbSoft.isChecked()) {
            recQualityFinal.soft = true;
        } else {
            recQualityFinal.soft = false;
        }
        if(cbBody.isChecked()) {
            recQualityFinal.body = true;
        } else {
            recQualityFinal.body = false;
        }
        if(cbHead.isChecked()) {
            recQualityFinal.head = true;
        } else {
            recQualityFinal.head = false;
        }
        if(cbCylindrical.isChecked()) {
            recQualityFinal.cylinrical = true;
        } else {
            recQualityFinal.cylinrical = false;
        }
        if(cbExpanded.isChecked()) {
            recQualityFinal.expanded = true;
        } else {
            recQualityFinal.expanded = false;
        }
        if(cbAreas.isChecked()) {
            recQualityFinal.areas = true;
        } else {
            recQualityFinal.areas = false;
        }

        recQualityFinal.signature = signatureView.getBitmap();
        recQualityFinal.signatureBytes = signatureView.getBytes();

        return true;
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

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbLightExfo) {
            selectedExfoRating = 3;
        } else if (checkedId == R.id.tbMidExfo) {
            selectedExfoRating = 2;
        } else if (checkedId == R.id.tbHighExfo) {
            selectedExfoRating = 1;
        } else if (checkedId == R.id.tbPaletteA) {
            selectedPaletteRating = 3;
        }else if (checkedId == R.id.tbPaletteB) {
            selectedPaletteRating = 2;
        } else if (checkedId == R.id.tbPaletteC) {
            selectedPaletteRating = 1;
        } else if (checkedId == R.id.tbBoxA) {
            selectedBoxRating = 3;
        } else if (checkedId == R.id.tbBoxB) {
            selectedBoxRating = 2;
        }else if (checkedId == R.id.tbBoxC) {
            selectedBoxRating = 1;
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
