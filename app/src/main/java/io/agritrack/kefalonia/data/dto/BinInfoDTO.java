package io.agritrack.kefalonia.data.dto;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.kefalonia.data.model.BinInfo;

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

    public static BinInfoDTO convert(BinInfo binInfo) {
        BinInfoDTO binInfoDTO = new BinInfoDTO();
        binInfoDTO.rfid = binInfo.rfid;
        binInfoDTO.lot = binInfo.lot;
        binInfoDTO.fishing_request = binInfo.fishingRequest;
        binInfoDTO.cage = binInfo.cage;
        binInfoDTO.species = binInfo.species;
        binInfoDTO.total_weight = binInfo.totalWeight;
        binInfoDTO.farm = binInfo.farm;
        binInfoDTO.last_update = binInfo.lastUpdate;
        if (binInfo.initedAt != null && String.valueOf(binInfo.initedAt).length() == 10) {
            binInfoDTO.inited_at = binInfo.initedAt * 1000L;
        } else {
            binInfoDTO.inited_at = binInfo.initedAt;
        }

        return binInfoDTO;
    }

    public static List<BinInfoDTO> convert(List<BinInfo> binInfoDTOs) {
        List<BinInfoDTO> result = new ArrayList<>();
        for (BinInfo binInfoDTO : binInfoDTOs) {
            BinInfoDTO itemDto = convert(binInfoDTO);
            result.add(itemDto);
        }
        return result;
    }
}
