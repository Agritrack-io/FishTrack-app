package io.agritrack.data.dto;

import io.agritrack.data.model.BinInfo;

public class BinInfoDTO {

    public String rfid;
    public String cage;
    public String species;
    public Double total_weight;
    public String farm;
    public Long last_update;

    public static BinInfo convert(BinInfoDTO binInfoDTO) {
        BinInfo binInfo = new BinInfo();
        binInfo.rfid = binInfoDTO.rfid;
        binInfo.cage = binInfoDTO.cage;
        binInfo.species = binInfoDTO.species;
        binInfo.totalWeight = binInfoDTO.total_weight;
        binInfo.farm = binInfoDTO.farm;
        binInfo.lastUpdate = binInfoDTO.last_update;

        return binInfo;
    }
}
