package io.agritrack.fish.ui.quality.afterpackage;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recQuality;
import static io.agritrack.fish.state.GlobalState.recTools;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.agritrack.R;
import io.agritrack.api.APIServiceGenerator;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.SeaTemperatureTxDTO;
import io.agritrack.data.model.tx.SeaTemperatureTransaction;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.quality.QualitySelectStepsActivity;
import io.agritrack.fish.ui.seaTemperature.SeaTemperatureActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AfterPackagingQualityActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private EditText etT1, etT2, etT3;
    private TextView tvCurrentDate;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private MobileDB db;
    private ProgressDialog progressDialog;

    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_packaging_quality);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderAfterPackagingQuality);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(AfterPackagingQualityActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

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

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(AfterPackagingQualityActivity.this);
            supportDialog.showDialog();
        });

        InputFilter filter = new InputFilter() {
            final int maxDigitsBeforeDecimalPoint=2;
            final int maxDigitsAfterDecimalPoint=2;

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source
                        .subSequence(start, end).toString());
                if (!builder.toString().matches(
                        "(([1-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"

                )) {
                    if(source.length()==0)
                        return dest.subSequence(dstart, dend);
                    return "";
                }

                return null;

            }
        };

        etT1.setFilters(new InputFilter[] { filter });
        etT2.setFilters(new InputFilter[] { filter });
        etT3.setFilters(new InputFilter[] { filter });

        tvCurrentDate.setText(Today());

        configFooter();
    }

    private void moveToNextScreen(){
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
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mLastLocation != null) {
                    recQuality.longitude = mLastLocation.getLongitude();
                    recQuality.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(AfterPackagingQualityActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
                }

                // Update state and proceed to next
                Boolean proceed = updateState();
                if (proceed) {
                    // move to next activity.
                    Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
                    startActivity(i);
                }
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToQualityMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), QualitySelectStepsActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        etT1 = findViewById(R.id.etT1);
        etT2 = findViewById(R.id.etT2);
        etT3 = findViewById(R.id.etT3);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private boolean updateState() {

        if (etT1.getText() != null && !Strings.isEmptyOrWhitespace(etT1.getText().toString())) {
            recQuality.etT1 = Double.valueOf(etT1.getText().toString());
        }
        if (etT2.getText() != null && !Strings.isEmptyOrWhitespace(etT2.getText().toString())) {
            recQuality.etT2 = Double.valueOf(etT2.getText().toString());
        }
        if (etT3.getText() != null && !Strings.isEmptyOrWhitespace(etT3.getText().toString())) {
            recQuality.etT3 = Double.valueOf(etT3.getText().toString());
        }
        String v = validate();
        if (!Strings.isEmptyOrWhitespace(v)) {
            CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            return false;
        }

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();

            // persist WHCorrelationTX Record data to local DB.
            SeaTemperatureTransaction tx = GlobalState.commitSeaTemp(db);

            // sync WH Correlation Tx
            Call<SeaTemperatureTxDTO> syncTxAsyncCall = updService.syncSeaTempTx(SeaTemperatureTxDTO.convert(tx), "Bearer " + token);
            syncTxAsyncCall.enqueue(new AfterPackagingQualityActivity.SyncTxCallBack());

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
        if(!IsDemo) {
            if (recQuality.etT1 == null) {
                sb.append(String.format("\n%s is missing", "'T1 temperature'"));
            }

            if (recQuality.etT2 == null) {
                sb.append(String.format("\n%s is missing", "'T2 temperature'"));
            }

            if (recQuality.etT3 == null) {
                sb.append(String.format("\n%s is missing", "'T3 temperature'"));
            }
        }

        return sb.toString();
    }

    public class SyncTxCallBack implements Callback<SeaTemperatureTxDTO> {
        @Override
        public void onResponse(Call<SeaTemperatureTxDTO> call, Response<SeaTemperatureTxDTO> response) {
            SeaTemperatureTxDTO rs = response.body();

            if (rs != null || IsDemo) {
                runOnUiThread(() -> CToast(getApplicationContext(), render("Tx successfully updated!!!"), Toast.LENGTH_LONG));
            } else {
                // could not update Fishing TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_CorrelationTx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<SeaTemperatureTxDTO> call, Throwable error) {
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

    public static String Today() {
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        return currentDateTimeString;
    }
}