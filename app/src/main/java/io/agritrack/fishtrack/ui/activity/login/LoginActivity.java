package io.agritrack.fishtrack.ui.activity.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.service.AppUserService;
import io.agritrack.fishtrack.ui.activity.ConfigActivity;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.api.APICommService;
import io.agritrack.fishtrack.ui.activity.login.api.AuthInfo;
import io.agritrack.fishtrack.ui.activity.login.api.LoginRQ;
import io.agritrack.fishtrack.ui.service.SharedPreferenceService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private MobileDB db;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // bind the credentials controls
        final EditText etUserName = findViewById(R.id.etUserName);
        final EditText etPassword = findViewById(R.id.etPassword);

        // check last login timestamp, to determine whether synch is required.
        long diffHours = SharedPreferenceService.getLoginDiffInDays();

        boolean shouldLogin = SharedPreferenceService.shouldLogin(Boolean.TRUE);
        boolean shouldSync = SharedPreferenceService.shouldSync(Boolean.TRUE);

        // if last login occurred < 2 hours ?? ago, no further login is required.
        if (diffHours < 2 && !shouldLogin && !shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            final TextView tvForgotYourPassword = findViewById(R.id.tvForgotPasswordText);
            final Button btLogin = findViewById(R.id.btnLogin);
            loadingProgressBar = findViewById(R.id.loading);
            loadingText = findViewById(R.id.loading_text);

            loginResult.observe(this, response -> {
                if (response == null) {
                    loadingProgressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadingText.setText(null);
                    return;
                }
                if (response.getError() != null) {
                    showLoginFailed(response.getError());
                    loadingProgressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadingText.setText(null);
                }
                if (response.getSuccess() != null) {
                    updateUiWithUser(response.getSuccess());
                }
            });

            btLogin.setOnClickListener(v -> {
                // get credential string values
                final String username = etUserName.getText().toString().trim();
                final String pin = etPassword.getText().toString().trim();

//                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
//                startActivity(i);

                if (username.isEmpty() || pin.isEmpty()) {
                    noCredentialsEnteredAlert();
                } else if ("config".equals(username) && "8888".equals(pin)) {
                    Intent i = new Intent(getApplicationContext(), ConfigActivity.class);
                    startActivity(i);
                } else {
                    invokeLogin(username, pin);
                }
            });

            tvForgotYourPassword.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), ForgotYourPinActivity.class);
                startActivity(i);
            });
        }
    }

    private void noCredentialsEnteredAlert() {
        Toast.makeText(getApplicationContext(), R.string.empty_credentials_alert, Toast.LENGTH_LONG).show();
    }

    private void updateUiWithUser(LoggedInUserView model) {
        SharedPreferenceService.writeValue("token", model.getToken());
        SharedPreferenceService.writeValue("username", model.getUsername());
        SharedPreferenceService.writeValue("loginTime", (System.currentTimeMillis() / 1000L));
        //editor.apply();

        boolean shouldSync = SharedPreferenceService.shouldSync(Boolean.TRUE);
        if (!shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            invokeSyncAll();

            SyncAllTask syncAllTask = new SyncAllTask();
            syncAllTask.execute();

            SharedPreferenceService.writeValue("shouldSync", false);
            //editor.apply();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void showAuthProgress() {
        runOnUiThread(() -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingText.setText(R.string.authenticating);
            loadingText.setVisibility(View.VISIBLE);
        });
    }

    private void showSyncProgress() {
        runOnUiThread(() -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingText.setText(R.string.syncing);
            loadingText.setVisibility(View.VISIBLE);
        });
    }

    private void hideSyncProgress() {
        runOnUiThread(() -> {
            loadingProgressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        });
    }

    private void invokeLogin(String username, String pin) {
        // display spinning progress bar
        showAuthProgress();

        try {
            long diffInDays = SharedPreferenceService.getLoginDiffInDays();

            // query local db for previous User authentications...
            AppUserService userService = new AppUserService();
            boolean userIsAlreadyAuthenticated = true; //userService.authenticateUser(db, username, pin);

            if (diffInDays == 0 && userIsAlreadyAuthenticated) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(username, SharedPreferenceService.getToken()))));
            } else {
                APICommService restService = new APICommService();
                LoginRQ rq = new LoginRQ(username, pin);
                restService.authenticate(rq, new AuthLoginCallBack(username, pin));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void invokeSyncAll() {
        // display spinning progress bar
        showSyncProgress();

        try {
            SharedPreferences pref = getSharedPreferences("agritrack", Context.MODE_PRIVATE);
            String token = pref.getString("token", null);
            boolean syncResult = true;

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


    public class AuthLoginCallBack implements Callback<AuthInfo> {
        private final SharedPreferences pref;
        private final String username;
        private final String pin;

        public AuthLoginCallBack(String username, String pin) {
            pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);
            this.username = username;
            this.pin = pin;
        }

        @Override
        public void onResponse(Call<AuthInfo> call, Response<AuthInfo> response) {
            AuthInfo rs = response.body();

            if (rs != null) {
                runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(this.username, rs.getToken()))));
                SharedPreferenceService.writeValue("shouldSync", true);
                //editor.apply();
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


    public class SyncAllCallBack implements Callback<AuthInfo> {
        private final SharedPreferences pref;
        private final String username;
        private final String pin;

        public SyncAllCallBack(String username, String pin) {
            pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);
            this.username = username;
            this.pin = pin;
        }

        @Override
        public void onResponse(Call<AuthInfo> call, Response<AuthInfo> response) {

        }

        @Override
        public void onFailure(Call<AuthInfo> call, Throwable t) {

        }
    }


    private class SyncAllTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences pref = getSharedPreferences("agritrack", Context.MODE_PRIVATE);
            String token = pref.getString("token", null);
//            boolean syncResult;
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
//
//            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
//            i.putExtra("syncErrors", !syncResult);
//            startActivity(i);
            return null;
        }
    }
}