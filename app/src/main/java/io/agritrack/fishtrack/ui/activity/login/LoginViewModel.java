package io.agritrack.fishtrack.ui.activity.login;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.data.service.AppUserService;
import io.agritrack.fishtrack.data.service.RestfulCommunicationSingleton;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private String mLoginUrl;

    LoginViewModel() {
        try {
            InputStream is = getContext().getAssets().open("connection.properties");
            Properties props = new Properties();
            props.load(is);
            mLoginUrl = props.getProperty("backend_endpoint", "http://localhost:5000") + "/login";
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(final String username, String password, MobileDB db) {

        // fetch fish-app related info from Shared Preferences
        SharedPreferences pref = getContext().getSharedPreferences("agrifish", Context.MODE_PRIVATE);
        long loginTime = pref.getLong("loginTime", 0);
        long currentTime = new Date().getTime();
        Date loginDate = new Date(loginTime);
        Date currentDate = new Date(currentTime);
        String token = pref.getString("token", null);

        AppUserService userService = new AppUserService();
        boolean authentication = userService.authenticateUser(db, username, password);
        if (loginDate.equals(currentDate) && authentication) {
            loginResult.setValue(new LoginResult(new LoggedInUserView(username, token)));
        } else {
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("username", username);
                jsonBody.put("pin", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String requestBody = jsonBody.toString();

            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest sr = new StringRequest(Request.Method.POST, mLoginUrl, future, future) {
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

            sr.setRetryPolicy(new DefaultRetryPolicy(15000,3,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RestfulCommunicationSingleton.getInstance(getContext()).addToRequestQueue(sr);
            try {
                String response = future.get(); // this will block
                loginResult.setValue(new LoginResult(new LoggedInUserView(username, response)));
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("shouldSync", true);
                editor.apply();
            } catch (InterruptedException | ExecutionException e) {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    }

}