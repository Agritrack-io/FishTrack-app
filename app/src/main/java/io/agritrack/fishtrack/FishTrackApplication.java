package io.agritrack.fishtrack;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.BuildConfig;
import com.facebook.stetho.Stetho;

public class FishTrackApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

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
