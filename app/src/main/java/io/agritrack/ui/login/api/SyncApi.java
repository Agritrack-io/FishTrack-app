package io.agritrack.ui.login.api;

import java.util.List;

import io.agritrack.data.dto.AppUserDTO;
import io.agritrack.data.dto.CageDetailsDTO;
import io.agritrack.data.dto.HarvestRequestDTO;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.dto.common.EmployeeDTO;
import io.agritrack.data.dto.common.FishSpeciesDTO;
import io.agritrack.data.dto.common.SupplierDTO;
import io.agritrack.data.dto.wh.AssetDTO;
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
    Call<SiteDTO> getSiteById(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/harvest-request/{siteId}")
    Call<List<HarvestRequestDTO>> getHarvestRequestsBySiteId(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/item/{assetId}")
    Call<AssetDTO> getAssetById(@Path("assetId") String assetId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/{siteId}")
    Call<List<AssetDTO>> getAssetsBySite(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/asset/{siteId}/{assetType}")
    Call<List<AssetDTO>> getAssetsBySiteAndType(@Path("siteId") Long siteId, @Path("assetType") String assetType, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/user/site/{siteId}")
    Call<List<AppUserDTO>> getUsersBySiteId(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/employees/{siteId}")
    Call<List<EmployeeDTO>> getEmployeesBySiteId(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/suppliers/{siteId}")
    Call<List<SupplierDTO>> getSuppliersBySiteId(@Path("siteId") Long siteId, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/species/{country}")
    Call<List<FishSpeciesDTO>> getSpeciesByCountryCode(@Path("country") String country, @Header("Authorization") String token);

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/cage-detail/{siteId}")
    Call<List<CageDetailsDTO>> getCageDetailsBySiteId(@Path("siteId") Long siteId, @Header("Authorization") String token);
}
