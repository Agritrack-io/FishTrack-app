package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.CorrelationTransaction;

public class CorrelationTxDTO {
    public String asset_type;
    public String logger_type;
    public String user;
    public String site;
    public String barcode;
    public String asset_rfid;
    public String logger_rfid;
    public Long timestamp;
    public Double longitude;
    public Double latitude;

    public static CorrelationTxDTO convert(CorrelationTransaction corrTx) {
        CorrelationTxDTO correlationTxDTO = new CorrelationTxDTO();
        correlationTxDTO.asset_type = corrTx.assetType;
        correlationTxDTO.logger_type = corrTx.loggerType;
        correlationTxDTO.user = corrTx.user;
        correlationTxDTO.site = corrTx.site;
        correlationTxDTO.asset_rfid = corrTx.assetRFID;
        correlationTxDTO.logger_rfid = corrTx.loggerRFID;
        correlationTxDTO.barcode = corrTx.barcode;
        correlationTxDTO.timestamp = corrTx.timestamp;
        correlationTxDTO.longitude = corrTx.longitude;
        correlationTxDTO.latitude = corrTx.latitude;

        return correlationTxDTO;
    }
}
