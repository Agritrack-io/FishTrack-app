package io.agritrack.philosofish.data.dto.common;

import com.google.gson.annotations.SerializedName;

public class PackageLabelCheckDTO {


    @SerializedName("start_packing")
    public Boolean startPacking;

    @SerializedName("change_packing")
    public Boolean changePacking;

    @SerializedName("middle_packing")
    public Boolean middlePacking;

    @SerializedName("end_packing")
    public Boolean endPacking;

    @SerializedName("label_comments")
    public String labelComments;

    @SerializedName("total_kg")
    public Integer totalKg;


    @SerializedName("disinfected_bins")
    public Integer disinfectedBins;

}
