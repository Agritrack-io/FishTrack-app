package io.agritrack.data.dto.wh;

import io.agritrack.data.model.wh.RFIDInventory;

public class RFIDInventoryDTO {

    public Long id;
    public String rfidInvType;
    public String site;
    public Long performedAt;
    public Double longitude;
    public Double latitude;

    public static RFIDInventoryDTO convert(RFIDInventory inventory) {
        RFIDInventoryDTO inventoryDTO = new RFIDInventoryDTO();
        inventoryDTO.id = inventory.id;
        inventoryDTO.rfidInvType = inventory.rfidInvType;
        inventoryDTO.site = inventory.site;
        inventoryDTO.performedAt = inventory.performedAt;
        inventoryDTO.longitude = inventory.longitude;
        inventoryDTO.latitude = inventory.latitude;

        return inventoryDTO;
    }
}
