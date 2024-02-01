package io.agritrack.settings.adapter;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import io.agritrack.common.utilities.ConnectionStringUtils;
import io.agritrack.data.dto.AgricenseDTO;
import io.agritrack.data.dto.EncodingSchemeDTO;
import io.agritrack.data.dto.ListenerConfigDTO;
import io.agritrack.data.dto.StationConfigDTO;
import io.agritrack.settings.ApplicationSettings;
import io.agritrack.settings.EncryptedSharedPreferences;

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
            settings.put("connectionString", appSettings.getConnectionString());
            settings.put("listenerIP", String.format("%s:%s", appSettings.getListenerIP(), appSettings.getListenerPort()));
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
            JSONArray incomingSites = new JSONArray();
            appSettings.getIncoming().stream().forEach(is -> incomingSites.put(is.trim()));
            settings.put("incoming", incomingSites);
            settings.put("licenseString", appSettings.licenseKey);
            JSONArray outgoingSites = new JSONArray();
            appSettings.getOutgoing().stream().forEach(os -> outgoingSites.put(os.trim()));
            settings.put("outgoing", outgoingSites);


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
            String json = new String(buffer, "UTF-8");
            JSONObject settings = new JSONObject(json);

            String connectionString = settings.getString("connectionString");
            configParamDto.dbCfg = ConnectionStringUtils.tokenizeDbConnectionString(connectionString);

            configParamDto.encodingScheme = new EncodingSchemeDTO();
            configParamDto.encodingScheme.tagPrefix = settings.getString("tagStart");
            try {
                configParamDto.encodingScheme.tagLength = Integer.parseInt(settings.getString("varLength"));
            } catch (Exception e) {
            }

            configParamDto.listenerCfg = new ListenerConfigDTO();
            String[] listenerIpWithPort = settings.getString("listenerIP").split(":");
            configParamDto.listenerCfg.serverIP = listenerIpWithPort[0];
            if (listenerIpWithPort.length > 1) {
                try {
                    configParamDto.listenerCfg.serverPort = Long.parseLong(listenerIpWithPort[1]);
                } catch (Exception e) {
                }
            }

            // LICENSE
            file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), licenseConfigFileName);
            is = new FileInputStream(file);
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            settings = new JSONObject(json);

            configParamDto.licenseKey = settings.getString("licenseString");

            configParamDto.stationCfg = new StationConfigDTO();
            configParamDto.stationCfg.centralSite = settings.getString("centralSite");

            List<String> incomingSites = new ArrayList<>();
            JSONArray incomingSitesObject = settings.getJSONArray("incoming");
            for (int i = 0; i < incomingSitesObject.length(); i++)
                incomingSites.add(incomingSitesObject.getString(i).trim());
            configParamDto.stationCfg.incoming = incomingSites;

            List<String> outgoingSites = new ArrayList<>();
            JSONArray outgoingSitesObject = settings.getJSONArray("outgoing");
            for (int i = 0; i < outgoingSitesObject.length(); i++)
                outgoingSites.add(outgoingSitesObject.getString(i).trim());
            configParamDto.stationCfg.outgoing = outgoingSites;

            return configParamDto;

        } catch (IOException | JSONException e) {
            throw new Exception(e);
        }

    }
}
