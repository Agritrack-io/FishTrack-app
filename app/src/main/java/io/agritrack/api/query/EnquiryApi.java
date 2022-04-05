package io.agritrack.api.query;

import java.util.List;

import io.agritrack.data.dto.LotDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EnquiryApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/collect/lot/{toteRFID}")
    Call<LotDTO> getCollectionLotByToteRfid(@Path("toteRFID") String toteRFID, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/collect/find-lots")
    Call<List<LotDTO>> getCollectionLotsByToteRfids(@Body List<String> toteRfids, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/packaging/fruit/lot/{paletteBarcode}")
    Call<LotDTO> getPackagingLotByPaletteBarcode(@Path("paletteBarcode") String paletteBarcode, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/plant/species/{poleRFID}")
    Call<SpeciesDTO> getSpeciesByPoleRfid(@Path("poleRFID") String poleRFID, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/plant/lot/{poleRFID}")
    Call<LotDTO> getPlantLotByPoleRfid(@Path("poleRFID") String poleRFID, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/packaging/fruit/ifcobatch/{ifcoBarcode}")
    Call<List<String>> getIfcoBatch(@Path("ifcoBarcode") String ifcoBarcode, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=UTF-8")
    @GET("/collect/fruit/rfidbatch/{rfidBarcode}")
    Call<List<String>> getRFIDBatch(@Path("rfidBarcode") String rfidBarcode, @Header("Authorization") String token);
}
