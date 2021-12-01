package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.CorrelationTransaction;

public class CorrelationTxDTO {
    public String asset_type;
    public String parent_type;
    public String user;
    public String site;
    public String barcode;
    public String asset_rfid;
    public String parent_rfid;
    public Long timestamp;
    public Double longitude;
    public Double latitude;

    public static CorrelationTxDTO convert(CorrelationTransaction corrTx) {
        CorrelationTxDTO correlationTxDTO = new CorrelationTxDTO();
        correlationTxDTO.asset_type = corrTx.assetType;
        correlationTxDTO.parent_type = corrTx.parentType;
        correlationTxDTO.user = corrTx.user;
        correlationTxDTO.site = corrTx.site;
        correlationTxDTO.asset_rfid = corrTx.assetRFID;
        correlationTxDTO.parent_rfid = corrTx.parentRFID;
        correlationTxDTO.barcode = corrTx.barcode;
        correlationTxDTO.timestamp = corrTx.timestamp;
        correlationTxDTO.longitude = corrTx.longitude;
        correlationTxDTO.latitude = corrTx.latitude;

        return correlationTxDTO;
    }
}
