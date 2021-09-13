package io.agritrack.fishtrack.ui.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import io.agritrack.fishtrack.data.dto.SiteDTO;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class LocalPreferences {
    public static final String Pref_Name = "agritrack";
    public static final String Token_Key = "token";
    public static final String Longitude_Key = "lon";
    public static final String Latitude_Key = "lat";
    public static final String LoginTime_Key = "loginTime";
    public static final String SelectedSite_Key = "selectedSite";
    public static final String SelectedSiteName_Key = "selectedSiteName";
    public static final String SelectedSiteId_Key = "selectedSiteId";
    public static final String SelectedCluster_Key = "selectedClusterId";
    public static final String Locale_Key = "localeCode";
    public static final String Logged_In_User_Key = "LoggedinUser";

    public static final String Driver_Names_Key = "DriverNames";
    public static final String Driver_Phones_Key = "DriverPhones";
    public static final String License_Plates_Key = "LicensePlates";

    private static SharedPreferences pref;

    static {
        if (pref == null) {
            pref = getAppContext().getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
        }
    }

    public static String Today() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

    public static String getCurrentSiteName() {
        return pref.getString(SelectedSiteName_Key, "N/A");
    }

    public static Long getCurrentSiteId() {
        return pref.getLong(SelectedSiteId_Key, -1l);
    }

    public static String getCurrentClusterId() {
        return pref.getString(SelectedCluster_Key, null);
    }

    public static String getLocale() {
        return pref.getString(Locale_Key, "en");
    }

    public static String getToken() {
        return pref.getString(Token_Key, null);
    }

    public static String getLongitude() {
        return pref.getString(Longitude_Key, null);
    }

    public static String getLatitude() {
        return pref.getString(Latitude_Key, null);
    }

    public static Long getLoginTime() {
        return pref.getLong(LoginTime_Key, Long.MIN_VALUE);
    }

    public static Boolean locationExists() {
        return getLongitude() != null && getLatitude() != null;
    }

    // stores in Local Preferences current epoch time, as last Login Time.
    public static void updateLoginTime() {
        writeValue(LoginTime_Key, System.currentTimeMillis() / 1000L);
    }

    public static SiteDTO getSelectedSite() {
        Gson gson = new Gson();
        String siteJson = pref.getString(SelectedSite_Key, null);
        if (siteJson != null) {
            return gson.fromJson(siteJson, SiteDTO.class);
        }
        return null;
    }

    public static void setSelectedSite(SiteDTO siteDTO) {
        if (siteDTO != null) {
            Gson gson = new Gson();
            writeValue(SelectedSite_Key, gson.toJson(siteDTO));
        }
    }

    public static Set<String> getDriverNames() {
        return pref.getStringSet(Driver_Names_Key, new HashSet<>());
    }

    public static void addDriverName(String name) {
        Set<String> namesSet = getDriverNames();
        namesSet.add(name);
        writeValue(Driver_Names_Key, namesSet);
    }

    public static Set<String> getDriverPhones() {
        return pref.getStringSet(Driver_Phones_Key, new HashSet<>());
    }

    public static void addDriverPhone(String phone) {
        Set<String> phonesSet = getDriverPhones();
        phonesSet.add(phone);
        pref.getStringSet(Driver_Phones_Key, phonesSet);
    }

    public static Set<String> getLicensePlates() {
        return pref.getStringSet(License_Plates_Key, new HashSet<>());
    }

    public static void addLicensePlate(String plate) {
        Set<String> platesSet = getLicensePlates();
        platesSet.add(plate);
        pref.getStringSet(License_Plates_Key, platesSet);
    }

    public static String HeaderMsg() {
        return getLoggedInUser("N/A") + " <-> " + Today();
    }

    public static String getLoggedInUser(String defVal) {
        return pref.getString(Logged_In_User_Key, defVal);
    }

    public static Long getLoginDiffInHours() {
        long loginUnixTime = getLoginTime();
        long unixTime = System.currentTimeMillis() / 1000L;

        return Math.abs(unixTime - loginUnixTime) / 3600L;
    }

    public static boolean writeValue(String key, Object value) {
        try {
            SharedPreferences.Editor editor = pref.edit();

            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof HashSet) {
                editor.putStringSet(key, (Set<String>) value);
            }
            editor.apply();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static void resetLogin() {
        SharedPreferences.Editor editor = pref.edit();
        //editor.remove(Token_Key);
        editor.remove(LoginTime_Key);
        editor.apply();
    }

    public static boolean Reset() {
        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
