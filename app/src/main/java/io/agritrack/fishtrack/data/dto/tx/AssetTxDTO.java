package io.agritrack.fishtrack.data.dto.tx;

import io.agritrack.fishtrack.data.model.tx.AssetTransaction;

public class AssetTxDTO {

    public Long id;
    public String rfid;
    public String state;

    public static AssetTransaction convert(AssetTxDTO assetTxDTO) {
        AssetTransaction assetTransaction = new AssetTransaction();
        assetTransaction.id = assetTxDTO.id;
        assetTransaction.rfid = assetTxDTO.rfid;
        assetTransaction.state = assetTxDTO.state;
        return assetTransaction;
    }
}
