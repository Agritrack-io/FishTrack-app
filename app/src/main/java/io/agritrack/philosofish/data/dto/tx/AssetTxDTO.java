package io.agritrack.philosofish.data.dto.tx;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.agritrack.philosofish.data.model.tx.AssetTransaction;

public class AssetTxDTO {

    @JsonIgnore
    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public UUID id;
    public String asset_type;
    public String source_site;
    public String target_site;
    public String source_asset;
    public String target_asset;
    public Map<String, List<AssetTxItemDTO>> rfid_items;
    public String state;
    public String site;
    public String user;
    public String created_at;
    public Double longitude;
    public Double latitude;

    public static AssetTxDTO convert(AssetTransaction assetTx) {

        AssetTxDTO assetTxDTO = new AssetTxDTO();
        //assetTxDTO.id = assetTx.id;
        assetTxDTO.asset_type = assetTx.assetType;
        assetTxDTO.state = assetTx.state;
//        assetTxDTO.rfid_items = assetTx.itemRFIDs;
        assetTxDTO.source_site = assetTx.fromSite;
        assetTxDTO.target_site = assetTx.toSite;
        assetTxDTO.source_asset = assetTx.fromAsset;
        assetTxDTO.target_asset = assetTx.toAsset;
        assetTxDTO.site = assetTx.site;
        assetTxDTO.user = assetTx.userId;
        assetTxDTO.created_at = sdf1.format(assetTx.timestamp);
        assetTxDTO.longitude = assetTx.longitude;
        assetTxDTO.latitude = assetTx.latitude;

        return assetTxDTO;
    }
}
