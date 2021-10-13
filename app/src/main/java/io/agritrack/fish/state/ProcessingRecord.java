package io.agritrack.fish.state;

import java.util.List;

public class ProcessingRecord {
    public String dispatchNote;
    public String securityClip;
    public String pLot;
    public String packagingSite;
    public String fishCondition;
    public int fishConditionPos = -1;
    public boolean cleanTruck = Boolean.TRUE;
    public boolean smellyTruck = Boolean.FALSE;
    public String remarks;
    public List<String> availBins;
    public String photoPath;

    public Double longitude;
    public Double latitude;
}
