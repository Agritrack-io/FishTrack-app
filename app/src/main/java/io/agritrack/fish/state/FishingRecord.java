package io.agritrack.fish.state;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.fish.ui.bo.BinTemperatureRecord;
import io.agritrack.fish.ui.bo.BinWeightRecord;

public class FishingRecord {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public long txKey;
    public int requesterPos = -1;
    public String harvestRq;
    public String requesterName;
    public Double reqWeight;
    public int speciesPos = -1;
    public String speciesName;
    public Double averageWeight;
    public String platformRFID;
    public String cageRFID;
    public String expectedCageRFID;
    public String cageCode; //Cage code assigned by Avramar
    public String netRFID;
    public List<String> availBins;
    public List<String> fishingTeam;
    public String pathologist;
    public LocalDate lastFed;
    public Boolean adequateIce = Boolean.TRUE;
    public String iceSupplier;
    public Integer totalFishWeight;
    public Short totalBinsUsed;
    public Double seaTemperature;
    public String harvestRqPkId;
    public Double longitude;
    public Double latitude;
    public String notes;
    public String packagingPlant;
    public BinTemperatureRecord binTemperatureRecord = new BinTemperatureRecord();
    public BinWeightRecord binWeightRecord = new BinWeightRecord();
    public String hlot;

    public FishingRecord() {
    }

    public static FishingRecord convert(FishingTransaction tx) {
        FishingRecord fishingRecord = new FishingRecord();

        fishingRecord.txKey = tx.id;
        fishingRecord.harvestRq = tx.harvestRq;
        fishingRecord.requesterName = tx.requester;
        fishingRecord.reqWeight = tx.orderedQuantity != null ? Double.valueOf(tx.orderedQuantity.toString()) : null;
        fishingRecord.speciesName = tx.fishType;
        fishingRecord.averageWeight = Double.valueOf(tx.averageWeight);
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
            fishingRecord.lastFed = tx.lastFeed;
        }
        fishingRecord.fishingTeam = tx.team;
        fishingRecord.longitude = tx.longitude;
        fishingRecord.latitude = tx.latitude;

        return fishingRecord;
    }
}
