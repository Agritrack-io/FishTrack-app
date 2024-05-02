package io.agritrack.kefalonia.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.agritrack.kefalonia.data.dto.AgricenseDTO;
import io.agritrack.kefalonia.data.dto.EncodingSchemeDTO;
import io.agritrack.kefalonia.data.dto.StationConfigDTO;

public class EncryptedSharedPreferences {

    private static SharedPreferences sharedPreferences;

    public EncryptedSharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("KEFALONIA_FISHTRACK_APP", Context.MODE_PRIVATE);
    }

    public static boolean Reset() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean savePreference(Map<String, Object> prefs) {
        if (prefs == null || prefs.isEmpty()) {
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            for (Map.Entry<String, Object> prefEntry : prefs.entrySet()) {
                if (prefEntry.getValue() == null) {
                    continue;
                }
                editor.putString(prefEntry.getKey(), prefEntry.getValue().toString());//Crypto.encryptAndEncode(prefEntry.getValue().toString()));
            }
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String loadPreference(String key) {
        String result = "";
        try {
            String encryptedValue = sharedPreferences.getString(key, "");
            if (!encryptedValue.isEmpty())
                result = encryptedValue;//Crypto.decodeAndDecrypt(encryptedValue);
        } finally {
            return result;
        }
    }

    public Long loadNumberPreference(String key) {
        Long result = 0l;
        try {
            String encryptedValue = sharedPreferences.getString(key, "");
            if (!encryptedValue.isEmpty())
                result = Long.parseLong(encryptedValue);//Crypto.decodeAndDecrypt(encryptedValue));
        } finally {
            return result;
        }
    }

    public boolean saveSettings(AgricenseDTO appSettings) {
        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("terminalId", appSettings.terminalId);
        settingsMap.put("agrisenseUrl", appSettings.agrisenseUrl);
        settingsMap.put("licenseKey", appSettings.licenseKey);
        savePreference(settingsMap);
        settingsMap = oMapper.convertValue(appSettings.encodingScheme, Map.class);
        savePreference(settingsMap);
        settingsMap = new HashMap<>();
        settingsMap.put("centralSite", appSettings.stationCfg.centralSite);
        savePreference(settingsMap);
        return true;
    }

    public AgricenseDTO loadSettings() {

        AgricenseDTO result = new AgricenseDTO();
        result.terminalId = loadPreference("terminalId");
        result.agrisenseUrl = loadPreference("agrisenseUrl");
        result.licenseKey = loadPreference("licenseKey");

        //tagPrefix tagLength
        result.encodingScheme = new EncodingSchemeDTO();
        result.encodingScheme.tagPrefix = loadPreference("tagPrefix");
        result.encodingScheme.tagLength = loadNumberPreference("tagLength").intValue();

        // StationConfigDTO ListenerConfigDTO
        result.stationCfg = new StationConfigDTO();
        result.stationCfg.centralSite = loadPreference("centralSite");

        return result;
    }

    public boolean savePreference(String key, String value) {
        if (key == null) {
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString(key, value);//Crypto.encryptAndEncode(prefEntry.getValue().toString()));
            editor.apply();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
