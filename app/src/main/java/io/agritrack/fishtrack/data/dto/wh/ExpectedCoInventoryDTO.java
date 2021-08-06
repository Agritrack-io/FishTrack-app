package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.ExpectedCoInventory;

public class ExpectedCoInventoryDTO {

    public Long id;
    public String barcode;
    public String code;
    public Long quantity;

    public static ExpectedCoInventory convert(ExpectedCoInventoryDTO expectedCoInventoryDTO) {
        ExpectedCoInventory expectedCoInventory = new ExpectedCoInventory();
        expectedCoInventory.id = expectedCoInventoryDTO.id;
        expectedCoInventory.barcode = expectedCoInventoryDTO.barcode;
        expectedCoInventory.code = expectedCoInventoryDTO.code;
        expectedCoInventory.quantity = expectedCoInventoryDTO.quantity;
        return expectedCoInventory;
    }
}
