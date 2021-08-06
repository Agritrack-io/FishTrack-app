package io.agritrack.fishtrack.data.dto;

import java.util.Date;
import io.agritrack.fishtrack.data.model.Fishing;

public class FishingDTO {

    public Long id;
    public String hlot;
    public String platformRFID;
    public String cageRFID;
    public String netRFID;
    public String ichthyopathologist;
    public String fishType;
    public String iceAdequacy;
    public String iceSupplier;
    public Date lastFeed;
    public Double orderedQuantity;
    public Double seaTemperature;
    public Double totalQty;
    public Short harvestBinsCnt;

    public static Fishing convert(FishingDTO fishingDTO) {
        Fishing fishing = new Fishing();
        fishing.id = fishingDTO.id;
        fishing.hlot = fishingDTO.hlot;
        fishing.platformRFID = fishingDTO.platformRFID;
        fishing.cageRFID = fishingDTO.cageRFID;
        fishing.netRFID = fishingDTO.netRFID;
        fishing.ichthyopathologist = fishingDTO.ichthyopathologist;
        fishing.fishType = fishingDTO.fishType;
        fishing.iceAdequacy = fishingDTO.iceAdequacy;
        fishing.iceSupplier = fishingDTO.iceSupplier;
        fishing.lastFeed = fishingDTO.lastFeed;
        fishing.orderedQuantity = fishingDTO.orderedQuantity;
        fishing.seaTemperature = fishingDTO.seaTemperature;
        fishing.totalQty = fishingDTO.totalQty;
        fishing.harvestBinsCnt = fishingDTO.harvestBinsCnt;
        return fishing;
    }
}
