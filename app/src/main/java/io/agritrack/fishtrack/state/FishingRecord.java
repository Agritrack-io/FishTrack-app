package io.agritrack.fishtrack.state;

import java.util.Date;
import java.util.List;

public class FishingRecord {

    public int requesterPos = -1;
    public String requesterName;
    public String reqWeight;
    public int speciesPos = -1;
    public String speciesName;
    public String platformRFID;
    public String cageRFID;
    public String netRFID;
    public List<String> availBins;
    public List<Long> fishingTeam;
    public String pathologist;
    public Date lastFed;
    public Boolean adequateIce = Boolean.FALSE;
    public String iceSupplier;
    public Double totalFishWeight;
    public Short totalBinsUsed;
    public Double seaTemperature;

    public FishingRecord() {}
}
