package io.agritrack.philosofish.data.model.common;

import com.google.gson.annotations.SerializedName;

import java.sql.Time;

public class TonneSample {
    @SerializedName("sample_time")
    private Time sampleTime;

    @SerializedName("fish_temp")
    private Double fishTemp;

    @SerializedName("corrective_action")
    private String corrAction;

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

    public String getCorrAction() {
        return corrAction;
    }

    public void setCorrAction(String corrAction) {
        this.corrAction = corrAction;
    }

    public TonneSample(Time sampleTime, Double fishTemp, String corrAction) {
        this.sampleTime = sampleTime;
        this.fishTemp = fishTemp;
        this.corrAction = corrAction;
    }


}
