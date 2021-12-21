package io.agritrack.data.dto.wh;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.data.model.wh.CoInventoryItem;

public class CoInventoryItemDTO {

    public Long id;
    public Long inventory;
    public String barcode;
    public Integer quantity;
    public String code;

    public static CoInventoryItemDTO convert(CoInventoryItem coInventoryItem) {
        CoInventoryItemDTO coInventoryItemDTO = new CoInventoryItemDTO();
        coInventoryItemDTO.id = coInventoryItem.id;
        coInventoryItemDTO.barcode = coInventoryItem.barcode;
        coInventoryItemDTO.quantity = coInventoryItem.quantity;
        coInventoryItemDTO.code = coInventoryItem.code;
        coInventoryItemDTO.inventory = coInventoryItem.coInventory;
        return coInventoryItemDTO;
    }

    public static List<CoInventoryItemDTO> convert(List<CoInventoryItem> coInventoryItems) {
        List<CoInventoryItemDTO> result = new ArrayList<>();
        for (CoInventoryItem consItem : coInventoryItems) {
            CoInventoryItemDTO itemDto = convert(consItem);
            result.add(itemDto);
        }
        return result;
    }
}
