package io.agritrack.data.dto.wh;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.data.model.wh.RFIDInventoryItem;
import io.agritrack.data.service.EncodingSchemeService;

public class RFIDInventoryItemDTO {

    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    public String rfid;
    public String code;

    public RFIDInventoryItemDTO() { }

    public RFIDInventoryItemDTO(String rfID) {
        this.rfid = rfID;
        this.code = schemeSvc.schemeCode(rfID,false); //rfID.substring(11, 15);
    }

    public static RFIDInventoryItemDTO convert(RFIDInventoryItem rFIDInventoryItem) {
        RFIDInventoryItemDTO rFIDInventoryItemDTO = new RFIDInventoryItemDTO();
        rFIDInventoryItemDTO.rfid = rFIDInventoryItem.itemRFID;
        rFIDInventoryItemDTO.code = rFIDInventoryItem.code;
        return rFIDInventoryItemDTO;
    }

    public static List<RFIDInventoryItemDTO> convert(List<RFIDInventoryItem> rfidInventoryItems) {
        List<RFIDInventoryItemDTO> result = new ArrayList<>();
        for (RFIDInventoryItem rfidItem : rfidInventoryItems) {
            RFIDInventoryItemDTO itemDto = convert(rfidItem);
            result.add(itemDto);
        }
        return result;
    }
}
