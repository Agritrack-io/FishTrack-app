package io.agritrack.ui.login.api;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface AuthApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/auth/login")
    Call<AuthInfo> login(@Body LoginRQ rq);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/auth/coords")
    Call<List<SiteInfo>> getSites(@QueryMap Map<String, Object> params);
}
