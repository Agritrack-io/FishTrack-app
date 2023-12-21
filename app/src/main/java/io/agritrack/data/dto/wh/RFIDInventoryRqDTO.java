package io.agritrack.data.dto.wh;

import java.util.List;
import java.util.Map;

import io.agritrack.data.model.wh.RFIDInventory;

public class RFIDInventoryRqDTO {
    public String inventory_type;
    public String site;
    public String user;
    public Long created_at;
    public Double longitude;
    public Double latitude;

    public Map<String, List<RFIDInventoryItemDTO>> rfid_items;

    public static RFIDInventoryRqDTO convert(RFIDInventory inventory) {
        RFIDInventoryRqDTO inventoryDTO = new RFIDInventoryRqDTO();
        inventoryDTO.inventory_type = inventory.rfidInvType;
        inventoryDTO.site = inventory.site;
        inventoryDTO.user = inventory.user;
        inventoryDTO.created_at = inventory.performedAt;
        inventoryDTO.longitude = inventory.longitude;
        inventoryDTO.latitude = inventory.latitude;

        return inventoryDTO;
    }
}