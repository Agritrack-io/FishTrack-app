package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.RFIDInventory;

public class RFIDInventoryDTO {

    public Long id;
    public String rfidInvType;
    public String site;
    public Long performedAt;

    public static RFIDInventoryDTO convert(RFIDInventory inventory) {
        RFIDInventoryDTO inventoryDTO = new RFIDInventoryDTO();
        inventoryDTO.id = inventory.id;
        inventoryDTO.rfidInvType = inventory.rfidInvType;
        inventoryDTO.site = inventory.site;
        inventoryDTO.performedAt = inventory.performedAt;
        return inventoryDTO;
    }
}
