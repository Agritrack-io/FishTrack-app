package io.agritrack.kefalonia.data.dto;

import io.agritrack.kefalonia.data.model.Flot;

public class FlotDTO {

    public Long id;
    public String siteId;
    public String cageRFId;
    public String fishType;
    public Long lastFedAt;

    public static Flot convert(FlotDTO flotDTO) {
        Flot flot = new Flot();
        flot.id = flotDTO.id;
        flot.siteId = flotDTO.siteId;
        flot.cageRFId = flotDTO.cageRFId;
        flot.fishType = flotDTO.fishType;
        flot.lastFedAt = flotDTO.lastFedAt;
        return flot;
    }
}
