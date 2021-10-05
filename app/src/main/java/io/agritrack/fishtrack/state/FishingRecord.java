package io.agritrack.fishtrack.state;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agritrack.fishtrack.data.model.tx.FishingTransaction;

public class FishingRecord {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public long txKey;
    public int requesterPos = -1;
    public String harvestRq;
    public String requesterName;
    public String reqWeight;
    public int speciesPos = -1;
    public String speciesName;
    public String platformRFID;
    public String cageRFID;
    public String netRFID;
    public List<String> availBins;
    public List<String> fishingTeam;
    public String pathologist;
    public String lastFed;
    public Boolean adequateIce = Boolean.TRUE;
    public String iceSupplier;
    public Integer totalFishWeight;
    public Short totalBinsUsed;
    public Double seaTemperature;
    public Long harvestRqPkId;
    public Double longitude;
    public Double latitude;

    public FishingRecord() {
    }

    public static FishingRecord convert(FishingTransaction tx) {
        FishingRecord fishingRecord = new FishingRecord();

        fishingRecord.txKey = tx.id;
        fishingRecord.harvestRq = tx.harvestRq;
        fishingRecord.requesterName = tx.requester;
        fishingRecord.reqWeight = tx.orderedQuantity != null ? tx.orderedQuantity.toString() : null;
        fishingRecord.speciesName = tx.fishType;
        fishingRecord.platformRFID = tx.platformRFID;
        fishingRecord.cageRFID = tx.cageRFID;
        fishingRecord.netRFID = tx.netRFID;
        fishingRecord.availBins = tx.harvestBins;
        fishingRecord.pathologist = tx.ichthyopathologist;
        fishingRecord.adequateIce = "True".equalsIgnoreCase(tx.iceAdequacy) ? Boolean.TRUE : Boolean.FALSE;
        fishingRecord.iceSupplier = tx.iceSupplier;
        fishingRecord.totalFishWeight = tx.totalQty;
        fishingRecord.totalBinsUsed = tx.harvestBinsCnt;
        fishingRecord.seaTemperature = tx.seaTemperature;
        if(tx.lastFeed!=null) {
            fishingRecord.lastFed = sdf.format(new Date(tx.lastFeed));
        }
        fishingRecord.fishingTeam = tx.team;
        fishingRecord.longitude = tx.longitude;
        fishingRecord.latitude = tx.latitude;

        return fishingRecord;
    }
}
