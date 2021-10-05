package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.HarvestTransaction;

public class HarvestTxDTO {

    public Long id;
    public String transactionType;
    public Double longitude;
    public Double latitude;

    public static HarvestTransaction convert(HarvestTxDTO harvestTxDTO) {
        HarvestTransaction harvestTransaction = new HarvestTransaction();
        harvestTransaction.id = harvestTxDTO.id;
        harvestTransaction.transactionType = harvestTxDTO.transactionType;
        harvestTransaction.longitude = harvestTxDTO.longitude;
        harvestTransaction.latitude = harvestTxDTO.latitude;

        return harvestTransaction;
    }
}
