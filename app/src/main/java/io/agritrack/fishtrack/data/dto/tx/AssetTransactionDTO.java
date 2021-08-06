package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.AssetTransaction;

public class AssetTransactionDTO {

    public Long id;
    public String rfid;
    public String state;

    public static AssetTransaction convert(AssetTransactionDTO assetTransactionDTO) {
        AssetTransaction assetTransaction = new AssetTransaction();
        assetTransaction.id = assetTransactionDTO.id;
        assetTransaction.rfid = assetTransactionDTO.rfid;
        assetTransaction.state = assetTransactionDTO.state;
        return assetTransaction;
    }
}
