package io.agritrack.data.dto;


import io.agritrack.data.model.FishingRequest;

public class FishingRequestDTO {
    public String request_id;
    public String harvest_date;
    public String plant;
    public String site;
    public String cage_rfid;
    public String cage_code;
    public String species;
    public Double avg_weight;
    public Double request_quantity;
    public String requester;
    public String comments;


    public static FishingRequest convert(FishingRequestDTO fishingRequestDTO) {
        FishingRequest fishingRequest = new FishingRequest();

        fishingRequest.requestId = fishingRequestDTO.request_id;
        fishingRequest.harvestDate = fishingRequestDTO.harvest_date;
        fishingRequest.packagingPlant = fishingRequestDTO.plant;
        fishingRequest.site = fishingRequestDTO.site;
        fishingRequest.cageRFID = fishingRequestDTO.cage_rfid;
        fishingRequest.harvestDate = fishingRequestDTO.harvest_date;
        fishingRequest.cageCode = fishingRequestDTO.cage_code;
        fishingRequest.species = fishingRequestDTO.species;
        fishingRequest.averageWeight = fishingRequestDTO.avg_weight;
        fishingRequest.reqQty = fishingRequestDTO.request_quantity;
        fishingRequest.requester = fishingRequestDTO.requester;
        fishingRequest.notes = fishingRequestDTO.comments;

        return fishingRequest;
    }
}