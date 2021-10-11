package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.CorrelationTransaction;

public class CorrelationTxDTO {
    public String assetType;
    public String barcode;
    public String rfid;
    public Long timestamp;
    public Double longitude;
    public Double latitude;

    public static CorrelationTxDTO convert(CorrelationTransaction corrTx) {
        CorrelationTxDTO correlationTxDTO = new CorrelationTxDTO();
        correlationTxDTO.assetType = corrTx.assetType;
        correlationTxDTO.rfid = corrTx.rfid;
        correlationTxDTO.barcode = corrTx.barcode;
        correlationTxDTO.timestamp = corrTx.timestamp;
        correlationTxDTO.longitude = corrTx.longitude;
        correlationTxDTO.latitude = corrTx.latitude;

        return correlationTxDTO;
    }
}
