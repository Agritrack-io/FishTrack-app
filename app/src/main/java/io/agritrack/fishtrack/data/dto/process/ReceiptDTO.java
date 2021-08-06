package io.agritrack.fishtrack.data.dto.process;

import io.agritrack.fishtrack.data.model.process.Receipt;

public class ReceiptDTO {

    public Long id;
    public String cleanTruck;
    public String plot;
    public String fishType;
    public String dispatchNote;
    public String fishCondition;
    public String securityClipNumber;

    public static Receipt convert(ReceiptDTO receiptDTO) {
        Receipt receipt = new Receipt();
        receipt.id = receiptDTO.id;
        receipt.cleanTruck = receiptDTO.cleanTruck;
        receipt.plot = receiptDTO.plot;
        receipt.fishType = receiptDTO.fishType;
        receipt.dispatchNote = receiptDTO.dispatchNote;
        receipt.fishCondition = receiptDTO.fishCondition;
        receipt.securityClipNumber = receiptDTO.securityClipNumber;
        return receipt;
    }
}
