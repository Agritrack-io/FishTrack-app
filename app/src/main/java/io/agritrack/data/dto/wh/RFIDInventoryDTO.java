package io.agritrack.data.dto.wh;

import java.util.List;
import java.util.Map;

import io.agritrack.data.model.wh.RFIDInventory;

public class RFIDInventoryDTO {
    public Long id;
    public String inventory_type;
    public String site;
    public String user;
    public Long created_at;
    public Double longitude;
    public Double latitude;

    public Map<String, List<RFIDInventoryItemDTO>> rfid_items;
    public Map<String, List<CoInventoryItemDTO>> bc_items;

    public static RFIDInventoryDTO convert(RFIDInventory inventory) {
        RFIDInventoryDTO inventoryDTO = new RFIDInventoryDTO();
        inventoryDTO.id = inventory.id;
        inventoryDTO.inventory_type = inventory.rfidInvType;
        inventoryDTO.site = inventory.site;
        inventoryDTO.user = inventory.user;
        inventoryDTO.created_at = inventory.performedAt;
        inventoryDTO.longitude = inventory.longitude;
        inventoryDTO.latitude = inventory.latitude;

        return inventoryDTO;
    }
}