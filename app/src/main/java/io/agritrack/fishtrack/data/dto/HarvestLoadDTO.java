package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.HarvestLoad;

public class HarvestLoadDTO {

    public Long id;
    public String status;
    public String fishType;
    public String fishSize;

    public static HarvestLoad convert(HarvestLoadDTO harvestLoadDTO) {
        HarvestLoad harvestLoad = new HarvestLoad();
        harvestLoad.id = harvestLoadDTO.id;
        harvestLoad.status = harvestLoadDTO.status;
        harvestLoad.fishType = harvestLoadDTO.fishType;
        harvestLoad.fishSize = harvestLoadDTO.fishSize;
        return harvestLoad;
    }
}
