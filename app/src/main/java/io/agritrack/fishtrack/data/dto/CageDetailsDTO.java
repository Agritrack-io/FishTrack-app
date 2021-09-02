package io.agritrack.fishtrack.data.dto;

import io.agritrack.fishtrack.data.model.CageDetails;

public class CageDetailsDTO {

    public Long id;
    public String asset_rfid;
    public String ichthyopathologist;
    public String hlot;
    public String fish_type;
    public Long site;
    public String last_fed;

    public static CageDetails convert(CageDetailsDTO detailsDTO) {
        CageDetails cageDetails = new CageDetails();
        cageDetails.id = detailsDTO.id;
        cageDetails.rfid = detailsDTO.asset_rfid;
        cageDetails.ichthyopathologist = detailsDTO.ichthyopathologist;
        cageDetails.hlot = detailsDTO.hlot;
        cageDetails.fishType = detailsDTO.fish_type;
        cageDetails.site = detailsDTO.site;
        cageDetails.lastFed = detailsDTO.last_fed;
        return cageDetails;
    }
}
