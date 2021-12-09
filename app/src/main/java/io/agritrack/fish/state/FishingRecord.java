package io.agritrack.fish.state;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;

public class FishingRecord {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public long txKey;
    public int requesterPos = -1;
    public String harvestRq;
    public String requesterName;
    public String reqWeight;
    public int speciesPos = -1;
    public String speciesName;
    public String fishSize;
    public String platformRFID;
    public String cageRFID;
    public String expectedCageRFID;
    public String cageCode; //Cage code assigned by Avramar
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
    public String notes;
    public String packagingPlant;
    public Long temperatureTime;
    public String temperature;

    public FishingRecord() {
    }

    public static FishingRecord convert(FishingTransaction tx) {
        FishingRecord fishingRecord = new FishingRecord();

        fishingRecord.txKey = tx.id;
        fishingRecord.harvestRq = tx.harvestRq;
        fishingRecord.requesterName = tx.requester;
        fishingRecord.reqWeight = tx.orderedQuantity != null ? tx.orderedQuantity.toString() : null;
        fishingRecord.speciesName = tx.fishType;
        fishingRecord.fishSize = tx.fishSize;
        fishingRecord.platformRFID = tx.platformRFID;
        fishingRecord.cageRFID = tx.cageRFID;
        fishingRecord.cageCode = tx.cageCode;
        fishingRecord.netRFID = tx.netRFID;
        fishingRecord.availBins = tx.harvestBins;
        fishingRecord.pathologist = tx.ichthyopathologist;
        fishingRecord.adequateIce = Boolean.TRUE;
        fishingRecord.iceSupplier = tx.iceSupplier;
        fishingRecord.totalFishWeight = tx.totalQty;
        fishingRecord.totalBinsUsed = tx.harvestBinsCnt;
        fishingRecord.seaTemperature = tx.seaTemperature;
        fishingRecord.notes = tx.notes;
        fishingRecord.packagingPlant = tx.packagingPlant;
        if(tx.lastFeed!=null) {
            fishingRecord.lastFed = sdf.format(new Date(tx.lastFeed));
        }
        fishingRecord.fishingTeam = tx.team;
        fishingRecord.longitude = tx.longitude;
        fishingRecord.latitude = tx.latitude;

        return fishingRecord;
    }
}
