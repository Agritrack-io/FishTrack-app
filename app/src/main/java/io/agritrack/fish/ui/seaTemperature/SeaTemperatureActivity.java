package io.agritrack.fish.ui.seaTemperature;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
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
import io.agritrack.dialog.TimeOutProgressDlg;
import io.agritrack.dialog.YesNoDialogFragment;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.transport.TransportSupervisorConfirmActivity;
import io.agritrack.ui.LocationAwareActivity;
import io.agritrack.ui.login.api.TransactionApi;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recTools;
import static io.agritrack.fish.state.GlobalState.recTransport;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class SeaTemperatureActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private EditText etReferenceTemp, etCageTemp;
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
        setContentView(R.layout.activity_sea_temperature);

        // activate GPS location update feature.
        super.findLocation();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelation);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(SeaTemperatureActivity.this);
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
            supportDialog = new SupportDialog(SeaTemperatureActivity.this);
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

        etReferenceTemp.setFilters(new InputFilter[] { filter });
        etCageTemp.setFilters(new InputFilter[] { filter });

        tvCurrentDate.setText(Today());

        showCurrentSite();

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
                    recTools.longitude = mLastLocation.getLongitude();
                    recTools.latitude = mLastLocation.getLatitude();
                } else {
                    CToast(SeaTemperatureActivity.this, "Error: Unable to get Location from GPS", Toast.LENGTH_LONG);
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

        ImageView ivBack = findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        etReferenceTemp = findViewById(R.id.etReferenceTemp);
        etCageTemp = findViewById(R.id.etCageTemp);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private boolean updateState() {

        if (etReferenceTemp.getText() != null && !Strings.isEmptyOrWhitespace(etReferenceTemp.getText().toString())) {
            recTools.referencePointTemp = Double.valueOf(etReferenceTemp.getText().toString());
        }
        if (etCageTemp.getText() != null && !Strings.isEmptyOrWhitespace(etCageTemp.getText().toString())) {
            recTools.cageTemp = Double.valueOf(etCageTemp.getText().toString());
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
            syncTxAsyncCall.enqueue(new SeaTemperatureActivity.SyncTxCallBack());

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
            if (recTools.referencePointTemp == null) {
                sb.append(String.format("\n%s is missing", "'Reference temperature'"));
            }

            if (recTools.cageTemp == null) {
                sb.append(String.format("\n%s is missing", "'Cage temperature'"));
            }
        }

        return sb.toString();
    }

    private void showCurrentSite() {
        final TextView tvCurrentSite = findViewById(R.id.tvCurrentSite);
        tvCurrentSite.setText(LocalPreferences.getCurrentSiteName());
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }
}