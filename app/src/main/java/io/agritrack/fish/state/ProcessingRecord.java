package io.agritrack.fish.state;
import java.util.List;

public class ProcessingRecord {
    public long txKey;
    public String dispatchNote;
    public String securityClip;

    public String packagingSite;
    public String fishCondition;
    public Short totalBinsUsed;
    public boolean cleanTruck = Boolean.TRUE;
    public boolean smellyTruck = Boolean.FALSE;
    public List<String> availBins;
    public Double longitude;
    public Double latitude;

    public ProcessingRecord() {
    }
}
