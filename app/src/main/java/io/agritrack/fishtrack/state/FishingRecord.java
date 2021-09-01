package io.agritrack.fishtrack.state;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.agritrack.fishtrack.data.model.tx.FishingTransaction;

public class FishingRecord {

    public long txKey;
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

    public static FishingRecord convert(FishingTransaction tx) {
        FishingRecord fishingRecord = new FishingRecord();

        fishingRecord.txKey = tx.id;
        fishingRecord.requesterName = tx.requester;
        fishingRecord.reqWeight = tx.orderedQuantity != null ? tx.orderedQuantity.toString() : null;
        fishingRecord.speciesName = tx.fishType;
        fishingRecord.platformRFID = tx.platformRFID;
        fishingRecord.cageRFID = tx.cageRFID;
        fishingRecord.netRFID = tx.netRFID;
        fishingRecord.availBins = tx.harvestBins!=null ? Arrays.asList(tx.harvestBins.split(",")) : null;
        fishingRecord.pathologist = tx.ichthyopathologist;
        fishingRecord.lastFed = tx.lastFeed;
        fishingRecord.adequateIce = "True".equalsIgnoreCase(tx.iceAdequacy) ? Boolean.TRUE : Boolean.FALSE;
        fishingRecord.iceSupplier = tx.iceSupplier;
        fishingRecord.totalFishWeight = tx.totalQty;
        fishingRecord.totalBinsUsed = tx.harvestBinsCnt;
        fishingRecord.seaTemperature = tx.seaTemperature;

        return fishingRecord;
    }
}
