package io.agritrack.data.dto.tx;

import java.util.UUID;

import io.agritrack.common.Constants;
import io.agritrack.data.model.tx.CorrelationTransaction;

public class CorrelationTxDTO {
    public String asset_type;
    public String asset_rfid;
    public String asset_code;
    public String rfid;
    public String type;
    public String code;
    public String user;
    public UUID site;
    public Long timestamp;
    public boolean create_if_empty = false;
    public Double longitude;
    public Double latitude;

    public static CorrelationTxDTO convert(CorrelationTransaction corrTx) {
        CorrelationTxDTO correlationTxDTO = new CorrelationTxDTO();
        correlationTxDTO.asset_type = corrTx.assetType;
        correlationTxDTO.asset_rfid = corrTx.assetRFID;
        correlationTxDTO.asset_code = corrTx.assetCode;
        correlationTxDTO.rfid = corrTx.rfid;
        correlationTxDTO.type = corrTx.type;
        correlationTxDTO.code = corrTx.code;
        correlationTxDTO.user = corrTx.user;
        correlationTxDTO.site = corrTx.site;
        correlationTxDTO.timestamp = corrTx.timestamp;
        correlationTxDTO.longitude = corrTx.longitude;
        correlationTxDTO.latitude = corrTx.latitude;
        correlationTxDTO.create_if_empty = corrTx.type.equalsIgnoreCase(Constants.ftBin) || corrTx.type.equalsIgnoreCase(Constants.ftPlatform) ? true : false;

        return correlationTxDTO;
    }
}
