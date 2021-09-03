package io.agritrack.fishtrack.ui.login.api;

import io.agritrack.fishtrack.data.dto.tx.TransportTxDTO;
import io.agritrack.fishtrack.data.dto.tx.AssetTxDTO;
import io.agritrack.fishtrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.fishtrack.data.dto.tx.FishingTxDTO;
import io.agritrack.fishtrack.data.dto.tx.ProcessingTxDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TransactionApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/fishing")
    Call<FishingTxDTO> syncFishingTx(@Body FishingTxDTO fishingTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/transport")
    Call<TransportTxDTO> syncTransportTx(@Body TransportTxDTO transportTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/receipt")
    Call<ProcessingTxDTO> syncProcessingTx(@Body ProcessingTxDTO processTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/tx")
    Call<AssetTxDTO> syncIOTx(@Body AssetTxDTO assetTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/correlate")
    Call<CorrelationTxDTO> syncCorrelationTx(@Body CorrelationTxDTO correlationTx, @Header("Authorization") String token);

}
