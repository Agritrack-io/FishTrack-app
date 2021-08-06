package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.ExpectedRFIDInventory;

public class ExpectedRFIDInventoryDTO {

    public Long id;
    public String barcode;
    public String code;
    public Long quantity;

    public static ExpectedRFIDInventory convert(ExpectedRFIDInventoryDTO expectedRFIDInventoryDTO) {
        ExpectedRFIDInventory expectedRFIDInventory = new ExpectedRFIDInventory();
        expectedRFIDInventory.id = expectedRFIDInventoryDTO.id;
        expectedRFIDInventory.barcode = expectedRFIDInventoryDTO.barcode;
        expectedRFIDInventory.code = expectedRFIDInventoryDTO.code;
        expectedRFIDInventory.quantity = expectedRFIDInventoryDTO.quantity;
        return expectedRFIDInventory;
    }
}
