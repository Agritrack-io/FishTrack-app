package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;

public class HarvestTransactionDTO {

    public Long id;
    public String transactionType;

    public static HarvestTransaction convert(HarvestTransactionDTO harvestTransactionDTO) {
        HarvestTransaction harvestTransaction = new HarvestTransaction();
        harvestTransaction.id = harvestTransactionDTO.id;
        harvestTransaction.transactionType = harvestTransactionDTO.transactionType;
        return harvestTransaction;
    }
}
