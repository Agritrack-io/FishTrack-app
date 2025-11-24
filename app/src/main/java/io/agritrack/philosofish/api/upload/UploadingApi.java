package io.agritrack.philosofish.api.upload;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadingApi {

    @POST("/philosofish/quality/pp1/photo")
    @Multipart
    Call<ResponseBody> uploadPhoto(@Part MultipartBody.Part file, @Header("Authorization") String token);

//    @POST("/philosofish/logs/crash/upload")
//    @Multipart
//    Call<ResponseBody> uploadCrashLog(@Part MultipartBody.Part file, @Header("Authorization") String token);
}
