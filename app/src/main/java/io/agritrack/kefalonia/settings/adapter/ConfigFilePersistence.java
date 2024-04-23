package io.agritrack.kefalonia.settings.adapter;

import android.content.Context;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import io.agritrack.kefalonia.data.dto.AgricenseDTO;
import io.agritrack.kefalonia.data.dto.EncodingSchemeDTO;
import io.agritrack.kefalonia.data.dto.StationConfigDTO;
import io.agritrack.kefalonia.settings.ApplicationSettings;
import io.agritrack.kefalonia.settings.EncryptedSharedPreferences;

public class ConfigFilePersistence implements IConfigPersistenceAdapter<AgricenseDTO> {

    private final String licenseConfigFileName = "settings.json";
    private final String appConfigFileName = "app_settings.json";
    private final Context context;

    public ConfigFilePersistence(Context context) {
        this.context = context;
    }

    @Override
    public void saveConfig(AgricenseDTO appSettings) throws Exception {

        // Save to settings.json
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), appConfigFileName);
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {

            JSONObject settings = new JSONObject();
            settings.put("serverIP", appSettings.backendUrl);
            settings.put("tagStart", appSettings.getTagPrefix());
            settings.put("terminalId", appSettings.terminalId);
            settings.put("varLength", appSettings.getTagLength());


            writer.write(settings.toString().replace("\\", ""));
        } catch (IOException | JSONException e) {
            throw new IOException(e);
        }

        // Save to app_settings.json
        file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), licenseConfigFileName);
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {

            JSONObject settings = new JSONObject();
            settings.put("centralSite", appSettings.getCentralSite());
            settings.put("licenseString", appSettings.licenseKey);

            writer.write(settings.toString().replace("\\", ""));
        } catch (IOException | JSONException e) {
            throw new IOException(e);
        }

        // Save to encrypted prefs
        EncryptedSharedPreferences encryptedSharedPreferences = new EncryptedSharedPreferences(context);
        encryptedSharedPreferences.saveSettings(appSettings);

        // reload app settings
        ApplicationSettings.loadSettings(appSettings);
    }

    @Override
    public AgricenseDTO loadConfig() throws Exception {

        AgricenseDTO configParamDto = new AgricenseDTO();

        try {
            // APPLICATION SETTINGS

            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), appConfigFileName);
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject settings = new JSONObject(json);

            configParamDto.encodingScheme = new EncodingSchemeDTO();
            configParamDto.encodingScheme.tagPrefix = settings.getString("tagStart");
            try {
                configParamDto.encodingScheme.tagLength = Integer.parseInt(settings.getString("varLength"));
            } catch (Exception e) {
            }

            // LICENSE
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), licenseConfigFileName);
            is = new FileInputStream(file);
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            settings = new JSONObject(json);

            configParamDto.licenseKey = settings.getString("licenseString");

            configParamDto.stationCfg = new StationConfigDTO();
            configParamDto.stationCfg.centralSite = settings.getString("centralSite");

            return configParamDto;

        } catch (IOException | JSONException e) {
            throw new Exception(e);
        }

    }
}
