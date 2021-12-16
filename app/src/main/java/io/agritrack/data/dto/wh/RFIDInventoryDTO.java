package io.agritrack.data.dto.wh;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.items.RFIDInventoryTxWithItems;
import io.agritrack.data.model.wh.RFIDInventory;

public class RFIDInventoryDTO {

    public Long id;
    public String inventory_type;
    public String site;
    public String user;
    public List<String> totes = new LinkedList<String>();
    public Long created_at;
    public Double longitude;
    public Double latitude;

    public static RFIDInventoryDTO convert(RFIDInventoryTxWithItems inventory) {
        RFIDInventoryDTO inventoryDTO = new RFIDInventoryDTO();
        if (inventory.rfidInventoryTx != null) {
            RFIDInventory rfidInventoryTx = inventory.rfidInventoryTx;
            inventoryDTO.id = rfidInventoryTx.id;
            inventoryDTO.inventory_type = rfidInventoryTx.rfidInvType;
            inventoryDTO.site = rfidInventoryTx.site;
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
