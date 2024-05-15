package io.agritrack.kefalonia.data.model;

public class TempSample {
    private String timeStamp;
    private String sample;
    private boolean isAfterFishing = true;

    public TempSample(String ts, String sample, boolean isAfterFishing) {
        this.timeStamp = ts;
        this.sample = sample;
        this.isAfterFishing = isAfterFishing;
    }

    public TempSample(String ts, String sample) {
        this.timeStamp = ts;
        this.sample = sample;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public boolean isAfterFishing() {
        return isAfterFishing;
    }

    public void setAfterFishing(boolean afterFishing) {
        isAfterFishing = afterFishing;
    }
}
