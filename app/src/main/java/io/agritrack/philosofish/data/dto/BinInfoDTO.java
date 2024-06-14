package io.agritrack.philosofish.data.dto;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.philosofish.data.model.BinInfo;

public class BinInfoDTO {

    public String rfid;
    public String cage;
    public String species;
    public String fishing_request;
    public String lot;
    public Double total_weight;
    public String farm;
    public String site;
    public Long last_update;
    public Long inited_at;
    public Long picked_at;
    public Long delivered_at;

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
        if (binInfoDTO.picked_at != null && String.valueOf(binInfoDTO.picked_at).length() == 10) {
            binInfo.pickedAt = binInfoDTO.picked_at * 1000L;
        } else {
            binInfo.pickedAt = binInfoDTO.picked_at;
        }
        if (binInfoDTO.delivered_at != null && String.valueOf(binInfoDTO.delivered_at).length() == 10) {
            binInfo.deliveredAt = binInfoDTO.delivered_at * 1000L;
        } else {
            binInfo.deliveredAt = binInfoDTO.delivered_at;
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
        if (binInfo.pickedAt != null && String.valueOf(binInfo.pickedAt).length() == 10) {
            binInfoDTO.picked_at = binInfo.pickedAt * 1000L;
        } else {
            binInfoDTO.picked_at = binInfo.pickedAt;
        }
        if (binInfo.deliveredAt != null && String.valueOf(binInfo.deliveredAt).length() == 10) {
            binInfoDTO.delivered_at = binInfo.deliveredAt * 1000L;
        } else {
            binInfoDTO.delivered_at = binInfo.deliveredAt;
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
