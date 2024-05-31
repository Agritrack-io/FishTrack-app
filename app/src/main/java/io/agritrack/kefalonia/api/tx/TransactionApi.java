package io.agritrack.kefalonia.api.tx;

import java.util.List;
import java.util.Map;

import io.agritrack.kefalonia.data.dto.BinInfoDTO;
import io.agritrack.kefalonia.data.dto.common.IotLoggerDTO;
import io.agritrack.kefalonia.data.dto.common.MediaDTO;
import io.agritrack.kefalonia.data.dto.common.TemperatureTimeSeriesDTO;
import io.agritrack.kefalonia.data.dto.tx.AssetTxDTO;
import io.agritrack.kefalonia.data.dto.tx.CorrelationTxDTO;
import io.agritrack.kefalonia.data.dto.tx.FishingTxDTO;
import io.agritrack.kefalonia.data.dto.tx.PostPackageQualityTxDTO;
import io.agritrack.kefalonia.data.dto.tx.ProcessingTxDTO;
import io.agritrack.kefalonia.data.dto.tx.QualityTxDTO;
import io.agritrack.kefalonia.data.dto.tx.TransportTxDTO;
import io.agritrack.kefalonia.data.dto.wh.RFIDInventoryDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface TransactionApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/bin-init")
    Call<List<BinInfoDTO>> syncBinInfoTx(@Body List<BinInfoDTO> binInfoTxs, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/fishing")
    Call<FishingTxDTO> syncFishingTx(@Body FishingTxDTO fishingTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/transport")
    Call<TransportTxDTO> syncTransportTx(@Body TransportTxDTO transportTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/transport/signature")
    Call<MediaDTO> syncTransportTxDriverSignature(@Body MediaDTO transportTxDriverSig, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/receipt")
    Call<ProcessingTxDTO> syncProcessingTx(@Body ProcessingTxDTO processTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/quality/pp1/quality")
    Call<QualityTxDTO> syncQualityTx(@Body QualityTxDTO qualityTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/quality/postpackage")
    Call<PostPackageQualityTxDTO> syncPostPackageQualityTx(@Body PostPackageQualityTxDTO postQualityTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/logger/temp")
    Call<List<TemperatureTimeSeriesDTO>> syncMeasurements(@Body List<TemperatureTimeSeriesDTO> measurements, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/tx")
    Call<AssetTxDTO> syncRFIDIOTx(@Body AssetTxDTO assetTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/logger/correlate")
    Call<CorrelationTxDTO> syncLoggerCorrelationTx(@Body CorrelationTxDTO correlationTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/wh/fish/correlate")
    Call<ResponseBody> syncAssetCorrelationTx(@Body List<CorrelationTxDTO> correlationTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/wh/fish/inventory")
    Call<ResponseBody> syncAssetWithAssetCorrelationTx(@Body List<CorrelationTxDTO> correlationTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/wh/fish/inventory")
    Call<RFIDInventoryDTO> syncRFIDInventoryTx(@Body RFIDInventoryDTO rFIDInventory, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/loggers/import")
    Call<List<IotLoggerDTO>> syncIotLoggers(@Body List<IotLoggerDTO> iotLoggers, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @PUT("/bin-ledger/update-init-ts")
    Call<Map<String,Long>> syncLoggerInitTs(@Body Map<String, Long> initTs, @Header("Authorization") String token);
}

