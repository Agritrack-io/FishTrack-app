package io.agritrack.ui.login.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface EnquiryApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/enquiry/collection-lot/{toteRFID}")
    Call<String> getCollectionLotByToteRfid(@Path("toteRFID") String toteRFID, @Header("Authorization") String token);
}
