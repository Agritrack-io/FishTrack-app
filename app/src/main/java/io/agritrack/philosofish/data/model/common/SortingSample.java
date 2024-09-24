package io.agritrack.philosofish.data.model.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.sql.Time;
import java.time.LocalDateTime;

public class SortingSample {

    @SerializedName("sample_time")
    private Time sampleTime;

    @SerializedName("fish_temp")
    private Double fishTemp;

    @SerializedName("water_temp")
    private Double waterTemp;

    public SortingSample(Time sampleTime, Double fishTemp, Double waterTemp) {
        this.sampleTime = sampleTime;
        this.fishTemp = fishTemp;
        this.waterTemp = waterTemp;
    }


    public Time getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(Time sampleTime) {
        this.sampleTime = sampleTime;
    }

    public Double getFishTemp() {
        return fishTemp;
    }

    public void setFishTemp(Double fishTemp) {
        this.fishTemp = fishTemp;
    }

    public Double getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(Double waterTemp) {
        this.waterTemp = waterTemp;
    }

}
