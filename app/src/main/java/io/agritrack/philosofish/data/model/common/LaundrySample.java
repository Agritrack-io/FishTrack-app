package io.agritrack.philosofish.data.model.common;

import java.time.LocalDateTime;

public class LaundrySample {

    private LocalDateTime sampleTime;

    public LocalDateTime getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(LocalDateTime sampleTime) {
        this.sampleTime = sampleTime;
    }

    public Integer getFishTemp() {
        return fishTemp;
    }

    public void setFishTemp(Integer fishTemp) {
        this.fishTemp = fishTemp;
    }

    public Integer getWaterTemp() {
        return waterTemp;
    }

    public void setWaterTemp(Integer waterTemp) {
        this.waterTemp = waterTemp;
    }

    private Integer fishTemp;
    private Integer waterTemp;
}
