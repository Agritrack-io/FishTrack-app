package io.agritrack;

import static io.agritrack.ui.service.LocalPreferences.AppProductName_Key;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.BuildConfig;
import com.facebook.stetho.Stetho;

import io.agritrack.ui.service.LocalPreferences;

public class FishTrackApplication extends Application {
    //This flag is used to redirect flow to different menu according to the product
    private static String PRODUCT = AgritrackProducts.FISH.name();  //[FISH, TOMATO, HOTEL, MILK]
    //This global variable is used to supply country info where is required
    public static final String COUNTRY = "gr";//[gr, es]
    //When true, no validation is performed in selected activities. This feature is enabled for Presentations and Demos.
    public static final boolean IsDemo = false;
    private static Context mContext;

    public static Context getAppContext() {
        return FishTrackApplication.mContext;
    }

    public static String getProduct() {
        return PRODUCT;
    }

    public static void setProduct(String product) {
        PRODUCT = product;
        LocalPreferences.writeValue(AppProductName_Key, product);
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        PRODUCT = LocalPreferences.getActivePRODUCT();
    }
}