package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.FishingHarvestBin;

public class FishingHarvestBinDTO {

    public Long id;
    public String fishingId;
    public String harvestBinRFId;
    public Integer quantity;
    public String temperature;

    public static FishingHarvestBin convert(FishingHarvestBinDTO fishingHarvestBinDTO) {
        FishingHarvestBin fishingHarvestBin = new FishingHarvestBin();
        fishingHarvestBin.id = fishingHarvestBinDTO.id;
        fishingHarvestBin.fishingId = fishingHarvestBinDTO.fishingId;
        fishingHarvestBin.harvestBinRFId = fishingHarvestBinDTO.harvestBinRFId;
        fishingHarvestBin.quantity = fishingHarvestBinDTO.quantity;
        fishingHarvestBin.temperature = fishingHarvestBinDTO.temperature;
        return fishingHarvestBin;
    }
}
