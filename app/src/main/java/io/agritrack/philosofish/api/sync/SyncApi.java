package io.agritrack.philosofish.api.sync;

import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.dto.AppUserDTO;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import io.agritrack.philosofish.data.dto.CageDetailsDTO;
import io.agritrack.philosofish.data.dto.EncodingSchemeDTO;
import io.agritrack.philosofish.data.dto.FishingRequestDTO;
import io.agritrack.philosofish.data.dto.SiteDTO;
import io.agritrack.philosofish.data.dto.common.CustomerDTO;
import io.agritrack.philosofish.data.dto.common.EmployeeDTO;
import io.agritrack.philosofish.data.dto.common.IotLoggerDTO;
import io.agritrack.philosofish.data.dto.common.SpeciesDTO;
import io.agritrack.philosofish.data.dto.common.SupplierDTO;
import io.agritrack.philosofish.data.dto.wh.AssetDTO;
import io.agritrack.philosofish.data.dto.wh.FoodSkuDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SyncApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/sites")
    Call<List<SiteDTO>> getSitesByCluster(@Query("clusterId") String clusterId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/site/{siteId}")
    Call<SiteDTO> getSiteById(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/fishing-requests/farm/{siteId}")
    Call<List<FishingRequestDTO>> getFishingRequestsBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/item/{assetId}")
    Call<AssetDTO> getAssetById(@Path("assetId") String assetId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/{siteId}")
    Call<List<AssetDTO>> getAssetsBySite(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/{siteId}/{assetType}")
    Call<List<AssetDTO>> getAssetsBySiteAndType(@Path("siteId") UUID siteId, @Path("assetType") String assetType, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/type/HARVEST_BIN")
    Call<List<AssetDTO>> getAssetsByHarvestBinType(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/type/CAGE")
    Call<List<AssetDTO>> getAssetsByCageType(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/type/NET")
    Call<List<AssetDTO>> getAssetsByNetType(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/asset/type/PLATFORM")
    Call<List<AssetDTO>> getAssetsByPlatformType(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/user/site/{siteId}")
    Call<List<AppUserDTO>> getUsersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/employees/{siteId}")
    Call<List<EmployeeDTO>> getEmployeesBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/supplier/{siteId}")
    Call<List<SupplierDTO>> getSuppliersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/suppliers")
    Call<List<SupplierDTO>> getAllSuppliers(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/customer/{siteId}")
    Call<List<CustomerDTO>> getCustomersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/species")
    Call<List<SpeciesDTO>> getSpeciesByCountryCodeAndType(@Query("country") String country, @Query("type") String type, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/cage-detail/{siteId}")
    Call<List<CageDetailsDTO>> getCageDetailsBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/transport/bin-info")
    Call<List<BinInfoDTO>> getCompleteBinLedger(@Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/logger/{siteId}")
    Call<List<IotLoggerDTO>> getIOTLoggersBySiteId(@Path("siteId") UUID siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/food-codes")
    Call<List<FoodSkuDTO>> getFoodSkus(@Header("Authorization") String token);

    //due to sync problems, this call is replaced by  .getEncodingScheme(token)
    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/encoding/customer/name/{clusterName}")
    Call<List<EncodingSchemeDTO>> getEncodingSchemeByCustomerName(@Path("clusterName") String customerName, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/philosofish/encoding")
    Call<List<EncodingSchemeDTO>> getEncodingScheme(@Header("Authorization") String token);
}