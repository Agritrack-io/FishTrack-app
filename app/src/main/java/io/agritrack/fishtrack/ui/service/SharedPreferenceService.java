package io.agritrack.fishtrack.ui.service;

import android.content.Context;
import android.content.SharedPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class SharedPreferenceService {
    public static final String Pref_Name = "agritrack";
    public static final String Token_Key = "token";
    public static final String Longitude_Key = "lon";
    public static final String Latitude_Key = "lat";
    public static final String LoginTime_Key = "loginTime";
    public static final String ShouldLogin_Key = "shouldLogin";
    public static final String ShouldSync_Key = "shouldSync";



    private static SharedPreferenceService mInstance;
    private static Context mContext;
    private static SharedPreferences pref;


    private SharedPreferenceService(Context context) {
        // hold the application context
        mContext = context;

        // hold the shared Preferences instance
        pref = getContext().getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
    }

    public static SharedPreferenceService getInstance() {
        if (mInstance == null) {
            mInstance = new SharedPreferenceService(getAppContext());
        }
        return mInstance;
    }

    public static String getToken() {
        return getInstance().pref.getString(Token_Key, null);
    }

    public static String getLongitude() {
        return getInstance().pref.getString(Longitude_Key, null);
    }

    public static String getLatitude() {
        return getInstance().pref.getString(Latitude_Key, null);
    }


    public static Long getLoginTime() {
        return getInstance().pref.getLong(LoginTime_Key, Long.MIN_VALUE);
    }

    public static Boolean shouldLogin(Boolean defVal) {
        return getInstance().pref.getBoolean(ShouldLogin_Key, defVal);
    }

    public static Boolean shouldSync(Boolean defVal) {
        return getInstance().pref.getBoolean(ShouldSync_Key, defVal);
    }


    public static Long getLoginDiffInDays() {
        long loginUnixTime = getLoginTime();
        long unixTime = System.currentTimeMillis() / 1000L;

        long diffInDays = Math.abs(unixTime - loginUnixTime) / 3600L / 24L;

        return diffInDays;
    }

    public static boolean writeValue(String key, Object value) {
        try {
            SharedPreferences.Editor editor = pref.edit();

            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            }
            editor.apply();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
