package io.agritrack.ui.login.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadingApi {

    @POST("hotel/files/upload")
    @Multipart
    Call<ResponseBody> uploadHotelInventory(@Part("status") RequestBody status, @Part MultipartBody.Part file, @Header("Authorization") String token);
}
