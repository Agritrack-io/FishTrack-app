package io.agritrack.fishtrack.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FishTrackAPIServiceGenerator {

    private static final String BASE_URL = "http://192.168.150.194:5000"; //"http://fishtrackbackend-env.eba-b2cqygnf.eu-central-1.elasticbeanstalk.com"; //"http://192.168.150.2:5000";
    private static final Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                                                                    .connectTimeout(20, TimeUnit.SECONDS)
                                                                    .readTimeout(30, TimeUnit.SECONDS)
                                                                    .writeTimeout(30, TimeUnit.SECONDS);
    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);

    public static <S> S createAPI(Class<S> serviceClass) {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            retrofitBuilder.client(httpClient.build());
            retrofit = retrofitBuilder.build();
        }
        return retrofit.create(serviceClass);
    }

    public static <S> S createAPI(Class<S> serviceClass, final String token) {
        if (token != null) {
            httpClient.interceptors().clear();
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder builder1 = original.newBuilder().header("Authorization", token);
                Request request = builder1.build();
                return chain.proceed(request);
            });
            retrofitBuilder.client(httpClient.build());
            retrofit = retrofitBuilder.build();
        }
        return retrofit.create(serviceClass);
    }
}