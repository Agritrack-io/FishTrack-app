package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.RFIDInventoryItem;

public class RFIDInventoryItemDTO {

    public Long id;
    public String rfid;
    public String code;

    public static RFIDInventoryItem convert(RFIDInventoryItemDTO rFIDInventoryItemDTO) {
        RFIDInventoryItem rFIDInventoryItem = new RFIDInventoryItem();
        rFIDInventoryItem.id = rFIDInventoryItemDTO.id;
        rFIDInventoryItem.rfid = rFIDInventoryItemDTO.rfid;
        rFIDInventoryItem.code = rFIDInventoryItemDTO.code;
        return rFIDInventoryItem;
    }
}
