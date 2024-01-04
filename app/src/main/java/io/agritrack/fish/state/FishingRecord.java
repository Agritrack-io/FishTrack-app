package io.agritrack.fish.state;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import io.agritrack.common.FishTrackUtils;
import io.agritrack.data.model.tx.FishingTransaction;

public class FishingRecord {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public long txKey;
    public String fishingRq;
    public String requesterName;
    public Double reqWeight;
    public String speciesName;
    public Double averageWeight;
    public String platformRFID;
    public String cageRFID;
    public String expectedCageRFID;
    public String cageCode; //Cage code assigned by Avramar
    public String hlot;
    public List<String> availBins;
    public List<String> fishingTeam;
    public String pathologist;
    public LocalDate lastFed;
    public Boolean adequateIce = Boolean.TRUE;
    public String iceSupplier;
    public Integer totalFishWeight;
    public Short totalBinsUsed;
    public Double longitude;
    public Double latitude;
    public String notes;
    public String packagingPlant;
//    public BinTemperatureRecord binTemperatureRecord = new BinTemperatureRecord();
//    public BinWeightRecord binWeightRecord = new BinWeightRecord();
    public String typedCageCode;
    public boolean outOfSystemFishing = false;
    public String reasonOutOfSystemFishing;
    public Short parentItinSno;
    public String reasonOfDeviation;

    public FishingRecord() {
    }

    public static FishingRecord convert(FishingTransaction tx) {
        FishingRecord fishingRecord = new FishingRecord();

        fishingRecord.txKey = tx.id;
        fishingRecord.outOfSystemFishing = tx.outOfSystemFishing;
        fishingRecord.reasonOfDeviation = tx.reasonOfDeviation;
        fishingRecord.fishingRq = tx.fishingRq;
        fishingRecord.requesterName = tx.requester;
        fishingRecord.reqWeight = tx.orderedQuantity != null ? Double.valueOf(tx.orderedQuantity.toString()) : null;
        fishingRecord.speciesName = tx.fishType;
        fishingRecord.parentItinSno = tx.parentItinSno;
        fishingRecord.availBins = tx.availBins;
        fishingRecord.averageWeight = FishTrackUtils.isNumeric(tx.averageWeight) ? Double.valueOf(tx.averageWeight) : null;
        fishingRecord.platformRFID = tx.platformRFID;
        fishingRecord.cageRFID = tx.cageRFID;
        fishingRecord.cageCode = tx.cageCode;
        fishingRecord.pathologist = tx.ichthyopathologist;
        fishingRecord.adequateIce = "TRUE".equalsIgnoreCase(tx.iceAdequacy) ? Boolean.TRUE : Boolean.FALSE;
        fishingRecord.iceSupplier = tx.iceSupplier;
        fishingRecord.totalFishWeight = tx.totalQty;
        fishingRecord.totalBinsUsed = tx.harvestBinsCnt;
        fishingRecord.notes = tx.notes;
        fishingRecord.packagingPlant = tx.packagingPlant;
        if(tx.lastFeed!=null) {
            fishingRecord.lastFed = tx.lastFeed;
        }
//        for (BinWeightRecord.BinRecord rec : tx.harvestBinsData){
//            fishingRecord.binWeightRecord.addRecord(rec.binEPC,rec.weight, rec.init, rec.from, rec.to);
//        }
        fishingRecord.fishingTeam = tx.team;
        fishingRecord.longitude = tx.longitude;
        fishingRecord.latitude = tx.latitude;

        return fishingRecord;
    }
}
