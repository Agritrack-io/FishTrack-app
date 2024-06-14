package io.agritrack.philosofish.settings;

import android.content.Context;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.dto.AgricenseDTO;

public class ApplicationSettings {

    private static String settingsFileName = "app_settings.json";
    private static String customerName = "PHILOSOFISH";
    private static int customerColor = R.color.agri_blue;

    private static AgricenseDTO settings;

    public static void loadSettings(AgricenseDTO settings) {
        ApplicationSettings.settings = settings;
    }

    public static void loadSettings(Context context) {
        EncryptedSharedPreferences preferences = new EncryptedSharedPreferences(context);
        settings = preferences.loadSettings();
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

}
