package io.agritrack.kefalonia.settings;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.dto.AgricenseDTO;

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
