package io.agritrack.fishtrack.ui.activity.login.api;

import io.agritrack.fishtrack.data.dto.tx.FishingTxDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TransactionApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/fishing")
    Call<FishingTxDTO> syncFishingTx(@Body FishingTxDTO fishingTx, @Header("Authorization") String token);
}
