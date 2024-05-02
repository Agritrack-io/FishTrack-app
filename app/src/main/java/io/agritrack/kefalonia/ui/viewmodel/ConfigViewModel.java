package io.agritrack.kefalonia.ui.viewmodel;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;
import static io.agritrack.kefalonia.ui.custom.CustomToast.CToast;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.BR;
import io.agritrack.kefalonia.common.DeviceUtils;
import io.agritrack.kefalonia.common.utilities.WifiUtils;
import io.agritrack.kefalonia.data.dto.AgricenseDTO;
import io.agritrack.kefalonia.data.dto.EncodingSchemeDTO;
import io.agritrack.kefalonia.data.dto.StationConfigDTO;
import io.agritrack.kefalonia.settings.EncryptedSharedPreferences;
import io.agritrack.kefalonia.settings.adapter.ConfigPersistenceFactory;
import io.agritrack.kefalonia.settings.adapter.IConfigPersistenceAdapter;

public class ConfigViewModel extends BaseObservable {

    private final Context context;
    @Bindable
    private String station;
    // License
    @Bindable
    private String currentLicense;
    // Encoding Scheme
    @Bindable
    private String tagPrefix;
    @Bindable
    private String varLength;
    // Terminal ID
    @Bindable
    private String serverIP;
    @Bindable
    private String terminalId;
    @Bindable
    private String deviceSN;
    // Persistence
    @Bindable
    private ConfigPersistenceFactory.PersistenceType currentPersistenceType = ConfigPersistenceFactory.PersistenceType.WEB;
    private IConfigPersistenceAdapter<AgricenseDTO> configPersistenceAdapter;

    public ConfigViewModel(Context context) {
        this.context = context;

        // Try to load Configuration
        loadConfig();
    }

    public boolean loadConfig() {
        try {
            configPersistenceAdapter = ConfigPersistenceFactory.getInstance(context, currentPersistenceType);

            AgricenseDTO configParamDTO = configPersistenceAdapter.loadConfig();

            if (configParamDTO != null) {


                // Station Details
                station = configParamDTO.getCentralSite();

                // License
                currentLicense = configParamDTO.licenseKey;

                // Encoding Scheme
                tagPrefix = configParamDTO.getTagPrefix();
                varLength = configParamDTO.getTagLength() != null ? String.valueOf(configParamDTO.getTagLength()) : null;

                // Terminal ID
                serverIP = configParamDTO.agrisenseUrl;
                terminalId = configParamDTO.terminalId;
                deviceSN = DeviceUtils.getSerialNumber(context);
                if (TextUtils.isEmpty(deviceSN)) {
                    EncryptedSharedPreferences prefs = new EncryptedSharedPreferences(context);
                    deviceSN = prefs.loadPreference("serial-no");
                }

                notifyChange();

                return true;
            } else {
                CToast(getAppContext(), "Could not load configuration data.", Toast.LENGTH_LONG);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CToast(getAppContext(), "Could not load configuration data.", Toast.LENGTH_LONG);
            return false;
        }
        return false;
    }

    public boolean saveConfig() {
        try {
            AgricenseDTO configParamDTO = convertToDTO();
            configPersistenceAdapter.saveConfig(configParamDTO);

            // save device s/n
            EncryptedSharedPreferences prefs = new EncryptedSharedPreferences(context);
            prefs.savePreference("serial-no", deviceSN);

            CToast(getAppContext(), "Settings saved.", Toast.LENGTH_LONG);
            return true;
        } catch (Exception e) {
            CToast(getAppContext(), "Settings not saved.", Toast.LENGTH_LONG);
            return false;
        }
    }

    public String getMacAddress() {
        return WifiUtils.getMacAddress();
    }

    public List<String> getPersistenceTypeList() {
        return Arrays.stream(ConfigPersistenceFactory.PersistenceType.values()).map(Enum::name).collect(Collectors.toList());
    }

    public int getCurrentPersistenceType() {
        return currentPersistenceType.ordinal();
    }

    public void setCurrentPersistenceType(int currentPersistenceType) {

        ConfigPersistenceFactory.PersistenceType previousType = this.currentPersistenceType;

        if (previousType.ordinal() == currentPersistenceType) {
            return;
        }
        this.currentPersistenceType = ConfigPersistenceFactory.PersistenceType.values()[currentPersistenceType];
        loadConfig();

        notifyChange();
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
        notifyPropertyChanged(BR.station);
    }

    public String getCurrentLicense() {
        return currentLicense;
    }

    public void setCurrentLicense(String currentLicense) {
        this.currentLicense = currentLicense;
        notifyChange();
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
        notifyPropertyChanged(BR.tagPrefix);
    }

    public String getVarLength() {
        return varLength;
    }

    public void setVarLength(String varLength) {
        this.varLength = varLength;
        notifyPropertyChanged(BR.varLength);
    }

    public String getDeviceId() {
        return DeviceUtils.getIMEIDeviceId(context);
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
        notifyPropertyChanged(BR.deviceSN);
    }

    /*
     **  PRIVATE METHODS
     */

    private AgricenseDTO convertToDTO() {
        AgricenseDTO configParamDTO = new AgricenseDTO();


        // Station Details
        configParamDTO.stationCfg = new StationConfigDTO();
        configParamDTO.stationCfg.centralSite = station;
        // License
        configParamDTO.licenseKey = currentLicense;

        // Encoding Scheme
        configParamDTO.encodingScheme = new EncodingSchemeDTO();
        configParamDTO.encodingScheme.tagPrefix = tagPrefix;
        if (varLength != null && !varLength.isEmpty()) {
            configParamDTO.encodingScheme.tagLength = Integer.parseInt(varLength);
        }

        configParamDTO.terminalId = terminalId;

        return configParamDTO;
    }
}
