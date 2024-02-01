package io.agritrack.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.R;
import io.agritrack.data.dto.AgricenseDTO;

public class ApplicationSettings {

    private static String settingsFileName = "app_settings.json";
    private static String customerName = "KEFALONIA";
    private static int customerColor = R.color.agri_blue;

    private static AgricenseDTO settings;

    public static void loadSettings(AgricenseDTO settings) {
        ApplicationSettings.settings = settings;
    }

    public static void loadSettings(Context context) {
        EncryptedSharedPreferences preferences = new EncryptedSharedPreferences(context);
        settings = preferences.loadSettings();
//        loadSettingsFromFile(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), settingsFileName);
    }

    public static String getConnectionString() {
        return settings == null || !settings.connStringValid() ? "" : settings.getConnectionString();
    }

    public static String getListenerIP() {
        return settings == null ? "" : settings.getListenerIP();
    }

    public static int getListenerPort() {
        return settings == null || settings.getListenerPort().isEmpty() ? 0 : Integer.parseInt(settings.getListenerPort());
    }

    public static String getTerminalId() {
        return settings == null ? "" : settings.terminalId;
    }

    public static String getTagStart() {
        return settings == null ? "" : settings.getTagPrefix();
    }

    public static int getVarLength() {
        return settings == null ? 0 : settings.getTagLength();
    }

    public static String getCentralSite() {
        return settings == null ? "" : settings.getCentralSite();
    }

    public static List<String> getIncomingSites() {
        return settings == null ? new ArrayList<>() : settings.getIncoming();
    }

    public static List<String> getOutgoingSites() {
        return settings == null ? new ArrayList<>() : settings.getOutgoing();
    }

    public static String getCustomerName() {
        return customerName;
    }

    public static int getCustomerColor() {
        return customerColor;
    }

//    private static void loadSettingsFromFile(File directory , String fileName){
//        File file = new File(directory, fileName);
//        try {
//            InputStream is = new FileInputStream(file);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            String json = new String(buffer, "UTF-8");
//            JSONObject settings = new JSONObject(json);
//
//            connectionString = settings.getString("connectionString");
//            listenerIP = settings.getString("listenerIP");
//            //terminalId = settings.getString("terminalId");
//            tagStart = settings.getString("tagStart");
//            varLength = settings.getInt("varLength");
//            serverIP = settings.getString("serverIP");
//
//        } catch (IOException | JSONException e) {
//        }
//    }

}
