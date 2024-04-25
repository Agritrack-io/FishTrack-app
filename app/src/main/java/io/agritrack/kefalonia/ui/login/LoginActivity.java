package io.agritrack.kefalonia.ui.login;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.common.LargeString.render;
import static io.agritrack.kefalonia.crypto.CryptoLicense.isLicenseKeyValid;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;
import static io.agritrack.kefalonia.ui.service.LocalPreferences.Logged_In_User_Key;
import static io.agritrack.kefalonia.ui.service.LocalPreferences.SelectedSiteName_Key;
import static io.agritrack.kefalonia.ui.service.LocalPreferences.Token_Key;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.agritrack.api.sync.EncodingSchemeCallBack;
import io.agritrack.kefalonia.api.sync.PendingBinInfoTxCallBack;
import io.agritrack.kefalonia.data.model.BinInfo;
import io.agritrack.kefalonia.fish.ui.FishHomeActivity;
import io.agritrack.kefalonia.AgritrackProducts;
import io.agritrack.kefalonia.FishTrackApplication;
import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.login.AuthApi;
import io.agritrack.kefalonia.api.sync.PendindQualityMeasurementsTxCallBack;
import io.agritrack.kefalonia.api.sync.PendingCorrelationTxCallBack;
import io.agritrack.kefalonia.api.sync.PendingFishingTxCallBack;
import io.agritrack.kefalonia.api.sync.PendingProcessTxCallBack;
import io.agritrack.kefalonia.api.sync.PendingQualityTxCallBack;
import io.agritrack.kefalonia.api.sync.SyncApi;
import io.agritrack.kefalonia.api.sync.SyncAssetsCallBack;
import io.agritrack.kefalonia.api.sync.SyncBinInfo;
import io.agritrack.kefalonia.api.sync.SyncCageDetailsCallBack;
import io.agritrack.kefalonia.api.sync.SyncClusterSitesCallBack;
import io.agritrack.kefalonia.api.sync.SyncCustomersCallBack;
import io.agritrack.kefalonia.api.sync.SyncEmployeesCallBack;
import io.agritrack.kefalonia.api.sync.SyncFishingRequestCallBack;
import io.agritrack.kefalonia.api.sync.SyncIOTLoggersCallBack;
import io.agritrack.kefalonia.api.sync.SyncSpeciesCallBack;
import io.agritrack.kefalonia.api.sync.SyncSuppliersCallBack;
import io.agritrack.kefalonia.api.sync.SyncUsersCallBack;
import io.agritrack.kefalonia.api.tx.TransactionApi;
import io.agritrack.kefalonia.common.DeviceUtils;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.AppUserDTO;
import io.agritrack.kefalonia.data.dto.BinInfoDTO;
import io.agritrack.kefalonia.data.dto.CageDetailsDTO;
import io.agritrack.kefalonia.data.dto.EncodingSchemeDTO;
import io.agritrack.kefalonia.data.dto.FishingRequestDTO;
import io.agritrack.kefalonia.data.dto.SiteDTO;
import io.agritrack.kefalonia.data.dto.common.CustomerDTO;
import io.agritrack.kefalonia.data.dto.common.EmployeeDTO;
import io.agritrack.kefalonia.data.dto.common.IotLoggerDTO;
import io.agritrack.kefalonia.data.dto.common.SpeciesDTO;
import io.agritrack.kefalonia.data.dto.common.SupplierDTO;
import io.agritrack.kefalonia.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.kefalonia.data.dto.tx.CorrelationTxDTO;
import io.agritrack.kefalonia.data.dto.tx.FishingTxDTO;
import io.agritrack.kefalonia.data.dto.tx.ProcessingTxDTO;
import io.agritrack.kefalonia.data.dto.tx.QualityTxDTO;
import io.agritrack.kefalonia.data.dto.wh.AssetDTO;
import io.agritrack.kefalonia.data.model.AppUser;
import io.agritrack.kefalonia.data.model.common.TemperatureTimeSeries;
import io.agritrack.kefalonia.data.model.tx.CorrelationTransaction;
import io.agritrack.kefalonia.data.model.tx.FishingTransaction;
import io.agritrack.kefalonia.data.model.tx.ProcessingTransaction;
import io.agritrack.kefalonia.data.model.tx.QualityTransaction;
import io.agritrack.kefalonia.dialog.YesNoDialogFragment;
import io.agritrack.kefalonia.settings.ApplicationSettings;
import io.agritrack.kefalonia.settings.EncryptedSharedPreferences;
import io.agritrack.kefalonia.settings.SettingsActivity;
import io.agritrack.kefalonia.su.AppOptionsFragment;
import io.agritrack.kefalonia.ui.login.api.AuthInfoRS;
import io.agritrack.kefalonia.ui.login.api.LoginRQ;
import io.agritrack.kefalonia.ui.service.AuthenticationService;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import io.agritrack.kefalonia.ui.tools.CAENLoggerActivity;
import io.agritrack.kefalonia.ui.viewmodel.ConfigViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean shouldCheckLicense = true;
    private static boolean isFirstLoad = true;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private MobileDB db;
    private YesNoDialogFragment confirmOfflineProcess;
    private ImageButton ibLocale;
    private ProgressDialog progressDialog;
    private int syncCounter = 1;
    private EditText etUserName, etPassword;
    private TextView tvInvalidLicense, tvForgotYourPassword, tvLoginWithCred;
    private EncryptedSharedPreferences pref;

    public static boolean checkAndRequestPermissions(final Activity context) {
        int extStorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        int locationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (extStorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get  references of the controls
        assignCtrlVars();

        // bind the flags button
        ibLocale = findViewById(R.id.ibLocale);

        checkAndRequestPermissions(this);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // init app
        Executors.newSingleThreadExecutor().execute(() -> {

            if (isFirstLoad) {
                // load app configuration
                ApplicationSettings.loadSettings(getApplicationContext());
                isFirstLoad = false;
            }

            // check device license
            pref = new EncryptedSharedPreferences(this);
            String deviceID = DeviceUtils.getIMEIDeviceId(this);
            String encrypted = pref.loadPreference("licenseKey");

            List<String> userRoles = new ArrayList<String>();

            if (shouldCheckLicense && (Strings.isEmptyOrWhitespace(encrypted) || !isLicenseKeyValid(encrypted, deviceID))) {
                userRoles.add("NO_ACCESS");
                runOnUiThread(() -> {
                    showCtrlVars(false);
                    tvInvalidLicense.setVisibility(View.VISIBLE);
                    tvInvalidLicense.setText(R.string.invalid_license);
                });
            } else {
                showCtrlVars(true);
                String siteName = pref.loadPreference("centralSite");
                // persist selected Site to local Preferences.
                LocalPreferences.writeValue(SelectedSiteName_Key, siteName);
                // show current Site
                showCurrentSite();
                userRoles.add("ROLE_WAREHOUSE");
                shouldCheckLicense = false;
            }
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        confirmOfflineProcess = YesNoDialogFragment.instance();
        confirmOfflineProcess.onConfirm(bundle -> {
            FishTrackApplication.IsOnline = false;
            AppUser user = db.userDAO().getByUsername(etUserName.getText().toString().trim());
            runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(user.username, null, user.roles))));
        });
        confirmOfflineProcess.onReject(bundle -> {
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.change_position_to_find_network_coverage)));
        });
        confirmOfflineProcess.setCancelable(false);

        // show previous loggeding user name
        String previousLoggedInUser = LocalPreferences.getLoggedInUser(null);
        if (previousLoggedInUser != null) {
            etUserName.setText(previousLoggedInUser);
        }

        // check last login timestamp, to determine whether synch is required.
        long diffHours = LocalPreferences.getLoginDiffInHours();

        // validate Security Token
        // -------------------------------
        boolean jwtIsValid = validateJwtToken(LocalPreferences.getToken());


        // -------------------------------

        // if last login occurred < 2 hours ?? ago, no further login is required.
        if (diffHours < 2 && jwtIsValid) {
//            LocalPreferences.writeValue(Token_Key, model.getToken());
            goToProductMenu();
        } else {
            final TextView tvForgotYourPassword = findViewById(R.id.tvForgotPasswordText);
            final Button btLogin = findViewById(R.id.btnLogin);

            loginResult.observe(this, response -> {
                if (response == null) {
                    toggleProgress(Boolean.FALSE, R.string.empty);
                    return;
                }
                if (response.getError() != null) {
                    showLoginFailed(response.getError());
                    toggleProgress(Boolean.FALSE, R.string.empty);
                }
                if (response.getSuccess() != null) {
                    toggleProgress(Boolean.FALSE, R.string.empty);
                    updateUiWithUser(response.getSuccess());
                }
            });

            syncResult.observe(this, response -> {
                syncCounter++;
                if (response == null) {
                    toggleProgress(Boolean.FALSE, R.string.empty);
                    return;
                }
                if (response != null) {
                    if (syncCounter > 6) {
                        toggleProgress(Boolean.FALSE, R.string.empty);
                    }
                }
            });

            etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        //Clear focus here from edittext
                        etPassword.clearFocus();
                    }
                    return false;
                }
            });

            etPassword.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable editable) {
                    // get credential string values
                    final String username = etUserName.getText().toString().trim();
                    final String pin = editable.toString().trim();

                    if (username.isEmpty()) {
                        CToast(getApplicationContext(), render(R.string.empty_username_alert), Toast.LENGTH_LONG);
                    } else if (pin.isEmpty()) {
                        noCredentialsEnteredAlert();
                    } else if ("caen".equals(username) && "8888".equals(pin)) {
                        Intent i = new Intent(getApplicationContext(), CAENLoggerActivity.class);
                        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
                        startActivity(i);
                        finish();
                    } else if ("root".equals(username) && "9999".equals(pin)) {
                        FragmentManager fm = getSupportFragmentManager();
                        AppOptionsFragment optionsDlg = AppOptionsFragment.newInstance();
                        optionsDlg.show(fm, AppOptionsFragment.TAG);
                        fm.executePendingTransactions();
                    } else if (editable != null && editable.length() == 4) {
                        // display spinning progress bar
                        toggleProgress(Boolean.TRUE, R.string.authenticating);

                        // invoke login
                        invokeLogin(username, pin);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });
        }

        // Tap PoweredByLogo to reset LocalSharedPreferences
        tapLogoToResetPreferences();

        // Tap Logo to access Settings activity
        tapLogoToAccessSettingsActivity();

        // SETTINGS ACCESS
        findViewById(R.id.ivPoweredByLogo).setOnClickListener(v -> {
            loadAndSaveConfig();
        });

        // show current Site
        showCurrentSite();

        ibLocale.setOnClickListener(view -> {
            String _lang = LocalPreferences.getLocale();
            String code = _lang;
            if ("EN".equalsIgnoreCase(_lang)) {
                code = "el";
            } else if ("EL".equalsIgnoreCase(_lang)) {
                code = "es";
            } else if ("ES".equalsIgnoreCase(_lang)) {
                code = "en";
            }
            drawFlag(code);
            applyLocale(code);
            LoginActivity.this.recreate();
        });
    }

    private void assignCtrlVars() {
        // bind the credentials controls
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        tvForgotYourPassword = findViewById(R.id.tvForgotPasswordText);
        tvLoginWithCred = findViewById(R.id.textView2);
        tvInvalidLicense = findViewById(R.id.tvInvalidLicense);
    }

    private void showCtrlVars(boolean show) {
        if (!show) {
            etUserName.setVisibility(View.INVISIBLE);
            etPassword.setVisibility(View.INVISIBLE);
            tvForgotYourPassword.setVisibility(View.INVISIBLE);
            tvLoginWithCred.setVisibility(View.INVISIBLE);
            tvInvalidLicense.setVisibility(View.VISIBLE);
        } else {
            etUserName.setVisibility(View.VISIBLE);
            etPassword.setVisibility(View.VISIBLE);
            tvForgotYourPassword.setVisibility(View.VISIBLE);
            tvLoginWithCred.setVisibility(View.VISIBLE);
            tvInvalidLicense.setVisibility(View.INVISIBLE);
        }
    }

    private void confirmAccessToSettings() {
        // Store the created AlertDialog instance.
        // Because only AlertDialog has cancel method.
        AlertDialog alertDialog = null;

        // Create a alert dialog builder.
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

        // Set title value.
        builder.setTitle(R.string.type_pin_for_accessing_settings);

        // Get custom login form view.
        final View accessDeprogramFormView = getLayoutInflater().inflate(R.layout.type_pin_for_accessing_deprogram, null);

        // assign variables to ui controls.
        final EditText etPin = accessDeprogramFormView.findViewById(R.id.etPin);

        // Set above view in alert dialog.
        builder.setView(accessDeprogramFormView);

        // Register button click listener.
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            String insertedPin = etPin.getText().toString().trim();

            if (TextUtils.isEmpty(insertedPin)) {
                CToast(getAppContext(), render(R.string.missing_pin), Toast.LENGTH_LONG);
                return;
            }

            if ("8888".equals(insertedPin)) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                dialog.dismiss();
            } else {
                CToast(getAppContext(), render(R.string.invalid_password), Toast.LENGTH_LONG);
                return;
            }
        });

        // Reset button click listener.
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            // Close Alert Dialog.
            dialog.cancel();
        });

        builder.setCancelable(true);
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void loadAndSaveConfig() {
        ConfigViewModel appSettingsViewModel = new ConfigViewModel(this);
        appSettingsViewModel.loadConfig();
        appSettingsViewModel.saveConfig();
        String siteName = pref.loadPreference("centralSite");
        // persist selected Site to local Preferences.
        LocalPreferences.writeValue(SelectedSiteName_Key, siteName);
        // show current Site
        showCurrentSite();
        showCtrlVars(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                                    "FishTrack Requires Access to Camara.", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FishTrack Requires Access to Your Storage.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FishTrack Requires Access to Your Location.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
//                    doWork();
                }
                break;
        }
    }

    private void noCredentialsEnteredAlert() {
        CToast(getApplicationContext(), render(R.string.empty_credentials_alert), Toast.LENGTH_LONG);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        LocalPreferences.writeValue(Token_Key, model.getToken());
        LocalPreferences.writeValue(Logged_In_User_Key, model.getUsername());
        LocalPreferences.setUserRoles(model.getRoles());
        LocalPreferences.updateLoginTime();

        boolean shouldSync = true; //LocalPreferences.shouldSync(Boolean.TRUE);
        if (!shouldSync) {
            goToProductMenu();
        } else {
            // display spinning progress bar
            toggleProgress(Boolean.TRUE, R.string.syncing);

            SyncAllTask syncAllTask = new SyncAllTask();
            syncAllTask.execute();
        }
    }

    private void showLoginFailed(String errorString) {
        CToast(getApplicationContext(), render(errorString), Toast.LENGTH_SHORT);
    }

    // show Progress bar
    private void toggleProgress(boolean show, @StringRes int info) {
        if (show) {
            this.progressDialog.setMessage(getText(info).toString());
            this.progressDialog.show();
        } else {
            this.progressDialog.hide();
        }
    }

    private void invokeLogin(String username, String pin) {
        try {
            long hoursSinceLastLogin = LocalPreferences.getLoginDiffInHours();

            // query local db for previous User authentications...
            AuthenticationService userService = new AuthenticationService();
            boolean authenticatedUser = userService.authenticateUser(db, username, pin);

            if (hoursSinceLastLogin <= 2 && authenticatedUser) {
                LocalPreferences.updateLoginTime();
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(username, LocalPreferences.getToken(), LocalPreferences.getUserRoles()))));
            } else {
                AuthApi authService = APIServiceGenerator.createAPI(AuthApi.class);
                LoginRQ loginRQ = new LoginRQ(username, pin);
                Call<AuthInfoRS> authAsyncCall = authService.login(loginRQ);
                authAsyncCall.enqueue(new AuthLoginCallBack(loginRQ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            toggleProgress(Boolean.FALSE, R.string.empty);
        }
    }

    private void invokeSyncAll() {
        try {
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            UUID siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync harvestRequests for current Site
            Call<List<FishingRequestDTO>> syncHarvestResAsyncCall = syncService.getFishingRequestsBySiteId(siteId, "Bearer " + token);
            syncHarvestResAsyncCall.enqueue(new SyncFishingRequestCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack(this.syncResult));

            // sync suppliers
            Call<List<SupplierDTO>> syncSuppliersAsyncCall = syncService.getAllSuppliers("Bearer " + token);
            syncSuppliersAsyncCall.enqueue(new SyncSuppliersCallBack(this.syncResult));

            // sync customers
            Call<List<CustomerDTO>> syncCustomersAsyncCall = syncService.getCustomersBySiteId(siteId, "Bearer " + token);
            syncCustomersAsyncCall.enqueue(new SyncCustomersCallBack(this.syncResult));

            //sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync Bin Info (complete BinLedger)
            Call<List<BinInfoDTO>> syncBinsByPlantAsyncCall = syncService.getCompleteBinLedger("Bearer " + token);
            syncBinsByPlantAsyncCall.enqueue(new SyncBinInfo(this.syncResult));

            // sync fish species
            Call<List<SpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCodeAndType(FishTrackApplication.COUNTRY, FishTrackApplication.getProduct(), "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

            // sync IOT Loggers
            Call<List<IotLoggerDTO>> syncIOTLoggersAsyncCall = syncService.getIOTLoggersBySiteId(siteId, "Bearer " + token);
            syncIOTLoggersAsyncCall.enqueue(new SyncIOTLoggersCallBack(this.syncResult));

            // sync Encoding scheme info
            Call<List<EncodingSchemeDTO>> syncEncodingShemeAsyncCall = syncService.getEncodingScheme("Bearer " + token);
            syncEncodingShemeAsyncCall.enqueue(new EncodingSchemeCallBack(this.syncResult));

            goToProductMenu();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeUploadPendingAll() {
        try {
            TransactionApi pendingTxSvc = APIServiceGenerator.createAPI(TransactionApi.class);
            String token = LocalPreferences.getToken();

            // select all pending binInfos TXs
            List<BinInfo> binInfoTXs = db.binInfoDAO().getAll();
            if (!binInfoTXs.isEmpty()) {
                Call<List<BinInfoDTO>> binInfoTxAsyncCall = pendingTxSvc.syncBinInfoTx(BinInfoDTO.convert(binInfoTXs), "Bearer " + token);
                binInfoTxAsyncCall.enqueue(new PendingBinInfoTxCallBack(this.syncResult));
            }

            // select all pending fishing TXs
            List<FishingTransaction> fishingTXs = db.fishingTransactionDAO().getAllCompleted();
            if (!fishingTXs.isEmpty()) {
                for (FishingTransaction fishingTX : fishingTXs) {
                    Call<FishingTxDTO> fishingTxAsyncCall = pendingTxSvc.syncFishingTx(FishingTxDTO.convert(fishingTX), "Bearer " + token);
                    fishingTxAsyncCall.enqueue(new PendingFishingTxCallBack(this.syncResult));
                }
            }

            // select all pending receipt TXs
            List<ProcessingTransaction> processTXs = db.processingTransactionDAO().getAll();
            if (!processTXs.isEmpty()) {
                for (ProcessingTransaction processTX : processTXs) {
                    Call<ProcessingTxDTO> processTxAsyncCall = pendingTxSvc.syncProcessingTx(ProcessingTxDTO.convert(processTX), "Bearer " + token);
                    processTxAsyncCall.enqueue(new PendingProcessTxCallBack(this.syncResult));
                }
            }

            // select all pending quality TXs
            List<QualityTransaction> qualityTXs = db.qualityTransactionDAO().getAll();
            if (!qualityTXs.isEmpty()) {
                for (QualityTransaction qualityTX : qualityTXs) {
                    Call<QualityTxDTO> qualityTxAsyncCall = pendingTxSvc.syncQualityTx(QualityTxDTO.convert(qualityTX), "Bearer " + token);
                    qualityTxAsyncCall.enqueue(new PendingQualityTxCallBack(this.syncResult));
                }
            }

            // select all pending post quality TXs
            List<TemperatureTimeSeriesDTO> temperatureTimeSeriesDTOs = new ArrayList<>();
            for (TemperatureTimeSeries ts : db.measurementsDAO().getAll()) {
                temperatureTimeSeriesDTOs.add(TemperatureTimeSeriesDTO.convert(ts));
            }

            if (!temperatureTimeSeriesDTOs.isEmpty()) {
                Call<List<TemperatureTimeSeriesDTO>> syncMsAsyncCall = pendingTxSvc.syncMeasurements(temperatureTimeSeriesDTOs, "Bearer " + token);
                syncMsAsyncCall.enqueue(new PendindQualityMeasurementsTxCallBack(this.syncResult));
            }

            //===================================================================================================
            // select all SIMPLE pending correlation TXs (identification events, e.g. correlate rfid <--> code)
            List<CorrelationTransaction> correlationTXs = db.correlationTransactionDAO().getAll();
            if (!correlationTXs.isEmpty()) {
                // filter out the simple correlation transactions
                List<CorrelationTransaction> identifications = correlationTXs.stream().filter(f -> f.assetRFID == null).collect(Collectors.toList());
                // filter out the inter-correlation transactions
                List<CorrelationTransaction> interCorrelations = correlationTXs.stream().filter(f -> f.assetRFID != null).collect(Collectors.toList());

                if (!identifications.isEmpty()) {
                    List<CorrelationTxDTO> identificationDTOs = identifications.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetIdentificationAsyncCall = pendingTxSvc.syncAssetCorrelationTx(identificationDTOs, "Bearer " + token);
                    assetIdentificationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }

                if (!interCorrelations.isEmpty()) {
                    List<CorrelationTxDTO> interCorrelationDTOs = interCorrelations.stream().map(tx -> CorrelationTxDTO.convert(tx)).collect(Collectors.toList());

                    Call<ResponseBody> assetInterCorrelationAsyncCall = pendingTxSvc.syncAssetWithAssetCorrelationTx(interCorrelationDTOs, "Bearer " + token);
                    assetInterCorrelationAsyncCall.enqueue(new PendingCorrelationTxCallBack(this.syncResult));
                }
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private void goToProductMenu() {
        if (AgritrackProducts.FISH.name().equalsIgnoreCase(FishTrackApplication.getProduct())) {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            i.putExtra("syncErrors", this.syncResult.toString());
            startActivity(i);
        }
    }

    private void showCurrentSite() {
        final TextView tvCurrentSite = findViewById(R.id.tvCurrentSite);
        tvCurrentSite.setText(LocalPreferences.getCurrentSiteName());
    }

    private void drawFlag(String _locale) {
        if ("EN".equalsIgnoreCase(_locale)) {
            ibLocale.setImageDrawable(getDrawable(R.drawable.flag_great_britain));
        } else if ("EL".equalsIgnoreCase(_locale)) {
            ibLocale.setImageDrawable(getDrawable(R.drawable.flag_greece));
        } else if ("ES".equalsIgnoreCase(_locale)) {
            ibLocale.setImageDrawable(getDrawable(R.drawable.flag_spain));
        }
    }

    private void applyLocale(String lang) {
        LocalPreferences.writeValue(LocalPreferences.Locale_Key, lang);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(config);
        } else {
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (this.progressDialog != null)
            this.progressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /**
     * Tap 6 times on PoweredByLogo to access Settings activity
     */
    private void tapLogoToAccessSettingsActivity() {
        final ImageView ivLogo = findViewById(R.id.ivLogoLogin);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            long lastTap = System.currentTimeMillis();
            int taps = 0;

            @Override
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                taps = (now - lastTap > 1500) ? 0 : taps;
                taps++;
                if (taps == 6) {
                    confirmAccessToSettings();
                    taps = 0;
                }
                lastTap = now;
            }
        });
    }

    /**
     * Tap 6 times on PoweredByLogo to clear LocalSharedPreferences
     */
    private void tapLogoToResetPreferences() {
        final TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setOnClickListener(new View.OnClickListener() {
            long lastTap = System.currentTimeMillis();
            int taps = 0;

            @Override
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                taps = (now - lastTap > 1500) ? 0 : taps;
                taps++;
                if (taps == 6) {
                    LocalPreferences.Reset();
                    EncryptedSharedPreferences.Reset();
                    taps = 0;
                    CToast(getApplicationContext(), render("Preferences Reset!!!"), Toast.LENGTH_SHORT);
                }
                lastTap = now;
            }
        });
    }

    public boolean validateJwtToken(String authToken) {
        try {
            DecodedJWT jwt = JWT.decode(authToken);
            if (jwt.getExpiresAt().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("Invalid JWT: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // show previous loggeding user name
        String previousLoggedInUser1 = LocalPreferences.getLoggedInUser(null);
        if (previousLoggedInUser1 != null && !"root".equalsIgnoreCase(previousLoggedInUser1)) {
            etUserName.setText(previousLoggedInUser1);
        } else if (previousLoggedInUser1 == null || "root".equalsIgnoreCase(previousLoggedInUser1)) {
            etUserName.setText("");
        }
        etPassword.setText("");
    }

    // ##########################
    public class AuthLoginCallBack implements Callback<AuthInfoRS> {
        private final String userName;

        public AuthLoginCallBack(LoginRQ loginRQ) {
            userName = loginRQ.getUsername();
        }

        @Override
        public void onResponse(Call<AuthInfoRS> call, Response<AuthInfoRS> response) {
            AuthInfoRS rs = response.body();

            if (rs != null) {
                FishTrackApplication.IsOnline = true;
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(this.userName, rs.getToken(), rs.getRoles()))));
                LocalPreferences.updateLoginTime();
            } else {
                // Probably Invalid Credentials
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.login_failed)));
            }
        }

        @Override
        public void onFailure(Call<AuthInfoRS> call, Throwable error) {
            // Probably Network Communication Error
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.error_connection_timeout)));
            } else if (error instanceof IOException) {
                FragmentManager fm = getSupportFragmentManager();
                confirmOfflineProcess.setMessage(getString(R.string.proceed_without_network));
                confirmOfflineProcess.showNow(fm, getString(R.string.confirm_selection));
            } else {
                if (call.isCanceled()) {
                    //Call was cancelled by user
                    runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.error_cancelled_call)));
                } else {
                    //Generic error handling
                    runOnUiThread(() -> loginResult.setValue(new LoginResult("Network Error :: " + error.getLocalizedMessage())));
                }
            }
        }
    }

    private class SyncAllTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            invokeSyncAll();
            invokeUploadPendingAll();
            SyncApi syncService = APIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            Call<List<AssetDTO>> syncNetsAsyncCall = syncService.getAssetsByNetType("Bearer " + token);
            syncNetsAsyncCall.enqueue(new SyncAssetsCallBack(syncResult));
            // sync only Cages assets
            Call<List<AssetDTO>> syncCagesAsyncCall = syncService.getAssetsByCageType("Bearer " + token);
            syncCagesAsyncCall.enqueue(new SyncAssetsCallBack(syncResult));
            return null;
        }
    }
}