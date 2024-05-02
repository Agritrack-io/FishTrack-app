package io.agritrack.kefalonia.data.dto;

import com.google.android.gms.common.util.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AgricenseDTO {
    @SerializedName("terminal-id")
    public String terminalId;

    @SerializedName("agrisense-url")
    public String agrisenseUrl;

    @SerializedName("license-key")
    public String licenseKey;

    @SerializedName("encoding-scheme")
    public EncodingSchemeDTO encodingScheme;

    @SerializedName("station-config")
    public StationConfigDTO stationCfg;

    public String getCentralSite() {
        if (stationCfg != null) {
            return String.format("%s", stationCfg.centralSite);
        }
        return "";
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
