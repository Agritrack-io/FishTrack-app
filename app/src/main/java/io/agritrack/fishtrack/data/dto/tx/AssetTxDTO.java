package io.agritrack.fishtrack.data.dto.tx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.agritrack.fishtrack.data.model.tx.AssetTransaction;

public class AssetTxDTO {

    public Long id;
    public String assetType;
    public String from;
    public String to;
    public String rfid;
    public String state;
    public String site;
    public Double longitude;
    public Double latitude;

    public static AssetTxDTO convert(AssetTransaction assetTx) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        AssetTxDTO assetTxDTO = new AssetTxDTO();
        assetTxDTO.id = assetTx.id;
        assetTxDTO.assetType = assetTx.assetType;
        assetTxDTO.state = assetTx.state;
        assetTxDTO.rfid = objectMapper.writeValueAsString(assetTx.itemRFIDs);
        assetTxDTO.from = assetTx.from;
        assetTxDTO.to = assetTx.to;
        assetTxDTO.site = assetTx.site;
        assetTxDTO.longitude = assetTx.longitude;
        assetTxDTO.latitude = assetTx.latitude;

        return assetTxDTO;
    }
}
