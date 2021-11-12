package io.agritrack.data.dto;

import io.agritrack.data.model.HarvestRequest;

public class HarvestRequestDTO {
    public Long id;
    public String request_id;
    public String fish_type;
    public String fish_size;
    public String request_quantity;
    public String requester;
    public String cage_rfid;
    public String cage_code;
    public String site;
    public String user;
    public String notes;

    public static HarvestRequest convert(HarvestRequestDTO harvestRequestDTO) {
        HarvestRequest harvestRequest = new HarvestRequest();
        harvestRequest.id = harvestRequestDTO.id;
        harvestRequest.fishName = harvestRequestDTO.fish_type;
        harvestRequest.fishSize = harvestRequestDTO.fish_size;
        harvestRequest.requestId = harvestRequestDTO.request_id;
        harvestRequest.reqQty = harvestRequestDTO.request_quantity;
        harvestRequest.requester = harvestRequestDTO.requester;
        harvestRequest.cageRFID = harvestRequestDTO.cage_rfid;
        harvestRequest.cageCode = harvestRequestDTO.cage_code;
        harvestRequest.site = harvestRequestDTO.site;
        harvestRequest.user = harvestRequestDTO.user;
        harvestRequest.notes = harvestRequestDTO.notes;

        return harvestRequest;
    }
}