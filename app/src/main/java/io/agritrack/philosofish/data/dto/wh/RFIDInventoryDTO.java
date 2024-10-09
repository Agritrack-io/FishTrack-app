package io.agritrack.philosofish.data.dto.wh;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.agritrack.philosofish.data.model.wh.RFIDInventory;

public class RFIDInventoryDTO {

    private static final SimpleDateFormat simpleDateTime =  new SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss");

    public String inventory_type;
    public String site;
    public String user;
    public String created_at;
    public Double longitude;
    public Double latitude;

    public Map<String, List<RFIDInventoryItemDTO>> rfid_items;

    public static RFIDInventoryDTO convert(RFIDInventory inventory) {
        RFIDInventoryDTO inventoryDTO = new RFIDInventoryDTO();
        inventoryDTO.inventory_type = inventory.rfidInvType;
        inventoryDTO.site = inventory.site;
        inventoryDTO.user = inventory.user;
        inventoryDTO.created_at = simpleDateTime.format(new Date(inventory.performedAt));
        inventoryDTO.longitude = inventory.longitude;
        inventoryDTO.latitude = inventory.latitude;

        return inventoryDTO;
    }
}