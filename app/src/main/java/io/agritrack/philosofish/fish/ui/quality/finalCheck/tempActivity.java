//package io.agritrack.philosofish.fish.ui.quality.finalCheck;
//
//import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
//import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
//import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
//import static io.agritrack.philosofish.common.LargeString.render;
//import static io.agritrack.philosofish.fish.state.GlobalState.recQualityFinal;
//import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.InputFilter;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.fragment.app.FragmentManager;
//
//import com.google.android.gms.common.util.Strings;
//
//import java.io.IOException;
//import java.net.SocketTimeoutException;
//
//import io.agritrack.philosofish.R;
//import io.agritrack.philosofish.api.APIServiceGenerator;
//import io.agritrack.philosofish.api.tx.TransactionApi;
//import io.agritrack.philosofish.api.upload.UploadingApi;
//import io.agritrack.philosofish.common.InputFilterMinMax;
//import io.agritrack.philosofish.data.db.MobileDB;
//import io.agritrack.philosofish.data.dto.common.MediaDTO;
//import io.agritrack.philosofish.data.dto.tx.FinalQualityTxDTO;
//import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
//import io.agritrack.philosofish.dialog.SupportDialog;
//import io.agritrack.philosofish.dialog.YesNoDialogFragment;
//import io.agritrack.philosofish.fish.state.FinalQualityRecord;
//import io.agritrack.philosofish.fish.state.GlobalState;
//import io.agritrack.philosofish.fish.ui.quality.QualitySelectStepsActivity;
//import io.agritrack.philosofish.ui.custom.CaptureSignatureView;
//import io.agritrack.philosofish.ui.custom.ToggleGroup;
//import io.agritrack.philosofish.ui.service.LocalPreferences;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class tempActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
//
//    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
//    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
//    private MobileDB db;
//    private YesNoDialogFragment confirmAllOkDlg;
//    private EditText size1, size2, size3, type1, type2, type3, number1, number2, number3, actual1, actual2, actual3,
//            under1, under2, under3, over1, over2, over3,  net1, net2, net3, ice1, ice2, ice3, temp1, temp2, temp3;
//    private CaptureSignatureView signatureView;
//    private ProgressDialog progressDialog;
//    private ImageView ivSupport, ivNext, ivBack;
//    private boolean proceedWithoutLocation = false;
//    private SupportDialog supportDialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_quality_final_check_second);
//
//        // set Header Info
//        TextView tvHeader = findViewById(R.id.tvHeaderFinalQuality);
//        tvHeader.setText(LocalPreferences.HeaderMsg());
//
//        // get an instance of local DB
//        this.db = MobileDB.getInstance(getAppContext());
//
//        // get  references of the controls
//        assignCtrlVars();
//
//        confirmAllOkDlg = YesNoDialogFragment.instance();
//        confirmAllOkDlg.setMessage(getText(R.string.confirm_submission));
//        confirmAllOkDlg.onConfirm(bundle -> {
//            moveToNextScreen();
//        });
//        confirmAllOkDlg.onReject(bundle -> {
//            //proceedWithoutLocation = false;
//        });
//
//        // instantiate ProgressDialog and set style.
//        progressDialog = new ProgressDialog(QualityFinalCheckSecondActivity.this);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//
//        // set (any?) previously selected values to activity Controls.
//        initControlsFromState();
//
//
//        ivSupport.setOnClickListener(view -> {
//            supportDialog = new SupportDialog(QualityFinalCheckSecondActivity.this);
//            supportDialog.showDialog();
//        });
//
//        configFooter();
//    }
//
//    private void moveToNextScreen() {
//        // Update state and proceed to next
//        updateState();
//
//        String v = validate();
//        if (!Strings.isEmptyOrWhitespace(v)) {
//            CToast(getApplicationContext(), render(getString(R.string.invalid_inputs) + v), Toast.LENGTH_LONG);
//        } else {
//            boolean proceed;
//            try {
//
//                progressDialog.setCancelable(false);
//                progressDialog.setMessage(render("Synchronizing data..."));
//                progressDialog.show();
//
//                String token = LocalPreferences.getToken();
//
//                // persist Processing Record data to local DB.
//                FinalQualityTransaction tx = GlobalState.commitFinalQuality(db, Boolean.TRUE);
//
//                if (IsOnline) {
//
//                    Call<MediaDTO> syncQualitySigAsyncCall = updService.syncQualityTxSignature(MediaDTO.convert(tx), "Bearer " + token);
//                    syncQualitySigAsyncCall.enqueue(new Callback<MediaDTO>() {
//                        @Override
//                        public void onResponse(Call<MediaDTO> call, Response<MediaDTO> response) {
//
//                        }
//
//                        @Override
//                        public void onFailure(Call<MediaDTO> call, Throwable t) {
//
//                        }
//                    });
//
//                    // sync Processing records
//                    Call<FinalQualityTxDTO> syncTxAsyncCall = updService.syncFinalQualityTx(FinalQualityTxDTO.convert(tx), "Bearer " + token);
//                    syncTxAsyncCall.enqueue(new QualityFinalCheckSecondActivity.SyncTxCallBack());
//                } else {
//                    for (int i = 0; i < 3; i++) {
//                        runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
//                    }
//                }
//
//                proceed = true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                CToast(this, "Error:" + e.getMessage(), Toast.LENGTH_LONG);
//                proceed = false;
//            } finally {
//                progressDialog.dismiss();
//            }
//            if (proceed) {
//                // move to next activity.
//                Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
//                startActivity(i);
//            }
//        }
//
//
//    }
//
//
//    private String validate() {
//        StringBuilder sb = new StringBuilder();
//        if (!IsDemo) {
//            if (!signatureView.isSigned()) {
//                sb.append(String.format(getString(R.string.field) + "\n%s " + getString(R.string.is_missing) + "\n", getString(R.string.signature)));
//            }
//        }
//
//        return sb.toString();
//    }
//
//    protected void configFooter() {
//        ivNext.setOnClickListener(v -> {
//            FragmentManager fm = getSupportFragmentManager();
//            confirmAllOkDlg.showNow(fm, getString(R.string.confirm_selection));
//        });
//
//        ivBack.setOnClickListener(view -> {
//            updateState();
//            Intent i = new Intent(getApplicationContext(), QualityFinalCheckActivity.class);
//            startActivity(i);
//        });
//    }
//
//    private void assignCtrlVars() {
//        size1 = findViewById(R.id.etSize1);
//        type1 = findViewById(R.id.etType1);
//        number1 = findViewById(R.id.etNumber1);
//        actual1 = findViewById(R.id.etActual1);
//        under1 = findViewById(R.id.etUnder11);
//        over1 = findViewById(R.id.etOver11);
//        net1 = findViewById(R.id.etNet1);
//        ice1 = findViewById(R.id.etIce1);
//        ice1.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
//        temp1 = findViewById(R.id.etTemp1);
//        size2 = findViewById(R.id.etSize2);
//        type2 = findViewById(R.id.etType2);
//        number2 = findViewById(R.id.etNumber2);
//        actual2 = findViewById(R.id.etActual2);
//        under2 = findViewById(R.id.etUnder12);
//        over2 = findViewById(R.id.etOver21);
//        net2 = findViewById(R.id.etNet2);
//        ice2 = findViewById(R.id.etIce2);
//        ice2.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
//        temp2 = findViewById(R.id.etTemp2);
//        size3 = findViewById(R.id.etSize3);
//        type3 = findViewById(R.id.etType3);
//        number3 = findViewById(R.id.etNumber3);
//        actual3 = findViewById(R.id.etActual3);
//        under3 = findViewById(R.id.etUnder13);
//        over3 = findViewById(R.id.etOver31);
//        net3 = findViewById(R.id.etNet3);
//        ice3 = findViewById(R.id.etIce3);
//        ice3.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
//        temp3 = findViewById(R.id.etTemp3);
//        ivSupport = findViewById(R.id.ivSupport);
//        ivNext = findViewById(R.id.ivToCongs);
//        ivBack = findViewById(R.id.ivBackFinalCheckStart);
//        signatureView = findViewById(R.id.signatureView);
//    }
//
//    private void initControlsFromState() {
//        FinalQualityRecord qltRecord = GlobalState.recQualityFinal;
//
//        if (!Strings.isEmptyOrWhitespace(recQualityFinal.sample1.size)) {
//            size1.setText(recQualityFinal.sample1.size);
//        }
//        if (recQualityFinal.sample1.boxType != null) {
//            type1.setText(String.valueOf(recQualityFinal.sample1.boxType));
//        }
//        if (recQualityFinal.sample1.labelPieces != null) {
//            number1.setText(String.valueOf(recQualityFinal.sample1.labelPieces));
//        }
//        if (recQualityFinal.sample1.countedPieces != null) {
//            actual1.setText(String.valueOf(recQualityFinal.sample1.countedPieces));
//        }
//        if (recQualityFinal.sample1.underWeight != null) {
//            under1.setText(String.valueOf(recQualityFinal.sample1.underWeight));
//        }
//        if (recQualityFinal.sample1.overWeight != null) {
//            over1.setText(String.valueOf(recQualityFinal.sample1.overWeight));
//        }
//        if (recQualityFinal.sample1.netWeight != null) {
//            net1.setText(String.valueOf(recQualityFinal.sample1.netWeight));
//        }
//        if (recQualityFinal.sample1.iceQuantity != null) {
//            ice1.setText(String.valueOf(recQualityFinal.sample1.iceQuantity));
//        }
//        if (recQualityFinal.sample1.fishTemp != null) {
//            temp1.setText(String.valueOf(recQualityFinal.sample1.fishTemp));
//        }
//
//
//        if (Strings.isEmptyOrWhitespace(recQualityFinal.sample2.size)) {
//            size2.setText(recQualityFinal.sample2.size);
//        }
//        if (recQualityFinal.sample2.boxType != null) {
//            type2.setText(String.valueOf(recQualityFinal.sample2.boxType));
//        }
//        if (recQualityFinal.sample2.labelPieces != null) {
//            number2.setText(String.valueOf(recQualityFinal.sample2.labelPieces));
//        }
//        if (recQualityFinal.sample2.countedPieces != null) {
//            actual2.setText(String.valueOf(recQualityFinal.sample2.countedPieces));
//        }
//        if (recQualityFinal.sample2.underWeight != null) {
//            under2.setText(String.valueOf(recQualityFinal.sample2.underWeight));
//        }
//        if (recQualityFinal.sample2.overWeight != null) {
//            over2.setText(String.valueOf(recQualityFinal.sample2.overWeight));
//        }
//        if (recQualityFinal.sample2.netWeight != null) {
//            net2.setText(String.valueOf(recQualityFinal.sample2.netWeight));
//        }
//        if (recQualityFinal.sample2.iceQuantity != null) {
//            ice2.setText(String.valueOf(recQualityFinal.sample2.iceQuantity));
//        }
//        if (recQualityFinal.sample2.fishTemp != null) {
//            temp2.setText(String.valueOf(recQualityFinal.sample2.fishTemp));
//        }
//
//
//        if (Strings.isEmptyOrWhitespace(recQualityFinal.sample3.size)) {
//            size3.setText(recQualityFinal.sample3.size);
//        }
//        if (recQualityFinal.sample3.boxType != null) {
//            type3.setText(String.valueOf(recQualityFinal.sample3.boxType));
//        }
//        if (recQualityFinal.sample3.labelPieces != null) {
//            number3.setText(String.valueOf(recQualityFinal.sample3.labelPieces));
//        }
//        if (recQualityFinal.sample3.countedPieces != null) {
//            actual3.setText(String.valueOf(recQualityFinal.sample3.countedPieces));
//        }
//        if (recQualityFinal.sample3.underWeight != null) {
//            under3.setText(String.valueOf(recQualityFinal.sample3.underWeight));
//        }
//        if (recQualityFinal.sample3.overWeight != null) {
//            over3.setText(String.valueOf(recQualityFinal.sample3.overWeight));
//        }
//        if (recQualityFinal.sample3.netWeight != null) {
//            net3.setText(String.valueOf(recQualityFinal.sample3.netWeight));
//        }
//        if (recQualityFinal.sample3.iceQuantity != null) {
//            ice3.setText(String.valueOf(recQualityFinal.sample3.iceQuantity));
//        }
//        if (recQualityFinal.sample3.fishTemp != null) {
//            temp3.setText(String.valueOf(recQualityFinal.sample3.fishTemp));
//        }
//
//    }
//
//
//    private boolean updateState() {
//
//
//        recQualityFinal.sample1.size = Strings.isEmptyOrWhitespace(size1.getText().toString()) ? null : size1.getText().toString();
//        recQualityFinal.sample1.boxType = Strings.isEmptyOrWhitespace(type1.getText().toString()) ? null : Integer.valueOf(type1.getText().toString());
//        recQualityFinal.sample1.labelPieces = Strings.isEmptyOrWhitespace(number1.getText().toString()) ? null : Integer.valueOf(number1.getText().toString());
//        recQualityFinal.sample1.countedPieces = Strings.isEmptyOrWhitespace(actual1.getText().toString()) ? null : Integer.valueOf(actual1.getText().toString());
//        recQualityFinal.sample1.underWeight = Strings.isEmptyOrWhitespace(under1.getText().toString()) ? null : Integer.valueOf(under1.getText().toString());
//        recQualityFinal.sample1.overWeight = Strings.isEmptyOrWhitespace(over1.getText().toString()) ? null : Integer.valueOf(over1.getText().toString());
//        recQualityFinal.sample1.netWeight = Strings.isEmptyOrWhitespace(net1.getText().toString()) ? null : Integer.valueOf(net1.getText().toString());
//        recQualityFinal.sample1.iceQuantity = Strings.isEmptyOrWhitespace(ice1.getText().toString()) ? null : Integer.valueOf(ice1.getText().toString());
//        recQualityFinal.sample1.fishTemp = Strings.isEmptyOrWhitespace(temp1.getText().toString()) ? null : Double.valueOf(temp1.getText().toString());
//
//        recQualityFinal.sample2.size = Strings.isEmptyOrWhitespace(size2.getText().toString()) ? null : size2.getText().toString();
//        recQualityFinal.sample2.boxType = Strings.isEmptyOrWhitespace(type2.getText().toString()) ? null : Integer.valueOf(type2.getText().toString());
//        recQualityFinal.sample2.labelPieces = Strings.isEmptyOrWhitespace(number2.getText().toString()) ? null : Integer.valueOf(number2.getText().toString());
//        recQualityFinal.sample2.countedPieces = Strings.isEmptyOrWhitespace(actual2.getText().toString()) ? null : Integer.valueOf(actual2.getText().toString());
//        recQualityFinal.sample2.underWeight = Strings.isEmptyOrWhitespace(under2.getText().toString()) ? null : Integer.valueOf(under2.getText().toString());
//        recQualityFinal.sample2.overWeight = Strings.isEmptyOrWhitespace(over2.getText().toString()) ? null : Integer.valueOf(over2.getText().toString());
//        recQualityFinal.sample2.netWeight = Strings.isEmptyOrWhitespace(net2.getText().toString()) ? null : Integer.valueOf(net2.getText().toString());
//        recQualityFinal.sample2.iceQuantity = Strings.isEmptyOrWhitespace(ice2.getText().toString()) ? null : Integer.valueOf(ice2.getText().toString());
//        recQualityFinal.sample2.fishTemp = Strings.isEmptyOrWhitespace(temp2.getText().toString()) ? null : Double.valueOf(temp2.getText().toString());
//
//        recQualityFinal.sample3.size = Strings.isEmptyOrWhitespace(size3.getText().toString()) ? null : size3.getText().toString();
//        recQualityFinal.sample3.boxType = Strings.isEmptyOrWhitespace(type3.getText().toString()) ? null : Integer.valueOf(type3.getText().toString());
//        recQualityFinal.sample3.labelPieces = Strings.isEmptyOrWhitespace(number3.getText().toString()) ? null : Integer.valueOf(number3.getText().toString());
//        recQualityFinal.sample3.countedPieces = Strings.isEmptyOrWhitespace(actual3.getText().toString()) ? null : Integer.valueOf(actual3.getText().toString());
//        recQualityFinal.sample3.underWeight = Strings.isEmptyOrWhitespace(under3.getText().toString()) ? null : Integer.valueOf(under3.getText().toString());
//        recQualityFinal.sample3.overWeight = Strings.isEmptyOrWhitespace(over3.getText().toString()) ? null : Integer.valueOf(over3.getText().toString());
//        recQualityFinal.sample3.netWeight = Strings.isEmptyOrWhitespace(net3.getText().toString()) ? null : Integer.valueOf(net3.getText().toString());
//        recQualityFinal.sample3.iceQuantity = Strings.isEmptyOrWhitespace(ice3.getText().toString()) ? null : Integer.valueOf(ice3.getText().toString());
//        recQualityFinal.sample3.fishTemp = Strings.isEmptyOrWhitespace(temp3.getText().toString()) ? null : Double.valueOf(temp3.getText().toString());
//
//
//        recQualityFinal.signature = signatureView.getBitmap();
//        recQualityFinal.signatureBytes = signatureView.getBytes();
//
//        return true;
//    }
//
//    private boolean qualityTxMarkSynced() {
//        try {
//            System.out.println("About to delete quality tx");
//            FinalQualityTransaction delObj = db.finalQualityTransactionDAO().getByLot(recQualityFinal.lot);
//            if (delObj != null) {
//                delObj.isSynced = true;
//                db.finalQualityTransactionDAO().update(delObj);
//                return true;
//            }
//            return false;
//        } catch (Exception x) {
//            x.printStackTrace();
//            return false;
//        }
//    }
//
//
//
//    public class SyncTxCallBack implements Callback<FinalQualityTxDTO> {
//        @Override
//        public void onResponse(Call<FinalQualityTxDTO> call, Response<FinalQualityTxDTO> response) {
//            if (response.isSuccessful() || IsDemo) {
//                qualityTxMarkSynced();
//                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
//            } else {
//                // could not update Processing TX on backend!!!
//                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_postquality_update_failure), Toast.LENGTH_LONG));
//            }
//        }
//
//        @Override
//        public void onFailure(Call<FinalQualityTxDTO> call, Throwable error) {
//            if (error instanceof SocketTimeoutException) {
//                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_connection_timeout), Toast.LENGTH_LONG));
//            } else if (error instanceof IOException) {
//                for (int i = 0; i < 3; i++) {
//                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_saved_local_find_network_and_sync), Toast.LENGTH_LONG));
//                }
//            } else {
//                if (call.isCanceled()) {
//                    //Call was cancelled by user
//                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_cancelled_call), Toast.LENGTH_LONG));
//                } else {
//                    //Generic error handling
//                    runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.general_error + error.getLocalizedMessage()), Toast.LENGTH_LONG));
//                }
//            }
//        }
//    }
//}
