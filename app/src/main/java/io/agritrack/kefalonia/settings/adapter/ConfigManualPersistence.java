package io.agritrack.kefalonia.settings.adapter;

import android.content.Context;

import io.agritrack.kefalonia.data.dto.AgricenseDTO;
import io.agritrack.kefalonia.settings.ApplicationSettings;
import io.agritrack.kefalonia.settings.EncryptedSharedPreferences;

public class ConfigManualPersistence implements IConfigPersistenceAdapter<AgricenseDTO> {

    private final String licenseConfigFileName = "settings.json";
    private final String appConfigFileName = "app_settings.json";
    private final Context context;

    public ConfigManualPersistence(Context context) {
        this.context = context;
    }

    @Override
    public void saveConfig(AgricenseDTO appSettings) throws Exception {

        // Save to encrypted prefs
        EncryptedSharedPreferences encryptedSharedPreferences = new EncryptedSharedPreferences(context);
        encryptedSharedPreferences.saveSettings(appSettings);

        // reload app settings
        ApplicationSettings.loadSettings(appSettings);
    }

    @Override
    public AgricenseDTO loadConfig() throws Exception {

        EncryptedSharedPreferences preferences = new EncryptedSharedPreferences(context);
        return preferences.loadSettings();
    }
}
