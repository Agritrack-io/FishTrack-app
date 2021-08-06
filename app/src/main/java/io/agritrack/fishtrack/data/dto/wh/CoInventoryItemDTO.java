package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.CoInventoryItem;

public class CoInventoryItemDTO {

    public Long id;
    public String barcode;
    public Long quantity;
    public String code;

    public static CoInventoryItem convert(CoInventoryItemDTO coInventoryItemDTO) {
        CoInventoryItem coInventoryItem = new CoInventoryItem();
        coInventoryItem.id = coInventoryItemDTO.id;
        coInventoryItem.barcode = coInventoryItemDTO.barcode;
        coInventoryItem.quantity = coInventoryItemDTO.quantity;
        coInventoryItem.code = coInventoryItemDTO.code;
        return coInventoryItem;
    }
}
