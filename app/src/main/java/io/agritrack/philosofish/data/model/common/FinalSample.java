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

    @SerializedName("under_weight_1")
    public Integer underWeight1;

    @SerializedName("under_weight_2")
    public Integer underWeight2;

    @SerializedName("under_weight_3")
    public Integer underWeight3;

    @SerializedName("over_weight_1")
    public Integer overWeight1;

    @SerializedName("over_weight_2")
    public Integer overWeight2;

    @SerializedName("over_weight_3")
    public Integer overWeight3;

    @SerializedName("net_weight")
    public Integer netWeight;

    @SerializedName("ice_quantity")
    public Integer iceQuantity;

    @SerializedName("fish_temp")
    public Double fishTemp;


}
