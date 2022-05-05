package io.agritrack.data.dto;

import com.google.android.gms.common.util.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.agritrack.data.model.CageDetails;

public class CageDetailsDTO {

    public UUID cage_id;
    public String asset_rfid;
    public String cage_code;
    public String ichthyopathologist;
    public String hlot;
    public String species;
    public UUID site;
    public String last_feeding_date;

    public static CageDetails convert(CageDetailsDTO detailsDTO) {
        CageDetails cageDetails = new CageDetails();
        cageDetails.id = detailsDTO.cage_id;
        cageDetails.rfid = detailsDTO.asset_rfid;
        cageDetails.cageCode = detailsDTO.cage_code;
        cageDetails.ichthyopathologist = detailsDTO.ichthyopathologist;
        cageDetails.hlot = detailsDTO.hlot;
        cageDetails.species = detailsDTO.species;
        cageDetails.site = detailsDTO.site;
        if(!Strings.isEmptyOrWhitespace(detailsDTO.last_feeding_date)) {
            LocalDate ld = LocalDateTime.parse(detailsDTO.last_feeding_date).toLocalDate();
            cageDetails.lastFed = ld;
        }
        //cageDetails.lastFed = detailsDTO.last_feeding_date;
        return cageDetails;
    }
}
