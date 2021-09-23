package io.agritrack.fishtrack.data.dto.wh;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.fishtrack.data.dto.tx.ConsumableTxDTO;
import io.agritrack.fishtrack.data.model.tx.ConsumableTransaction;
import io.agritrack.fishtrack.data.model.wh.CoInventoryItem;
import io.agritrack.fishtrack.data.model.wh.RFIDInventoryItem;

public class CoInventoryItemDTO {

    public Long id;
    public String barcode;
    public Integer quantity;
    public String code;

    public static CoInventoryItemDTO convert(CoInventoryItem coInventoryItem) {
        CoInventoryItemDTO coInventoryItemDTO = new CoInventoryItemDTO();
        coInventoryItemDTO.id = coInventoryItem.id;
        coInventoryItemDTO.barcode = coInventoryItem.barcode;
        coInventoryItemDTO.quantity = coInventoryItem.quantity;
        coInventoryItemDTO.code = coInventoryItem.code;
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
