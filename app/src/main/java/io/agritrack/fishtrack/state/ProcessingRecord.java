package io.agritrack.fishtrack.state;

import java.util.List;

public class ProcessingRecord {
    public String dispatchNote;
    public String securityClip;
    public String packagingSite;
    public int packagingSitePos = -1;
    public String fishCondition;
    public int fishConditionPos = -1;
    public String fishFarm;
    public int fishFarmPos = -1;
    public boolean cleanTruck = Boolean.TRUE;
    public boolean smellyTruck = Boolean.FALSE;
    public String remarks;
    public List<String> availBins;
    public String packagingLot;
    public int packagingLotPos = -1;
}
