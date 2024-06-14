package io.agritrack.philosofish.data.dto;

import io.agritrack.philosofish.data.model.HarvestLoad;

public class HarvestLoadDTO {

    public Long id;
    public String status;
    public String species;
    public String fishSize;

    public static HarvestLoad convert(HarvestLoadDTO harvestLoadDTO) {
        HarvestLoad harvestLoad = new HarvestLoad();
        harvestLoad.id = harvestLoadDTO.id;
        harvestLoad.status = harvestLoadDTO.status;
        harvestLoad.species = harvestLoadDTO.species;
        harvestLoad.fishSize = harvestLoadDTO.fishSize;
        return harvestLoad;
    }
}
