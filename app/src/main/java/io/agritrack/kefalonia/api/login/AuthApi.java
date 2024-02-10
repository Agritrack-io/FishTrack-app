package io.agritrack.kefalonia.api.login;

import java.util.List;
import java.util.Map;

import io.agritrack.kefalonia.ui.login.api.AuthInfoRS;
import io.agritrack.kefalonia.ui.login.api.LoginRQ;
import io.agritrack.kefalonia.ui.login.api.SiteInfoRS;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface AuthApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/auth/login")
    Call<AuthInfoRS> login(@Body LoginRQ rq);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/auth/coords")
    Call<List<SiteInfoRS>> getSites(@QueryMap Map<String, Object> params);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/auth/refresh-access-token")
    Call<AuthInfoRS> refreshJWT(@Header("Authorization") String token);
}
