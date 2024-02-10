package io.agritrack.kefalonia.settings.adapter;

import android.app.Activity;
import android.content.Context;
import android.widget.EditText;

import com.google.android.gms.common.util.Strings;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.api.APIServiceGenerator;
import io.agritrack.kefalonia.api.config.ConfigAPI;
import io.agritrack.kefalonia.common.DeviceUtils;
import io.agritrack.kefalonia.common.utilities.WifiUtils;
import io.agritrack.kefalonia.data.dto.AgricenseDTO;
import io.agritrack.kefalonia.settings.ApplicationSettings;
import io.agritrack.kefalonia.settings.EncryptedSharedPreferences;
import retrofit2.Call;
import retrofit2.Response;

public class ConfigWebPersistence implements IConfigPersistenceAdapter<AgricenseDTO> {

    private static final String jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJnLnNrcmltcGFzIiwiaWF0IjoxNjUyMTEyMTY4LCJleHAiOjE2ODM2NDgxNjh9.-evYr78uqcwLmkvBzc0a05Ys_2AjRs5RJ9UtvbqvPTs";

    private final Context context;

    public ConfigWebPersistence(Context context) {
        this.context = context;
    }

    @Override
    public void saveConfig(AgricenseDTO appSettings) throws Exception {

        // save to web rest api?
        EditText etBackendURL = ((Activity) this.context).findViewById(R.id.etBackendURL);

        if (etBackendURL != null && etBackendURL.getText() != null && !Strings.isEmptyOrWhitespace(etBackendURL.getText().toString())) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> postConfiguration(etBackendURL.getText().toString(), appSettings)).get();
        }

        // Save to encrypted prefs
        EncryptedSharedPreferences encryptedSharedPreferences = new EncryptedSharedPreferences(context);
        encryptedSharedPreferences.saveSettings(appSettings);

        // reload app settings
        ApplicationSettings.loadSettings(appSettings);
    }

    @Override
    public AgricenseDTO loadConfig() throws Exception {
        String etBackendURL = ((Activity) this.context).findViewById(R.id.etBackendURL) != null
                ? ((EditText) ((Activity) this.context).findViewById(R.id.etBackendURL)).getText().toString()
                : APIServiceGenerator.getAgrisenseUrl();

        if (etBackendURL != null && !Strings.isEmptyOrWhitespace(etBackendURL)) {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            return executorService.submit(() -> getConfiguration(etBackendURL)).get();
        }

        return null;
    }

    private AgricenseDTO getConfiguration(String url) {
        try {
            // load from web rest api
            ConfigAPI cfgService = APIServiceGenerator.createAgrisenseAPI(ConfigAPI.class);

            String deviceId = DeviceUtils.getIMEIDeviceId(context);
            String deviceSN = DeviceUtils.getSerialNumber(context);

            if (Strings.isEmptyOrWhitespace(deviceSN)) {
                EditText etSerialNo = ((Activity) this.context).findViewById(R.id.etSerialNumber);
                deviceSN = etSerialNo != null ? etSerialNo.getText().toString() : "";
            }

            Call<AgricenseDTO> cfgCall = cfgService.getConfiguration(deviceId, deviceSN, WifiUtils.getMacAddress(), jwt);
            Response<AgricenseDTO> rs = null;

            rs = cfgCall.execute();
            return rs.body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void postConfiguration(String url, AgricenseDTO cfg) {
        try {
            // load from web rest api
            ConfigAPI cfgService = APIServiceGenerator.createAPI(ConfigAPI.class);
            Call<AgricenseDTO> cfgCall = cfgService.postConfiguration(cfg, jwt);
            Response<AgricenseDTO> rs = cfgCall.execute();
            rs.code();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
