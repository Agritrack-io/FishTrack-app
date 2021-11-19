package io.agritrack;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.BuildConfig;
import com.facebook.stetho.Stetho;

public class FishTrackApplication extends Application {
    private static Context mContext;

    //This flag is used to redirect flow to different menu according to the product
    public static final  String PRODUCT = "TOMATO";//[FISH, TOMATO, MILK]

    //This global variable is used to supply country info where is required
    public static final  String COUNTRY = "gr";//[gr, es]

    //When true, no validation is performed in selected activities. This is required for Demo purposes
    public static final boolean IsDemo = true;

    public static Context getAppContext() {
        return FishTrackApplication.mContext;
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
