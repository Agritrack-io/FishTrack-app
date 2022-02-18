package io.agritrack.data.dto.tx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import io.agritrack.data.model.tx.AssetTransaction;

public class AssetTxDTO {

    public Long id;
    public String asset_type;
    public String source_site;
    public String target_site;
    public Map<String, List<String>> rfid_items;
    public String state;
    public String site;
    public Double longitude;
    public Double latitude;

    public static AssetTxDTO convert(AssetTransaction assetTx) {

        AssetTxDTO assetTxDTO = new AssetTxDTO();
        assetTxDTO.id = assetTx.id;
        assetTxDTO.asset_type = assetTx.assetType;
        assetTxDTO.state = assetTx.state;
        assetTxDTO.rfid_items = assetTx.itemRFIDs;
        assetTxDTO.source_site = assetTx.from;
        assetTxDTO.target_site = assetTx.to;
        assetTxDTO.site = assetTx.site;
        assetTxDTO.longitude = assetTx.longitude;
        assetTxDTO.latitude = assetTx.latitude;

        return assetTxDTO;
    }
}
