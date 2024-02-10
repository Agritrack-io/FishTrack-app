package io.agritrack.kefalonia.data.dto.tx;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.kefalonia.data.model.tx.AssetTxItem;
import io.agritrack.kefalonia.data.service.EncodingSchemeService;

public class AssetTxItemDTO {

    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    public String rfid;
    public String code;

    public AssetTxItemDTO() {
    }

    public AssetTxItemDTO(String rfID) {
        this.rfid = rfID;
        this.code = schemeSvc.schemeCode(rfID, false); //rfID.substring(11, 15);
    }

    public static AssetTxItemDTO convert(AssetTxItem assetTxItem) {
        AssetTxItemDTO assetTxItemDTO = new AssetTxItemDTO();
        assetTxItemDTO.rfid = assetTxItem.itemRFID;
        assetTxItemDTO.code = assetTxItem.code;
        return assetTxItemDTO;
    }

    public static List<AssetTxItemDTO> convert(List<AssetTxItem> assetTxItems) {
        List<AssetTxItemDTO> result = new ArrayList<>();
        for (AssetTxItem assetTxItem : assetTxItems) {
            AssetTxItemDTO itemDto = convert(assetTxItem);
            result.add(itemDto);
        }
        return result;
    }
}
