package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.Consumable;

public class ConsumableDTO {

    public Long id;
    public String barcode;
    public String code;
    public String lot;
    public String consumableType;
    public Integer qty;
    public Integer packagingQty;
    public String description;
    public String details1;
    public String details2;

    public static Consumable convert(ConsumableDTO consumableDTO) {
        Consumable consumable = new Consumable();
        consumable.id = consumableDTO.id;
        consumable.barcode = consumableDTO.barcode;
        consumable.code = consumableDTO.code;
        consumable.lot = consumableDTO.lot;
        consumable.consumableType = consumableDTO.consumableType;
        consumable.qty = consumableDTO.qty;
        consumable.packagingQty = consumableDTO.packagingQty;
        consumable.description = consumableDTO.description;
        consumable.details1 = consumableDTO.details1;
        consumable.details2 = consumableDTO.details2;
        return consumable;
    }
}
