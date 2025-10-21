package io.agritrack.philosofish;

import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.ui.service.LocalPreferences.AppProductName_Key;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.facebook.stetho.BuildConfig;
import com.facebook.stetho.Stetho;
import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.ui.custom.CustomToast;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class FishTrackApplication extends Application {
    //This global variable is used to supply country info where is required
    public static final String COUNTRY = "gr";//[gr, es]
    //When true, no validation is performed in selected activities. This feature is enabled for Presentations and Demos.
    public static final boolean IsDemo = true;
    //When true, no signal is required in selected activities.
    public static boolean IsOnline = true;
    //This flag is used to redirect flow to different menu according to the product
    private static String PRODUCT = AgritrackProducts.FISH.name();  //[FISH]
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

        // Override Global crash handler
        CrashHandler.getInstance().init(this);

        if (PRODUCT != null) {
            setProduct(PRODUCT);
        }

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        if (Strings.isEmptyOrWhitespace(LocalPreferences.getActivePRODUCT())) {
            CustomToast.CToast(getAppContext(), render("Please Select a Product!!!"), Toast.LENGTH_LONG);
        } else {
            PRODUCT = LocalPreferences.getActivePRODUCT();
        }
    }
}