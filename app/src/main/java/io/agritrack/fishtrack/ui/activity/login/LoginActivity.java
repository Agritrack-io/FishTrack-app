package io.agritrack.fishtrack.ui.activity.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Locale;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.api.FishTrackAPIServiceGenerator;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.dto.AppUserDTO;
import io.agritrack.fishtrack.data.dto.SiteDTO;
import io.agritrack.fishtrack.data.dto.common.EmployeeDTO;
import io.agritrack.fishtrack.data.dto.common.FishSpeciesDTO;
import io.agritrack.fishtrack.data.dto.wh.AssetDTO;
import io.agritrack.fishtrack.data.service.AppUserService;
import io.agritrack.fishtrack.ui.activity.ConfigActivity;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.login.api.AuthApi;
import io.agritrack.fishtrack.ui.activity.login.api.AuthInfo;
import io.agritrack.fishtrack.ui.activity.login.api.LoginRQ;
import io.agritrack.fishtrack.ui.activity.login.api.SyncApi;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Logged_In_User_Key;
import static io.agritrack.fishtrack.ui.service.LocalPreferences.Token_Key;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private MobileDB db;
    private ImageButton ibLocale;
    private AlertDialog dialog;
    private TextView tvProgressMessage;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LocaleHelper.onAttach(newBase));
//    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // bind the flags button
        ibLocale = findViewById(R.id.ibLocale);

        // Remove focus from children controls...
        ConstraintLayout rootLayout = findViewById(R.id.loginActivityLayout);
        rootLayout.requestFocus();

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // bind the credentials controls
        final EditText etUserName = findViewById(R.id.etUserName);
        final EditText etPassword = findViewById(R.id.etPassword);

        // show previous loggeding user name
        String previousLoggedInUser = LocalPreferences.getLoggedInUser(null);
        if(previousLoggedInUser!=null) {
            etUserName.setText(previousLoggedInUser);
        }

        // check last login timestamp, to determine whether synch is required.
        long diffHours = LocalPreferences.getLoginDiffInDays();

        boolean shouldLogin = LocalPreferences.shouldLogin(Boolean.TRUE);
        boolean shouldSync = LocalPreferences.shouldSync(Boolean.TRUE);

        // if last login occurred < 2 hours ?? ago, no further login is required.
        if (diffHours < 2 && !shouldLogin && !shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            final TextView tvForgotYourPassword = findViewById(R.id.tvForgotPasswordText);
            final Button btLogin = findViewById(R.id.btnLogin);

            loginResult.observe(this, response -> {
                if (response == null) {
                    hideSyncProgress();
                    return;
                }
                if (response.getError() != null) {
                    showLoginFailed(response.getError());
                    hideSyncProgress();
                }
                if (response.getSuccess() != null) {
                    hideSyncProgress();
                    updateUiWithUser(response.getSuccess());
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

        //************************************************************************
        // instantiate an AlertDialog with countdown functionality
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_indicator, null);
        tvProgressMessage = dialogView.findViewById(R.id.progressMsg);
        tvProgressMessage.setTextSize(24.0f);
        tvProgressMessage.setText(R.string.syncing);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setCancelable(false);
        dialog = dlgBuilder.create();
    }

    private void noCredentialsEnteredAlert() {
        Toast.makeText(getApplicationContext(), R.string.empty_credentials_alert, Toast.LENGTH_LONG).show();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        LocalPreferences.writeValue(Token_Key, model.getToken());
        LocalPreferences.writeValue(Logged_In_User_Key, model.getUsername());
        LocalPreferences.updateLoginTime();

        boolean shouldSync = LocalPreferences.shouldSync(Boolean.TRUE);
        if (!shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            invokeSyncAll();


            LocalPreferences.writeValue("shouldSync", Boolean.FALSE);
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showAuthProgress() {
        runOnUiThread(() -> {
            tvProgressMessage.setText(R.string.authenticating);
            dialog.show();
        });
    }

    private void showSyncProgress() {
        runOnUiThread(() -> {
            tvProgressMessage.setText(R.string.syncing);
            dialog.show();
        });
    }

    private void hideSyncProgress() {
        runOnUiThread(() -> {
            dialog.dismiss();
        });
    }

    private void invokeLogin(String username, String pin) {
        // display spinning progress bar
        showAuthProgress();

        try {
            long diffInDays = LocalPreferences.getLoginDiffInDays();

            // query local db for previous User authentications...
            AppUserService userService = new AppUserService();
            boolean userIsAlreadyAuthenticated = userService.authenticateUser(db, username, pin);

            if (diffInDays == 0 && userIsAlreadyAuthenticated) {
                LocalPreferences.updateLoginTime();
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(username, LocalPreferences.getToken()))));
            } else {
                AuthApi authService = FishTrackAPIServiceGenerator.createAPI(AuthApi.class);
                LoginRQ loginRQ = new LoginRQ(username, pin);
                Call<AuthInfo> authAsyncCall = authService.login(loginRQ);
                authAsyncCall.enqueue(new AuthLoginCallBack(loginRQ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            hideSyncProgress();
        }
    }

    private void invokeSyncAll() {
        // display spinning progress bar
        showSyncProgress();

        try {
            SyncApi syncService = FishTrackAPIServiceGenerator.createAPI(SyncApi.class);
            String token = LocalPreferences.getToken();
            Long siteId = LocalPreferences.getCurrentSiteId();
            boolean syncResult = true;

            // sync sites
            Call<SiteDTO> syncSitesAsyncCall = syncService.getSiteById(siteId, "Bearer " + token);
            syncSitesAsyncCall.enqueue(new SyncSitesCallBack());

            // sync users
            Call<List<AppUserDTO>> syncUsersAsyncCall = syncService.getUsersBySiteId(siteId, "Bearer " + token);
            syncUsersAsyncCall.enqueue(new SyncUsersCallBack());

            // sync employees
            Call<List<EmployeeDTO>> syncEmployeesAsyncCall = syncService.getEmployeesBySiteId(siteId, "Bearer " + token);
            syncEmployeesAsyncCall.enqueue(new SyncEmployeesCallBack());

            // sync assets  (cages, nets, bins, platforms)
            Call<List<AssetDTO>> syncAssetsAsyncCall = syncService.getAssetsBySite(siteId, "Bearer " + token);
            syncAssetsAsyncCall.enqueue(new SyncAssetsCallBack());

            // sync fish species
            Call<List<FishSpeciesDTO>> syncSpeciesAsyncCall = syncService.getSpeciesByCountryCode("gr", "Bearer " + token);
            syncSpeciesAsyncCall.enqueue(new SyncSpeciesCallBack());


//            RouteSyncService routeSyncService = new RouteSyncService();
//            syncResult = routeSyncService.syncRoute(db, token);
//
//            DriverAndTrucksSyncService driverAndTrucksSyncService = new DriverAndTrucksSyncService();
//            syncResult &= driverAndTrucksSyncService.syncDriverAndTrucks(db, token);
//
//            DistributorsAndPlantsSyncService distributorsAndPlantsSyncService = new DistributorsAndPlantsSyncService();
//            syncResult &= distributorsAndPlantsSyncService.syncDistributorsAndPlants(db, token);
//
//            TanksAndProducersSyncService tanksAndProducersSyncService = new TanksAndProducersSyncService();
//            syncResult &= tanksAndProducersSyncService.syncTanksAndProducers(db, token);

            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            i.putExtra("syncErrors", !syncResult);
            startActivity(i);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hideSyncProgress();
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
//        getApplicationContext().createConfigurationContext(config);
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
//        if (recreate == true) {
//            LoginActivity.this.recreate();
//        } else {
//            setContentView(R.layout.activity_login);
//        }
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
                    Toast.makeText(getApplicationContext(), "Preferences Reset!!!", Toast.LENGTH_SHORT).show();
                }
                lastTap = now;
            }
        });
    }

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
                LocalPreferences.writeValue("shouldSync", true);
                LocalPreferences.updateLoginTime();
            } else {
                // Probably Invalid Credentials
                runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.login_failed)));
            }
        }

        @Override
        public void onFailure(Call<AuthInfo> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.login_failed)));
        }
    }

    public class SyncSitesCallBack implements Callback<SiteDTO> {

        @Override
        public void onResponse(Call<SiteDTO> call, Response<SiteDTO> response) {
            SiteDTO siteDTO = response.body();

            if (siteDTO != null) {
                db.siteDAO().insert(SiteDTO.convert(siteDTO));
                LocalPreferences.setSelectedSite(siteDTO);
            } else {
                // no Sites found
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.no_sites_found_alert, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<SiteDTO> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.synch_failed)));
        }
    }

    public class SyncAssetsCallBack implements Callback<List<AssetDTO>> {

        @Override
        public void onResponse(Call<List<AssetDTO>> call, Response<List<AssetDTO>> response) {
            List<AssetDTO> rs = response.body();

            if (rs != null) {
                for (AssetDTO assetDTO : rs) {
                    db.assetDAO().insert(AssetDTO.convert(assetDTO));
                }
            } else {
                // no Assets found
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.no_sites_found_alert, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<List<AssetDTO>> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.synch_failed)));
        }
    }

    public class SyncUsersCallBack implements Callback<List<AppUserDTO>> {

        @Override
        public void onResponse(Call<List<AppUserDTO>> call, Response<List<AppUserDTO>> response) {
            List<AppUserDTO> rs = response.body();

            if (rs != null) {
                for (AppUserDTO userDTO : rs) {
                    db.userDAO().insert(AppUserDTO.convert(userDTO));
                }
            } else {
                // no Users found
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.no_users_found_alert, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<List<AppUserDTO>> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.synch_failed)));
        }
    }

    public class SyncEmployeesCallBack implements Callback<List<EmployeeDTO>> {

        @Override
        public void onResponse(Call<List<EmployeeDTO>> call, Response<List<EmployeeDTO>> response) {
            List<EmployeeDTO> rs = response.body();

            if (rs != null) {
                for (EmployeeDTO employeeDTO : rs) {
                    db.employeeDAO().insert(EmployeeDTO.convert(employeeDTO));
                }
            } else {
                // no Employees found
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.no_employees_found_alert, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<List<EmployeeDTO>> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.synch_failed)));
        }
    }

    public class SyncSpeciesCallBack implements Callback<List<FishSpeciesDTO>> {

        @Override
        public void onResponse(Call<List<FishSpeciesDTO>> call, Response<List<FishSpeciesDTO>> response) {
            List<FishSpeciesDTO> rs = response.body();

            if (rs != null) {
                for (FishSpeciesDTO speciesDTO : rs) {
                    db.speciesDAO().insert(FishSpeciesDTO.convert(speciesDTO));
                }
            } else {
                // no Fish Species found
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.no_species_found_alert, Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onFailure(Call<List<FishSpeciesDTO>> call, Throwable t) {
            // Probably Network Communication Error
            runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.synch_failed)));
        }
    }
}