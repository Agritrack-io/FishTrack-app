package io.agritrack.fishtrack.ui.activity.api;

import java.io.InputStream;
import java.util.Properties;

import io.agritrack.fishtrack.ui.activity.login.LoginActivity;
import io.agritrack.fishtrack.ui.activity.login.api.AuthApi;
import io.agritrack.fishtrack.ui.activity.login.api.AuthInfo;
import io.agritrack.fishtrack.ui.activity.login.api.LoginRQ;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class APICommService {
    private static String BASE_URL = "http://192.168.150.4:5000";
    private AuthApi api;

    public APICommService() {
        BASE_URL = loadBaseUrl();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(AuthApi.class);
    }

    public void authenticate(LoginRQ req, LoginActivity.AuthLoginCallBack callBack) {
        Call<AuthInfo> call = api.login(req);
        call.enqueue(callBack);
    }

    private String loadBaseUrl() {
        String mUrlString = null;
        try {
            InputStream is = getContext().getAssets().open("connection.properties");
            Properties props = new Properties();
            props.load(is);
            mUrlString = props.getProperty("api_endpoint", "http://192.168.150.4:5000");
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mUrlString;
    }
}
