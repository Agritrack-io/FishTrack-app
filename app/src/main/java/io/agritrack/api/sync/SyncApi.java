package io.agritrack.api.sync;

import java.util.List;
import java.util.UUID;

import io.agritrack.data.dao.wh.FoodSkuDAO;
import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.BinInfoDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.EncodingSchemeDTO;
import io.agritrack.data.dto.HarvestRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.CustomerDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.IotLoggerDTO;
import io.agritrack.data.dto.common.SpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.AssetDTO;
import io.agritrack.data.dto.wh.FoodSkuDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SyncApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/site")
    Call<List<SiteDTO>> getSitesByCluster(@Query("clusterId") String clusterId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/site/{siteId}")
    Call<SiteDTO> getSiteById(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/harvest-requests/{siteId}")
    Call<List<HarvestRequestDTO>> getHarvestRequestsBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/item/{assetId}")
    Call<AssetDTO> getAssetById(@Path("assetId") String assetId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/{siteId}")
    Call<List<AssetDTO>> getAssetsBySite(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/{siteId}/{assetType}")
    Call<List<AssetDTO>> getAssetsBySiteAndType(@Path("siteId") UUID siteId, @Path("assetType") String assetType, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/user/site/{siteId}")
    Call<List<AppUserDTO>> getUsersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/employees/{siteId}")
    Call<List<EmployeeDTO>> getEmployeesBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/supplier/{siteId}")
    Call<List<SupplierDTO>> getSuppliersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/customer/{siteId}")
    Call<List<CustomerDTO>> getCustomersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/species")
    Call<List<SpeciesDTO>> getSpeciesByCountryCodeAndType(@Query("country") String country, @Query("type") String type, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/cage-detail/{siteId}")
    Call<List<CageDetailsDTO>> getCageDetailsBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/transport/bin-info/plant/{siteId}")
    Call<List<BinInfoDTO>> getBinsByPlant(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/transport/bin-info/site/{siteId}")
    Call<List<BinInfoDTO>> getBinsByTargetSite(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/logger/{siteId}")
    Call<List<IotLoggerDTO>> getIOTLoggersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/food-codes")
    Call<List<FoodSkuDTO>> getFoodSkus(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/encoding/customer/name/{clusterName}")
    Call<List<EncodingSchemeDTO>> getEncodingSchemeByCustomerName(@Path("clusterName") String customerName, @Header("Authorization") String token);




}
