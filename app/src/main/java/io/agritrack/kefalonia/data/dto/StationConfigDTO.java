package io.agritrack.kefalonia.data.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StationConfigDTO {
    @SerializedName("central-site")
    public String centralSite;

    @SerializedName("incoming")
    public List<String> incoming;

    @SerializedName("outgoing")
    public List<String> outgoing;
}
