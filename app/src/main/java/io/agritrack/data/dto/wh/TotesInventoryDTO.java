package io.agritrack.data.dto.wh;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.items.TotesInventoryTxWithItems;
import io.agritrack.data.model.wh.RFIDInventory;

public class TotesInventoryDTO {

    public String inventory_type;
    public String user;
    public String site;
    public List<String> totes = new LinkedList<String>();
    public Integer totes_cnt;
    public Long created_at;
    public Double longitude;
    public Double latitude;

    public static TotesInventoryDTO convert(TotesInventoryTxWithItems inventory) {
        TotesInventoryDTO inventoryDTO = new TotesInventoryDTO();
        if (inventory.rfidInventoryTx != null) {
            RFIDInventory rfidInventoryTx = inventory.rfidInventoryTx;
            inventoryDTO.inventory_type = rfidInventoryTx.rfidInvType;
            inventoryDTO.site = rfidInventoryTx.site;
            inventoryDTO.totes_cnt = rfidInventoryTx.totesCnt;
            inventoryDTO.user = rfidInventoryTx.user;
            if (!CollectionUtils.isEmpty(inventory.totes)){
                inventoryDTO.totes = inventory.totes.stream().map(x-> x.epc).collect(Collectors.toList());
            }
            inventoryDTO.created_at = rfidInventoryTx.performedAt;
            inventoryDTO.longitude = rfidInventoryTx.longitude;
            inventoryDTO.latitude = rfidInventoryTx.latitude;
        }

        return inventoryDTO;
    }
}
