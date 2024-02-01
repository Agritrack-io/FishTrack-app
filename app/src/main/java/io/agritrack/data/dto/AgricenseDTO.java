package io.agritrack.data.dto;

import com.google.android.gms.common.util.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AgricenseDTO {
    @SerializedName("terminal-id")
    public String terminalId;

    @SerializedName("backend-url")
    public String backendUrl;

    @SerializedName("license-key")
    public String licenseKey;

    @SerializedName("db-config")
    public DbConfigDTO dbCfg;

    @SerializedName("encoding-scheme")
    public EncodingSchemeDTO encodingScheme;

    @SerializedName("listener-config")
    public ListenerConfigDTO listenerCfg;

    @SerializedName("station-config")
    public StationConfigDTO stationCfg;

    public boolean connStringValid() {
        return !Strings.isEmptyOrWhitespace(dbCfg.driver) && !Strings.isEmptyOrWhitespace(dbCfg.host) && !Strings.isEmptyOrWhitespace(dbCfg.login);
    }

    public String getConnectionString() {
        if (dbCfg != null) {
            // TODO:: Add login credentials
            return String.format("%s%s:%s/%s;user=%s;password=%s", dbCfg.driver, dbCfg.host, dbCfg.port, dbCfg.dbase, dbCfg.login, dbCfg.pwd);
        }
        return null;
    }

    public String getListenerIP() {
        if (listenerCfg != null) {
            return String.format("%s", listenerCfg.serverIP);
        }
        return "";
    }

    public String getListenerPort() {
        if (listenerCfg != null) {
            return String.format("%s", listenerCfg.serverPort);
        }
        return "";
    }

    public String getCentralSite() {
        if (stationCfg != null) {
            return String.format("%s", stationCfg.centralSite);
        }
        return "";
    }

    public List<String> getIncoming() {
        if (stationCfg != null) {
            return stationCfg.incoming;
        }
        return new ArrayList<>();
    }

    public List<String> getOutgoing() {
        if (stationCfg != null) {
            return stationCfg.outgoing;
        }
        return new ArrayList<>();
    }

    public String getTagPrefix() {
        if (encodingScheme != null) {
            return encodingScheme.tagPrefix;
        }
        return "";
    }

    public Integer getTagLength() {
        if (encodingScheme != null) {
            return encodingScheme.tagLength;
        }
        return 0;
    }
}
