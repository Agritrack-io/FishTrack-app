package io.agritrack.philosofish.api.query;

import java.util.List;

import io.agritrack.philosofish.data.dto.common.ReaderDTO;
import io.agritrack.philosofish.data.type.ConfigDevice;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EnquiryApi {

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/fishing/epc-list/{epc}")
    Call<List<String>> getFishingEpcsBatch(@Path("epc") String epc, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/reader/{deviceId}/current-epcs")
    Call<ConfigDevice> getCurrentEpcsByDevice(@Path("deviceId") String deviceId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @PUT("/reader/{deviceId}/current-epcs")
    Call<ReaderDTO> setCurrentEpcsByDevice(@Path("deviceId") String deviceId, @Body ConfigDevice cDev, @Header("Authorization") String token);
}
