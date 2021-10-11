package io.agritrack.data.dto.wh;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.data.model.wh.RFIDInventoryItem;

public class RFIDInventoryItemDTO {

    public Long id;
    public Long rfidInventoryId;
    public String rfid;
    public String code;

    public static RFIDInventoryItemDTO convert(RFIDInventoryItem rFIDInventoryItem) {

        RFIDInventoryItemDTO rFIDInventoryItemDTO = new RFIDInventoryItemDTO();
        rFIDInventoryItemDTO.id = rFIDInventoryItem.itmId;
        rFIDInventoryItemDTO.rfid = rFIDInventoryItem.itemRFID;
        rFIDInventoryItemDTO.code = rFIDInventoryItem.code;
        rFIDInventoryItemDTO.rfidInventoryId = rFIDInventoryItem.inventory;
        return rFIDInventoryItemDTO;
    }

    public static List<RFIDInventoryItemDTO> convert(List<RFIDInventoryItem> rfidInventoryItems) {
        List<RFIDInventoryItemDTO> result = new ArrayList<>();
        for(RFIDInventoryItem rfidItem: rfidInventoryItems){
            RFIDInventoryItemDTO itemDto = convert(rfidItem);
            result.add(itemDto);
        }
        return result;
    }
}
