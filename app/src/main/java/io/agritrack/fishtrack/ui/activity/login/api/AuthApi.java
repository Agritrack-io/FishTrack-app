package io.agritrack.fishtrack.ui.activity.login.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/auth/login")
    Call<AuthResponse> login(@Body LoginRequest rq);
}
