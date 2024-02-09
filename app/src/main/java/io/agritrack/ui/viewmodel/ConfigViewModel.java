package io.agritrack.ui.viewmodel;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.kefalonia.BR;
import io.agritrack.common.DeviceUtils;
import io.agritrack.common.utilities.WifiUtils;
import io.agritrack.data.dto.AgricenseDTO;
import io.agritrack.data.dto.DbConfigDTO;
import io.agritrack.data.dto.EncodingSchemeDTO;
import io.agritrack.data.dto.ListenerConfigDTO;
import io.agritrack.data.dto.StationConfigDTO;
import io.agritrack.settings.EncryptedSharedPreferences;
import io.agritrack.settings.adapter.ConfigPersistenceFactory;
import io.agritrack.settings.adapter.IConfigPersistenceAdapter;

public class ConfigViewModel extends BaseObservable {

    private final Context context;
    // Database connection
    @Bindable
    private String dbDriver = "";
    @Bindable
    private String dbServerIP = "";
    @Bindable
    private String dbPort = "";
    @Bindable
    private String dbName = "";
    @Bindable
    private String dbUser = "";
    @Bindable
    private String dbPassword = "";
    // Listener Service
    @Bindable
    private String listenerIP;
    @Bindable
    private String listenerPort;
    // Station Details
    @Bindable
    private String station;
    @Bindable
    private String stationEntrance;
    @Bindable
    private String stationExit;
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

                // Db connection
                if (configParamDTO.dbCfg != null) {
                    dbDriver = configParamDTO.dbCfg.driver;
                    dbServerIP = configParamDTO.dbCfg.host;
                    dbPort = configParamDTO.dbCfg.port.toString();
                    dbName = configParamDTO.dbCfg.dbase;
                    dbUser = configParamDTO.dbCfg.login;
                    dbPassword = configParamDTO.dbCfg.pwd;
                }

                // Listener Service
                listenerIP = configParamDTO.getListenerIP();
                listenerPort = configParamDTO.getListenerPort();

                // Station Details
                station = configParamDTO.getCentralSite();
                stationEntrance = configParamDTO.getIncoming().toString().replace("[", "").replace("]", "");
                stationExit = configParamDTO.getOutgoing().toString().replace("[", "").replace("]", "");

                // License
                currentLicense = configParamDTO.licenseKey;

                // Encoding Scheme
                tagPrefix = configParamDTO.getTagPrefix();
                varLength = configParamDTO.getTagLength() != null ? String.valueOf(configParamDTO.getTagLength()) : null;

                // Terminal ID
                serverIP = configParamDTO.backendUrl;
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

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
        notifyPropertyChanged(BR.dbDriver);
    }

    public String getDbServerIP() {
        return dbServerIP;
    }

    public void setDbServerIP(String dbServerIP) {
        this.dbServerIP = dbServerIP;
        notifyPropertyChanged(BR.dbServerIP);
    }

    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
        notifyPropertyChanged(BR.dbPort);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        notifyPropertyChanged(BR.dbName);
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
        notifyPropertyChanged(BR.dbUser);
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
        notifyPropertyChanged(BR.dbPassword);
    }

    public String getListenerIP() {
        return listenerIP;
    }

    public void setListenerIP(String listenerIP) {
        this.listenerIP = listenerIP;
        notifyPropertyChanged(BR.listenerIP);
    }

    public String getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(String listenerPort) {
        this.listenerPort = listenerPort;
        notifyPropertyChanged(BR.listenerPort);
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
        notifyPropertyChanged(BR.station);
    }

    public String getStationEntrance() {
        return stationEntrance;
    }

    public void setStationEntrance(String stationEntrance) {
        this.stationEntrance = stationEntrance;
        notifyPropertyChanged(BR.stationEntrance);
    }

    public String getStationExit() {
        return stationExit;
    }

    public void setStationExit(String stationExit) {
        this.stationExit = stationExit;
        notifyPropertyChanged(BR.stationExit);
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

        // Database connection
        configParamDTO.dbCfg = new DbConfigDTO();
        configParamDTO.dbCfg.dbase = dbName;
        configParamDTO.dbCfg.driver = dbDriver;
        configParamDTO.dbCfg.host = dbServerIP;
        if (!dbPort.isEmpty()) {
            configParamDTO.dbCfg.port = Long.parseLong(dbPort);
        }
        configParamDTO.dbCfg.login = dbUser;
        configParamDTO.dbCfg.pwd = dbPassword;

        // Listener Service
        configParamDTO.listenerCfg = new ListenerConfigDTO();
        configParamDTO.listenerCfg.serverIP = listenerIP;
        if (!listenerPort.isEmpty()) {
            configParamDTO.listenerCfg.serverPort = Long.parseLong(listenerPort);
        }

        // Station Details
        configParamDTO.stationCfg = new StationConfigDTO();
        configParamDTO.stationCfg.centralSite = station;
        configParamDTO.stationCfg.incoming = Arrays.stream(stationEntrance.split(",")).map(String::trim).filter(s -> s.length() > 0).collect(Collectors.toList());
        configParamDTO.stationCfg.outgoing = Arrays.stream(stationExit.split(",")).map(String::trim).filter(s -> s.length() > 0).collect(Collectors.toList());

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
