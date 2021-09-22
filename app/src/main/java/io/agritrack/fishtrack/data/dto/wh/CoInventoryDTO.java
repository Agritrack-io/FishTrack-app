package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.CoInventory;

public class CoInventoryDTO {

    public Long id;
    public String coInvType;
    public String site;
    public Long performedAt;

    public static CoInventoryDTO convert(CoInventory inventory) {
        CoInventoryDTO coInventoryDTO = new CoInventoryDTO();
        coInventoryDTO.id = inventory.id;
        coInventoryDTO.coInvType = inventory.coInvType;
        coInventoryDTO.site = inventory.site;
        coInventoryDTO.performedAt = inventory.performedAt;
        return coInventoryDTO;
    }
}
