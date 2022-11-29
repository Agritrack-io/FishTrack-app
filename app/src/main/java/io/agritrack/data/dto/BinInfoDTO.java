package io.agritrack.data.dto;

import io.agritrack.data.model.BinInfo;

public class BinInfoDTO {

    public String rfid;
    public String cage;
    public String species;
    public String fishing_request;
    public String lot;
    public Double total_weight;
    public String farm;
    public Long last_update;
    public Long inited_at;

    public static BinInfo convert(BinInfoDTO binInfoDTO) {
        BinInfo binInfo = new BinInfo();
        binInfo.rfid = binInfoDTO.rfid;
        binInfo.lot = binInfoDTO.lot;
        binInfo.fishingRequest = binInfoDTO.fishing_request;
        binInfo.cage = binInfoDTO.cage;
        binInfo.species = binInfoDTO.species;
        binInfo.totalWeight = binInfoDTO.total_weight;
        binInfo.farm = binInfoDTO.farm;
        binInfo.lastUpdate = binInfoDTO.last_update;
        if (binInfoDTO.inited_at != null && String.valueOf(binInfoDTO.inited_at).length() == 10) {
            binInfo.initedAt = binInfoDTO.inited_at * 1000L;
        } else {
            binInfo.initedAt = binInfoDTO.inited_at;
        }

        return binInfo;
    }
}
