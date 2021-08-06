package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.HarvestRequest;

public class HarvestRequestDTO {

    public Long id;
    public String requestId;
    public String reqQty;
    public String requestor;

    public static HarvestRequest convert(HarvestRequestDTO harvestRequestDTO) {
        HarvestRequest harvestRequest = new HarvestRequest();
        harvestRequest.id = harvestRequestDTO.id;
        harvestRequest.requestId = harvestRequestDTO.requestId;
        harvestRequest.reqQty = harvestRequestDTO.reqQty;
        harvestRequest.requestor = harvestRequestDTO.requestor;
        return harvestRequest;
    }
}
