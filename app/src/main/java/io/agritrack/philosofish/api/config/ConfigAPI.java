package io.agritrack.philosofish.api.config;

import io.agritrack.philosofish.data.dto.AgricenseDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ConfigAPI {

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/agrisense/device/{terminalId}/info")
    Call<AgricenseDTO> getConfiguration(@Path("terminalId") String terminalId,
                                        @Query("serialNo") String serialNo,
                                        @Query("macAddress") String macAddress,
                                        @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/agrisense/config")
    Call<AgricenseDTO> postConfiguration(@Body AgricenseDTO configParams,
                                         @Header("Authorization") String token);
}
//serialNo=CW1805020059&macAddress=00:B5:D0:16:B2:3D