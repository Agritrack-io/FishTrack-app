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

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.model.AppUser;
import io.agritrack.fishtrack.data.service.AppUserService;
import io.agritrack.fishtrack.data.service.RestfulCommunicationSingleton;
import io.agritrack.fishtrack.ui.activity.HomeActivity;


import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private MobileDB db;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();


    private static void accept(List<AppUser> appUsers) {
    }


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
    protected void onDestroy() {
//        if (subscription != null && !subscription.isUnsubscribed()) {
//            subscription.unsubscribe();
//        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // check last login timestamp, to determine whether synch is required.
        SharedPreferences pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);
        long loginTime = pref.getLong("loginTime", 0);
        boolean shouldLogin = pref.getBoolean("shouldLogin", false);
        boolean shouldSync = pref.getBoolean("shouldSync", true);

        long currentTime = new Date().getTime();
        long diffHours = (currentTime - loginTime) / (60 * 60 * 1000);

        // if last login occurred < 2 hours ?? ago, no further login is required.
        if (diffHours < 2 && !shouldLogin && !shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            final EditText etUserName = findViewById(R.id.etUserName);
            final EditText etPassword = findViewById(R.id.etPassword);
            final TextView tvForgotYourPassword = findViewById(R.id.tvForgotPasswordText);
            final Button btLogin = findViewById(R.id.btnLogin);
            loadingProgressBar = findViewById(R.id.loading);
            loadingText = findViewById(R.id.loading_text);

            loginResult.observe(this, loginResult -> {
                if (loginResult == null) {
                    loadingProgressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadingText.setText(null);
                    return;
                }
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                    loadingProgressBar.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    loadingText.setText(null);
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
            });

            btLogin.setOnClickListener(v -> {
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
//                LoginTask loginTask = new LoginTask(etUserName, etPassword);
//                loginTask.execute();
            });

            tvForgotYourPassword.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), ForgotYourPinActivity.class);
                startActivity(i);
            });
        }
    }

    private void updateUiWithUser(LoggedInUserView model) {

        SharedPreferences pref = getSharedPreferences("agritrack", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", model.getToken());
        editor.putString("username", model.getUsername());
        editor.putLong("loginTime", new Date().getTime());
        editor.apply();

        boolean shouldSync = pref.getBoolean("shouldSync", true);
        if (!shouldSync) {
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        } else {
            SyncAllTask syncAllTask = new SyncAllTask();
            syncAllTask.execute();
            editor.putBoolean("shouldSync", false);
            editor.apply();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }


    private void invokeLogin(String username) {

    }



    private class SyncAllTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingText.setText(R.string.syncing);
            loadingText.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingProgressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        }

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

    private class LoginTask extends AsyncTask<Void, Integer, Void> {
        private final EditText usernameEditText;
        private final EditText passwordEditText;

        private LoginTask(EditText usernameEditText, EditText passwordEditText) {
            this.usernameEditText = usernameEditText;
            this.passwordEditText = passwordEditText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingText.setText(R.string.authenticating);
            loadingText.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                InputStream is = getContext().getAssets().open("connection.properties");
                Properties props = new Properties();
                props.load(is);
                String mUrlString = props.getProperty("url", "http://192.168.0.195:8090") + "/login";
                is.close();

                SharedPreferences pref = getContext().getSharedPreferences("agritrack", Context.MODE_PRIVATE);
                long loginTime = pref.getLong("loginTime", 0);
                long currentTime = new Date().getTime();
                Date loginDate = new Date(loginTime);
                Date currentDate = new Date(currentTime);
                String token = pref.getString("token", null);

                AppUserService userService = new AppUserService();
                boolean authentication = userService.authenticateUser(db, usernameEditText.getText().toString(), passwordEditText.getText().toString());
                if (loginDate.getDay() == currentDate.getDay() && authentication) {
                    runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(usernameEditText.getText().toString(), token))));
                } else {
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("username", usernameEditText.getText().toString());
                        jsonBody.put("pin", passwordEditText.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final String requestBody = jsonBody.toString();

                    RequestFuture<String> future = RequestFuture.newFuture();
                    StringRequest sr = new StringRequest(Request.Method.POST, mUrlString, future, future) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() {
                            return requestBody.getBytes(StandardCharsets.UTF_8);
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String token = "";
                            if (response != null) {
                                token = response.headers.get("Authorization");
                            }
                            return Response.success(token, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    sr.setRetryPolicy(new DefaultRetryPolicy(15000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    RestfulCommunicationSingleton.getInstance(getContext()).addToRequestQueue(sr);
                    try {
                        String response = future.get(); // this will block
                        runOnUiThread(() -> loginResult.setValue(new LoginResult(new LoggedInUserView(usernameEditText.getText().toString(), response))));
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("shouldSync", true);
                        editor.apply();
                    } catch (InterruptedException | ExecutionException e) {
                        runOnUiThread(() -> loginResult.setValue(new LoginResult(R.string.login_failed)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}