package io.agritrack.data.dto;


import io.agritrack.data.model.HarvestRequest;

public class HarvestRequestDTO {
    public String request_id;
    public String harvest_date;
    public String plant;
    public String site;
    public String cage_rfid;
    public String cage_code;
    public String species;
    public String fish_size;
    public Double avg_weight;
    public Double request_quantity;
    public String requester;
    public String notes;
    public String user;


    public static HarvestRequest convert(HarvestRequestDTO harvestRequestDTO) {
        HarvestRequest harvestRequest = new HarvestRequest();

        harvestRequest.requestId = harvestRequestDTO.request_id;
        harvestRequest.harvestDate = harvestRequestDTO.harvest_date;
        harvestRequest.packagingPlant = harvestRequestDTO.plant;
        harvestRequest.site = harvestRequestDTO.site;
        harvestRequest.cageRFID = harvestRequestDTO.cage_rfid;
        harvestRequest.harvestDate = harvestRequestDTO.harvest_date;
        harvestRequest.cageCode = harvestRequestDTO.cage_code;
        harvestRequest.species = harvestRequestDTO.species;
        //harvestRequest.averageWeight = harvestRequestDTO.fish_size;
        harvestRequest.averageWeight = harvestRequestDTO.avg_weight;
        harvestRequest.reqQty = harvestRequestDTO.request_quantity;
        harvestRequest.requester = harvestRequestDTO.requester;
        harvestRequest.notes = harvestRequestDTO.notes;
        harvestRequest.user = harvestRequestDTO.user;

        return harvestRequest;
    }
}