package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.Inventory;

public class InventoryDTO {

    public Long id;
    public String invType;

    public static Inventory convert(InventoryDTO inventoryDTO) {
        Inventory inventory = new Inventory();
        inventory.id = inventoryDTO.id;
        inventory.invType = inventoryDTO.invType;
        return inventory;
    }
}
