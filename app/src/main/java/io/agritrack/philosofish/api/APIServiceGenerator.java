package io.agritrack.philosofish.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIServiceGenerator {

    private static String AGRISENSE_URL = "http://agrisense.agritrack.info:5100";
    //private static String BASE_URL = "http://3.123.142.122:5000"; //dev
    private static String BASE_URL = "https://aqua-be.agritrack.org"; //dev
    //private static String BASE_URL = "http://192.168.150.191:5000"; //localRIgas
    //private static String BASE_URL = "http://fish-kefalonia-be.eu-central-1.elasticbeanstalk.com"; //production

    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create());
    private static Retrofit retrofit = retrofitBuilder.build();
    private static Retrofit.Builder retrofitAgrisenseBuilder = new Retrofit.Builder().baseUrl(AGRISENSE_URL).addConverterFactory(GsonConverterFactory.create());
    private static Retrofit retrofitAgrisense = retrofitAgrisenseBuilder.build();
    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS);
    private static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);
    private static final Gson gson = new GsonBuilder().setLenient().create();

    public static <S> S createAgrisenseAPI(Class<S> serviceClass) {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            retrofitAgrisenseBuilder.client(httpClient.build());
            retrofitAgrisense = retrofitAgrisenseBuilder.addConverterFactory(GsonConverterFactory.create(gson)).build();
        }
        return retrofitAgrisense.create(serviceClass);
    }

    public static String getAgrisenseUrl() {
        return AGRISENSE_URL;
    }

    public static <S> S createAPI(Class<S> serviceClass) {
        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            retrofitBuilder.client(httpClient.build());
            retrofit = retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gson)).build();
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

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static void setAgrisenseUrl(String agriUrl) {
        AGRISENSE_URL = agriUrl;
        retrofitAgrisenseBuilder = new Retrofit.Builder().baseUrl(AGRISENSE_URL).addConverterFactory(GsonConverterFactory.create());
        retrofitAgrisense = retrofitAgrisenseBuilder.build();
        //retrofitBuilder = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create());
    }

    public static void setBaseUrl(String beUrl) {
        BASE_URL = beUrl;
        retrofitBuilder = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create());
        retrofit = retrofitBuilder.build();
    }
}