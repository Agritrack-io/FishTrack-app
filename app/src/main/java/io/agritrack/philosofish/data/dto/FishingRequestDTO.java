package io.agritrack.philosofish.data.dto;


import io.agritrack.philosofish.data.model.FishingRequest;

public class FishingRequestDTO {
    public String request_id;
    public String lot;
    public String harvest_date;
    public String farm_arrival;
    public String plant;
    public String site;
    public String cage_rfid;
    public String cage_code;
    public String species;
    public Double avg_weight;
    public Double request_quantity;
    public Double quantity;
    public String requester;
    public String driver;
    public String comments;
    public Short itin_no;


    public static FishingRequest convert(FishingRequestDTO fishingRequestDTO) {
        FishingRequest fishingRequest = new FishingRequest();

        fishingRequest.requestId = fishingRequestDTO.request_id;
        fishingRequest.lot = fishingRequestDTO.lot;
        fishingRequest.harvestDate = fishingRequestDTO.harvest_date;
        if (fishingRequestDTO.farm_arrival != null) {
            fishingRequest.farmArrival = fishingRequestDTO.farm_arrival.replace("T", " ");
        } else {
            fishingRequest.farmArrival = null;
        }
        fishingRequest.packagingPlant = fishingRequestDTO.plant;
        fishingRequest.site = fishingRequestDTO.site;
        fishingRequest.cageRFID = fishingRequestDTO.cage_rfid;
        fishingRequest.harvestDate = fishingRequestDTO.harvest_date;
        fishingRequest.cageCode = fishingRequestDTO.cage_code;
        fishingRequest.species = fishingRequestDTO.species;
        fishingRequest.averageWeight = fishingRequestDTO.avg_weight;
        fishingRequest.reqQty = fishingRequestDTO.request_quantity;
        fishingRequest.quantity = fishingRequestDTO.quantity;
        fishingRequest.driver = fishingRequestDTO.driver;
        fishingRequest.requester = fishingRequestDTO.requester;
        fishingRequest.notes = fishingRequestDTO.comments;
        fishingRequest.itinSNo = fishingRequestDTO.itin_no;

        return fishingRequest;
    }
}