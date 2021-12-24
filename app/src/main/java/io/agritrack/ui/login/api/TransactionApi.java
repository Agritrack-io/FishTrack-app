package io.agritrack.ui.login.api;

import java.util.List;

import io.agritrack.data.dto.common.MeasurementsDTO;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.dto.tx.CollectTxDTO;
import io.agritrack.data.dto.tx.ConsumableTxDTO;
import io.agritrack.data.dto.tx.CorrelationTxDTO;
import io.agritrack.data.dto.tx.FishingTxDTO;
import io.agritrack.data.dto.tx.PackageTxDTO;
import io.agritrack.data.dto.tx.PlantTxDTO;
import io.agritrack.data.dto.tx.ProcessingTxDTO;
import io.agritrack.data.dto.tx.SeaTemperatureTxDTO;
import io.agritrack.data.dto.tx.ShippingTxDTO;
import io.agritrack.data.dto.tx.StorageTxDTO;
import io.agritrack.data.dto.tx.TransportTxDTO;
import io.agritrack.data.dto.wh.CoInventoryDTO;
import io.agritrack.data.dto.wh.IfcoInventoryDTO;
import io.agritrack.data.dto.wh.RFIDInventoryDTO;
import io.agritrack.data.dto.wh.TotesInventoryDTO;
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
    @POST("/transport/shipping")
    Call<ShippingTxDTO> syncShippingTx(@Body ShippingTxDTO shippingTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/receipt")
    Call<ProcessingTxDTO> syncProcessingTx(@Body ProcessingTxDTO processTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/measurements")
    Call<MeasurementsDTO> syncMeasurements(@Body MeasurementsDTO measurements, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/plant")
    Call<PlantTxDTO> syncPlantTx(@Body PlantTxDTO plantTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/collect")
    Call<CollectTxDTO> syncCollectingTx(@Body CollectTxDTO collectingTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/storage")
    Call<StorageTxDTO> syncStorageTx(@Body StorageTxDTO storageTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/packaging/fruit")
    Call<PackageTxDTO> syncPackageTx(@Body PackageTxDTO packageTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/consumable/fruit/tx")
    Call<ConsumableTxDTO> syncIncomingTx(@Body ConsumableTxDTO consumableTxs, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/tx")
    Call<AssetTxDTO> syncRFIDIOTx(@Body AssetTxDTO assetTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/consumable/tx")
    Call<List<ConsumableTxDTO>> syncBarcodeIOTx(@Body List<ConsumableTxDTO> consumableTxs, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/asset/logger/correlate")
    Call<CorrelationTxDTO> syncCorrelationTx(@Body CorrelationTxDTO correlationTx, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/inventory/box")
    Call<TotesInventoryDTO> syncTotesInventoryTx(@Body TotesInventoryDTO rFIDInventory, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/inventory/box")
    Call<IfcoInventoryDTO> syncIfcoInventoryTx(@Body IfcoInventoryDTO coInventory, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/inventory/wh")
    Call<RFIDInventoryDTO> syncRFIDInventoryTx(@Body RFIDInventoryDTO rFIDInventory, @Header("Authorization") String token);

//    @Headers("Content-Type: application/json; charset=utf-8")
//    @POST("/inventory/wh")
//    Call<List<RFIDInventoryItemDTO>> syncRFIDInventoryItemTx(@Body List<RFIDInventoryItemDTO> rFIDInventoryItems, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/inventory/wh")
    Call<CoInventoryDTO> syncCoInventoryTx(@Body CoInventoryDTO coInventory, @Header("Authorization") String token);
//
//    @Headers("Content-Type: application/json; charset=utf-8")
//    @POST("/inventory/consumable/items")
//    Call<List<CoInventoryItemDTO>> syncCoInventoryItemTx(@Body List<CoInventoryItemDTO> coInventoryItems, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/temperatures/cages")
    Call<SeaTemperatureTxDTO> syncSeaTempTx(@Body SeaTemperatureTxDTO seaTemperatureTx, @Header("Authorization") String token);
}
