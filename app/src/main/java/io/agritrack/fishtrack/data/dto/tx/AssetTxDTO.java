package io.agritrack.fishtrack.data.dto.tx;

import android.text.TextUtils;

import io.agritrack.fishtrack.data.model.tx.AssetTransaction;

public class AssetTxDTO {

    public Long id;
    public String assetType;
    public String from;
    public String to;
    public String rfid;
    public String state;

    public static AssetTxDTO convert(AssetTransaction assetTx) {
        AssetTxDTO assetTxDTO = new AssetTxDTO();
        assetTxDTO.id = assetTx.id;
        assetTxDTO.assetType = assetTx.assetType;
        assetTxDTO.state = assetTx.state;
        assetTxDTO.rfid = TextUtils.join(",", assetTx.itemRFIDs);
        assetTxDTO.from = assetTx.from;
        assetTxDTO.to = assetTx.to;

        return assetTxDTO;
    }
}
