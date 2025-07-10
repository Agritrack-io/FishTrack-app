package io.agritrack.philosofish.data.dto;

import com.google.gson.annotations.SerializedName;

public class AgricenseDTO {
    @SerializedName("terminal-uid")
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
