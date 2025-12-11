package io.agritrack.philosofish.fish.ui.transport;

import static io.agritrack.philosofish.FishTrackApplication.IsDemo;
import static io.agritrack.philosofish.FishTrackApplication.IsOnline;
import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recTransport;
import static io.agritrack.philosofish.ui.custom.CustomToast.CToast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.common.MediaDTO;
import io.agritrack.philosofish.data.dto.tx.TransportTxDTO;
import io.agritrack.philosofish.data.model.tx.TransportTransaction;
import io.agritrack.philosofish.dialog.ScanQrDialog;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.fish.state.TransportationRecord;
import io.agritrack.philosofish.ui.LocationAwareActivity;
import io.agritrack.philosofish.ui.service.AuthenticationService;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransportSupervisorConfirmActivity extends LocationAwareActivity {

    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmGPSSelectionDlg;

    private TextView tvSitePackaging, tvNumberOfBinsCount, tvDriverName, tvLicensePlate, tvSecurityClipNumber;
    private TextView tvUsername;
    private ProgressDialog progressDialog;
    private EditText etPIN;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;
    private ScanQrDialog scanQrDialog;

    private Bitmap bitmap;
    //private QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_supervisor_confirm);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderTransportSupervisorConfirm);
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
        progressDialog = new ProgressDialog(TransportSupervisorConfirmActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(TransportSupervisorConfirmActivity.this);
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
                generateQrCode();
            }
        }
    }

    protected void configFooter() {
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etPIN.getText().toString())) {
                    CToast(TransportSupervisorConfirmActivity.this, render(R.string.missing_pin), Toast.LENGTH_LONG);
                    return;
                }
                boolean userIsValid = isAuthenticated();
                if (!userIsValid) {
                    CToast(TransportSupervisorConfirmActivity.this, render(R.string.invalid_password), Toast.LENGTH_LONG);
                    return;
                } else if (mLastLocation != null) {
                    recTransport.longitude = mLastLocation.getLongitude();
                    recTransport.latitude = mLastLocation.getLatitude();
                    proceedWithoutLocation = true;
                    moveToNextScreen();
                } else if (!proceedWithoutLocation) {
                    FragmentManager fm = getSupportFragmentManager();
                    confirmGPSSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                }
            }
        });

        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TransportDriverConfirmActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        tvSitePackaging = findViewById(R.id.tvSitePackaging);
        tvNumberOfBinsCount = findViewById(R.id.tvNumberOfBinsCount);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvSecurityClipNumber = findViewById(R.id.tvSecurityClipNumber);
        tvUsername = findViewById(R.id.tvUsername);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToCongs);
        ivBack = findViewById(R.id.ivBackToDriverConfirm);
        etPIN = findViewById(R.id.etPasswordTransport);
    }

    private void initControlsFromState() {
        TransportationRecord trns = GlobalState.recTransport;

        if (!Strings.isEmptyOrWhitespace(trns.packagingSite)) {
            tvSitePackaging.setText(trns.packagingSite);
        }

        if (trns.availBins != null) {
            tvNumberOfBinsCount.setText(String.valueOf(trns.availBins.size()));
        }

        if (!Strings.isEmptyOrWhitespace(trns.driverName)) {
            tvDriverName.setText(trns.driverName);
        }

        if (!Strings.isEmptyOrWhitespace(trns.licensePlate)) {
            tvLicensePlate.setText(trns.licensePlate);
        }

        if (!Strings.isEmptyOrWhitespace(trns.clipNumber)) {
            tvSecurityClipNumber.setText(trns.clipNumber);
        }

        tvUsername.setText(LocalPreferences.getLoggedInUser("").trim());
    }

    private boolean isAuthenticated() {
        String login = LocalPreferences.getLoggedInUser("").trim();
        String pin = etPIN.getText().toString().trim();

        // use typed-in PIN to compare credentials with those stored in the Local DB.
        AuthenticationService authSvc = new AuthenticationService();
        boolean authentication = authSvc.authenticateUser(this.db, login, pin);

        return authentication;
    }

    private void generateQrCode() {
        if (TextUtils.isEmpty(recTransport.driverName) || TextUtils.isEmpty(recTransport.clipNumber)
                || TextUtils.isEmpty(recTransport.licensePlate) || TextUtils.isEmpty(recTransport.packagingSite)) {

            // if the edittext inputs are empty then execute
            // this method showing a toast message.
            runOnUiThread(() -> CToast(getApplicationContext(), render("Some info is missing to generate QR Code"), Toast.LENGTH_LONG));
        } else {
            // below line is for getting
            // the windowmanager service.
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // initializing a variable for default display.
            Display display = manager.getDefaultDisplay();

            // creating a variable for point which
            // is to be displayed in QR Code.
            Point point = new Point();
            display.getSize(point);

            // getting width and
            // height of a point
            int width = point.x;
            int height = point.y;

            // generating dimension from width and height.
            int dimen = width < height ? width : height;
            dimen = dimen * 3 / 4;

            String message = null;
            try {
                JSONObject json = new JSONObject();
                json.put("driver_name", recTransport.driverName);
                json.put("security_clip_number", recTransport.clipNumber);
                json.put("license_plate", recTransport.licensePlate);
                json.put("packaging_site", recTransport.packagingSite);
                json.put("farm", LocalPreferences.getCurrentSiteName());
                json.put("date", LocalDateTime.now().toString());

                message = json.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // setting this dimensions inside our qr code
            // encoder to generate our qr code.
            //qrgEncoder = new QRGEncoder(message, null, QRGContents.Type.TEXT, dimen);
            try {
                // getting our qrcode in the form of bitmap.
                //bitmap = qrgEncoder.encodeAsBitmap();
                // the bitmap is set inside our image
                // view using .setimagebitmap method.
                scanQrDialog = new ScanQrDialog(TransportSupervisorConfirmActivity.this, bitmap);
                scanQrDialog.showDialog();
            } catch (Exception e) {
                // this method is called for
                // exception handling.
                Log.e("Tag", e.toString());
            }
        }
    }

    private boolean updateState() {
        try {
            progressDialog.setCancelable(false);
            progressDialog.setMessage(render("Synchronizing data..."));
            progressDialog.show();

            String token = LocalPreferences.getToken();
            //runOnUiThread(() -> loadingText.setText(R.string.syncing_routes));

            // persist Transportation Record data to local DB.
            TransportTransaction tx = GlobalState.commitTransport(db);

            if (IsOnline) {
                // send signature
                Call<MediaDTO> syncDriverSigAsyncCall = updService.syncTransportTxDriverSignature(MediaDTO.convert(tx), "Bearer " + token);
                syncDriverSigAsyncCall.enqueue(new Callback<MediaDTO>() {
                    @Override
                    public void onResponse(Call<MediaDTO> call, Response<MediaDTO> response) {

                    }

                    @Override
                    public void onFailure(Call<MediaDTO> call, Throwable t) {

                    }
                });
                // sync fish species
                Call<TransportTxDTO> syncTxAsyncCall = updService.syncTransportTx(TransportTxDTO.convert(tx), "Bearer " + token);
                syncTxAsyncCall.enqueue(new SyncTxCallBack());
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

    private void deleteTransportTx() {
        try {
            List<TransportTransaction> transportTxs = db.transportTransactionDAO().getAllByHash(recTransport.hashCode);
            for (TransportTransaction tx : transportTxs) {
                db.transportTransactionDAO().delete(tx);
            }
                /*int count = db.transportTransactionDAO().deleteAllByHash(recTransport.hashCode);
                System.out.println("About to delete transport tx" + count);*/
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public class SyncTxCallBack implements Callback<TransportTxDTO> {
        @Override
        public void onResponse(Call<TransportTxDTO> call, Response<TransportTxDTO> response) {
            if (response.isSuccessful() || IsDemo) {
                deleteTransportTx();
                //                runOnUiThread(TransportSupervisorConfirmActivity.this::generateQrCode);
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.tx_successfully_updated), Toast.LENGTH_SHORT));
            } else {
                // could not update Transport TX on backend!!!
                runOnUiThread(() -> CToast(getApplicationContext(), render(R.string.error_transport_tx_update_failure), Toast.LENGTH_LONG));
            }
        }

        @Override
        public void onFailure(Call<TransportTxDTO> call, Throwable error) {
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
