package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.HarvestRequest;

public class HarvestRequestDTO {
    public Long id;
    public String request_id;
    public String fish_type;
    public String request_quantity;
    public String requester;
    public String site;
    public String user;

    public static HarvestRequest convert(HarvestRequestDTO harvestRequestDTO) {
        HarvestRequest harvestRequest = new HarvestRequest();
        harvestRequest.id = harvestRequestDTO.id;
        harvestRequest.fishName = harvestRequestDTO.fish_type;
        harvestRequest.requestId = harvestRequestDTO.request_id;
        harvestRequest.reqQty = harvestRequestDTO.request_quantity;
        harvestRequest.requester = harvestRequestDTO.requester;
        harvestRequest.site = harvestRequestDTO.site;
        harvestRequest.user = harvestRequestDTO.user;

        return harvestRequest;
    }
}
