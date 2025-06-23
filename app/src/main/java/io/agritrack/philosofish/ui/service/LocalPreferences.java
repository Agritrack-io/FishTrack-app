package io.agritrack.philosofish.ui.service;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import io.agritrack.philosofish.data.dto.SiteDTO;
import io.agritrack.philosofish.data.type.EpcPerDevice;

public class LocalPreferences {
    public static final String Pref_Name = "agritrack";
    public static final String AppProductName_Key = "activeAppProduct";
    public static final String Token_Key = "token";
    public static final String Longitude_Key = "lon";
    public static final String Latitude_Key = "lat";
    public static final String LoginTime_Key = "loginTime";
    public static final String SelectedSite_Key = "selectedSite";
    public static final String SelectedSiteName_Key = "selectedSiteName";
    public static final String SelectedSiteId_Key = "selectedSiteId";
    public static final String SelectedCluster_Key = "selectedClusterId";
    public static final String SelectedSiteLevel_Key = "selectedSiteLevel3";
    public static final String Locale_Key = "localeCode";
    public static final String Logged_In_User_Key = "LoggedinUser";
    public static final String Logged_User_Roles_Key = "LoggedUserRoles";
    public static final String Fasting_Days = "FastingDays";

    public static final String Driver_Names_Key = "DriverNames";
    public static final String Driver_Phones_Key = "DriverPhones";
    public static final String License_Plates_Key = "LicensePlates";
    public static final String Truck_Capacity_Key = "TruckCapacity";

    public static final String Box_Sn_Key = "BoxSns";

    public static final String Device_Key = "DeviceModel";
    public static final String Step_Key = "Step";
    public static final String Prefix_Key = "Prefix";
    public static final String BLE_Password = "BLE_Pasword";


    public static final String Current_Epc_Key = "CurrentEpcs";

    private final static String DEF_VRY_CODE_NUM = "0000000000000000";

    private static final Gson gson = new Gson();

    private static SharedPreferences pref;

    public static final String Power_Level_Key = "PowerLevel";


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

    public static String getActivePRODUCT() {
        return pref.getString(AppProductName_Key, null);
    }

    public static String getCurrentSiteName() {
        return pref.getString(SelectedSiteName_Key, "N/A");
    }

    public static String getLoggerPassword() {
        return pref.getString(BLE_Password, "0000000000000000");
    }

    public static UUID getCurrentSiteId() {
        return UUID.fromString(pref.getString(SelectedSiteId_Key, "00000000-0000-0000-0000-000000000000"));
    }

    public static String getCurrentClusterId() {
        return pref.getString(SelectedCluster_Key, null);
    }

    public static String getCurrentSiteLevel3() {
        return pref.getString(SelectedSiteLevel_Key, null);
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
        writeValue(Driver_Phones_Key, phonesSet);
    }

    public static Set<String> getLicensePlates() {
        return pref.getStringSet(License_Plates_Key, new HashSet<>());
    }

    public static Integer getCurrentPower() {
        return pref.getInt(Power_Level_Key, 33);
    }
    public static void addLicensePlate(String plate) {
        Set<String> platesSet = getLicensePlates();
        platesSet.add(plate);
        writeValue(License_Plates_Key, platesSet);
    }

    public static Integer getTruckCapacity(String plate) {
        plate = plate.replaceAll("\\D+", "");
        return pref.getInt(Truck_Capacity_Key + "." + plate, 0);
    }

    public static void addTruckCapacity(String plate, Integer capacity) {
        plate = plate.replaceAll("\\D+", "");
        writeValue(Truck_Capacity_Key + "." + plate, capacity);
    }

    public static void addBoxSn(String name) {
        Set<String> boxSnSet = getBoxSn();
        boxSnSet.add(name);
        writeValue(Box_Sn_Key, boxSnSet);
    }

    public static Set<String> getBoxSn() {
        return pref.getStringSet(Box_Sn_Key, new HashSet<>());
    }

    public static String HeaderMsg() {
//        return "";//TODO:: Remove
        return String.format(getCurrentSiteName() + "\n" + getLoggedInUser("N/A") + " <-> " + Today());
    }

    public static String getLoggedInUser(String defVal) {
        return pref.getString(Logged_In_User_Key, defVal);
    }

    public static String getLinenRFID(String productCode) {
        return pref.getString("Linen-" + productCode, null);
    }

    public static void setLinenRFID(String productCode, String rfid) {
        writeValue("Linen-" + productCode, rfid);
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
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof UUID) {
                editor.putString(key, value.toString());
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

    public static List<String> getUserRoles() {
        Set<String> rolesSet = pref.getStringSet(Logged_User_Roles_Key, new HashSet<>());
        return new ArrayList<String>(rolesSet);
    }

    public static void setUserRoles(List<String> roles) {
        writeValue(Logged_User_Roles_Key, new HashSet<String>(roles));
    }

    public static String getDeviceModel() {
        return pref.getString(Device_Key, null);
    }

    public static Long getFastingDays() {
        return pref.getLong(Fasting_Days, 2);
    }

    public static void setFastingDays(Long days) {
        writeValue(Fasting_Days, days);
    }

    public static String getStep() {
        return pref.getString(Step_Key, null);
    }

    public static String getPrefix() {
        return pref.getString(Prefix_Key, null);
    }

    public static void putCurrentEpcList(List<EpcPerDevice> objs) {

        String json = gson.toJson(objs);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Current_Epc_Key, json);
        editor.commit();
    }

    public static List<EpcPerDevice> getCurrentEpcList() {
        String json = pref.getString(Current_Epc_Key, "[]");
        if (!json.equalsIgnoreCase("[]")) {
            return Arrays.asList(gson.fromJson(json, EpcPerDevice[].class));
        } else {
            return null;
        }
    }
}
