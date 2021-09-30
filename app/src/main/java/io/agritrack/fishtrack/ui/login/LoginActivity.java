package io.agritrack.fishtrack.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.aseem.versatileprogressbar.ProgBar;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Locale;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.APIServiceGenerator;
import io.agritrack.fishtrack.api.sync.SyncAssetsCallBack;
import io.agritrack.fishtrack.api.sync.SyncCageDetailsCallBack;
import io.agritrack.fishtrack.api.sync.SyncClusterSitesCallBack;
import io.agritrack.fishtrack.api.sync.SyncEmployeesCallBack;
import io.agritrack.fishtrack.api.sync.SyncHarvestRequestCallBack;
import io.agritrack.fishtrack.api.sync.SyncSpeciesCallBack;
import io.agritrack.fishtrack.api.sync.SyncSuppliersCallBack;
import io.agritrack.fishtrack.api.sync.SyncUsersCallBack;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.AppUserDTO;
import io.agritrack.fishtrack.data.dto.CageDetailsDTO;
import io.agritrack.fishtrack.data.dto.HarvestRequestDTO;
import io.agritrack.fishtrack.data.dto.SiteDTO;
import io.agritrack.fishtrack.data.dto.common.EmployeeDTO;
import io.agritrack.fishtrack.data.dto.common.FishSpeciesDTO;
import io.agritrack.fishtrack.data.dto.common.SupplierDTO;
import io.agritrack.fishtrack.data.dto.wh.AssetDTO;
import io.agritrack.fishtrack.ui.HomeActivity;
import io.agritrack.fishtrack.ui.config.ConfigActivity;
import io.agritrack.fishtrack.ui.fishing.FishingConfirmActivity;
import io.agritrack.fishtrack.ui.login.api.AuthApi;
import io.agritrack.fishtrack.ui.login.api.AuthInfo;
import io.agritrack.fishtrack.ui.login.api.LoginRQ;
import io.agritrack.fishtrack.ui.login.api.SyncApi;
import io.agritrack.fishtrack.ui.service.AuthenticationService;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Logged_In_User_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Token_Key;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> syncResult = new MutableLiveData<>();
    private MobileDB db;
    private ImageButton ibLocale;
    private ProgressDialog progressDialog;
    private int syncCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // bind the flags button
        ibLocale = findViewById(R.id.ibLocale);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // bind the credentials controls
        final EditText etUserName = findViewById(R.id.etUserName);
        final EditText etPassword = findViewById(R.id.etPassword);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // show previous loggeding user name
        String previousLoggedInUser = LocalPreferences.getLoggedInUser(null);
        if (previousLoggedInUser != null) {
            etUserName.setText(previousLoggedInUser);
        }

        // check last login timestamp, to determine whether synch is required.
        long diffHours = LocalPreferences.getLoginDiffInHours();

        // if last login occurred < 2 hours ?? ago, no further login is required.
        if (diffHours < 2) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
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

            btLogin.setOnClickListener(v -> {
                // get credential string values
                final String username = etUserName.getText().toString().trim();
                final String pin = etPassword.getText().toString().trim();

                if (username.isEmpty() || pin.isEmpty()) {
                    noCredentialsEnteredAlert();
                } else if ("config".equals(username) && "8888".equals(pin)) {
                    Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
                    i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // disables back button...
                    startActivity(i);
                    finish();
                } else {
                    // display spinning progress bar
                    toggleProgress(Boolean.TRUE, R.string.authenticating);

                    // invoke login
                    invokeLogin(username, pin);
                }
            });

            tvForgotYourPassword.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), ForgotYourPinActivity.class);
                startActivity(i);
            });
        }

        // Tap PoweredByLogo to reset LocalSharedPreferences
        tapLogToResetPreferences();

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

    private void noCredentialsEnteredAlert() {
        CToast(getApplicationContext(), render(R.string.empty_credentials_alert), Toast.LENGTH_LONG);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        LocalPreferences.writeValue(Token_Key, model.getToken());
        LocalPreferences.writeValue(Logged_In_User_Key, model.getUsername());
        LocalPreferences.updateLoginTime();

        boolean shouldSync = true; //LocalPreferences.shouldSync(Boolean.TRUE);
        if (!shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            // display spinning progress bar
            toggleProgress(Boolean.TRUE, R.string.syncing);

            // invoke sync all.
            invokeSyncAll();
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
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(username, LocalPreferences.getToken()))));
            } else {
                AuthApi authService = APIServiceGenerator.createAPI(AuthApi.class);
                LoginRQ loginRQ = new LoginRQ(username, pin);
                Call<AuthInfo> authAsyncCall = authService.login(loginRQ);
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
            Long siteId = LocalPreferences.getCurrentSiteId();
            String clusterId = LocalPreferences.getCurrentClusterId();

            // sync sites for current cluster
            Call<List<SiteDTO>> syncSitesAsyncCall = syncService.getSitesByCluster(clusterId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncClusterSitesCallBack(this.syncResult));

            // sync harvestRequests for current Site
            Call<List<HarvestRequestDTO>> syncHarvestResAsyncCall = syncService.getHarvestRequestsBySiteId(siteId, "Bearer " + token);
            syncHarvestResAsyncCall.enqueue(new SyncHarvestRequestCallBack(this.syncResult));

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack(this.syncResult));

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack(this.syncResult));

            // sync suppliers
            Call<List<SupplierDTO>> syncSuppliersAsyncCall = syncService.getSuppliersBySiteId(siteId, "Bearer " + token);
            syncSuppliersAsyncCall.enqueue(new SyncSuppliersCallBack(this.syncResult));

            // sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack(this.syncResult));

            // sync Cage Details
            Call<List<CageDetailsDTO>> syncCageDetailsAsyncCall = syncService.getCageDetailsBySiteId(siteId, "Bearer " + token);
            syncCageDetailsAsyncCall.enqueue(new SyncCageDetailsCallBack(this.syncResult));

            // sync fish species
            Call<List<FishSpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCode("gr", "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack(this.syncResult));

            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            i.putExtra("syncErrors", this.syncResult.toString());
            startActivity(i);

        } catch (Exception e) {
            e.printStackTrace();
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
     * Tap 6 times on PoweredByLogo to clear LocalSharedPreferences
     */
    private void tapLogToResetPreferences() {
        final ImageView ivLogo = findViewById(R.id.ivPoweredByLogo);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            long lastTap = System.currentTimeMillis();
            int taps = 0;

            @Override
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                taps = (now - lastTap > 1500) ? 0 : taps;
                taps++;
                if (taps == 6) {
                    LocalPreferences.Reset();
                    taps = 0;
                    CToast(getApplicationContext(), render("Preferences Reset!!!"), Toast.LENGTH_SHORT);
                }
                lastTap = now;
            }
        });
    }

    // ##########################
    public class AuthLoginCallBack implements Callback<AuthInfo> {
        private final String userName;

        public AuthLoginCallBack(LoginRQ loginRQ) {
            userName = loginRQ.getUsername();
        }

        @Override
        public void onResponse(Call<AuthInfo> call, Response<AuthInfo> response) {
            AuthInfo rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(this.userName, rs.getToken()))));
                LocalPreferences.updateLoginTime();
            } else {
                // Probably Invalid Credentials
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.login_failed)));
            }
        }

        @Override
        public void onFailure(Call<AuthInfo> call, Throwable error) {
            // Probably Network Communication Error
            if (error instanceof SocketTimeoutException) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.error_connection_timeout)));
            } else if (error instanceof IOException) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.error_timeout)));
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
}