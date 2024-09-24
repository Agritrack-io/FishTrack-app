package io.agritrack.philosofish.data.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class FinalSample {

    @JsonProperty
    public String size;

    @SerializedName("box_type")
    public Integer boxType;

    @SerializedName("label_pieces")
    public Integer labelPieces;

    @SerializedName("counted_pieces")
    public Integer countedPieces;

    @SerializedName("under_weight")
    public Integer underWeight;

    @SerializedName("over_weight")
    public Integer overWeight;

    @SerializedName("net_weight")
    public Integer netWeight;

    @SerializedName("ice_quantity")
    public Integer iceQuantity;

    @SerializedName("fish_temp")
    public Integer fishTemp;


}
